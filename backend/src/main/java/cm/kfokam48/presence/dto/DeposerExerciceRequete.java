package cm.kfokam48.presence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Corps imposé de POST /api/exercices. La forme du lien est vérifiée par RegleLien (RG11). */
public record DeposerExerciceRequete(@NotNull(message = "CHAMP_MANQUANT") Long sessionId,
		@NotNull(message = "CHAMP_MANQUANT") Long etudiantId, @NotBlank(message = "CHAMP_MANQUANT") String lien) {
}
