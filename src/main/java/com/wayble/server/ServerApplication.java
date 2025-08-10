package com.wayble.server;

import com.wayble.server.common.client.tmap.TMapProperties;
import com.wayble.server.direction.external.kric.KricProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
		exclude = ReactiveElasticsearchRepositoriesAutoConfiguration.class
)
@EnableJpaAuditing
@EnableScheduling
@EnableConfigurationProperties({TMapProperties.class, KricProperties.class})
@EnableElasticsearchRepositories(basePackages = {"com.wayble.server.explore.repository", "com.wayble.server.logging.repository", "com.wayble.server.direction.repository"})
@EntityScan(basePackages = "com.wayble.server")
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

}
