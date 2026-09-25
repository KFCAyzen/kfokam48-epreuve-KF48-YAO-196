package cm.kfokam48.presence.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import cm.kfokam48.presence.entity.Promotion;
import cm.kfokam48.presence.repository.PromotionRepository;

/** EF2 : POST /api/sessions, opération imposée par le contrat. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SessionControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private PromotionRepository promotions;

	private Promotion promo;

	@BeforeEach
	void promotion() {
		promo = promotions.save(new Promotion("Promo test EF2"));
	}

	private MvcResult ouvrir(String titre) throws Exception {
		return mvc
			.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON)
				.content("{\"titre\": \"%s\", \"promotionId\": %d}".formatted(titre, promo.getId())))
			.andExpect(status().isCreated())
			.andReturn();
	}

	@Test
	void ouvreUneSessionEtRenvoieLeCodeEtSesHeuresRg1() throws Exception {
		MvcResult resultat = ouvrir("Streams Java");
		String json = resultat.getResponse().getContentAsString();

		assertThat((Integer) JsonPath.read(json, "$.id")).isPositive();
		assertThat((String) JsonPath.read(json, "$.code")).matches("[A-HJ-NP-Z2-9]{6}");
		Instant ouverture = Instant.parse(JsonPath.read(json, "$.ouvertureAt"));
		Instant expiration = Instant.parse(JsonPath.read(json, "$.expirationAt"));
		assertThat(Duration.between(ouverture, expiration)).isEqualTo(Duration.ofMinutes(15));
	}

	@Test
	void deuxSessionsOntDesCodesDifferentsRg5() throws Exception {
		String premier = JsonPath.read(ouvrir("Séance 1").getResponse().getContentAsString(), "$.code");
		String second = JsonPath.read(ouvrir("Séance 2").getResponse().getContentAsString(), "$.code");
		assertThat(premier).isNotEqualTo(second);
	}

	@Test
	void titreManquantRepond400ChampManquant() throws Exception {
		mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON)
			.content("{\"promotionId\": %d}".formatted(promo.getId())))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("CHAMP_MANQUANT"));
	}

	@Test
	void promotionManquanteRepond400ChampManquant() throws Exception {
		mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON).content("{\"titre\": \"Streams\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("CHAMP_MANQUANT"));
	}

	@Test
	void promotionInexistanteRepond400PromotionInconnue() throws Exception {
		mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON)
			.content("{\"titre\": \"Streams\", \"promotionId\": 999999}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
	}

	@Test
	void listeLesSessionsDeLaPromotionLaPlusRecenteDAbord() throws Exception {
		ouvrir("Séance 1");
		ouvrir("Séance 2");

		mvc.perform(get("/api/promotions/{id}/sessions", promo.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].titre").value("Séance 2"))
			.andExpect(jsonPath("$[0].presents").value(0))
			.andExpect(jsonPath("$[0].clotureeAt").isEmpty());
	}

}
