package cm.kfokam48.presence.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

/**
 * RG19 : moyenne arithmétique des notes reçues sur les exercices relus, arrondie à 2 décimales
 * (au plus proche, 0,005 vers le haut) ; null quand l'étudiant n'a reçu aucune note.
 * Seule l'API la calcule (F3).
 */
public final class RegleMoyenne {

	private RegleMoyenne() {
	}

	public static Double moyenne(Collection<Integer> notes) {
		if (notes == null || notes.isEmpty()) {
			return null;
		}
		long somme = notes.stream().mapToLong(Integer::longValue).sum();
		return BigDecimal.valueOf(somme)
			.divide(BigDecimal.valueOf(notes.size()), 2, RoundingMode.HALF_UP)
			.doubleValue();
	}

}
