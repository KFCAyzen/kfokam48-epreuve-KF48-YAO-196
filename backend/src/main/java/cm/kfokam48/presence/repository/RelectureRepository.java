package cm.kfokam48.presence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cm.kfokam48.presence.entity.Relecture;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

	/** Relectures assignées dans une session, par relecteur : [relecteur_id, nombre] (RG14). */
	@Query("SELECT r.relecteur.id, COUNT(r) FROM Relecture r WHERE r.exercice.session.id = :sessionId GROUP BY r.relecteur.id")
	List<Object[]> chargeParRelecteur(@Param("sessionId") Long sessionId);

}
