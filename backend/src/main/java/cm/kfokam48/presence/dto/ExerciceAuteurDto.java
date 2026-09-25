package cm.kfokam48.presence.dto;

import java.util.List;

import cm.kfokam48.presence.entity.StatutExercice;

/**
 * Schéma « ExerciceAuteur » : l'exercice vu par son auteur (EF12). Note retenue (RG25), marquée provisoire
 * tant qu'une seule relecture est rendue ; commentaires sans leur auteur, jamais d'identité de relecteur (RG20).
 */
public record ExerciceAuteurDto(Long id, Long sessionId, String sessionTitre, String lien, StatutExercice statut,
		Double note, boolean noteProvisoire, List<String> commentaires) {
}
