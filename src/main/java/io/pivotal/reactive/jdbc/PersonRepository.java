package io.pivotal.reactive.jdbc;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PersonRepository extends ReactiveCrudRepository<Person, UUID> {

	@Query("SELECT * FROM people")
	Flux<Person> allPeople();

	Mono<Person> findById(UUID id);
}
