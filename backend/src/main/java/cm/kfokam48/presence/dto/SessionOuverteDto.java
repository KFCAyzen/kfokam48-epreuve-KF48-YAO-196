package cm.kfokam48.presence.dto;

import java.time.Instant;

import cm.kfokam48.presence.entity.SessionCours;

/** Réponse imposée de POST /api/sessions : { id, code, ouvertureAt, expirationAt }. */
public record SessionOuverteDto(Long id, String code, Instant ouvertureAt, Instant expirationAt) {

	public static SessionOuverteDto de(SessionCours session) {
		return new SessionOuverteDto(session.getId(), session.getCode(), session.getOuvertureAt(),
				session.getExpirationAt());
	}

}
