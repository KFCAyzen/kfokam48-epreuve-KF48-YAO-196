package cm.kfokam48.presence.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.entity.Etudiant;
import cm.kfokam48.presence.entity.Promotion;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.PromotionRepository;

/**
 * Données de démonstration chargées au démarrage (ENF5), pour que le correcteur n'ouvre pas une
 * application vide. Profil « demo », actif par défaut ; jamais chargé pendant les tests.
 * Idempotent : rien n'est inséré si une promotion existe déjà.
 */
@Component
@Profile("demo")
public class DonneesDemo implements ApplicationRunner {

	private static final Logger LOG = LoggerFactory.getLogger(DonneesDemo.class);

	private final PromotionRepository promotions;

	private final EtudiantRepository etudiants;

	public DonneesDemo(PromotionRepository promotions, EtudiantRepository etudiants) {
		this.promotions = promotions;
		this.etudiants = etudiants;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (promotions.count() > 0) {
			return;
		}
		Promotion fullstack = promotions.save(new Promotion("KFOKAM48 Fullstack Java 2026"));
		Promotion data = promotions.save(new Promotion("KFOKAM48 Data et IA 2026"));

		List.of("Ateba Yannick", "Essomba Joel", "Fotso Brice", "Kamga Doris", "Mbarga Kevin", "Ngono Aline",
				"Nkoulou Ines", "Tchoupo Laure")
			.forEach(nom -> etudiants.save(new Etudiant(nom, fullstack)));
		List.of("Mballa Chris", "Ndzi Farida", "Owona Serge", "Tagne Ruth")
			.forEach(nom -> etudiants.save(new Etudiant(nom, data)));

		LOG.info("Données de démonstration chargées : {} promotions, {} étudiants", promotions.count(), etudiants.count());
	}

}
