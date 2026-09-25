package cm.kfokam48.presence.dto;

import java.time.Instant;

import cm.kfokam48.presence.entity.Relecture;
import cm.kfokam48.presence.entity.StatutExercice;

/**
 * Schéma « RelectureAssignee », vu par le relecteur. id = id de l'exercice relu.
 * Le lien n'est renvoyé qu'une fois la relecture commencée (RG23).
 */
public record RelectureAssigneeDto(Long id, Long sessionId, String sessionTitre, StatutExercice statut, String lien,
		Integer note, String commentaire, Instant rendueAt) {

	public static RelectureAssigneeDto de(Relecture r) {
		var exercice = r.getExercice();
		return new RelectureAssigneeDto(r.getId(), exercice.getSession().getId(), exercice.getSession().getTitre(),
				exercice.getStatut(), r.estCommencee() ? exercice.getLien() : null, r.getNote(), r.getCommentaire(),
				r.getRendueAt());
	}

}
