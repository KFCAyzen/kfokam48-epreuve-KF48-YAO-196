package cm.kfokam48.presence.entity;

/** Cycle de vie d'un exercice, diagramme D4. */
public enum StatutExercice {

	/** Déposé, sans relecteur : aucun étudiant éligible n'était présent (RG15). */
	DEPOSE,

	/** Un relecteur est assigné, il n'a pas encore commencé (RG14). */
	EN_ATTENTE_RELECTURE,

	/** Le relecteur a commencé : le lien n'est plus remplaçable (RG23). */
	EN_COURS_DE_RELECTURE,

	/** La relecture est rendue, définitivement (RG18). */
	RELU

}
