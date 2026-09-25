package cm.kfokam48.presence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * Étape 3 : une base remplie sous V1 (une relecture par exercice) survit à la migration V2
 * (deux relecteurs par exercice). Base PostgreSQL dédiée, migrée d'abord jusqu'à V1 seulement.
 */
class MigrationV2Test {

	@Test
	void lesDonneesEcritesEnV1SurviventAV2() {
		// Base PostgreSQL 16 dédiée à ce test (Testcontainers), migrée d'abord jusqu'à V1 seulement.
		SingleConnectionDataSource base = new SingleConnectionDataSource(
				"jdbc:tc:postgresql:16-alpine:///migration?TC_DAEMON=true", "test", "test", true);
		Flyway.configure().dataSource(base).locations("classpath:db/migration").target("1").load().migrate();

		JdbcTemplate sql = new JdbcTemplate(base);
		sql.update("INSERT INTO promotion (id, nom) VALUES (1, 'P')");
		for (Object[] e : new Object[][] { { 1, "Auteur" }, { 2, "Relecteur" }, { 3, "Autre" } }) {
			sql.update("INSERT INTO etudiant (id, nom, promotion_id) VALUES (?, ?, 1)", e);
		}
		sql.update("INSERT INTO session_cours (id, titre, promotion_id, code, ouverture_at, expiration_at) "
				+ "VALUES (1, 'S1', 1, 'MIG234', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
		String exercice = "INSERT INTO exercice (id, session_id, etudiant_id, lien, statut, depose_at) VALUES (?, 1, ?, ?, ?, CURRENT_TIMESTAMP)";
		sql.update(exercice, 10, 1, "https://a.cm/relu", "RELU");
		sql.update(exercice, 11, 3, "https://a.cm/attente", "EN_ATTENTE_RELECTURE");
		sql.update("INSERT INTO relecture (exercice_id, relecteur_id, note, commentaire, assignee_at, rendue_at) VALUES "
				+ "(10, 2, 14, 'Bien', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
		sql.update("INSERT INTO relecture (exercice_id, relecteur_id, assignee_at) VALUES (11, 2, CURRENT_TIMESTAMP)");

		Flyway.configure().dataSource(base).locations("classpath:db/migration").load().migrate();

		List<Map<String, Object>> relectures = sql.queryForList(
				"SELECT id, exercice_id, relecteur_id, note, commentaire FROM relecture ORDER BY exercice_id");
		assertThat(relectures).as("aucune relecture perdue").hasSize(2);
		assertThat(relectures).allSatisfy(r -> assertThat(r.get("id")).as("chaque relecture a son propre id").isNotNull());
		assertThat(relectures.get(0)).containsEntry("note", 14).containsEntry("commentaire", "Bien");
		assertThat(sql.queryForObject("SELECT relecteurs_requis FROM exercice WHERE id = 10", Integer.class))
			.as("un exercice déjà relu garde son relecteur unique (RG26)").isEqualTo(1);
		assertThat(sql.queryForObject("SELECT relecteurs_requis FROM exercice WHERE id = 11", Integer.class))
			.as("un exercice en attente passe à deux relecteurs (RG13)").isEqualTo(2);

		// Deux relecteurs différents sont désormais possibles pour un même exercice, pas deux fois le même.
		sql.update("INSERT INTO relecture (exercice_id, relecteur_id, assignee_at) VALUES (11, 1, CURRENT_TIMESTAMP)");
		assertThat(sql.queryForObject("SELECT COUNT(*) FROM relecture WHERE exercice_id = 11", Integer.class)).isEqualTo(2);
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> sql
			.update("INSERT INTO relecture (exercice_id, relecteur_id, assignee_at) VALUES (11, 1, CURRENT_TIMESTAMP)"))
			.as("unicité (exercice, relecteur)")
			.isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
	}

}
