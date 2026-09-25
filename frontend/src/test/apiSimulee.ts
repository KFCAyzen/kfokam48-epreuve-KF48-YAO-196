import { vi } from 'vitest'

export type Reponse = { statut?: number; corps?: unknown }
export type Appel = { methode: string; chemin: string; corps: unknown; entetes: Record<string, string> }

/**
 * Remplace fetch par une API simulée : chaque route « MÉTHODE /chemin » renvoie une réponse fixe.
 * Aucun appel réseau réel. Renvoie la liste des appels reçus pour vérifier ce que le front envoie.
 */
export function simulerApi(routes: Record<string, Reponse | ((appel: Appel) => Reponse)>): Appel[] {
  const appels: Appel[] = []
  vi.stubGlobal(
    'fetch',
    vi.fn(async (entree: string, init: RequestInit = {}) => {
      const appel: Appel = {
        methode: init.method ?? 'GET',
        chemin: entree,
        corps: init.body ? JSON.parse(String(init.body)) : undefined,
        entetes: (init.headers ?? {}) as Record<string, string>,
      }
      appels.push(appel)
      const route = routes[`${appel.methode} ${appel.chemin}`]
      const reponse = typeof route === 'function' ? route(appel) : (route ?? { statut: 404, corps: { code: 'RESSOURCE_INTROUVABLE', message: 'Route non simulée' } })
      const statut = reponse.statut ?? 200
      return new Response(reponse.corps === undefined ? null : JSON.stringify(reponse.corps), {
        status: statut,
        headers: { 'Content-Type': 'application/json' },
      })
    }),
  )
  return appels
}
