package cm.kfokam48.presence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cm.kfokam48.presence.entity.Presence;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

	boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

	/** Étudiants présents à une session, toutes sources confondues (RG14). */
	@Query("SELECT p.etudiant.id FROM Presence p WHERE p.session.id = :sessionId")
	List<Long> etudiantsPresents(@Param("sessionId") Long sessionId);

}
