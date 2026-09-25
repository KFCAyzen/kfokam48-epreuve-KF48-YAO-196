package cm.kfokam48.presence.config;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Hasard injecté : le tirage du relecteur (RG14) se remplace par une graine fixe dans les tests. */
@Configuration
public class HasardConfig {

	@Bean
	public RandomGenerator hasard() {
		return new SecureRandom();
	}

}
