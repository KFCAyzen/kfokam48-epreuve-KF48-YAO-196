package cm.kfokam48.presence.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.dto.EtudiantDto;
import cm.kfokam48.presence.dto.PromotionDto;
import cm.kfokam48.presence.entity.Promotion;
import cm.kfokam48.presence.exception.ErreurMetier;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.PromotionRepository;

@Service
@Transactional(readOnly = true)
public class PromotionService {

	private final PromotionRepository promotions;

	private final EtudiantRepository etudiants;

	public PromotionService(PromotionRepository promotions, EtudiantRepository etudiants) {
		this.promotions = promotions;
		this.etudiants = etudiants;
	}

	public List<PromotionDto> lister() {
		return promotions.findAllByOrderByNomAsc().stream().map(PromotionDto::de).toList();
	}

	/** EF3 : la liste dans laquelle l'étudiant choisit son nom (Q1). */
	public List<EtudiantDto> etudiants(Long promotionId) {
		Promotion promotion = trouver(promotionId);
		return etudiants.findByPromotionIdOrderByNomAsc(promotion.getId()).stream().map(EtudiantDto::de).toList();
	}

	/** Promotion désignée par le chemin d'une requête : 404 si elle n'existe pas. */
	public Promotion trouver(Long promotionId) {
		return promotions.findById(promotionId).orElseThrow(() -> ErreurMetier.promotionInconnue(HttpStatus.NOT_FOUND));
	}

}
