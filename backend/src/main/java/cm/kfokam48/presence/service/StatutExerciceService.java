package cm.kfokam48.presence.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.entity.Exercice;
import cm.kfokam48.presence.entity.Relecture;
import cm.kfokam48.presence.repository.RelectureRepository;

/** Recalcule le statut d'un exercice à partir de ses relectures (diagramme D4, RG25). */
@Service
@Transactional
public class StatutExerciceService {

	private final RelectureRepository relectures;

	public StatutExerciceService(RelectureRepository relectures) {
		this.relectures = relectures;
	}

	public void recalculer(Exercice exercice) {
		List<Relecture> liste = relectures.findByExerciceId(exercice.getId());
		int commencees = (int) liste.stream().filter(Relecture::estCommencee).count();
		int rendues = (int) liste.stream().filter(Relecture::estRendue).count();
		exercice.changerStatut(
				RegleNoteRetenue.statut(exercice.getRelecteursRequis(), liste.size(), commencees, rendues));
	}

}
