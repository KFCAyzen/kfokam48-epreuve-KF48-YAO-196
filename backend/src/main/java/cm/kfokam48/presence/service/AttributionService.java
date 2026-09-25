package cm.kfokam48.presence.service;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/** EF5 : le système assigne à chaque exercice un relecteur tiré parmi les présents (RG2, RG13, RG14, RG15). */
@Service
@Transactional
public class AttributionService {

	private final RelectureRepository relectures;

	private final ExerciceRepository exercices;

	private final PresenceRepository presences;

	private final EtudiantRepository etudiants;

	private final RandomGenerator hasard;

	private final Clock horloge;

	public AttributionService(RelectureRepository relectures, ExerciceRepository exercices,
			PresenceRepository presences, EtudiantRepository etudiants, RandomGenerator hasard, Clock horloge) {
		this.relectures = relectures;
		this.exercices = exercices;
		this.presences = presences;
		this.etudiants = etudiants;
		this.hasard = hasard;
		this.horloge = horloge;
	}

	/**
	 * Tire un relecteur pour un exercice encore DEPOSE. Sans éligible présent, l'exercice reste DEPOSE (RG15).
	 * @return true si un relecteur a été assigné
	 */
	public boolean assigner(Exercice exercice) {
		if (exercice.getStatut() != StatutExercice.DEPOSE) {
			return false;
		}
		Long sessionId = exercice.getSession().getId();
		List<Long> presents = presences.etudiantsPresents(sessionId);
		return SelecteurRelecteur.choisir(exercice.getEtudiant().getId(), presents, charge(sessionId), hasard)
			.map(relecteurId -> {
				relectures.save(new Relecture(exercice, etudiants.getReferenceById(relecteurId), horloge.instant()));
				exercice.changerStatut(StatutExercice.EN_ATTENTE_RELECTURE);
				return true;
			})
			.orElse(false);
	}

	/** RG15 : dès qu'un étudiant devient présent, les exercices de la session sans relecteur en reçoivent un. */
	public void assignerEnAttente(Long sessionId) {
		exercices.findBySessionIdAndStatutOrderByDeposeAtAscIdAsc(sessionId, StatutExercice.DEPOSE)
			.forEach(this::assigner);
	}

	private Map<Long, Long> charge(Long sessionId) {
		Map<Long, Long> charge = new HashMap<>();
		for (Object[] ligne : relectures.chargeParRelecteur(sessionId)) {
			charge.put((Long) ligne[0], ((Number) ligne[1]).longValue());
		}
		return charge;
	}

}
