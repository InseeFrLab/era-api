package fr.insee.era.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.ForwardedHeaderFilter;

public class ForwardHeaderForReverseProxy {

        @Bean ForwardedHeaderFilter forwardedHeaderFilter() {
                return new ForwardedHeaderFilter();
        }
}