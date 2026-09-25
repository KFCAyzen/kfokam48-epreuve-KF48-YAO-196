package cm.kfokam48.presence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cm.kfokam48.presence.entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

	List<Promotion> findAllByOrderByNomAsc();

}
