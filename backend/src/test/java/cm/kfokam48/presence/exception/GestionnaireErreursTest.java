package cm.kfokam48.presence.exception;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import cm.kfokam48.support.ControleurDeTest;

/** ENF4 / B4 : toute erreur répond { code, message }, jamais de stack trace ni de page Spring. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ControleurDeTest.class)
class GestionnaireErreursTest {

	@Autowired
	private MockMvc mvc;

	private static ResultActions formatImpose(ResultActions resultat, String code) throws Exception {
		return resultat.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.code").value(code))
			.andExpect(jsonPath("$.message").isNotEmpty())
			.andExpect(jsonPath("$.trace").doesNotExist())
			.andExpect(jsonPath("$.exception").doesNotExist());
	}

	private ResultActions envoyer(String json) throws Exception {
		return mvc.perform(post("/test-erreurs").contentType(MediaType.APPLICATION_JSON).content(json));
	}

	@Test
	void champManquant() throws Exception {
		formatImpose(envoyer("{\"note\": 12}").andExpect(status().isBadRequest()), "CHAMP_MANQUANT")
			.andExpect(jsonPath("$.message").value(containsString("etudiantId")));
	}

	@Test
	void jsonMalForme() throws Exception {
		formatImpose(envoyer("{\"etudiantId\": ").andExpect(status().isBadRequest()), "REQUETE_INVALIDE");
	}

	@Test
	void corpsAbsent() throws Exception {
		formatImpose(mvc.perform(post("/test-erreurs").contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest()), "REQUETE_INVALIDE");
	}

	@Test
	void noteNonEntiereRefuseeSansArrondi() throws Exception {
		formatImpose(envoyer("{\"etudiantId\": 1, \"note\": 12.5}").andExpect(status().isBadRequest()), "NOTE_INVALIDE");
	}

	@Test
	void noteHorsBornes() throws Exception {
		formatImpose(envoyer("{\"etudiantId\": 1, \"note\": 21}").andExpect(status().isBadRequest()), "NOTE_INVALIDE");
	}

	@Test
	void routeInconnue() throws Exception {
		formatImpose(mvc.perform(get("/api/route-qui-n-existe-pas")).andExpect(status().isNotFound()),
				"RESSOURCE_INTROUVABLE");
	}

	@Test
	void methodeNonAutorisee() throws Exception {
		formatImpose(mvc.perform(delete("/api/promotions")).andExpect(status().isMethodNotAllowed()),
				"METHODE_NON_AUTORISEE");
	}

	@Test
	void typeDeContenuNonSupporte() throws Exception {
		formatImpose(mvc.perform(post("/test-erreurs").contentType(MediaType.TEXT_PLAIN).content("bonjour"))
			.andExpect(status().isUnsupportedMediaType()), "TYPE_NON_SUPPORTE");
	}

	@Test
	void parametreDeCheminMalForme() throws Exception {
		formatImpose(mvc.perform(get("/api/promotions/abc/etudiants")).andExpect(status().isBadRequest()),
				"PARAMETRE_INVALIDE");
	}

	@Test
	void erreurInattendueSansDetailTechnique() throws Exception {
		formatImpose(mvc.perform(get("/test-erreurs/panne")).andExpect(status().isInternalServerError()), "ERREUR_INTERNE")
			.andExpect(content().string(not(containsString("détail interne"))))
			.andExpect(content().string(not(containsString("IllegalStateException"))));
	}

	@Test
	void pageErreurDeSpringRemplacee() throws Exception {
		formatImpose(mvc.perform(get("/error").requestAttr("jakarta.servlet.error.status_code", 404))
			.andExpect(status().isNotFound()), "RESSOURCE_INTROUVABLE");
	}

}
