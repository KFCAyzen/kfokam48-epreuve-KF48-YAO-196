package cm.kfokam48.presence.dto;

import cm.kfokam48.presence.entity.Presence;
import cm.kfokam48.presence.entity.SourcePresence;

/** Réponse imposée de POST /api/presences : { id, sessionId, etudiantId, source }. */
public record PresenceDto(Long id, Long sessionId, Long etudiantId, SourcePresence source) {

	public static PresenceDto de(Presence presence) {
		return new PresenceDto(presence.getId(), presence.getSession().getId(), presence.getEtudiant().getId(),
				presence.getSource());
	}

}
