package cm.kfokam48.presence.service;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

import org.springframework.stereotype.Component;

/**
 * RG5 : code de 6 caractères tirés de A-Z et 2-9, sans les caractères ambigus 0, O, 1 et I,
 * pour qu'un code lu au tableau se recopie sans erreur. Tirage cryptographique : un code
 * ne doit pas pouvoir se deviner (Q4).
 */
@Component
public class GenerateurCode {

	static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

	static final int LONGUEUR = 6;

	private final RandomGenerator hasard;

	public GenerateurCode() {
		this(new SecureRandom());
	}

	GenerateurCode(RandomGenerator hasard) {
		this.hasard = hasard;
	}

	public String generer() {
		StringBuilder code = new StringBuilder(LONGUEUR);
		for (int i = 0; i < LONGUEUR; i++) {
			code.append(ALPHABET.charAt(hasard.nextInt(ALPHABET.length())));
		}
		return code.toString();
	}

}
