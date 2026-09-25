package cm.kfokam48.presence.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam48.presence.dto.LigneTableauDto;
import cm.kfokam48.presence.repository.EtudiantRepository;
import cm.kfokam48.presence.repository.TableauRepository;
import cm.kfokam48.presence.service.RegleNoteRetenue.NoteRetenue;

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
		Map<Long, List<NoteRetenue>> notesRetenues = notesRetenuesParAuteur(tableau.notesRecues(promotionId));
		return etudiants.findByPromotionIdOrderByNomAsc(promotionId).stream().map(e -> {
			List<NoteRetenue> notes = notesRetenues.getOrDefault(e.getId(), List.of());
			return new LigneTableauDto(e.getId(), e.getNom(), presences.getOrDefault(e.getId(), 0L),
					exercices.getOrDefault(e.getId(), 0L), moyenne(notes), enAttente.getOrDefault(e.getId(), 0L),
					notes.stream().anyMatch(NoteRetenue::provisoire));
		}).toList();
	}

	/** RG25 : une note retenue par exercice relu, à partir de ses notes rendues. */
	private static Map<Long, List<NoteRetenue>> notesRetenuesParAuteur(List<Object[]> lignes) {
		Map<Long, List<Integer>> notesParExercice = new LinkedHashMap<>();
		Map<Long, Long> auteurParExercice = new HashMap<>();
		Map<Long, Integer> requisParExercice = new HashMap<>();
		for (Object[] ligne : lignes) {
			Long exerciceId = (Long) ligne[0];
			auteurParExercice.put(exerciceId, (Long) ligne[1]);
			requisParExercice.put(exerciceId, ((Number) ligne[2]).intValue());
			notesParExercice.computeIfAbsent(exerciceId, id -> new ArrayList<>()).add((Integer) ligne[3]);
		}
		Map<Long, List<NoteRetenue>> parAuteur = new HashMap<>();
		notesParExercice.forEach((exerciceId, notes) -> parAuteur
			.computeIfAbsent(auteurParExercice.get(exerciceId), id -> new ArrayList<>())
			.add(RegleNoteRetenue.calculer(notes, requisParExercice.get(exerciceId))));
		return parAuteur;
	}

	/** RG19 : moyenne des notes retenues, 2 décimales, null sans note. */
	private static Double moyenne(List<NoteRetenue> notes) {
		if (notes.isEmpty()) {
			return null;
		}
		BigDecimal somme = notes.stream().map(n -> BigDecimal.valueOf(n.note())).reduce(BigDecimal.ZERO, BigDecimal::add);
		return somme.divide(BigDecimal.valueOf(notes.size()), 2, RoundingMode.HALF_UP).doubleValue();
	}

	private static Map<Long, Long> compteParEtudiant(List<Object[]> lignes) {
		Map<Long, Long> compte = new HashMap<>();
		for (Object[] ligne : lignes) {
			compte.put((Long) ligne[0], ((Number) ligne[1]).longValue());
		}
		return compte;
	}

}
