package fr.insee.era.configuration;

import fr.insee.era.extraction_rp_famille.model.exception.CommuneInconnueException;
import fr.insee.era.extraction_rp_famille.model.exception.ConfigurationException;
import fr.insee.era.extraction_rp_famille.model.exception.PasDeBIDuBonSexeException;
import fr.insee.era.extraction_rp_famille.model.exception.RimInconnueException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionsHandlers extends ResponseEntityExceptionHandler {

    @ExceptionHandler({DataAccessException.class})
    public ResponseEntity<String> exceptionBaseDeDonnee(final HttpServletRequest req, final DataAccessException exception) {
        log.error("exceptionBaseDeDonnee  : {}",exception.getMessage());
        return new ResponseEntity<>("Erreur d'accès aux bases de données du RP ", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<String> accessRefuse(final HttpServletRequest req, final AccessDeniedException exception) {
        log.error("accessRefuse uri={} -  user={}  ", req.getRequestURI(),req.getUserPrincipal());
        return new ResponseEntity<>("Accès refusé", HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler({IOException.class})
    public ResponseEntity<String> exceptionEntreeSortie(final HttpServletRequest req, final IOException exception) {
        log.error("exceptionEntreeSortie  : {}",exception.getMessage());
        return new ResponseEntity<>("Erreur d'entree/sortie ", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler({ CommuneInconnueException.class})
    public ResponseEntity<String> exceptionCommuneInconnue(final HttpServletRequest req, final CommuneInconnueException exception) {
        log.info("exceptionCommuneInconnue : {}",exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ RimInconnueException.class})
    public ResponseEntity<String> rimInconnueException(final HttpServletRequest req, final RimInconnueException exception) {
        log.info("rimInconnueException  : {}",exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ PasDeBIDuBonSexeException.class})
    public ResponseEntity<String> pasDeBIDuBonSexeException(final HttpServletRequest req, final PasDeBIDuBonSexeException exception) {
        log.info("pasDeBIDuBonSexeException  : {}",exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConfigurationException.class)
    public ResponseEntity<String> handleConfigurationException(final HttpServletRequest req, final ConfigurationException exception) {
        log.info("handleConfigurationException  : {}",exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ClientAbortException.class)
    public void handleLockException(ClientAbortException exception, HttpServletRequest request) {
        final String message = "ClientAbortException generated by request {} {} from remote address {} with X-FORWARDED-FOR {}";
        final String headerXFF = request.getHeader("X-FORWARDED-FOR");
        log.warn(message, request.getMethod(), request.getRequestURL(), request.getRemoteAddr(), headerXFF);
    }
}