import { describe, expect, it, vi } from 'vitest'
import { simulerApi } from '../test/apiSimulee'
import { ApiError, requete } from './http'

describe('requete', () => {
  it('renvoie le JSON de la réponse', async () => {
    simulerApi({ 'GET /api/promotions': { corps: [{ id: 1, nom: 'Promo A' }] } })
    await expect(requete('/api/promotions')).resolves.toEqual([{ id: 1, nom: 'Promo A' }])
  })

  it('envoie le corps en JSON avec son Content-Type', async () => {
    const appels = simulerApi({ 'POST /api/sessions': { statut: 201, corps: { id: 7 } } })
    await requete('/api/sessions', { methode: 'POST', corps: { titre: 'Streams', promotionId: 1 } })
    expect(appels[0].corps).toEqual({ titre: 'Streams', promotionId: 1 })
    expect(appels[0].entetes['Content-Type']).toBe('application/json')
  })

  it('traduit une erreur { code, message } de l’API en ApiError (ENF8)', async () => {
    simulerApi({
      'POST /api/presences': { statut: 410, corps: { code: 'CODE_EXPIRE', message: 'Le code de présence a expiré.' } },
    })
    const erreur = await requete('/api/presences', { methode: 'POST', corps: {} }).catch((e: unknown) => e)
    expect(erreur).toBeInstanceOf(ApiError)
    expect(erreur).toMatchObject({ statut: 410, code: 'CODE_EXPIRE', message: 'Le code de présence a expiré.' })
  })

  it('signale une panne réseau sans lever d’erreur brute', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')))
    await expect(requete('/api/promotions')).rejects.toMatchObject({ statut: 0, code: 'RESEAU' })
  })
})
