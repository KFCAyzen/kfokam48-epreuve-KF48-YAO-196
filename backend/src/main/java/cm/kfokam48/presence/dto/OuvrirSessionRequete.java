package cm.kfokam48.presence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corps imposé de POST /api/sessions. Le code d'erreur de chaque contrainte est dans « message ». */
public record OuvrirSessionRequete(
		@NotBlank(message = "CHAMP_MANQUANT") @Size(max = 200, message = "REQUETE_INVALIDE") String titre,
		@NotNull(message = "CHAMP_MANQUANT") Long promotionId) {
}
