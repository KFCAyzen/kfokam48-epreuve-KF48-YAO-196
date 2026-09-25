package cm.kfokam48.presence.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Relecture d'un exercice par un pair. Relation 1-1 avec l'exercice (Q6, RG13) : elle reprend son
 * identifiant, si bien que l'id renvoyé par POST /api/exercices sert aussi pour POST /api/relectures/{id}.
 */
@Entity
@Table(name = "relecture")
public class Relecture {

	@Id
	@Column(name = "exercice_id")
	private Long id;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "exercice_id")
	private Exercice exercice;

	/** Le relecteur est un étudiant présent à la session, jamais l'auteur (RG2, RG14). */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "relecteur_id", nullable = false)
	private Etudiant relecteur;

	private Integer note;

	@Column(length = 2000)
	private String commentaire;

	@Column(name = "assignee_at", nullable = false)
	private Instant assigneeAt;

	@Column(name = "commencee_at")
	private Instant commenceeAt;

	@Column(name = "rendue_at")
	private Instant rendueAt;

	protected Relecture() {
	}

	public Relecture(Exercice exercice, Etudiant relecteur, Instant assigneeAt) {
		this.exercice = exercice;
		this.relecteur = relecteur;
		this.assigneeAt = assigneeAt;
	}

	public boolean estCommencee() {
		return commenceeAt != null;
	}

	/** RG18 : une relecture rendue est définitive. */
	public boolean estRendue() {
		return rendueAt != null;
	}

	/** Le relecteur commence : l'exercice passe EN_COURS_DE_RELECTURE et son lien n'est plus remplaçable (RG23). */
	public void commencer(Instant maintenant) {
		if (commenceeAt == null) {
			commenceeAt = maintenant;
			exercice.changerStatut(StatutExercice.EN_COURS_DE_RELECTURE);
		}
	}

	/** Rend la note et le commentaire, une seule fois (RG18) : l'exercice passe RELU. */
	public void rendre(int note, String commentaire, Instant maintenant) {
		if (estRendue()) {
			throw new IllegalStateException("Relecture déjà rendue");
		}
		this.note = note;
		this.commentaire = commentaire;
		this.rendueAt = maintenant;
		if (commenceeAt == null) {
			commenceeAt = maintenant;
		}
		exercice.changerStatut(StatutExercice.RELU);
	}

	public Long getId() {
		return id;
	}

	public Exercice getExercice() {
		return exercice;
	}

	public Etudiant getRelecteur() {
		return relecteur;
	}

	public Integer getNote() {
		return note;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public Instant getAssigneeAt() {
		return assigneeAt;
	}

	public Instant getCommenceeAt() {
		return commenceeAt;
	}

	public Instant getRendueAt() {
		return rendueAt;
	}

}
