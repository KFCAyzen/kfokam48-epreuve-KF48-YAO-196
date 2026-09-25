package cm.kfokam48.presence.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import cm.kfokam48.presence.dto.MarquerPresenceRequete;
import cm.kfokam48.presence.entity.Etudiant;
import cm.kfokam48.presence.entity.Exercice;
import cm.kfokam48.presence.entity.Promotion;
import cm.kfokam48.presence.entity.SessionCours;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.ExerciceRepository;
import cm.kfokam48.presence.repository.PresenceRepository;
import cm.kfokam48.presence.repository.PromotionRepository;
import cm.kfokam48.presence.repository.RelectureRepository;
import cm.kfokam48.presence.repository.SessionCoursRepository;

/**
 * Issue #56 : deux étudiants valident le code au même instant alors qu'un exercice attend un relecteur (RG15).
 * Le test n'est pas transactionnel : chaque présence s'enregistre dans sa propre transaction, comme en production.
 * Une barrière placée au moment du tirage du relecteur fait se croiser les deux transactions à coup sûr.
 */
@SpringBootTest
@ActiveProfiles("test")
class PresencesSimulteesTest {

	/** Le tirage du relecteur attend que l'autre transaction l'ait rejoint, 1,5 s au plus. */
	@TestConfiguration
	static class HasardAvecBarriere {

		static final CyclicBarrier BARRIERE = new CyclicBarrier(2);

		@Bean
		@Primary
		RandomGenerator hasardAvecBarriere() {
			return new RandomGenerator() {
				@Override
				public long nextLong() {
					return 0;
				}

				@Override
				public int nextInt(int borne) {
					try {
						BARRIERE.await(1500, TimeUnit.MILLISECONDS);
					}
					catch (Exception e) {
						// L'autre transaction ne viendra pas (elle attend la fin de celle-ci) : on continue.
					}
					return 0;
				}
			};
		}

	}

	@Autowired
	private PresenceService presenceService;

	@Autowired
	private PromotionRepository promotions;

	@Autowired
	private EtudiantRepository etudiants;

	@Autowired
	private SessionCoursRepository sessions;

	@Autowired
	private ExerciceRepository exercices;

	@Autowired
	private PresenceRepository presences;

	@Autowired
	private RelectureRepository relectures;

	@Autowired
	private GenerateurCode generateur;

	private final ExecutorService fils = Executors.newFixedThreadPool(2);

	private Etudiant etudiantA;

	private Etudiant etudiantB;

	private SessionCours session;

	private Exercice exerciceEnAttente;

	@BeforeEach
	void sessionAvecUnExerciceSansRelecteur() {
		HasardAvecBarriere.BARRIERE.reset();
		Promotion promo = promotions.save(new Promotion("Promo bug 56 " + System.nanoTime()));
		etudiantA = etudiants.save(new Etudiant("A", promo));
		etudiantB = etudiants.save(new Etudiant("B", promo));
		Etudiant auteur = etudiants.save(new Etudiant("C", promo));
		session = sessions.save(SessionCours.ouvrir("Séance du matin", promo, generateur.generer(), Instant.now()));
		// C a déposé seul : son exercice attend un relecteur (RG15).
		exerciceEnAttente = exercices.save(new Exercice(session, auteur, "https://github.com/c/exo", Instant.now()));
	}

	@AfterEach
	void arreter() {
		fils.shutdownNow();
	}

	@Test
	void deuxPresencesSimulteesSontToutesDeuxEnregistrees() throws Exception {
		CountDownLatch depart = new CountDownLatch(1);
		List<Throwable> erreurs = new CopyOnWriteArrayList<>();
		Callable<Void> marquerA = () -> marquer(etudiantA, depart, erreurs);
		Callable<Void> marquerB = () -> marquer(etudiantB, depart, erreurs);

		Future<Void> a = fils.submit(marquerA);
		Future<Void> b = fils.submit(marquerB);
		depart.countDown();
		a.get(20, TimeUnit.SECONDS);
		b.get(20, TimeUnit.SECONDS);

		assertThat(erreurs).as("aucun des deux étudiants ne reçoit d'erreur").isEmpty();
		assertThat(presences.etudiantsPresents(session.getId())).as("les deux présences sont enregistrées (RG4, EF1)")
			.containsExactlyInAnyOrder(etudiantA.getId(), etudiantB.getId());
		assertThat(relectures.findByExerciceId(exerciceEnAttente.getId())).as("l'exercice en attente a reçu ses deux relecteurs (RG13, RG15)")
			.extracting(r -> r.getRelecteur().getId())
			.containsExactlyInAnyOrder(etudiantA.getId(), etudiantB.getId());
	}

	private Void marquer(Etudiant etudiant, CountDownLatch depart, List<Throwable> erreurs) throws InterruptedException {
		depart.await();
		try {
			presenceService.marquer(new MarquerPresenceRequete(session.getCode(), etudiant.getId()));
		}
		catch (RuntimeException e) {
			erreurs.add(e);
		}
		return null;
	}

}
