package fr.insee.era.extraction_rp_famille.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class OmerDataSourceConfiguration {

        @Bean
        @ConfigurationProperties("spring.datasource.omer")
        public DataSourceProperties omerDataSourceProperties() {
                return new DataSourceProperties();
        }

        @Bean
        public DataSource omerDataSource() {
                return omerDataSourceProperties()
                    .initializeDataSourceBuilder()
                    .build();
        }

        @Bean
        public JdbcTemplate omerJdbcTemplate(@Qualifier("omerDataSource") DataSource dataSource) {
                return new JdbcTemplate(dataSource);
        }
}
