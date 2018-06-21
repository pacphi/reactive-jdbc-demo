package io.pivotal.reactive.jdbc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.PgPool;

@Configuration
public class ReactiveJdbcDemoConfig {
	
	@Profile("!cloud")
	@Configuration
	static class LocalConfig {
		
		@Bean
		PgPool pgPool(PgSettings settings) {
			return PgClient.pool(settings.getConnectionUri());
		}
		
	}
	
	@Profile("cloud")
	@Configuration
	static class CloudConfig {
		
		@Bean
		PgPool pgPool() {
			return PgClient.pool();
		}
	}
	
	@Bean
	public RouterFunction<ServerResponse> routerFunction(PersonHandler personHandler) { 
		return RouterFunctions.route(GET("/person/{id}").and(accept(APPLICATION_JSON)), personHandler::getPerson)
				.andRoute(GET("/person").and(accept(APPLICATION_JSON)), personHandler::listPeople)
				.andRoute(POST("/person").and(contentType(APPLICATION_JSON)), personHandler::createPerson);
	}
	
}
