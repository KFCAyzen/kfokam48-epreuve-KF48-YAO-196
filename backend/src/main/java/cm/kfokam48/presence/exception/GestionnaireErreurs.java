package cm.kfokam48.presence.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cm.kfokam48.presence.dto.ErreurDto;

/** Point unique de traduction des erreurs au format { code, message } (B4). */
@RestControllerAdvice
public class GestionnaireErreurs {

	@ExceptionHandler(ErreurMetier.class)
	public ResponseEntity<ErreurDto> erreurMetier(ErreurMetier e) {
		return ResponseEntity.status(e.getStatut()).body(new ErreurDto(e.getCode(), e.getMessage()));
	}

}
