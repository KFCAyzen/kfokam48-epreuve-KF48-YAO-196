package cm.kfokam48.presence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cm.kfokam48.presence.entity.Presence;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

	boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

}
