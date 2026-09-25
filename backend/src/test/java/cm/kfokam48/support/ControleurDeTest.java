package cm.kfokam48.support;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur réservé aux tests du gestionnaire d'erreurs (#2). Il est hors du paquet de
 * l'application : seul le test qui l'importe l'enregistre.
 */
@RestController
@RequestMapping("/test-erreurs")
public class ControleurDeTest {

	public record Corps(@NotNull(message = "CHAMP_MANQUANT") Long etudiantId,
			@NotNull(message = "NOTE_INVALIDE") @Min(value = 0, message = "NOTE_INVALIDE")
			@Max(value = 20, message = "NOTE_INVALIDE") Integer note) {
	}

	@PostMapping
	public Corps renvoyer(@Valid @RequestBody Corps corps) {
		return corps;
	}

	@GetMapping("/panne")
	public String panne() {
		throw new IllegalStateException("détail interne à ne jamais montrer au client");
	}

}
