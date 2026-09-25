package cm.kfokam48.presence.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cm.kfokam48.presence.entity.Exercice;
import cm.kfokam48.presence.entity.StatutExercice;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

	boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

	/** Exercices d'une session à qui il peut manquer un relecteur (RG15). */
	List<Exercice> findBySessionIdAndStatutInOrderByDeposeAtAscIdAsc(Long sessionId, Collection<StatutExercice> statuts);

	/** Les exercices d'un étudiant, le plus récent d'abord (EF12). */
	List<Exercice> findByEtudiantIdOrderByDeposeAtDescIdDesc(Long etudiantId);

}
