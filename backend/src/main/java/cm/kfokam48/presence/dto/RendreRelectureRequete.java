package cm.kfokam48.presence.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corps imposé de POST /api/relectures/{id}. RG3 : note entière de 0 à 20 (une valeur décimale est
 * refusée dès la lecture du JSON) ; RG17 : commentaire obligatoire, 2000 caractères au plus.
 */
public record RendreRelectureRequete(
		@NotNull(message = "NOTE_INVALIDE") @Min(value = 0, message = "NOTE_INVALIDE") @Max(value = 20, message = "NOTE_INVALIDE") Integer note,
		@NotBlank(message = "COMMENTAIRE_INVALIDE") @Size(max = 2000, message = "COMMENTAIRE_INVALIDE") String commentaire) {
}
