package io.pivotal.reactive.jdbc;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.reactivestreams.Publisher;
import org.springframework.stereotype.Repository;

import io.reactiverse.pgclient.PgConnection;
import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.PgPreparedQuery;
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Tuple;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@AllArgsConstructor
public class PersonRepository {

	private final PgPool pool;

	public Flux<Person> allPeople() {
		List<Person> result = new ArrayList<>();
		pool.getConnection(arc -> {
			PgConnection conn = arc.result();
			conn.prepare("SELECT * FROM people", arp -> {
				PgPreparedQuery pq = arp.result();
				pq.execute(are -> {
					PgRowSet rowSet = are.result();
					Flux.fromIterable(rowSet)
						.map(r -> Person.builder()
								.id(r.getUUID("id"))
								.firstName(r.getString("first_name").trim())
								.lastName(r.getString("last_name").trim())
								.age(r.getInteger("age"))
								.build())
						.log()
						.collectList()
						.subscribe(result::addAll);
				});
				conn.close();
			});
		});
		return Flux.fromIterable(result).delaySubscription(Duration.ofMillis(100));
	}

	public Publisher<Void> savePerson(Mono<Person> person) {
		person.subscribe(p -> {
			pool.getConnection(arc -> {
				PgConnection conn = arc.result();
				conn.prepare("INSERT INTO people (id, first_name, last_name, age) VALUES ($1, $2, $3, $4)", arp -> {
					PgPreparedQuery pq = arp.result();
					pq.execute(Tuple.of(UUID.randomUUID(), p.getFirstName(), p.getLastName(), p.getAge()), are -> {});
					conn.close();
				});
			});
		});
		return Mono.empty();
	}

	public Mono<Person> getPerson(UUID personId) {
		List<Person> result = new ArrayList<>();
		pool.getConnection(arc -> {
			PgConnection conn = arc.result();
			conn.prepare("SELECT * FROM people WHERE id=$1", arp -> {
				PgPreparedQuery pq = arp.result();
				pq.execute(Tuple.of(personId), are -> {
					PgRowSet rowSet = are.result();
					Flux.fromIterable(rowSet)
						.map(r -> Person.builder()
								.id(r.getUUID("id"))
								.firstName(r.getString("first_name").trim())
								.lastName(r.getString("last_name").trim())
								.age(r.getInteger("age"))
								.build())
						.log()
						.collectList()
						.subscribe(result::addAll);
				});
				conn.close();
			});
		});
		return Flux.fromIterable(result).next().delaySubscription(Duration.ofMillis(100));
	}

}
