package cm.kfokam48.presence.dto;

import cm.kfokam48.presence.entity.Etudiant;

/** Schéma « Etudiant » de api/contrat.yaml. */
public record EtudiantDto(Long id, String nom, Long promotionId) {

	public static EtudiantDto de(Etudiant etudiant) {
		return new EtudiantDto(etudiant.getId(), etudiant.getNom(), etudiant.getPromotion().getId());
	}

}
