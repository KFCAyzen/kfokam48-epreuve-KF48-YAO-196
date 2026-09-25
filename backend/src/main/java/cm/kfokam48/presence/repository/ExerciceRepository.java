package cm.kfokam48.presence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cm.kfokam48.presence.entity.Exercice;
import cm.kfokam48.presence.entity.StatutExercice;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

	boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

	List<Exercice> findBySessionIdAndStatutOrderByDeposeAtAscIdAsc(Long sessionId, StatutExercice statut);

}
