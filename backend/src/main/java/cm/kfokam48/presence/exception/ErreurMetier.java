package cm.kfokam48.presence.exception;

import org.springframework.http.HttpStatus;

/**
 * Erreur métier prévue par le contrat : elle porte son statut HTTP et son code stable
 * (liste complète dans le composant « Erreur » de api/contrat.yaml).
 */
public class ErreurMetier extends RuntimeException {

	private final HttpStatus statut;

	private final String code;

	public ErreurMetier(HttpStatus statut, String code, String message) {
		super(message);
		this.statut = statut;
		this.code = code;
	}

	public HttpStatus getStatut() {
		return statut;
	}

	public String getCode() {
		return code;
	}

	public static ErreurMetier promotionInconnue(HttpStatus statut) {
		return new ErreurMetier(statut, "PROMOTION_INCONNUE", "Cette promotion n'existe pas.");
	}

}
