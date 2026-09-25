package cm.kfokam48.presence.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cm.kfokam48.presence.dto.EtudiantDto;
import cm.kfokam48.presence.dto.PromotionDto;
import cm.kfokam48.presence.dto.SessionResumeDto;
import cm.kfokam48.presence.service.PromotionService;
import cm.kfokam48.presence.service.SessionService;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

	private final PromotionService service;

	private final SessionService sessions;

	public PromotionController(PromotionService service, SessionService sessions) {
		this.service = service;
		this.sessions = sessions;
	}

	@GetMapping
	public List<PromotionDto> lister() {
		return service.lister();
	}

	@GetMapping("/{id}/etudiants")
	public List<EtudiantDto> etudiants(@PathVariable Long id) {
		return service.etudiants(id);
	}

	/** Sessions de la promotion, la plus récente d'abord (EF2 : historique du formateur, EF4 : choix de la session). */
	@GetMapping("/{id}/sessions")
	public List<SessionResumeDto> sessions(@PathVariable Long id) {
		return sessions.sessionsDeLaPromotion(id);
	}

}
