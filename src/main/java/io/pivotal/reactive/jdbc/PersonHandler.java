package io.pivotal.reactive.jdbc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@AllArgsConstructor
public class PersonHandler {

	private final PersonRepository repository;

	public Mono<ServerResponse> listPeople(ServerRequest request) {
		log.info("Attempting " + request.method().name() + " " + request.path());
		Flux<Person> people = repository.allPeople();
		return ServerResponse.ok()
				.contentType(APPLICATION_JSON)
				.body(people, Person.class);
	}

	public Mono<ServerResponse> createPerson(ServerRequest request) {
		log.info("Attempting " + request.method().name() + " " + request.path());
		return request.bodyToMono(Person.class)
				.flatMap(repository::save)
				.flatMap(person -> ServerResponse.accepted()
						.contentType(APPLICATION_JSON)
						.body(fromValue(person)));
	}

	public Mono<ServerResponse> getPerson(ServerRequest request) {
		log.info("Attempting " + request.method().name() + " " + request.path());
		UUID personId = UUID.fromString(request.pathVariable("id"));
		Mono<ServerResponse> notFound = ServerResponse.notFound().build();
		return repository.findById(personId)
				.flatMap(person -> ServerResponse.ok()
						.contentType(APPLICATION_JSON)
						.body(fromValue(person)))
				.switchIfEmpty(notFound);
	}
}