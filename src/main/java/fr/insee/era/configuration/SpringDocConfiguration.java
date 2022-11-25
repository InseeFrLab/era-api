package fr.insee.era.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SpringDocConfiguration {

  @Value("${fr.insee.era.springdoc.issuer.url.authorization:}")
  public String issuerAuthorizationURL;

  @Value("${fr.insee.era.springdoc.issuer.url.refresh:}")
  public String issuerRefreshURL;

  @Value("${fr.insee.era.springdoc.issuer.url.token:}")
  public String issuerTokenURL;

  @Value("${fr.insee.era.springdoc.issuer.description:}")
  public String issuerDescription;

  @Value("${fr.insee.era.springdoc.description:}")
  public String applicationDescription;

  @Value("${fr.insee.era.springdoc.contact.name:}")
  public String contactName;

  @Value("${fr.insee.era.springdoc.contact.email:}")
  public String contactEmail;

  @Autowired BuildProperties buildProperties;

  public final String OAUTHSCHEME = "oAuth";
  //public final String SCHEMEBASIC = "basic";

  @Bean
  public OpenAPI customOpenAPIBasicAndOIDC() {
    final OpenAPI openapi = createOpenAPI();
    openapi.components(
        new Components()
            /*.addSecuritySchemes(
                SCHEMEBASIC,
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme(SCHEMEBASIC)
                    .in(SecurityScheme.In.HEADER)
                    .description("Authentification Basic"))*/
            .addSecuritySchemes(
                OAUTHSCHEME,
                new SecurityScheme()
                    .type(SecurityScheme.Type.OAUTH2)
                    .in(SecurityScheme.In.HEADER)
                    .description(issuerDescription)
                    .flows(
                        new OAuthFlows()
                            //.implicit(
                            .authorizationCode(
                                new OAuthFlow()
                                    .authorizationUrl(issuerAuthorizationURL)
                                    .tokenUrl(issuerTokenURL)
                                    .refreshUrl(issuerRefreshURL)))));
    return openapi;
  }

  private OpenAPI createOpenAPI() {
    log.info("surcharge de la configuration swagger");
    Contact contact = new Contact()
        //.url("https://github.com/InseeFrLab/sugoi-api")
     ;
    if (contactEmail != null) {
      contact = contact.email(contactEmail).name(contactEmail);
    }
    final OpenAPI openapi =
        new OpenAPI()
            .info(
                new Info()
                    .title(buildProperties.getName())
                    .description(applicationDescription)
                    .version(buildProperties.getVersion())
                    /*.license(
                        new License()
                            .name("Apache 2.0")
                            .url("http://www.apache.org/licenses/LICENSE-2.0.html"))*/
                    .contact(contact));

    return openapi;
  }

  @Bean
  public OperationCustomizer addAuth() {
    return (operation, handlerMethod) -> {
      return operation
         // .addSecurityItem(new SecurityRequirement().addList(SCHEMEBASIC))
          .addSecurityItem(new SecurityRequirement().addList(OAUTHSCHEME));
    };
  }
}