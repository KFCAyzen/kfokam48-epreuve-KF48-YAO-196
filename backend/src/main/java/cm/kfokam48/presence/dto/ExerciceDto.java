package cm.kfokam48.presence.dto;

import cm.kfokam48.presence.entity.Exercice;
import cm.kfokam48.presence.entity.StatutExercice;

/** Réponse imposée de POST /api/exercices : { id, statut }. */
public record ExerciceDto(Long id, StatutExercice statut) {

	public static ExerciceDto de(Exercice exercice) {
		return new ExerciceDto(exercice.getId(), exercice.getStatut());
	}

}
