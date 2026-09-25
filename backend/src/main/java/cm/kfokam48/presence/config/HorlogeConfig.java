package cm.kfokam48.presence.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Heure injectée : les règles qui dépendent du temps (RG1, RG7) se testent sans attendre. */
@Configuration
public class HorlogeConfig {

	@Bean
	public Clock horloge() {
		return Clock.systemUTC();
	}

}
