package cm.kfokam48.presence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import cm.kfokam48.presence.entity.Etudiant;
import cm.kfokam48.presence.entity.Promotion;
import cm.kfokam48.presence.entity.SessionCours;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.PromotionRepository;
import cm.kfokam48.presence.repository.RelectureRepository;
import cm.kfokam48.presence.repository.SessionCoursRepository;

/** EF5 de bout en bout : dépôt, présence et tirage des deux relecteurs (RG2, RG13, RG14, RG15). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AttributionIntegrationTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private PromotionRepository promotions;

	@Autowired
	private EtudiantRepository etudiants;

	@Autowired
	private SessionCoursRepository sessions;

	@Autowired
	private RelectureRepository relectures;

	private Etudiant auteur;

	private Etudiant pair;

	private SessionCours session;

	@BeforeEach
	void donnees() {
		Promotion promo = promotions.save(new Promotion("Promo test EF5"));
		auteur = etudiants.save(new Etudiant("Mbarga, Aïcha", promo));
		pair = etudiants.save(new Etudiant("Nkoulou, Brice", promo));
		session = sessions.save(SessionCours.ouvrir("Streams", promo, "ATT234", Instant.now()));
	}

	private ResultActions presence(Etudiant etudiant) throws Exception {
		return mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
			.content("{\"code\": \"ATT234\", \"etudiantId\": %d}".formatted(etudiant.getId())))
			.andExpect(status().isCreated());
	}

	private ResultActions deposer(Etudiant etudiant) throws Exception {
		return mvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
			.content("{\"sessionId\": %d, \"etudiantId\": %d, \"lien\": \"https://github.com/kf48/exo\"}"
				.formatted(session.getId(), etudiant.getId())))
			.andExpect(status().isCreated());
	}

	private long id(ResultActions resultat) throws Exception {
		return ((Number) JsonPath.read(resultat.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
	}

	@Test
	void unPairPresentDevientRelecteurDesLeDepotRg14() throws Exception {
		presence(auteur);
		presence(pair);

		long exerciceId = id(deposer(auteur).andExpect(jsonPath("$.statut").value("EN_ATTENTE_RELECTURE")));

		assertThat(relectures.findByExerciceId(exerciceId)).as("un seul éligible : un relecteur en attendant le second")
			.extracting(r -> r.getRelecteur().getId())
			.containsExactly(pair.getId());
	}

	@Test
	void deuxPairsPresentsDeviennentLesDeuxRelecteursRg13() throws Exception {
		Etudiant second = etudiants.save(new Etudiant("Tagne, Joël", auteur.getPromotion()));
		presence(auteur);
		presence(pair);
		presence(second);

		long exerciceId = id(deposer(auteur).andExpect(jsonPath("$.statut").value("EN_ATTENTE_RELECTURE")));

		assertThat(relectures.findByExerciceId(exerciceId)).extracting(r -> r.getRelecteur().getId())
			.as("deux relecteurs différents, jamais l'auteur (RG2, RG13)")
			.containsExactlyInAnyOrder(pair.getId(), second.getId());
	}

	@Test
	void seulPresentLExerciceResteDeposeSansRelecteurRg15() throws Exception {
		presence(auteur);

		long exerciceId = id(deposer(auteur).andExpect(jsonPath("$.statut").value("DEPOSE")));

		assertThat(relectures.findByExerciceId(exerciceId)).isEmpty();
	}

	@Test
	void unRelecteurEstAssigneDesQuUnPairMarqueSaPresenceRg15() throws Exception {
		presence(auteur);
		long exerciceId = id(deposer(auteur).andExpect(jsonPath("$.statut").value("DEPOSE")));

		presence(pair);

		assertThat(relectures.findByExerciceId(exerciceId)).extracting(r -> r.getRelecteur().getId())
			.containsExactly(pair.getId());
		assertThat(relectures.findByExerciceId(exerciceId).getFirst().getExercice().getStatut().name())
			.isEqualTo("EN_ATTENTE_RELECTURE");
	}

	@Test
	void unAbsentPeutDeposerMaisNeRelitPasRg14() throws Exception {
		presence(pair);

		long exerciceId = id(deposer(auteur).andExpect(jsonPath("$.statut").value("EN_ATTENTE_RELECTURE")));

		assertThat(relectures.findByExerciceId(exerciceId)).extracting(r -> r.getRelecteur().getId()).containsExactly(pair.getId());
	}

}
