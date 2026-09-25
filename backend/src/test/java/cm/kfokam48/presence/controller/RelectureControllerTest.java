package cm.kfokam48.presence.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.entity.Etudiant;
import cm.kfokam48.presence.entity.Exercice;
import cm.kfokam48.presence.entity.Promotion;
import cm.kfokam48.presence.entity.Relecture;
import cm.kfokam48.presence.entity.SessionCours;
import cm.kfokam48.presence.entity.StatutExercice;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.ExerciceRepository;
import cm.kfokam48.presence.repository.PromotionRepository;
import cm.kfokam48.presence.repository.RelectureRepository;
import cm.kfokam48.presence.repository.SessionCoursRepository;

/** EF6 : POST /api/relectures/{id}, opération imposée, et le début de relecture. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RelectureControllerTest {

	private static final String LIEN = "https://github.com/ambarga/kf48-api-contrat";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private PromotionRepository promotions;

	@Autowired
	private EtudiantRepository etudiants;

	@Autowired
	private SessionCoursRepository sessions;

	@Autowired
	private ExerciceRepository exercices;

	@Autowired
	private RelectureRepository relectures;

	private Etudiant auteur;

	private Etudiant relecteur;

	private Etudiant autre;

	private Long id;

	@BeforeEach
	void exerciceAssigne() {
		Promotion promo = promotions.save(new Promotion("Promo test EF6"));
		auteur = etudiants.save(new Etudiant("Mbarga, Aïcha", promo));
		relecteur = etudiants.save(new Etudiant("Nkoulou, Brice", promo));
		autre = etudiants.save(new Etudiant("Tagne, Joël", promo));
		SessionCours session = sessions.save(SessionCours.ouvrir("Conception d'API REST", promo, "REL234", Instant.now()));
		Exercice exercice = exercices.save(new Exercice(session, auteur, LIEN, Instant.now()));
		exercice.changerStatut(StatutExercice.EN_ATTENTE_RELECTURE);
		id = relectures.saveAndFlush(new Relecture(exercice, relecteur, Instant.now())).getId();
	}

	private ResultActions rendre(String corps, Long appelant) throws Exception {
		MockHttpServletRequestBuilder requete = post("/api/relectures/{id}", id).contentType(MediaType.APPLICATION_JSON)
			.content(corps);
		if (appelant != null) {
			requete.header("X-Etudiant-Id", appelant);
		}
		return mvc.perform(requete);
	}

	private static String corps(Object note) {
		return "{\"note\": %s, \"commentaire\": \"Contrat complet, exemples 409 manquants.\"}".formatted(note);
	}

	@Test
	void relectureRendue200EtExerciceRelu() throws Exception {
		rendre(corps(14), relecteur.getId()).andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id))
			.andExpect(jsonPath("$.statut").value("RELU"))
			.andExpect(jsonPath("$.note").value(14))
			.andExpect(jsonPath("$.rendueAt").isNotEmpty());
	}

	@Test
	void sansEnTeteLaRelectureEstAttribueeAuRelecteurAssigne() throws Exception {
		rendre(corps(20), null).andExpect(status().isOk()).andExpect(jsonPath("$.statut").value("RELU"));
	}

	@Test
	void noteHorsBornes400Rg3() throws Exception {
		rendre(corps(21), relecteur.getId()).andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));
		rendre(corps(-1), relecteur.getId()).andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));
	}

	@Test
	void noteNonEntiere400Rg3() throws Exception {
		rendre(corps(12.5), relecteur.getId()).andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));
	}

	@Test
	void commentaireVide400Rg17() throws Exception {
		rendre("{\"note\": 12, \"commentaire\": \"  \"}", relecteur.getId()).andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMENTAIRE_INVALIDE"));
	}

	@Test
	void lAuteurNePeutPasRelireSonExercice403Rg2() throws Exception {
		rendre(corps(20), auteur.getId()).andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("AUTO_RELECTURE"));
	}

	@Test
	void unAutreEtudiantQueLeRelecteurAssigne403Rg16() throws Exception {
		rendre(corps(10), autre.getId()).andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("RELECTEUR_NON_ASSIGNE"));
	}

	@Test
	void secondEnvoi409RelectureDejaRendueRg18() throws Exception {
		rendre(corps(14), relecteur.getId()).andExpect(status().isOk());
		rendre(corps(18), relecteur.getId()).andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("RELECTURE_DEJA_RENDUE"));
	}

	@Test
	void relectureInconnue404() throws Exception {
		mvc.perform(post("/api/relectures/{id}", 999_999).contentType(MediaType.APPLICATION_JSON).content(corps(12)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("RELECTURE_INCONNUE"));
	}

	@Test
	void commencerReveleLeLienEtPasseEnCoursRg23() throws Exception {
		mvc.perform(get("/api/etudiants/{id}/relectures", relecteur.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(id))
			.andExpect(jsonPath("$[0].statut").value("EN_ATTENTE_RELECTURE"))
			.andExpect(jsonPath("$[0].lien").isEmpty());

		mvc.perform(post("/api/relectures/{id}/debut", id).header("X-Etudiant-Id", relecteur.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.statut").value("EN_COURS_DE_RELECTURE"))
			.andExpect(jsonPath("$.lien").value(LIEN));
	}

	@Test
	void commencerSansEnTete400() throws Exception {
		mvc.perform(post("/api/relectures/{id}/debut", id))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("CHAMP_MANQUANT"));
	}

}
