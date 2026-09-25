package cm.kfokam48.presence.exception;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import cm.kfokam48.presence.dto.ErreurDto;
import tools.jackson.core.JacksonException;

/**
 * Point unique de traduction des erreurs au format imposé { code, message } (B4, ENF4).
 * Aucune réponse ne contient de stack trace ni de détail technique.
 */
@RestControllerAdvice
public class GestionnaireErreurs {

	private static final Logger LOG = LoggerFactory.getLogger(GestionnaireErreurs.class);

	@ExceptionHandler(ErreurMetier.class)
	public ResponseEntity<ErreurDto> erreurMetier(ErreurMetier e) {
		return reponse(e.getStatut(), e.getCode(), e.getMessage());
	}

	/**
	 * Bean Validation sur un corps de requête. Chaque contrainte porte son code d'erreur dans
	 * son attribut {@code message} (ex. {@code @NotNull(message = "CHAMP_MANQUANT")}).
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErreurDto> validation(MethodArgumentNotValidException e) {
		FieldError erreur = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.min(Comparator.comparing(FieldError::getField).thenComparing(f -> Objects.toString(f.getDefaultMessage())))
			.orElse(null);
		if (erreur == null) {
			return reponse(HttpStatus.BAD_REQUEST, "REQUETE_INVALIDE");
		}
		String code = Optional.ofNullable(erreur.getDefaultMessage())
			.filter(m -> m.matches("[A-Z_]+"))
			.orElse("REQUETE_INVALIDE");
		return reponse(HttpStatus.BAD_REQUEST, code, MessagesErreur.pour(code, erreur.getField()));
	}

	/** JSON illisible, ou valeur du mauvais type : une note 12.5 est une note invalide (RG3). */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErreurDto> corpsIllisible(HttpMessageNotReadableException e) {
		Optional<String> champ = champEnCause(e);
		if (champ.filter("note"::equals).isPresent()) {
			return reponse(HttpStatus.BAD_REQUEST, "NOTE_INVALIDE");
		}
		if (champ.isPresent()) {
			return reponse(HttpStatus.BAD_REQUEST, "REQUETE_INVALIDE",
					"Le champ « %s » n'a pas le bon type.".formatted(champ.get()));
		}
		return reponse(HttpStatus.BAD_REQUEST, "REQUETE_INVALIDE");
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErreurDto> parametreManquant(MissingServletRequestParameterException e) {
		return reponse(HttpStatus.BAD_REQUEST, "CHAMP_MANQUANT", MessagesErreur.pour("CHAMP_MANQUANT", e.getParameterName()));
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<ErreurDto> enteteManquant(MissingRequestHeaderException e) {
		return reponse(HttpStatus.BAD_REQUEST, "CHAMP_MANQUANT", MessagesErreur.pour("CHAMP_MANQUANT", e.getHeaderName()));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErreurDto> parametreMalForme(MethodArgumentTypeMismatchException e) {
		return reponse(HttpStatus.BAD_REQUEST, "PARAMETRE_INVALIDE", MessagesErreur.pour("PARAMETRE_INVALIDE", e.getName()));
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ErreurDto> parametreInvalide(HandlerMethodValidationException e) {
		String nom = e.getParameterValidationResults()
			.stream()
			.findFirst()
			.map(r -> r.getMethodParameter().getParameterName())
			.orElse("?");
		return reponse(HttpStatus.BAD_REQUEST, "PARAMETRE_INVALIDE", MessagesErreur.pour("PARAMETRE_INVALIDE", nom));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErreurDto> routeInconnue(NoResourceFoundException e) {
		return reponse(HttpStatus.NOT_FOUND, "RESSOURCE_INTROUVABLE");
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErreurDto> methodeNonAutorisee(HttpRequestMethodNotSupportedException e) {
		return reponse(HttpStatus.METHOD_NOT_ALLOWED, "METHODE_NON_AUTORISEE");
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ErreurDto> typeNonSupporte(HttpMediaTypeNotSupportedException e) {
		return reponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "TYPE_NON_SUPPORTE");
	}

	/** Deux requêtes simultanées butent sur une contrainte d'unicité de la base (ENF3). */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErreurDto> conflit(DataIntegrityViolationException e) {
		LOG.warn("Contrainte d'intégrité violée : {}", e.getMostSpecificCause().getMessage());
		return reponse(HttpStatus.CONFLICT, "CONFLIT");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErreurDto> inattendue(Exception e) {
		LOG.error("Erreur inattendue", e);
		return reponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERREUR_INTERNE");
	}

	private static Optional<String> champEnCause(Throwable e) {
		for (Throwable t = e; t != null; t = t.getCause()) {
			if (t instanceof JacksonException jackson) {
				return jackson.getPath()
					.stream()
					.map(JacksonException.Reference::getPropertyName)
					.filter(Objects::nonNull)
					.reduce((premier, dernier) -> dernier);
			}
		}
		return Optional.empty();
	}

	private static ResponseEntity<ErreurDto> reponse(HttpStatus statut, String code) {
		return reponse(statut, code, MessagesErreur.pour(code));
	}

	private static ResponseEntity<ErreurDto> reponse(HttpStatus statut, String code, String message) {
		return ResponseEntity.status(statut).body(new ErreurDto(code, message));
	}

}
