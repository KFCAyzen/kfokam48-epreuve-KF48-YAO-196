package cm.kfokam48.presence.service;

import java.time.Clock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.random.RandomGenerator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.entity.Exercice;
import cm.kfokam48.presence.entity.Relecture;
import cm.kfokam48.presence.entity.StatutExercice;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.ExerciceRepository;
import cm.kfokam48.presence.repository.PresenceRepository;
import cm.kfokam48.presence.repository.RelectureRepository;

/**
 * EF5 : le système assigne à chaque exercice ses relecteurs, tirés parmi les présents (RG2, RG13, RG14,
 * RG15). Depuis l'étape 3, deux relecteurs différents par exercice.
 */
@Service
@Transactional
public class AttributionService {

	private static final List<StatutExercice> A_COMPLETER = List.of(StatutExercice.DEPOSE,
			StatutExercice.EN_ATTENTE_RELECTURE, StatutExercice.EN_COURS_DE_RELECTURE);

	private final RelectureRepository relectures;

	private final ExerciceRepository exercices;

	private final PresenceRepository presences;

	private final EtudiantRepository etudiants;

	private final StatutExerciceService statuts;

	private final RandomGenerator hasard;

	private final Clock horloge;

	public AttributionService(RelectureRepository relectures, ExerciceRepository exercices,
			PresenceRepository presences, EtudiantRepository etudiants, StatutExerciceService statuts,
			RandomGenerator hasard, Clock horloge) {
		this.relectures = relectures;
		this.exercices = exercices;
		this.presences = presences;
		this.etudiants = etudiants;
		this.statuts = statuts;
		this.hasard = hasard;
		this.horloge = horloge;
	}

	/**
	 * Complète les relecteurs d'un exercice jusqu'au nombre requis, chacun différent de l'auteur et des
	 * relecteurs déjà assignés (RG2, RG13, RG14). Faute d'éligible, la place reste vide (RG15).
	 */
	public void completer(Exercice exercice) {
		List<Relecture> existantes = relectures.findByExerciceId(exercice.getId());
		int manquants = exercice.getRelecteursRequis() - existantes.size();
		if (manquants <= 0) {
			return;
		}
		Long sessionId = exercice.getSession().getId();
		Set<Long> dejaAssignes = new HashSet<>();
		existantes.forEach(r -> dejaAssignes.add(r.getRelecteur().getId()));
		List<Long> presents = presences.etudiantsPresents(sessionId);
		Map<Long, Long> charge = charge(sessionId);
		for (int i = 0; i < manquants; i++) {
			List<Long> eligibles = presents.stream().filter(id -> !dejaAssignes.contains(id)).toList();
			var choisi = SelecteurRelecteur.choisir(exercice.getEtudiant().getId(), eligibles, charge, hasard);
			if (choisi.isEmpty()) {
				break;
			}
			Long relecteurId = choisi.get();
			relectures.save(new Relecture(exercice, etudiants.getReferenceById(relecteurId), horloge.instant()));
			dejaAssignes.add(relecteurId);
			charge.merge(relecteurId, 1L, Long::sum);
		}
		statuts.recalculer(exercice);
	}

	/** RG15 : dès qu'un étudiant devient présent, les exercices de la session à qui il manque un relecteur en reçoivent. */
	public void assignerEnAttente(Long sessionId) {
		exercices.findBySessionIdAndStatutInOrderByDeposeAtAscIdAsc(sessionId, A_COMPLETER).forEach(this::completer);
	}

	private Map<Long, Long> charge(Long sessionId) {
		Map<Long, Long> charge = new HashMap<>();
		for (Object[] ligne : relectures.chargeParRelecteur(sessionId)) {
			charge.put((Long) ligne[0], ((Number) ligne[1]).longValue());
		}
		return charge;
	}

}
