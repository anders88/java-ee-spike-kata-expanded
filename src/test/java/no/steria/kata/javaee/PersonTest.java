package no.steria.kata.javaee;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

public class PersonTest {
	@Test
	public void factoryShouldReturnPersonWithGivenName() throws Exception {
		assertThat(Person.withName("Darth", "Vader").getFullName()).isEqualTo("Darth Vader");
	}
	
	@Test
	public void equalPeopleShouldBeEqual() throws Exception {
		assertThat(Person.withName("Darth", "Vader"))
			.isEqualTo(Person.withName("Darth", "Vader"))
			.isNotEqualTo(Person.withName("Anakin", "Skywalker"))
			.isNotEqualTo(null)
			;
		assertThat(Person.withName(null,null))
			.isEqualTo(Person.withName(null,null))
			.isNotEqualTo(Person.withName(null,"Skywalker"))
			.isNotEqualTo(Person.withName("Anakin",null))
			;
	}
	
	@Test
	public void shouldHaveCorrectHashCode() throws Exception {
		assertThat(Person.withName("Darth","Vader").hashCode())
			.isEqualTo(Person.withName("Darth","Vader").hashCode())
			.isNotEqualTo(Person.withName("Anakin","Vader").hashCode())
			.isNotEqualTo(Person.withName(null,null).hashCode());
	}
}
