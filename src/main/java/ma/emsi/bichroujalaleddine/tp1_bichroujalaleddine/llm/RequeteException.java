package ma.emsi.bichroujalaleddine.tp1_bichroujalaleddine.llm;

/**
 * Exception personnalisée pour les erreurs lors de l'appel à l'API du LLM.
 */
public class RequeteException extends Exception {

    private String detailsJson;

    /**
     * Constructeur avec un message simple.
     */
    public RequeteException(String message) {
        super(message);
    }

    /**
     * Constructeur avec un message et une cause (exception sous-jacente).
     */
    public RequeteException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * ⭐ CE CONSTRUCTEUR EST NÉCESSAIRE ⭐
     * Constructeur avec un message et des détails JSON.
     * Utilisé quand on veut garder le JSON de la requête pour le debug.
     */
    public RequeteException(String message, String detailsJson) {
        super(message);
        this.detailsJson = detailsJson;
    }

    /**
     * Retourne les détails JSON associés à l'exception (si disponibles).
     */
    public String getDetailsJson() {
        return detailsJson;
    }

    @Override
    public String getMessage() {
        if (detailsJson != null && !detailsJson.isEmpty()) {
            return super.getMessage() + "\nDétails JSON : " + detailsJson;
        }
        return super.getMessage();
    }
}
