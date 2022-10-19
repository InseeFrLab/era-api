package fr.insee.era.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@OpenAPIDefinition(
    info = @io.swagger.v3.oas.annotations.info.Info(title = "Extraction dans les formats attendus par colleman",
        description = "Extraction des données de contacts et des externals "
    //        ,contact = @Contact(
    //            name = "Equipe informatique",
    //            url = "https://reflectoring.io",
    //            email = "crabe@insee.fr"
    //        )
    // ,
    //        license = @License(
    //            name = "MIT Licence",
    //            url = "https://github.com/thombergs/code-examples/blob/master/LICENSE")
)//,
    //servers = @Server(url = "http://localhost:8080")
) public class SwaggerConfig {

        @Value("${keycloak.realm}") private String keycloakRealm;
        @Value("${keycloak.auth-server-url}") private String keyCloakUrl;
        @Value("${project.name}") private String projectName;
        @Value("${project.version}") private String projectVersion;

        @Bean public OpenAPI customOpenAPI() {
                OAuthFlows flows = new OAuthFlows();
                OAuthFlow flow = new OAuthFlow();

                flow.setAuthorizationUrl(keyCloakUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/auth");
                flow.setTokenUrl(keyCloakUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token");
                Scopes scopes = new Scopes();
                // scopes.addString("global", "accessEverything");
                flow.setScopes(scopes);
                flows = flows.authorizationCode(flow);

                return new OpenAPI().components(
                        new Components().addSecuritySchemes("keycloak", new SecurityScheme().type(SecurityScheme.Type.OAUTH2).flows(flows)))
                    .info(new Info().title(projectName).version(projectVersion))
                    .addSecurityItem(new SecurityRequirement().addList("keycloak", Arrays.asList("read", "write")));
        }
}
