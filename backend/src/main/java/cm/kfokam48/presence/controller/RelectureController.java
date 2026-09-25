package cm.kfokam48.presence.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cm.kfokam48.presence.dto.RelectureAssigneeDto;
import cm.kfokam48.presence.dto.RelectureRendueDto;
import cm.kfokam48.presence.dto.RendreRelectureRequete;
import cm.kfokam48.presence.service.RelectureService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/relectures")
public class RelectureController {

	static final String EN_TETE_ETUDIANT = "X-Etudiant-Id";

	private final RelectureService service;

	public RelectureController(RelectureService service) {
		this.service = service;
	}

	/** Opération imposée : 200, 400 NOTE_INVALIDE, 403 AUTO_RELECTURE, 409 RELECTURE_DEJA_RENDUE. */
	@PostMapping("/{id}")
	public RelectureRendueDto rendre(@PathVariable Long id,
			@RequestHeader(name = EN_TETE_ETUDIANT, required = false) Long appelantId,
			@Valid @RequestBody RendreRelectureRequete requete) {
		return service.rendre(id, appelantId, requete);
	}

	@PostMapping("/{id}/debut")
	public RelectureAssigneeDto commencer(@PathVariable Long id, @RequestHeader(EN_TETE_ETUDIANT) Long appelantId) {
		return service.commencer(id, appelantId);
	}

}
