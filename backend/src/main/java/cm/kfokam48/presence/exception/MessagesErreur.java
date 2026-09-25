package cm.kfokam48.presence.exception;

import java.util.Map;

/**
 * Phrases lisibles associées aux codes d'erreur techniques ou de validation (ENF8).
 * Les erreurs métier portent leur propre message, au plus près de la règle qu'elles expriment.
 */
public final class MessagesErreur {

	private static final Map<String, String> MESSAGES = Map.ofEntries(
			Map.entry("CHAMP_MANQUANT", "Le champ « %s » est obligatoire."),
			Map.entry("REQUETE_INVALIDE", "Le corps de la requête est absent ou n'est pas un JSON valide."),
			Map.entry("PARAMETRE_INVALIDE", "Le paramètre « %s » n'est pas valide."),
			Map.entry("NOTE_INVALIDE", "La note doit être un nombre entier compris entre 0 et 20."),
			Map.entry("LIEN_INVALIDE",
					"Le lien doit être une adresse web complète commençant par http:// ou https:// (500 caractères au plus)."),
			Map.entry("COMMENTAIRE_INVALIDE", "Le commentaire est obligatoire et fait au plus 2000 caractères."),
			Map.entry("RESSOURCE_INTROUVABLE", "Cette adresse n'existe pas dans l'API."),
			Map.entry("METHODE_NON_AUTORISEE", "Cette opération n'est pas autorisée sur cette adresse."),
			Map.entry("TYPE_NON_SUPPORTE", "Le corps de la requête doit être envoyé en JSON (Content-Type: application/json)."),
			Map.entry("CONFLIT", "Cette opération entre en conflit avec une donnée déjà enregistrée. Recharge et réessaie."),
			Map.entry("ERREUR_INTERNE", "Une erreur inattendue est survenue. Réessaie dans un instant."));

	private MessagesErreur() {
	}

	public static String pour(String code) {
		return pour(code, "");
	}

	public static String pour(String code, String champ) {
		return MESSAGES.getOrDefault(code, "La requête n'est pas valide.").formatted(champ);
	}

}
