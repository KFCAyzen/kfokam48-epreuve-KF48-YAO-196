package cm.kfokam48.presence.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.entity.Etudiant;
import cm.kfokam48.presence.entity.Promotion;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.PromotionRepository;

/** EF3 : les listes dans lesquelles l'étudiant choisit sa promotion puis son nom (Q1). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PromotionControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private PromotionRepository promotions;

	@Autowired
	private EtudiantRepository etudiants;

	@Test
	void listeLesEtudiantsDUnePromotionTriesParNom() throws Exception {
		Promotion promo = promotions.save(new Promotion("Promo test EF3"));
		etudiants.save(new Etudiant("Zeh Paul", promo));
		etudiants.save(new Etudiant("Abena Marie", promo));

		mvc.perform(get("/api/promotions/{id}/etudiants", promo.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].nom").value("Abena Marie"))
			.andExpect(jsonPath("$[0].promotionId").value(promo.getId()));
	}

	@Test
	void listeLesPromotions() throws Exception {
		promotions.save(new Promotion("Promo test liste"));

		mvc.perform(get("/api/promotions"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.nom == 'Promo test liste')]").exists());
	}

	@Test
	void promotionInconnueRepond404AuFormatImpose() throws Exception {
		mvc.perform(get("/api/promotions/{id}/etudiants", 999_999))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"))
			.andExpect(jsonPath("$.message").isNotEmpty());
	}

}
