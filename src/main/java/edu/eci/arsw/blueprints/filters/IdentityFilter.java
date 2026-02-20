package edu.eci.arsw.blueprints.filters;

import edu.eci.arsw.blueprints.model.Blueprint;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Filtro por defecto: retorna el blueprint sin modificaciones.
 * Solo está activo cuando NO se usa el perfil "redundancy" ni "undersampling".
 *
 * Para activar un filtro distinto, arranca con:
 *   mvn spring-boot:run -Dspring-boot.run.profiles=redundancy
 *   mvn spring-boot:run -Dspring-boot.run.profiles=undersampling
 */
@Component
@Profile("default")
public class IdentityFilter implements BlueprintsFilter {
    @Override
    public Blueprint apply(Blueprint bp) { return bp; }
}