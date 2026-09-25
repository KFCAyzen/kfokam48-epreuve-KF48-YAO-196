package cm.kfokam48.presence.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
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

import cm.kfokam48.presence.entity.Etudiant;
import cm.kfokam48.presence.entity.Promotion;
import cm.kfokam48.presence.entity.SessionCours;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.PromotionRepository;
import cm.kfokam48.presence.repository.SessionCoursRepository;

/** EF4 : POST /api/exercices, opération imposée. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExerciceControllerTest {

	private static final String LIEN = "https://github.com/ambarga/kf48-api-contrat";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private PromotionRepository promotions;

	@Autowired
	private EtudiantRepository etudiants;

	@Autowired
	private SessionCoursRepository sessions;

	private Promotion promo;

	private Etudiant aicha;

	private SessionCours session;

	@BeforeEach
	void donnees() {
		promo = promotions.save(new Promotion("Promo test EF4"));
		aicha = etudiants.save(new Etudiant("Mbarga, Aïcha", promo));
		session = sessions.save(SessionCours.ouvrir("Streams", promo, "EXO234", Instant.now()));
	}

	private ResultActions deposer(Long sessionId, Long etudiantId, String lien) throws Exception {
		return mvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
			.content("{\"sessionId\": %d, \"etudiantId\": %d, \"lien\": \"%s\"}".formatted(sessionId, etudiantId, lien)));
	}

	@Test
	void depot201AvecIdEtStatut() throws Exception {
		deposer(session.getId(), aicha.getId(), LIEN).andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.statut").isString());
	}

	@Test
	void secondDepot409ExerciceDejaDeposeRg10() throws Exception {
		deposer(session.getId(), aicha.getId(), LIEN).andExpect(status().isCreated());
		deposer(session.getId(), aicha.getId(), LIEN + "-v2").andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("EXERCICE_DEJA_DEPOSE"));
	}

	@Test
	void lienInvalide400Rg11() throws Exception {
		deposer(session.getId(), aicha.getId(), "github.com/ambarga").andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("LIEN_INVALIDE"));
	}

	@Test
	void depotAccepteApresExpirationDuCodeRg12() throws Exception {
		SessionCours hier = sessions.save(SessionCours.ouvrir("Hier", promo, "HIE234", Instant.now().minus(Duration.ofHours(20))));
		deposer(hier.getId(), aicha.getId(), LIEN).andExpect(status().isCreated());
	}

	@Test
	void sessionCloturee409Rg12() throws Exception {
		session.cloturer(Instant.now());
		sessions.saveAndFlush(session);
		deposer(session.getId(), aicha.getId(), LIEN).andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
	}

	@Test
	void sessionDUneAutrePromotion400Rg24() throws Exception {
		Promotion autre = promotions.save(new Promotion("Autre promo EF4"));
		SessionCours ailleurs = sessions.save(SessionCours.ouvrir("Ailleurs", autre, "AIL234", Instant.now()));
		deposer(ailleurs.getId(), aicha.getId(), LIEN).andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("ETUDIANT_HORS_PROMOTION"));
	}

	@Test
	void champManquant400() throws Exception {
		mvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
			.content("{\"sessionId\": %d, \"etudiantId\": %d}".formatted(session.getId(), aicha.getId())))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("CHAMP_MANQUANT"));
	}

}
