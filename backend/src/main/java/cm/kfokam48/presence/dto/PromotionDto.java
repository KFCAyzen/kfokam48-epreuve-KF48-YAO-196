package cm.kfokam48.presence.dto;

import cm.kfokam48.presence.entity.Promotion;

/** Schéma « Promotion » de api/contrat.yaml. */
public record PromotionDto(Long id, String nom) {

	public static PromotionDto de(Promotion promotion) {
		return new PromotionDto(promotion.getId(), promotion.getNom());
	}

}
