package cm.kfokam48.presence.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** RG11 : URL absolue en http:// ou https://, 500 caractères au plus. */
class RegleLienTest {

	@ParameterizedTest
	@ValueSource(strings = { "https://github.com/ambarga/kf48-api-contrat", "http://exemple.cm/exercice?id=4",
			"HTTPS://GITLAB.COM/A/B" })
	void rg11AccepteUneUrlHttpAbsolue(String lien) {
		assertThat(RegleLien.estValide(lien)).isTrue();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "github.com/ambarga", "ftp://exemple.cm/a", "https://", "javascript:alert(1)",
			"https://exemple .cm/a", "mon exercice" })
	void rg11RefuseCeQuiNEstPasUneUrlHttpAbsolue(String lien) {
		assertThat(RegleLien.estValide(lien)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(ints = { 500, 501 })
	void rg11LimiteLaLongueurA500Caracteres(int longueur) {
		String prefixe = "https://exemple.cm/";
		String lien = prefixe + "a".repeat(longueur - prefixe.length());
		assertThat(RegleLien.estValide(lien)).isEqualTo(longueur <= 500);
	}

}
