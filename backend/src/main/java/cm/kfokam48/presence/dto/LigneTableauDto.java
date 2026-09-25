package cm.kfokam48.presence.dto;

/** Élément imposé de GET /api/tableau : { etudiantId, nom, presences, exercicesDeposes, moyenne, relecturesEnAttente }. */
public record LigneTableauDto(Long etudiantId, String nom, long presences, long exercicesDeposes, Double moyenne,
		long relecturesEnAttente, boolean moyenneProvisoire) {
}
