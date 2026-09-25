package cm.kfokam48.presence.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Tirage du relecteur, sans accès à la base : testable avec une graine fixe.
 * <ul>
 * <li>RG2 : l'auteur n'est jamais tiré ;</li>
 * <li>RG14 : tirage au hasard parmi les étudiants présents à la session, en retenant ceux qui ont
 * le moins de relectures assignées dans cette session ;</li>
 * <li>RG15 : aucun éligible, aucun relecteur.</li>
 * </ul>
 */
public final class SelecteurRelecteur {

	private SelecteurRelecteur() {
	}

	/**
	 * @param auteurId auteur de l'exercice
	 * @param presents étudiants présents à la session
	 * @param charge nombre de relectures déjà assignées dans la session, par étudiant (absent = 0)
	 */
	public static Optional<Long> choisir(Long auteurId, Collection<Long> presents, Map<Long, Long> charge,
			RandomGenerator hasard) {
		List<Long> eligibles = presents.stream().filter(id -> !id.equals(auteurId)).distinct().sorted().toList();
		if (eligibles.isEmpty()) {
			return Optional.empty();
		}
		long chargeMin = eligibles.stream().mapToLong(id -> charge.getOrDefault(id, 0L)).min().orElse(0);
		List<Long> moinsCharges = eligibles.stream().filter(id -> charge.getOrDefault(id, 0L) == chargeMin).toList();
		return Optional.of(moinsCharges.get(hasard.nextInt(moinsCharges.size())));
	}

}
