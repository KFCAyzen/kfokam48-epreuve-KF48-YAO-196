package cm.kfokam48.presence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

/** RG1 : le code de présence expire 15 minutes après l'ouverture de la session. Test unitaire (B6). */
class SessionCoursTest {

	private static final Instant OUVERTURE = Instant.parse("2026-09-25T08:00:00Z");

	private final SessionCours session = SessionCours.ouvrir("Streams Java", new Promotion("P"), "ABC234", OUVERTURE);

	@Test
	void rg1LExpirationEstFixeeQuinzeMinutesApresLOuverture() {
		assertThat(session.getOuvertureAt()).isEqualTo(OUVERTURE);
		assertThat(Duration.between(session.getOuvertureAt(), session.getExpirationAt())).isEqualTo(Duration.ofMinutes(15));
	}

	@Test
	void rg1LeCodeEstValableJusquALaDerniereSeconde() {
		assertThat(session.codeExpire(OUVERTURE)).isFalse();
		assertThat(session.codeExpire(OUVERTURE.plus(Duration.ofMinutes(15)).minusMillis(1))).isFalse();
	}

	@Test
	void rg1LeCodeNeMarchePlusAQuinzeMinutesPile() {
		assertThat(session.codeExpire(OUVERTURE.plus(Duration.ofMinutes(15)))).isTrue();
		assertThat(session.codeExpire(OUVERTURE.plus(Duration.ofHours(2)))).isTrue();
	}

	@Test
	void uneSessionOuverteNEstPasCloturee() {
		assertThat(session.estCloturee()).isFalse();
	}

}
