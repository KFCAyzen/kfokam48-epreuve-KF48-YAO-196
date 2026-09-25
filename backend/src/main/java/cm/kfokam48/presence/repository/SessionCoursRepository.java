package cm.kfokam48.presence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cm.kfokam48.presence.entity.SessionCours;
import jakarta.persistence.LockModeType;

public interface SessionCoursRepository extends JpaRepository<SessionCours, Long> {

	boolean existsByCode(String code);

	Optional<SessionCours> findByCode(String code);

	/**
	 * Verrouille la session jusqu'à la fin de la transaction (SELECT ... FOR UPDATE). Les présences et les
	 * dépôts d'une même session s'enregistrent l'un après l'autre : deux transactions ne peuvent plus
	 * assigner le même exercice en même temps (#56, RG13, RG15).
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM SessionCours s WHERE s.id = :id")
	Optional<SessionCours> verrouiller(@Param("id") Long id);

	List<SessionCours> findByPromotionIdOrderByOuvertureAtDescIdDesc(Long promotionId);

	/** Nombre de présents par session d'une promotion : [session_id, nombre]. Une requête pour toutes (ENF2). */
	@Query(nativeQuery = true, value = """
			SELECT p.session_id, COUNT(*)
			FROM presence p JOIN session_cours s ON s.id = p.session_id
			WHERE s.promotion_id = :promotionId
			GROUP BY p.session_id
			""")
	List<Object[]> presentsParSession(@Param("promotionId") Long promotionId);

	/** Exercices déposés et non encore relus par session : [session_id, déposés, en attente]. */
	@Query(nativeQuery = true, value = """
			SELECT e.session_id, COUNT(*), SUM(CASE WHEN e.statut <> 'RELU' THEN 1 ELSE 0 END)
			FROM exercice e JOIN session_cours s ON s.id = e.session_id
			WHERE s.promotion_id = :promotionId
			GROUP BY e.session_id
			""")
	List<Object[]> exercicesParSession(@Param("promotionId") Long promotionId);

}
