package cm.kfokam48.presence.config;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.entity.Etudiant;
import cm.kfokam48.presence.entity.Exercice;
import cm.kfokam48.presence.entity.Presence;
import cm.kfokam48.presence.entity.Promotion;
import cm.kfokam48.presence.entity.Relecture;
import cm.kfokam48.presence.entity.SessionCours;
import cm.kfokam48.presence.entity.SourcePresence;
import cm.kfokam48.presence.entity.StatutExercice;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.ExerciceRepository;
import cm.kfokam48.presence.repository.PresenceRepository;
import cm.kfokam48.presence.repository.PromotionRepository;
import cm.kfokam48.presence.repository.RelectureRepository;
import cm.kfokam48.presence.repository.SessionCoursRepository;
import cm.kfokam48.presence.service.GenerateurCode;

/**
 * Données de démonstration chargées au démarrage (ENF5), reprises des maquettes (docs/maquettes/README.md,
 * « Données de référence ») : promotion 2026-A, 12 étudiants, sessions S1 à S5 clôturées et S6 ouverte
 * au démarrage, pour que son code soit utilisable pendant 15 minutes. Deux relecteurs par exercice (étape 3). Profil « demo », actif par défaut,
 * jamais pendant les tests. Idempotent : rien n'est inséré si une promotion existe déjà.
 */
@Component
@Profile("demo")
public class DonneesDemo implements ApplicationRunner {

	private static final Logger LOG = LoggerFactory.getLogger(DonneesDemo.class);

	private static final List<String> ETUDIANTS = List.of("Ateba, Grâce", "Djomo, Hervé", "Fotso, Daniel",
			"Kamga, Fabrice", "Mbarga, Aïcha", "Mvondo, Inès", "Ngo Biyong, Estelle", "Nkoulou, Brice", "Tagne, Joël",
			"Tchoumi, Carine", "Wamba, Loïc", "Yomba, Sandrine");

	/** Registre de présence S1 à S6 par étudiant : E par le code, F ajouté par le formateur, A absent. */
	private static final List<String> REGISTRE = List.of("EEEEEE", "EEAEFE", "EEEEEE", "EAEFAF", "EEEEEE", "AEAAEA",
			"EEEEEE", "EEFEEE", "AAEAAA", "EEEEEE", "EEEAEE", "EFEEEA");

	private static final List<String> SEANCES = List.of("Git : branches et pull requests",
			"Architecture en couches Spring Boot", "Tests d'intégration avec MockMvc", "Persistance avec JPA et Flyway",
			"Validation et gestion des erreurs", "Conception d'API REST — atelier contrat OpenAPI");

	private static final List<String> COMMENTAIRES = List.of("Travail propre et bien découpé.",
			"Bonne structure, quelques tests manquent.", "Le contrat est respecté, les erreurs sont claires.",
			"Correct, mais les noms de variables gagneraient à être plus explicites.",
			"Très bon travail, rien à redire.");

	private final PromotionRepository promotions;

	private final EtudiantRepository etudiants;

	private final SessionCoursRepository sessions;

	private final PresenceRepository presences;

	private final ExerciceRepository exercices;

	private final RelectureRepository relectures;

	private final GenerateurCode generateur;

	private final Clock horloge;

