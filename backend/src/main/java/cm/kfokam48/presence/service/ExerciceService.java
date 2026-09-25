package cm.kfokam48.presence.service;

import java.time.Clock;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.dto.DeposerExerciceRequete;
import cm.kfokam48.presence.dto.ExerciceDto;
import cm.kfokam48.presence.entity.Etudiant;
import cm.kfokam48.presence.entity.Exercice;
import cm.kfokam48.presence.entity.SessionCours;
import cm.kfokam48.presence.exception.ErreurMetier;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.ExerciceRepository;
import cm.kfokam48.presence.repository.SessionCoursRepository;

/** EF4 : l'étudiant dépose le lien de son exercice pour une session de sa promotion. */
@Service
@Transactional
public class ExerciceService {

	private final ExerciceRepository exercices;

	private final SessionCoursRepository sessions;

	private final EtudiantRepository etudiants;

	private final Clock horloge;

	public ExerciceService(ExerciceRepository exercices, SessionCoursRepository sessions, EtudiantRepository etudiants,
			Clock horloge) {
		this.exercices = exercices;
		this.sessions = sessions;
		this.etudiants = etudiants;
		this.horloge = horloge;
	}

	public ExerciceDto deposer(DeposerExerciceRequete requete) {
		Etudiant auteur = etudiants.findById(requete.etudiantId())
			.orElseThrow(() -> new ErreurMetier(HttpStatus.BAD_REQUEST, "ETUDIANT_INCONNU", "Cet étudiant n'existe pas."));
		SessionCours session = sessions.findById(requete.sessionId())
			.orElseThrow(() -> new ErreurMetier(HttpStatus.BAD_REQUEST, "SESSION_INCONNUE", "Cette session n'existe pas."));
		if (!session.getPromotion().getId().equals(auteur.getPromotion().getId())) {
			throw new ErreurMetier(HttpStatus.BAD_REQUEST, "ETUDIANT_HORS_PROMOTION",
					"Cette session appartient à une autre promotion que la vôtre.");
		}
		String lien = requete.lien().strip();
		if (!RegleLien.estValide(lien)) {
			throw new ErreurMetier(HttpStatus.BAD_REQUEST, "LIEN_INVALIDE",
					"Le lien doit être une adresse web complète commençant par http:// ou https:// (500 caractères au plus).");
		}
		// RG12 : dépôt accepté après l'expiration du code, tant que la session n'est pas clôturée (Q12).
		if (session.estCloturee()) {
			throw new ErreurMetier(HttpStatus.CONFLICT, "SESSION_CLOTUREE",
					"Cette session est clôturée : le dépôt n'est plus possible.");
		}
		if (exercices.existsBySessionIdAndEtudiantId(session.getId(), auteur.getId())) {
			throw dejaDepose();
		}
		try {
			return ExerciceDto.de(exercices.saveAndFlush(new Exercice(session, auteur, lien, horloge.instant())));
		}
		catch (DataIntegrityViolationException e) {
			throw dejaDepose();
		}
	}

	private static ErreurMetier dejaDepose() {
		return new ErreurMetier(HttpStatus.CONFLICT, "EXERCICE_DEJA_DEPOSE",
				"Vous avez déjà déposé un exercice pour cette session. Vous pouvez en remplacer le lien.");
	}

}
