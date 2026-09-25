package cm.kfokam48.presence.service;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.dto.OuvrirSessionRequete;
import cm.kfokam48.presence.dto.SessionOuverteDto;
import cm.kfokam48.presence.dto.SessionResumeDto;
import cm.kfokam48.presence.entity.Promotion;
import cm.kfokam48.presence.entity.SessionCours;
import cm.kfokam48.presence.exception.ErreurMetier;
import cm.kfokam48.presence.repository.PromotionRepository;
import cm.kfokam48.presence.repository.SessionCoursRepository;

@Service
@Transactional
public class SessionService {

	private static final int ESSAIS_MAX = 20;

	private final SessionCoursRepository sessions;

	private final PromotionRepository promotions;

	private final PromotionService promotionService;

	private final GenerateurCode generateur;

	private final Clock horloge;

	public SessionService(SessionCoursRepository sessions, PromotionRepository promotions,
			PromotionService promotionService, GenerateurCode generateur, Clock horloge) {
		this.sessions = sessions;
		this.promotions = promotions;
		this.promotionService = promotionService;
		this.generateur = generateur;
		this.horloge = horloge;
	}

	/** EF2 : ouvre une session et lui attribue un code jamais utilisé (RG1, RG5). */
	public SessionOuverteDto ouvrir(OuvrirSessionRequete requete) {
		// Promotion inexistante dans le corps : 400, on reste dans les codes de l'opération imposée.
		Promotion promotion = promotions.findById(requete.promotionId())
			.orElseThrow(() -> ErreurMetier.promotionInconnue(HttpStatus.BAD_REQUEST));
		SessionCours session = SessionCours.ouvrir(requete.titre().strip(), promotion, codeInedit(),
				horloge.instant());
		return SessionOuverteDto.de(sessions.save(session));
	}

	/** Sessions de la promotion, la plus récente d'abord, avec leurs compteurs : 3 requêtes au total (ENF2). */
	@Transactional(readOnly = true)
	public List<SessionResumeDto> sessionsDeLaPromotion(Long promotionId) {
		promotionService.trouver(promotionId);
		Map<Long, long[]> compteurs = new HashMap<>();
		for (Object[] ligne : sessions.presentsParSession(promotionId)) {
			compteurs.computeIfAbsent(id(ligne), k -> new long[3])[0] = nombre(ligne[1]);
		}
		for (Object[] ligne : sessions.exercicesParSession(promotionId)) {
			long[] c = compteurs.computeIfAbsent(id(ligne), k -> new long[3]);
			c[1] = nombre(ligne[1]);
			c[2] = nombre(ligne[2]);
		}
		return sessions.findByPromotionIdOrderByOuvertureAtDescIdDesc(promotionId).stream().map(s -> {
			long[] c = compteurs.getOrDefault(s.getId(), new long[3]);
			return SessionResumeDto.de(s, c[0], c[1], c[2]);
		}).toList();
	}

	private static Long id(Object[] ligne) {
		return nombre(ligne[0]);
	}

	private static long nombre(Object valeur) {
		return valeur == null ? 0 : ((Number) valeur).longValue();
	}

	/** RG5 : un code n'est jamais réutilisé, même par une session passée. */
	private String codeInedit() {
		for (int essai = 0; essai < ESSAIS_MAX; essai++) {
			String code = generateur.generer();
			if (!sessions.existsByCode(code)) {
				return code;
			}
		}
		throw new IllegalStateException("Impossible de générer un code de présence inédit");
	}

}
