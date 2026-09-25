package cm.kfokam48.presence;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** #75 : la documentation générée décrit les cinq opérations imposées par le contrat, et Swagger UI est servi. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentationApiTest {

	@Autowired
	private MockMvc mvc;

	@Test
	void laDocumentationGenereeDecritLesCinqOperationsImposees() throws Exception {
		mvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.info.title").value("Présence et relecture entre pairs — API KFOKAM48"))
			.andExpect(jsonPath("$.paths['/api/sessions'].post").exists())
			.andExpect(jsonPath("$.paths['/api/presences'].post").exists())
			.andExpect(jsonPath("$.paths['/api/exercices'].post").exists())
			.andExpect(jsonPath("$.paths['/api/relectures/{id}'].post").exists())
			.andExpect(jsonPath("$.paths['/api/tableau'].get").exists());
	}

	@Test
	void swaggerUiEstServi() throws Exception {
		mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
	}

}
