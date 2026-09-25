import { useState, type FormEvent } from 'react'
import { listerEtudiants } from '../api/promotions'
import type { Promotion } from '../api/types'
import ChoixPromotion from '../components/ChoixPromotion'
import { Chargement, MessageErreur } from '../components/Etat'
import { useApi } from '../hooks/useApi'
import { useIdentite } from './contexte'

/** Spécification 05 : « Qui êtes-vous ? », sans mot de passe (EF3, Q1). */
export default function Identification() {
  const { choisir } = useIdentite()
  const [promotion, setPromotion] = useState<Promotion | null>(null)
  const [etudiantId, setEtudiantId] = useState<number | null>(null)

  const etudiants = useApi(promotion === null ? null : `etudiants-${promotion.id}`, () =>
    listerEtudiants(promotion!.id),
  )
  const choisi = etudiants.donnees?.find((e) => e.id === etudiantId)

  function soumettre(evenement: FormEvent<HTMLFormElement>) {
    evenement.preventDefault()
    if (!choisi || !promotion) return
    choisir({ etudiantId: choisi.id, nom: choisi.nom, promotionId: promotion.id, promotionNom: promotion.nom })
  }

  return (
    <form className="identification" onSubmit={soumettre}>
      <h1>Qui êtes-vous ?</h1>

      <ChoixPromotion
        valeur={promotion?.id ?? null}
        onChange={(_, choix) => {
          setPromotion(choix)
          setEtudiantId(null)
        }}
      />

      {etudiants.chargement ? <Chargement /> : null}
      <MessageErreur erreur={etudiants.erreur} />
      {etudiants.donnees ? (
        <fieldset className="liste-noms">
          <legend>Votre nom</legend>
          {etudiants.donnees.map((e) => (
            <label key={e.id} className={e.id === etudiantId ? 'nom choisi' : 'nom'}>
              <input
                type="radio"
                name="etudiant"
                value={e.id}
                checked={e.id === etudiantId}
                onChange={() => setEtudiantId(e.id)}
              />
              {e.nom}
            </label>
          ))}
        </fieldset>
      ) : null}

      <button type="submit" disabled={!choisi}>
        Continuer
      </button>
      <p className="notes">
        Aucun mot de passe n'est demandé. Votre choix est conservé sur cet appareil jusqu'à ce que vous choisissiez
        « Changer d'identité ».
      </p>
    </form>
  )
}
