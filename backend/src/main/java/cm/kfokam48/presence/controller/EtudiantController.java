package cm.kfokam48.presence.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cm.kfokam48.presence.dto.ExerciceAuteurDto;
import cm.kfokam48.presence.dto.RelectureAssigneeDto;
import cm.kfokam48.presence.service.ExerciceService;
import cm.kfokam48.presence.service.RelectureService;

@RestController
@RequestMapping("/api/etudiants")
public class EtudiantController {

	private final RelectureService relectures;

	private final ExerciceService exercices;

	public EtudiantController(RelectureService relectures, ExerciceService exercices) {
		this.relectures = relectures;
		this.exercices = exercices;
	}

	/** Écran étudiant : ses exercices, la note retenue et les commentaires, sans relecteur (EF12, RG20, RG25). */
	@GetMapping("/{id}/exercices")
	public List<ExerciceAuteurDto> exercices(@PathVariable Long id) {
		return exercices.exercicesDe(id);
	}

	/** Écran relecteur : les relectures assignées à l'étudiant (EF6). */
	@GetMapping("/{id}/relectures")
	public List<RelectureAssigneeDto> relectures(@PathVariable Long id) {
		return relectures.relecturesDe(id);
	}

}
