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

/**
 * EF1 : POST /api/presences, opération imposée. Un test par branche du diagramme D3,
 * avec le code HTTP du contrat : 201, 400, 409, 410.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PresenceControllerTest {

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

	private SessionCours ouverte;

	@BeforeEach
	void donnees() {
		promo = promotions.save(new Promotion("Promo test EF1"));
		aicha = etudiants.save(new Etudiant("Mbarga, Aïcha", promo));
		ouverte = sessions.save(SessionCours.ouvrir("Streams", promo, "K7M4QX", Instant.now()));
	}

	private ResultActions marquer(String code, Long etudiantId) throws Exception {
		return mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
			.content("{\"code\": \"%s\", \"etudiantId\": %d}".formatted(code, etudiantId)));
	}

	@Test
	void casNominal201AvecLaSourceEtudiantRg8() throws Exception {
		marquer("K7M4QX", aicha.getId()).andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.sessionId").value(ouverte.getId()))
			.andExpect(jsonPath("$.etudiantId").value(aicha.getId()))
			.andExpect(jsonPath("$.source").value("ETUDIANT"));
	}

	@Test
	void codeSaisiEnMinusculesAccepte() throws Exception {
		marquer(" k7m4qx ", aicha.getId()).andExpect(status().isCreated());
	}

	@Test
	void codeInconnu400Rg6() throws Exception {
		marquer("ZZZZZZ", aicha.getId()).andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("CODE_INCONNU"));
	}

	@Test
	void codeValideDUneAutrePromotionTraiteCommeInconnuRg6() throws Exception {
		Promotion autre = promotions.save(new Promotion("Autre promo"));
		sessions.save(SessionCours.ouvrir("Autre séance", autre, "B2C3D4", Instant.now()));

		marquer("B2C3D4", aicha.getId()).andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("CODE_INCONNU"));
	}

	@Test
	void codeExpire410Rg1() throws Exception {
		sessions.save(SessionCours.ouvrir("Séance d'hier", promo, "EXP234", Instant.now().minus(Duration.ofMinutes(16))));

		marquer("EXP234", aicha.getId()).andExpect(status().isGone())
			.andExpect(jsonPath("$.code").value("CODE_EXPIRE"))
			.andExpect(jsonPath("$.message").isNotEmpty());
	}

	@Test
	void sessionCloturee410Rg9() throws Exception {
		ouverte.cloturer(Instant.now());
		sessions.saveAndFlush(ouverte);

		marquer("K7M4QX", aicha.getId()).andExpect(status().isGone())
			.andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
	}

	@Test
	void dejaPresent409Rg4() throws Exception {
		marquer("K7M4QX", aicha.getId()).andExpect(status().isCreated());

		marquer("K7M4QX", aicha.getId()).andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("DEJA_PRESENT"));
	}

	@Test
	void etudiantInconnu400() throws Exception {
		marquer("K7M4QX", 999_999L).andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("ETUDIANT_INCONNU"));
	}

	@Test
	void champManquant400() throws Exception {
		mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON).content("{\"code\": \"K7M4QX\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("CHAMP_MANQUANT"));
	}

}
