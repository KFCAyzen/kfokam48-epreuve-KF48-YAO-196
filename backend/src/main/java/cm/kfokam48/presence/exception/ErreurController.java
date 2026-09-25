package cm.kfokam48.presence.exception;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cm.kfokam48.presence.dto.ErreurDto;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Remplace la page d'erreur par défaut de Spring pour les erreurs levées hors des contrôleurs
 * (filtres, conteneur de servlets) : elles respectent elles aussi le format { code, message }.
 */
@RestController
public class ErreurController implements ErrorController {

	@RequestMapping(path = "/error", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ErreurDto> erreur(HttpServletRequest requete) {
		Object attribut = requete.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		HttpStatus statut = attribut instanceof Integer valeur && HttpStatus.resolve(valeur) != null
				? HttpStatus.valueOf(valeur) : HttpStatus.INTERNAL_SERVER_ERROR;
		String code = switch (statut) {
			case NOT_FOUND -> "RESSOURCE_INTROUVABLE";
			case METHOD_NOT_ALLOWED -> "METHODE_NON_AUTORISEE";
			case UNSUPPORTED_MEDIA_TYPE -> "TYPE_NON_SUPPORTE";
			case BAD_REQUEST -> "REQUETE_INVALIDE";
			default -> statut.is5xxServerError() ? "ERREUR_INTERNE" : "REQUETE_INVALIDE";
		};
		return ResponseEntity.status(statut).body(new ErreurDto(code, MessagesErreur.pour(code)));
	}

}
