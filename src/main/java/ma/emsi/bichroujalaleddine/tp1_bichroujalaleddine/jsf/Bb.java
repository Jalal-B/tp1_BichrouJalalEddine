package ma.emsi.bichroujalaleddine.tp1_bichroujalaleddine.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.bichroujalaleddine.tp1_bichroujalaleddine.llm.JsonUtilPourGemini;
import ma.emsi.bichroujalaleddine.tp1_bichroujalaleddine.llm.LlmInteraction;
import ma.emsi.bichroujalaleddine.tp1_bichroujalaleddine.llm.RequeteException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean pour la page JSF index.xhtml.
 * Portée view pour conserver l'état de la conversation qui dure pendant plusieurs requêtes HTTP.
 * La portée view nécessite l'implémentation de Serializable (le backing bean peut être mis en mémoire secondaire).
 */
@Named
@ViewScoped
public class Bb implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Rôle "système" que l'on attribuera plus tard à un LLM.
     * Valeur par défaut que l'utilisateur peut modifier.
     * Possible d'écrire un nouveau rôle dans la liste déroulante.
     */
    private String roleSysteme;

    /**
     * Quand le rôle est choisi par l'utilisateur dans la liste déroulante,
     * il n'est plus possible de le modifier (voir code de la page JSF), sauf si on veut un nouveau chat.
     */
    private boolean roleSystemeChangeable = true;

    /**
     * Liste de tous les rôles de l'API prédéfinis.
     */
    private List<SelectItem> listeRolesSysteme;

    /**
     * Dernière question posée par l'utilisateur.
     */
    private String question;

    /**
     * Dernière réponse de l'API Gemini.
     */
    private String reponse;

    /**
     * La conversation depuis le début.
     */
    private StringBuilder conversation = new StringBuilder();

    /**
     * Mode debug + JSON envoyé/reçu
     */
    private boolean debug;
    private String texteRequeteJson;
    private String texteReponseJson;

    /**
     * Contexte JSF. Utilisé pour qu'un message d'erreur s'affiche dans le formulaire.
     */
    @Inject
    private FacesContext facesContext;

    /**
     * Injection de JsonUtilPourGemini pour gérer les requêtes JSON
     */
    @Inject
    private JsonUtilPourGemini jsonUtil;

    /**
     * Obligatoire pour un bean CDI (classe gérée par CDI), s'il y a un autre constructeur.
     */
    public Bb() {
    }

    // --- GETTERS & SETTERS ---
    public String getRoleSysteme() {
        return roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getTexteRequeteJson() {
        return texteRequeteJson;
    }

    public void setTexteRequeteJson(String texteRequeteJson) {
        this.texteRequeteJson = texteRequeteJson;
    }

    public String getTexteReponseJson() {
        return texteReponseJson;
    }

    public void setTexteReponseJson(String texteReponseJson) {
        this.texteReponseJson = texteReponseJson;
    }

    public void toggleDebug() {
        this.setDebug(!isDebug());
    }

    /**
     * Envoie la question au serveur.
     * Utilise JsonUtilPourGemini pour communiquer avec l'API Gemini.
     *
     * @return null pour rester sur la même page.
     */
    public String envoyer() {
        if (question == null || question.isBlank()) {
            // Erreur ! Le formulaire va être réaffiché en réponse à la requête POST, avec un message d'erreur.
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        try {
            // Si la conversation n'a pas encore commencé, envoyer le rôle système
            if (this.conversation.isEmpty()) {
                jsonUtil.setSystemRole(this.roleSysteme);
                // Invalide le bouton pour changer le rôle système
                this.roleSystemeChangeable = false;
            }

            // Envoi de la requête à l'API via JsonUtilPourGemini
            LlmInteraction interaction = jsonUtil.envoyerRequete(question);
            this.reponse = interaction.reponseExtraite();
            this.texteRequeteJson = interaction.questionJson();
            this.texteReponseJson = interaction.reponseJson();

            // La conversation contient l'historique des questions-réponses depuis le début.
            afficherConversation();

        } catch (RequeteException e) {
            FacesMessage message =
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Problème de connexion avec l'API du LLM",
                            "Problème de connexion avec l'API du LLM: " + e.getMessage());
            facesContext.addMessage(null, message);
        }
        return null;
    }

    /**
     * Pour un nouveau chat.
     * Termine la portée view en retournant "index" (la page index.xhtml sera affichée après le traitement
     * effectué pour construire la réponse) et pas null.
     * Le fait de changer de vue va faire supprimer l'instance en cours du backing bean par CDI.
     * @return "index"
     */
    public String nouveauChat() {
        return "index";
    }

    /**
     * Pour afficher la conversation dans le textArea de la page JSF.
     */
    private void afficherConversation() {
        this.conversation.append("== User:\n").append(question).append("\n== Serveur:\n").append(reponse).append("\n");
    }

    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            // Génère les rôles de l'API prédéfinis
            this.listeRolesSysteme = new ArrayList<>();

            String role = "You are a helpful assistant. You help the user to find the information they need.\nIf the user type a question, you answer it.";
            this.listeRolesSysteme.add(new SelectItem(role, "Assistant"));

            role = "You are an interpreter. You translate from English to French and from French to English.\nIf the user type a French text, you translate it into English.\nIf the user type an English text, you translate it into French.\nIf the text contains only one to three words, give some examples of usage of these words in English.";
            this.listeRolesSysteme.add(new SelectItem(role, "Traducteur Anglais-Français"));

            role = "Your are a travel guide. If the user type the name of a country or of a town,\nyou tell them what are the main places to visit in the country or the town\nand you tell them the average price of a meal.";
            this.listeRolesSysteme.add(new SelectItem(role, "Guide touristique"));

            // RÔLE SYSTÈME BONUS
            role = "You are a stand-up comedian. Reply to any question by making a joke or giving an answer in a humorous way. Always keep the tone funny.";
            this.listeRolesSysteme.add(new SelectItem(role, "Humoriste"));
        }

        return this.listeRolesSysteme;
    }
}
