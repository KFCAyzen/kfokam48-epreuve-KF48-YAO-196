package cm.kfokam48.presence.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/** RG11 : un lien d'exercice est une URL absolue en http:// ou https:// de 500 caractères au plus. */
public final class RegleLien {

	public static final int LONGUEUR_MAX = 500;

	private RegleLien() {
	}

	public static boolean estValide(String lien) {
		if (lien == null || lien.isBlank() || lien.length() > LONGUEUR_MAX || lien.chars().anyMatch(Character::isWhitespace)) {
			return false;
		}
		try {
			URI uri = new URI(lien);
			String schema = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
			return (schema.equals("http") || schema.equals("https")) && uri.getHost() != null && !uri.getHost().isBlank();
		}
		catch (URISyntaxException e) {
			return false;
		}
	}

}
