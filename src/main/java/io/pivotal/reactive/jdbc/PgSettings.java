package io.pivotal.reactive.jdbc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix="pg")
public class PgSettings {

	private String connectionUri;
	private String lookupKey;
}
