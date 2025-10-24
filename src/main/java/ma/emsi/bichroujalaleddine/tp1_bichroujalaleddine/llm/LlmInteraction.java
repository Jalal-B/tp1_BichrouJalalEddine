package ma.emsi.bichroujalaleddine.tp1_bichroujalaleddine.llm;

import java.io.Serializable;

/**
 * Record pour encapsuler les informations d'une interaction avec le LLM.
 * Contient le JSON de la requête, le JSON de la réponse et la réponse extraite.
 */
public record LlmInteraction(
        String questionJson,
        String reponseJson,
        String reponseExtraite
) implements Serializable {}
