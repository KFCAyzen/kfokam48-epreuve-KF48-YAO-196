package cm.kfokam48.presence.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Exercice déposé par un étudiant pour une session ; au plus un par étudiant et par session (RG10). */
@Entity
@Table(name = "exercice")
public class Exercice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	private SessionCours session;

	/** L'auteur de l'exercice. */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "etudiant_id", nullable = false)
	private Etudiant etudiant;

	@Column(nullable = false, length = 500)
	private String lien;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 25)
	private StatutExercice statut;

	@Column(name = "depose_at", nullable = false)
	private Instant deposeAt;

	@Column(name = "modifie_at")
	private Instant modifieAt;

	protected Exercice() {
	}

	public Exercice(SessionCours session, Etudiant etudiant, String lien, Instant deposeAt) {
		this.session = session;
		this.etudiant = etudiant;
		this.lien = lien;
		this.deposeAt = deposeAt;
		this.statut = StatutExercice.DEPOSE;
	}

	public void changerStatut(StatutExercice statut) {
		this.statut = statut;
	}

	public Long getId() {
		return id;
	}

	public SessionCours getSession() {
		return session;
	}

	public Etudiant getEtudiant() {
		return etudiant;
	}

	public String getLien() {
		return lien;
	}

	public StatutExercice getStatut() {
		return statut;
	}

	public Instant getDeposeAt() {
		return deposeAt;
	}

	public Instant getModifieAt() {
		return modifieAt;
	}

}
