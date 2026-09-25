package cm.kfokam48.presence.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cm.kfokam48.presence.dto.OuvrirSessionRequete;
import cm.kfokam48.presence.dto.SessionOuverteDto;
import cm.kfokam48.presence.service.SessionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

	private final SessionService service;

	public SessionController(SessionService service) {
		this.service = service;
	}

	/** Opération imposée : 201 { id, code, ouvertureAt, expirationAt }, 400 si un champ manque. */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SessionOuverteDto ouvrir(@Valid @RequestBody OuvrirSessionRequete requete) {
		return service.ouvrir(requete);
	}

}
