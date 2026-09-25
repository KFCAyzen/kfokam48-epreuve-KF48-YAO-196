package cm.kfokam48.presence.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;

/** Règles de gestion du tirage du relecteur, testées sans base (B6). */
class SelecteurRelecteurTest {

	private static final Long AUTEUR = 1L;

	private final Random hasard = new Random(48);

	@Test
	void rg2LAuteurNEstJamaisTireSur1000Tirages() {
		List<Long> presents = List.of(1L, 2L, 3L, 4L);
		for (int i = 0; i < 1000; i++) {
			assertThat(SelecteurRelecteur.choisir(AUTEUR, presents, Map.of(), hasard)).isPresent().get().isNotEqualTo(AUTEUR);
		}
	}

	@Test
	void rg14LeRelecteurEstUnEtudiantPresent() {
		assertThat(SelecteurRelecteur.choisir(AUTEUR, List.of(1L, 7L), Map.of(), hasard)).contains(7L);
	}

	@Test
	void rg14OnTireParmiLesMoinsCharges() {
		Map<Long, Long> charge = Map.of(2L, 2L, 3L, 1L, 4L, 1L);
		for (int i = 0; i < 200; i++) {
			assertThat(SelecteurRelecteur.choisir(AUTEUR, List.of(1L, 2L, 3L, 4L), charge, hasard).orElseThrow()).isIn(3L, 4L);
		}
	}

	@Test
	void rg14LeTirageResteAuHasardEntreEgaux() {
		Map<Long, Integer> tirages = new HashMap<>();
		for (int i = 0; i < 1000; i++) {
			tirages.merge(SelecteurRelecteur.choisir(AUTEUR, List.of(1L, 2L, 3L), Map.of(), hasard).orElseThrow(), 1, Integer::sum);
		}
		assertThat(tirages).containsOnlyKeys(2L, 3L);
		assertThat(tirages.get(2L)).isBetween(400, 600);
	}

	@Test
	void rg15AucunRelecteurQuandLAuteurEstSeulPresent() {
		assertThat(SelecteurRelecteur.choisir(AUTEUR, List.of(1L), Map.of(), hasard)).isEmpty();
		assertThat(SelecteurRelecteur.choisir(AUTEUR, List.of(), Map.of(), hasard)).isEmpty();
	}

}
