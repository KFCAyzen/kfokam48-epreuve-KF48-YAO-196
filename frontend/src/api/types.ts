// Types des réponses de l'API, recopiés des schémas de api/contrat.yaml.

export type Promotion = {
  id: number
  nom: string
}

export type Etudiant = {
  id: number
  nom: string
  promotionId: number
}