	public DonneesDemo(PromotionRepository promotions, EtudiantRepository etudiants, SessionCoursRepository sessions,
			PresenceRepository presences, ExerciceRepository exercices, RelectureRepository relectures,
			GenerateurCode generateur, Clock horloge) {
		this.promotions = promotions;
		this.etudiants = etudiants;
		this.sessions = sessions;
		this.presences = presences;
		this.exercices = exercices;
		this.relectures = relectures;
		this.generateur = generateur;
		this.horloge = horloge;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (promotions.count() > 0) {
			return;
		}
		Instant maintenant = horloge.instant();
		Promotion promo = promotions.save(new Promotion("2026-A · Développement logiciel"));
		List<Etudiant> classe = ETUDIANTS.stream().map(nom -> etudiants.save(new Etudiant(nom, promo))).toList();
		Promotion data = promotions.save(new Promotion("2026-B · Data et IA"));
		List.of("Mballa, Chris", "Ndzi, Farida", "Owona, Serge", "Tagne, Ruth")
			.forEach(nom -> etudiants.save(new Etudiant(nom, data)));

		int noteSuivante = 0;
		for (int s = 0; s < SEANCES.size(); s++) {
			boolean derniere = s == SEANCES.size() - 1;
			// S1 à S5 ont eu lieu les jours précédents et sont clôturées ; S6 s'ouvre au démarrage.
			Instant ouverture = derniere ? maintenant : maintenant.minus(Duration.ofDays(2L * (SEANCES.size() - 1 - s) + 2));
			SessionCours session = sessions.save(SessionCours.ouvrir(SEANCES.get(s), promo, codeInedit(), ouverture));

			List<Etudiant> presents = new ArrayList<>();
			for (int e = 0; e < classe.size(); e++) {
				char marque = REGISTRE.get(e).charAt(s);
				if (marque != 'A') {
					SourcePresence source = marque == 'F' ? SourcePresence.FORMATEUR : SourcePresence.ETUDIANT;
					presences.save(new Presence(session, classe.get(e), source, ouverture.plus(Duration.ofMinutes(3 + e))));
					presents.add(classe.get(e));
				}
			}

			// Chaque présent dépose ; ses deux relecteurs sont les deux présents suivants, jamais lui-même (RG2, RG13, RG14).
			int deposes = derniere ? 3 : presents.size();
			for (int i = 0; i < deposes; i++) {
				Etudiant auteur = presents.get(i);
				Exercice exercice = exercices.save(new Exercice(session, auteur,
						"https://github.com/kf48-demo/" + slug(auteur.getNom()) + "/s" + (s + 1),
						ouverture.plus(Duration.ofMinutes(30 + i))));
				// S5 : deux exercices n'ont qu'une relecture rendue, note provisoire (RG25) ; S6 : aucune rendue (Q11).
				boolean provisoire = s == SEANCES.size() - 2 && i >= deposes - 2;
				int rendues = derniere ? 0 : provisoire ? 1 : 2;
				for (int k = 1; k <= 2; k++) {
					Etudiant relecteur = presents.get((i + k) % presents.size());
					Relecture relecture = relectures.save(new Relecture(exercice, relecteur, ouverture.plus(Duration.ofMinutes(31 + i))));
					if (k <= rendues) {
						int note = 10 + (noteSuivante * 7) % 10;
						relecture.rendre(note, COMMENTAIRES.get(noteSuivante % COMMENTAIRES.size()),
								ouverture.plus(Duration.ofHours(2 + k)));
						noteSuivante++;
					}
				}
				exercice.changerStatut(rendues == 2 ? StatutExercice.RELU
						: rendues == 1 ? StatutExercice.EN_COURS_DE_RELECTURE : StatutExercice.EN_ATTENTE_RELECTURE);
			}
			if (!derniere) {
				session.cloturer(ouverture.plus(Duration.ofHours(8)));
			}
		}
		LOG.info("Données de démonstration chargées : 2 promotions, {} étudiants, {} sessions, {} relectures",
				etudiants.count(), sessions.count(), relectures.count());
	}

	private String codeInedit() {
		String code;
		do {
			code = generateur.generer();
		}
		while (sessions.existsByCode(code));
		return code;
	}

	private static String slug(String nom) {
		return java.text.Normalizer.normalize(nom, java.text.Normalizer.Form.NFD)
			.replaceAll("\\p{M}", "")
			.toLowerCase()
			.replaceAll("[^a-z]+", "-")
			.replaceAll("(^-|-$)", "");
	}

}
