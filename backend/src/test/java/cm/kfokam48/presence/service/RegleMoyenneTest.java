package cm.kfokam48.presence.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

/** RG19 : moyenne des notes reçues, 2 décimales, null sans note. Test unitaire (B6). */
class RegleMoyenneTest {

	@Test
	void rg19MoyenneArithmetique() {
		assertThat(RegleMoyenne.moyenne(List.of(12, 15))).isEqualTo(13.5);
		assertThat(RegleMoyenne.moyenne(List.of(15, 14, 15, 15))).isEqualTo(14.75);
	}

	@Test
	void rg19ArrondiADeuxDecimales() {
		assertThat(RegleMoyenne.moyenne(List.of(13, 13, 14))).isEqualTo(13.33);
		assertThat(RegleMoyenne.moyenne(List.of(14, 15, 15))).isEqualTo(14.67);
	}

	@Test
	void rg19NullQuandAucuneNote() {
		assertThat(RegleMoyenne.moyenne(List.of())).isNull();
		assertThat(RegleMoyenne.moyenne(null)).isNull();
	}

	@Test
	void rg19BornesDeLaNote() {
		assertThat(RegleMoyenne.moyenne(List.of(0))).isEqualTo(0.0);
		assertThat(RegleMoyenne.moyenne(List.of(20, 20))).isEqualTo(20.0);
	}

}
