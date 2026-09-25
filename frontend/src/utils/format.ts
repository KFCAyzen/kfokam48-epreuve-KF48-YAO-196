// Mise en forme des données de l'API pour l'affichage. Aucune règle métier ici (F3).

const formatHeure = new Intl.DateTimeFormat('fr-FR', { hour: '2-digit', minute: '2-digit' })
const formatDate = new Intl.DateTimeFormat('fr-FR', { day: 'numeric', month: 'short', year: 'numeric' })

/** « 10 h 15 », comme dans la maquette. */
export function heure(iso: string): string {
  return formatHeure.format(new Date(iso)).replace(':', ' h ')
}

/** « 25 sept. 2026 ». */
export function date(iso: string): string {
  return formatDate.format(new Date(iso))
}

/** Durée restante au format « 12:48 » ; « 0:00 » une fois écoulée. */
export function minuteur(millisecondes: number): string {
  const secondes = Math.max(0, Math.floor(millisecondes / 1000))
  const minutes = Math.floor(secondes / 60)
  return `${minutes}:${String(secondes % 60).padStart(2, '0')}`
}

/** Moyenne renvoyée par l'API : « 14,75 », ou « — » quand elle vaut null (RG19). */
export function moyenne(valeur: number | null): string {
  return valeur === null ? '—' : valeur.toLocaleString('fr-FR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
