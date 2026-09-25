package cm.kfokam48.presence.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import cm.kfokam48.presence.entity.StatutExercice;
import cm.kfokam48.presence.service.RegleNoteRetenue.NoteRetenue;

/** Étape 3, RG25 et D4 : note retenue et statut d'un exercice relu par deux pairs. Test unitaire (B6). */
class RegleNoteRetenueTest {

	@Test
	void rg25LaNoteRetenueEstLaMoyenneDesDeux() {
		assertThat(RegleNoteRetenue.calculer(List.of(12, 15), 2)).isEqualTo(new NoteRetenue(13.5, false));
		assertThat(RegleNoteRetenue.calculer(List.of(14, 14), 2)).isEqualTo(new NoteRetenue(14.0, false));
	}

	@Test
	void rg25UneSeuleRelectureRendueDonneUneNoteProvisoire() {
		assertThat(RegleNoteRetenue.calculer(List.of(16), 2)).isEqualTo(new NoteRetenue(16.0, true));
	}

	@Test
	void rg25AucuneNoteSansRelectureRendue() {
		assertThat(RegleNoteRetenue.calculer(List.of(), 2)).isEqualTo(new NoteRetenue(null, false));
	}

	@Test
	void rg26UnExerciceReluAvantLeChangementGardeSaNoteUniqueDefinitive() {
		assertThat(RegleNoteRetenue.calculer(List.of(16), 1)).isEqualTo(new NoteRetenue(16.0, false));
	}

	@Test
	void d4LeStatutSuitLesRelectures() {
		assertThat(RegleNoteRetenue.statut(2, 0, 0, 0)).isEqualTo(StatutExercice.DEPOSE);
		assertThat(RegleNoteRetenue.statut(2, 1, 0, 0)).isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);
		assertThat(RegleNoteRetenue.statut(2, 2, 1, 0)).isEqualTo(StatutExercice.EN_COURS_DE_RELECTURE);
		assertThat(RegleNoteRetenue.statut(2, 2, 1, 1)).isEqualTo(StatutExercice.EN_COURS_DE_RELECTURE);
		assertThat(RegleNoteRetenue.statut(2, 2, 2, 2)).isEqualTo(StatutExercice.RELU);
		assertThat(RegleNoteRetenue.statut(1, 1, 1, 1)).isEqualTo(StatutExercice.RELU);
	}

}
