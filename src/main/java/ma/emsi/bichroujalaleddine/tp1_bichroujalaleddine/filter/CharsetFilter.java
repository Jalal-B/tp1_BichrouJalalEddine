package ma.emsi.bichroujalaleddine.tp1_bichroujalaleddine.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;

/**
 * Filtre pour forcer l'encodage UTF-8 sur toutes les requêtes.
 */
@WebFilter("/*")
public class CharsetFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Force l'encodage UTF-8 pour la requête
        request.setCharacterEncoding("UTF-8");

        // Continue la chaîne de filtres
        chain.doFilter(request, response);
    }
}
