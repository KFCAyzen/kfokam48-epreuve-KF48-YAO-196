package cm.kfokam48.presence.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.dto.LigneTableauDto;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.TableauRepository;

/** EF7 : le tableau de la promotion, une ligne par étudiant, en 5 requêtes quel que soit l'effectif (ENF2). */
@Service
@Transactional(readOnly = true)
public class TableauService {

	private final TableauRepository tableau;

	private final EtudiantRepository etudiants;

	private final PromotionService promotions;

	public TableauService(TableauRepository tableau, EtudiantRepository etudiants, PromotionService promotions) {
		this.tableau = tableau;
		this.etudiants = etudiants;
		this.promotions = promotions;
	}

	public List<LigneTableauDto> tableau(Long promotionId) {
		promotions.trouver(promotionId);
		Map<Long, Long> presences = compteParEtudiant(tableau.presences(promotionId));
		Map<Long, Long> exercices = compteParEtudiant(tableau.exercicesDeposes(promotionId));
		Map<Long, Long> enAttente = compteParEtudiant(tableau.relecturesEnAttente(promotionId));
		Map<Long, List<Integer>> notes = new HashMap<>();
		for (Object[] ligne : tableau.notesRecues(promotionId)) {
			notes.computeIfAbsent((Long) ligne[0], id -> new ArrayList<>()).add((Integer) ligne[1]);
		}
		return etudiants.findByPromotionIdOrderByNomAsc(promotionId)
			.stream()
			.map(e -> new LigneTableauDto(e.getId(), e.getNom(), presences.getOrDefault(e.getId(), 0L),
					exercices.getOrDefault(e.getId(), 0L), RegleMoyenne.moyenne(notes.get(e.getId())),
					enAttente.getOrDefault(e.getId(), 0L)))
			.toList();
	}

	private static Map<Long, Long> compteParEtudiant(List<Object[]> lignes) {
		Map<Long, Long> compte = new HashMap<>();
		for (Object[] ligne : lignes) {
			compte.put((Long) ligne[0], ((Number) ligne[1]).longValue());
		}
		return compte;
	}

}
