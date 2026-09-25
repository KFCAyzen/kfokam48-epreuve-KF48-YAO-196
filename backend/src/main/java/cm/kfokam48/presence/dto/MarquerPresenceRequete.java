package cm.kfokam48.presence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Corps imposé de POST /api/presences. */
public record MarquerPresenceRequete(@NotBlank(message = "CHAMP_MANQUANT") String code,
		@NotNull(message = "CHAMP_MANQUANT") Long etudiantId) {
}
