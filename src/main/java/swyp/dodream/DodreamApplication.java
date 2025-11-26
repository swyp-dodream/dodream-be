package swyp.dodream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableJpaRepositories(
    basePackages = "swyp.dodream.domain",
    excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
        classes = swyp.dodream.domain.search.repository.PostDocumentRepository.class
    )
)
@EnableElasticsearchRepositories(basePackages = "swyp.dodream.domain.search.repository")
public class DodreamApplication {

    public static void main(String[] args) {
        SpringApplication.run(DodreamApplication.class, args);
    }
}

