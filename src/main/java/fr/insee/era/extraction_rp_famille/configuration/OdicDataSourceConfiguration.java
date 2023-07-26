package fr.insee.era.extraction_rp_famille.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class OdicDataSourceConfiguration {

        @Bean
        @ConfigurationProperties("spring.datasource.odic")
        public DataSourceProperties odicDataSourceProperties() {
                DataSourceProperties con =  new DataSourceProperties();
//                con.setType(SingleConnectionDataSource.class);
                return con ;
        }

        @Bean
        public DataSource odicDataSource() {
                return odicDataSourceProperties()
                    .initializeDataSourceBuilder()
                    .build();
        }

        @Bean
        public JdbcTemplate odicJdbcTemplate(@Qualifier("odicDataSource") DataSource dataSource) {
                return new JdbcTemplate(dataSource);
        }



}
