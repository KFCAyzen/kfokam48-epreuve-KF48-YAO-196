package cm.kfokam48.presence.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.dto.MarquerPresenceRequete;
import cm.kfokam48.presence.dto.PresenceDto;
import cm.kfokam48.presence.entity.Etudiant;
import cm.kfokam48.presence.entity.Presence;
import cm.kfokam48.presence.entity.SessionCours;
import cm.kfokam48.presence.entity.SourcePresence;
import cm.kfokam48.presence.exception.ErreurMetier;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.PresenceRepository;
import cm.kfokam48.presence.repository.SessionCoursRepository;

/** EF1 : l'étudiant marque sa présence avec le code. Vérifications dans l'ordre du diagramme D3. */
@Service
@Transactional
public class PresenceService {

	private final PresenceRepository presences;

	private final SessionCoursRepository sessions;

	private final EtudiantRepository etudiants;

	private final AttributionService attribution;

	private final Clock horloge;

	public PresenceService(PresenceRepository presences, SessionCoursRepository sessions,
			EtudiantRepository etudiants, AttributionService attribution, Clock horloge) {
		this.presences = presences;
		this.sessions = sessions;
		this.etudiants = etudiants;
		this.attribution = attribution;
		this.horloge = horloge;
	}

	public PresenceDto marquer(MarquerPresenceRequete requete) {
		Instant maintenant = horloge.instant();
		Etudiant etudiant = etudiants.findById(requete.etudiantId())
			.orElseThrow(() -> new ErreurMetier(HttpStatus.BAD_REQUEST, "ETUDIANT_INCONNU",
					"Cet étudiant n'existe pas. Choisissez votre nom dans la liste."));

		// RG6 : un code d'une autre promotion est traité comme inconnu. Saisie en minuscules tolérée.
		String code = requete.code().strip().toUpperCase(Locale.ROOT);
		Long sessionId = sessions.findByCode(code)
			.filter(s -> s.getPromotion().getId().equals(etudiant.getPromotion().getId()))
			.map(SessionCours::getId)
			.orElseThrow(() -> new ErreurMetier(HttpStatus.BAD_REQUEST, "CODE_INCONNU",
					"Code inconnu. Vérifiez le code affiché par votre formateur."));
		// #56 : on attend la fin de toute autre présence ou dépôt de la session avant de vérifier puis d'écrire.
		SessionCours session = sessions.verrouiller(sessionId).orElseThrow();

		if (session.codeExpire(maintenant)) {
			throw new ErreurMetier(HttpStatus.GONE, "CODE_EXPIRE",
					"Le code de présence a expiré. Votre formateur peut encore vous ajouter à la main.");
		}
		if (session.estCloturee()) {
			throw new ErreurMetier(HttpStatus.GONE, "SESSION_CLOTUREE", "Cette session est clôturée.");
		}
		if (presences.existsBySessionIdAndEtudiantId(session.getId(), etudiant.getId())) {
			throw dejaPresent();
		}
		try {
			Presence presence = presences.saveAndFlush(new Presence(session, etudiant, SourcePresence.ETUDIANT, maintenant));
			attribution.assignerEnAttente(session.getId());
			return PresenceDto.de(presence);
		}
		catch (DataIntegrityViolationException e) {
			// Deux validations simultanées : la contrainte UNIQUE tranche (RG4, ENF3).
			throw dejaPresent();
		}
	}

	private static ErreurMetier dejaPresent() {
		return new ErreurMetier(HttpStatus.CONFLICT, "DEJA_PRESENT", "Votre présence est déjà enregistrée pour cette session.");
	}

}
