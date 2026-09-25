package cm.kfokam48.presence.dto;

import java.time.Instant;

import cm.kfokam48.presence.entity.Relecture;
import cm.kfokam48.presence.entity.StatutExercice;

/** Schéma « RelectureRendue » : réponse 200 de POST /api/relectures/{id}. */
public record RelectureRendueDto(Long id, StatutExercice statut, Integer note, String commentaire, Instant rendueAt) {

	public static RelectureRendueDto de(Relecture r) {
		return new RelectureRendueDto(r.getId(), r.getExercice().getStatut(), r.getNote(), r.getCommentaire(),
				r.getRendueAt());
	}

}
