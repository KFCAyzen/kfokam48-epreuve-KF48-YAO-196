package cm.kfokam48.presence.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

/** RG5 : 6 caractères parmi A-Z et 2-9, sans 0, O, 1 ni I. */
class GenerateurCodeTest {

	@Test
	void rg5LeCodeFaitSixCaracteresSansCaractereAmbigu() {
		GenerateurCode generateur = new GenerateurCode(new Random(48));
		Set<String> codes = new HashSet<>();
		for (int i = 0; i < 1000; i++) {
			String code = generateur.generer();
			assertThat(code).hasSize(6).matches("[A-HJ-NP-Z2-9]{6}").doesNotContain("0", "O", "1", "I");
			codes.add(code);
		}
		assertThat(codes).as("les codes tirés varient").hasSizeGreaterThan(990);
	}

}
