package cm.kfokam48.presence.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import cm.kfokam48.presence.entity.Promotion;
import cm.kfokam48.presence.entity.Relecture;
import cm.kfokam48.presence.entity.SessionCours;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.ExerciceRepository;
import cm.kfokam48.presence.repository.PromotionRepository;
import cm.kfokam48.presence.repository.RelectureRepository;
import cm.kfokam48.presence.repository.SessionCoursRepository;

/** EF12 : l'auteur voit sa note retenue, provisoire tant qu'une seule relecture est rendue (RG25), sans relecteur (RG20). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExercicesAuteurTest {

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

	private Relecture premiere;

	private Relecture seconde;

	@BeforeEach
	void exerciceAvecDeuxRelecteurs() {
		Promotion promo = promotions.save(new Promotion("Promo test EF12"));
		auteur = etudiants.save(new Etudiant("Mbarga, Aïcha", promo));
		Etudiant brice = etudiants.save(new Etudiant("Nkoulou, Brice", promo));
		Etudiant joel = etudiants.save(new Etudiant("Tagne, Joël", promo));
		SessionCours session = sessions.save(SessionCours.ouvrir("Conception d'API REST", promo, "AUT234", Instant.now()));
		Exercice exercice = exercices.save(new Exercice(session, auteur, "https://github.com/ambarga/kf48", Instant.now()));
		premiere = relectures.save(new Relecture(exercice, brice, Instant.now()));
		seconde = relectures.save(new Relecture(exercice, joel, Instant.now()));
	}

	@Test
	void uneSeuleRelectureRendueNoteProvisoireRg25() throws Exception {
		premiere.rendre(14, "Contrat complet.", Instant.now());
		relectures.flush();

		mvc.perform(get("/api/etudiants/{id}/exercices", auteur.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].note").value(14.0))
			.andExpect(jsonPath("$[0].noteProvisoire").value(true))
			.andExpect(jsonPath("$[0].commentaires[0]").value("Contrat complet."))
			.andExpect(jsonPath("$[0].sessionTitre").value("Conception d'API REST"));
	}

	@Test
	void lesDeuxRenduesLaNoteRetenueEstLaMoyenneRg25() throws Exception {
		premiere.rendre(14, "Contrat complet.", Instant.now());
		seconde.rendre(11, "Exemples 409 manquants.", Instant.now());
		relectures.flush();

		mvc.perform(get("/api/etudiants/{id}/exercices", auteur.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].note").value(12.5))
			.andExpect(jsonPath("$[0].noteProvisoire").value(false))
			.andExpect(jsonPath("$[0].commentaires.length()").value(2));
	}

	@Test
	void aucuneDonneeSurLesRelecteursRg20() throws Exception {
		premiere.rendre(14, "Contrat complet.", Instant.now());
		relectures.flush();

		mvc.perform(get("/api/etudiants/{id}/exercices", auteur.getId()))
			.andExpect(status().isOk())
			.andExpect(content().string(not(containsString("Nkoulou"))))
			.andExpect(content().string(not(containsString("relecteur"))));
	}

	@Test
	void etudiantInconnu404() throws Exception {
		mvc.perform(get("/api/etudiants/{id}/exercices", 999_999))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("ETUDIANT_INCONNU"));
	}

}
