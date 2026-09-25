package cm.kfokam48.presence.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

import cm.kfokam48.presence.entity.StatutExercice;

/**
 * Règles de la double relecture (étape 3), sans accès à la base : testables unitairement.
 * <ul>
 * <li>RG25 : la note retenue d'un exercice est la moyenne des notes rendues, marquée provisoire tant
 * que toutes les relectures attendues ne sont pas rendues ; aucune note sans relecture rendue ;</li>
 * <li>D4 : le statut de l'exercice se déduit de ses relectures ; RELU quand toutes sont rendues.</li>
 * </ul>
 */
public final class RegleNoteRetenue {

	/** Note retenue d'un exercice : null sans relecture rendue. */
	public record NoteRetenue(Double note, boolean provisoire) {
	}

	private RegleNoteRetenue() {
	}

	public static NoteRetenue calculer(Collection<Integer> notesRendues, int relecteursRequis) {
		if (notesRendues == null || notesRendues.isEmpty()) {
			return new NoteRetenue(null, false);
		}
		long somme = notesRendues.stream().mapToLong(Integer::longValue).sum();
		double note = BigDecimal.valueOf(somme)
			.divide(BigDecimal.valueOf(notesRendues.size()), 2, RoundingMode.HALF_UP)
			.doubleValue();
		return new NoteRetenue(note, notesRendues.size() < relecteursRequis);
	}

	/** D4 : statut d'un exercice selon ses relectures assignées, commencées et rendues. */
	public static StatutExercice statut(int relecteursRequis, int assignees, int commencees, int rendues) {
		if (assignees == 0) {
			return StatutExercice.DEPOSE;
		}
		if (rendues >= relecteursRequis) {
			return StatutExercice.RELU;
		}
		if (commencees > 0 || rendues > 0) {
			return StatutExercice.EN_COURS_DE_RELECTURE;
		}
		return StatutExercice.EN_ATTENTE_RELECTURE;
	}

}
