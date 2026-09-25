package cm.kfokam48.presence.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.entity.Etudiant;
import cm.kfokam48.presence.entity.Exercice;
import cm.kfokam48.presence.entity.Presence;
import cm.kfokam48.presence.entity.Promotion;
import cm.kfokam48.presence.entity.Relecture;
import cm.kfokam48.presence.entity.SessionCours;
import cm.kfokam48.presence.entity.SourcePresence;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.ExerciceRepository;
import cm.kfokam48.presence.repository.PresenceRepository;
import cm.kfokam48.presence.repository.PromotionRepository;
import cm.kfokam48.presence.repository.RelectureRepository;
import cm.kfokam48.presence.repository.SessionCoursRepository;

/** EF7 : GET /api/tableau, opération imposée. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TableauControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private PromotionRepository promotions;

	@Autowired
	private EtudiantRepository etudiants;

	@Autowired
	private SessionCoursRepository sessions;

	@Autowired
	private PresenceRepository presences;

	@Autowired
	private ExerciceRepository exercices;

	@Autowired
	private RelectureRepository relectures;

	private Promotion promo;

	@BeforeEach
	void promotionAvecActivite() {
		Instant t = Instant.now();
		promo = promotions.save(new Promotion("Promo test EF7"));
		Etudiant aicha = etudiants.save(new Etudiant("Mbarga, Aïcha", promo));
		Etudiant brice = etudiants.save(new Etudiant("Nkoulou, Brice", promo));
		etudiants.save(new Etudiant("Tagne, Joël", promo));
		SessionCours s1 = sessions.save(SessionCours.ouvrir("S1", promo, "TAB234", t));
		SessionCours s2 = sessions.save(SessionCours.ouvrir("S2", promo, "TAB345", t));

		// Aïcha : présente 2 fois (dont 1 ajoutée par le formateur), 2 exercices notés 12 et 15.
		presences.save(new Presence(s1, aicha, SourcePresence.ETUDIANT, t));
		presences.save(new Presence(s2, aicha, SourcePresence.FORMATEUR, t));
		presences.save(new Presence(s1, brice, SourcePresence.ETUDIANT, t));
		Relecture r1 = relectures.save(new Relecture(exercices.save(new Exercice(s1, aicha, "https://a.cm/1", t)), brice, t));
		Relecture r2 = relectures.save(new Relecture(exercices.save(new Exercice(s2, aicha, "https://a.cm/2", t)), brice, t));
		r1.rendre(12, "Correct", t);
		r2.rendre(15, "Bien", t);
		// Brice : un exercice dont la relecture, assignée à Aïcha, n'est pas rendue.
		relectures.save(new Relecture(exercices.save(new Exercice(s1, brice, "https://b.cm/1", t)), aicha, t));
		relectures.flush();
	}

	@Test
	void uneLigneParEtudiantTrieeParNomAvecSesQuatreIndicateurs() throws Exception {
		mvc.perform(get("/api/tableau").param("promotionId", promo.getId().toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(3))
			.andExpect(jsonPath("$[0].nom").value("Mbarga, Aïcha"))
			.andExpect(jsonPath("$[0].presences").value(2))
			.andExpect(jsonPath("$[0].exercicesDeposes").value(2))
			.andExpect(jsonPath("$[0].moyenne").value(13.5))
			.andExpect(jsonPath("$[0].relecturesEnAttente").value(1))
			.andExpect(jsonPath("$[0].moyenneProvisoire").value(true))
			.andExpect(jsonPath("$[1].nom").value("Nkoulou, Brice"))
			.andExpect(jsonPath("$[1].presences").value(1))
			.andExpect(jsonPath("$[1].moyenne").isEmpty())
			.andExpect(jsonPath("$[1].relecturesEnAttente").value(0))
			.andExpect(jsonPath("$[1].moyenneProvisoire").value(false))
			.andExpect(jsonPath("$[2].nom").value("Tagne, Joël"))
			.andExpect(jsonPath("$[2].presences").value(0))
			.andExpect(jsonPath("$[2].exercicesDeposes").value(0))
			.andExpect(jsonPath("$[2].moyenne").isEmpty());
	}

	@Test
	void promotionInconnue404() throws Exception {
		mvc.perform(get("/api/tableau").param("promotionId", "999999"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
	}

	@Test
	void promotionIdManquant400() throws Exception {
		mvc.perform(get("/api/tableau")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("CHAMP_MANQUANT"));
	}

}
