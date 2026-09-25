package cm.kfokam48.presence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import cm.kfokam48.presence.entity.Etudiant;

/**
 * Requêtes agrégées du tableau du formateur (EF7) : une requête par indicateur pour toute la
 * promotion, jamais une requête par étudiant (ENF2). Chaque ligne : [etudiant_id, valeur].
 */
public interface TableauRepository extends Repository<Etudiant, Long> {

	/** RG22 : sessions où l'étudiant est présent, toutes sources confondues (une présence par session, RG4). */
	@Query("SELECT p.etudiant.id, COUNT(p) FROM Presence p WHERE p.etudiant.promotion.id = :promotionId GROUP BY p.etudiant.id")
	List<Object[]> presences(@Param("promotionId") Long promotionId);

	@Query("SELECT e.etudiant.id, COUNT(e) FROM Exercice e WHERE e.etudiant.promotion.id = :promotionId GROUP BY e.etudiant.id")
	List<Object[]> exercicesDeposes(@Param("promotionId") Long promotionId);

	/** Notes reçues par chaque auteur sur ses exercices relus (RG19). */
	@Query("SELECT r.exercice.etudiant.id, r.note FROM Relecture r WHERE r.rendueAt IS NOT NULL AND r.exercice.etudiant.promotion.id = :promotionId")
	List<Object[]> notesRecues(@Param("promotionId") Long promotionId);

	/** RG22 : relectures assignées à l'étudiant et non rendues (Q11). */
	@Query("SELECT r.relecteur.id, COUNT(r) FROM Relecture r WHERE r.rendueAt IS NULL AND r.relecteur.promotion.id = :promotionId GROUP BY r.relecteur.id")
	List<Object[]> relecturesEnAttente(@Param("promotionId") Long promotionId);

}
