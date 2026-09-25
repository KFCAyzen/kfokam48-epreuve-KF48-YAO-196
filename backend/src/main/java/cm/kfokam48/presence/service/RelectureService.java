package cm.kfokam48.presence.service;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.dto.RelectureAssigneeDto;
import cm.kfokam48.presence.dto.RelectureRendueDto;
import cm.kfokam48.presence.dto.RendreRelectureRequete;
import cm.kfokam48.presence.entity.Relecture;
import cm.kfokam48.presence.exception.ErreurMetier;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.RelectureRepository;

/** EF6 : le relecteur commence une relecture assignée, puis rend une note et un commentaire. */
@Service
@Transactional
public class RelectureService {

	private final RelectureRepository relectures;

	private final EtudiantRepository etudiants;

	private final Clock horloge;

	public RelectureService(RelectureRepository relectures, EtudiantRepository etudiants, Clock horloge) {
		this.relectures = relectures;
		this.etudiants = etudiants;
		this.horloge = horloge;
	}

	/**
	 * Opération imposée. L'en-tête X-Etudiant-Id est facultatif (section 7 du cahier des charges) :
	 * sans lui, la relecture est attribuée au relecteur assigné.
	 */
	public RelectureRendueDto rendre(Long id, Long appelantId, RendreRelectureRequete requete) {
		Relecture relecture = trouver(id);
		if (appelantId != null) {
			verifierAppelant(relecture, appelantId);
		}
		if (relecture.estRendue()) {
			throw dejaRendue();
		}
		relecture.rendre(requete.note(), requete.commentaire().strip(), horloge.instant());
		return RelectureRendueDto.de(relecture);
	}

	/** Le relecteur commence : le lien lui est révélé et ne peut plus être remplacé (RG23). Idempotent. */
	public RelectureAssigneeDto commencer(Long id, Long appelantId) {
		Relecture relecture = trouver(id);
		verifierAppelant(relecture, appelantId);
		if (relecture.estRendue()) {
			throw dejaRendue();
		}
		relecture.commencer(horloge.instant());
		return RelectureAssigneeDto.de(relecture);
	}

	/** Les relectures assignées à un étudiant, les non rendues d'abord. */
	@Transactional(readOnly = true)
	public List<RelectureAssigneeDto> relecturesDe(Long etudiantId) {
		if (!etudiants.existsById(etudiantId)) {
			throw new ErreurMetier(HttpStatus.NOT_FOUND, "ETUDIANT_INCONNU", "Cet étudiant n'existe pas.");
		}
		return relectures.findByRelecteurId(etudiantId)
			.stream()
			.sorted(Comparator.comparing(Relecture::estRendue).thenComparing(Relecture::getAssigneeAt, Comparator.reverseOrder()))
			.map(RelectureAssigneeDto::de)
			.toList();
	}

	/** RG2 puis RG16 : jamais l'auteur, seulement le relecteur assigné. */
	private static void verifierAppelant(Relecture relecture, Long appelantId) {
		if (relecture.getExercice().getEtudiant().getId().equals(appelantId)) {
			throw new ErreurMetier(HttpStatus.FORBIDDEN, "AUTO_RELECTURE", "Vous ne pouvez pas relire votre propre exercice.");
		}
		if (!relecture.getRelecteur().getId().equals(appelantId)) {
			throw new ErreurMetier(HttpStatus.FORBIDDEN, "RELECTEUR_NON_ASSIGNE",
					"Cette relecture est assignée à un autre étudiant.");
		}
	}

	private Relecture trouver(Long id) {
		return relectures.findById(id)
			.orElseThrow(() -> new ErreurMetier(HttpStatus.NOT_FOUND, "RELECTURE_INCONNUE",
					"Aucune relecture n'existe pour cet exercice."));
	}

	private static ErreurMetier dejaRendue() {
		return new ErreurMetier(HttpStatus.CONFLICT, "RELECTURE_DEJA_RENDUE",
				"Cette relecture a déjà été rendue : elle est définitive.");
	}

}
