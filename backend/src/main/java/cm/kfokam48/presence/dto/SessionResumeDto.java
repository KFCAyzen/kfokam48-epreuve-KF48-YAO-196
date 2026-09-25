package cm.kfokam48.presence.dto;

import java.time.Instant;

import cm.kfokam48.presence.entity.SessionCours;

/** Schéma « SessionResume » de api/contrat.yaml. */
public record SessionResumeDto(Long id, String titre, Long promotionId, String code, Instant ouvertureAt,
		Instant expirationAt, Instant clotureeAt, long presents, long exercicesDeposes, long exercicesEnAttente) {

	public static SessionResumeDto de(SessionCours s, long presents, long exercicesDeposes, long exercicesEnAttente) {
		return new SessionResumeDto(s.getId(), s.getTitre(), s.getPromotion().getId(), s.getCode(),
				s.getOuvertureAt(), s.getExpirationAt(), s.getClotureeAt(), presents, exercicesDeposes,
				exercicesEnAttente);
	}

}
