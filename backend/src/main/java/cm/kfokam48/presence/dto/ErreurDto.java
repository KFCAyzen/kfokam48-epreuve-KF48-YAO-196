package cm.kfokam48.presence.dto;

/** Format d'erreur imposé par api/contrat.yaml pour toutes les erreurs, sans exception. */
public record ErreurDto(String code, String message) {
}
