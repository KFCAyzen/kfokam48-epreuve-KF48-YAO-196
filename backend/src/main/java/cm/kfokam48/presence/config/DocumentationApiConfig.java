package cm.kfokam48.presence.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Documentation Swagger générée depuis le code (#75) : http://localhost:8080/swagger-ui.html.
 * Elle décrit l'API réelle ; le contrat imposé, qui fait foi (B2), reste api/contrat.yaml.
 */
@Configuration
public class DocumentationApiConfig {

	@Bean
	public OpenAPI documentationApi() {
		return new OpenAPI()
			.info(new Info().title("Présence et relecture entre pairs — API KFOKAM48")
				.version("1.0")
				.description("Documentation générée depuis le code Spring Boot. Toute erreur répond au format "
						+ "{ code, message }. L'étudiant se désigne par l'en-tête X-Etudiant-Id (Q1). "
						+ "Le contrat imposé, qui fait foi, est api/contrat.yaml."))
			.externalDocs(new ExternalDocumentation().description("Contrat imposé : api/contrat.yaml")
				.url("https://github.com/KFCAyzen/kfokam48-epreuve-KF48-YAO-196/blob/main/api/contrat.yaml"));
	}

}
