package io.pivotal.reactive.jdbc;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table("people")
@Builder
@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
public class Person {

	@Id
	private UUID id;

	@Column("first_name")
	private String firstName;

	@Column("last_name")
	private String lastName;

	private Integer age;
}
