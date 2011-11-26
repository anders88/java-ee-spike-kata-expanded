package no.steria.kata.javaee;

import static org.fest.assertions.Assertions.assertThat;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractPersonDaoTest {

	private PersonDao personDao;

	public AbstractPersonDaoTest() {
		super();
	}

	@Before
	public void createDao() throws Exception {
		personDao = createPersonDao();		
		personDao.beginTransaction();
	}

	protected abstract PersonDao createPersonDao() throws Exception;

	@After
	public void rollback() {
		personDao.endTransaction(false);
	}

	@Test
	public void shouldSaveAndRetrivePerson() throws Exception {
		Person darth = Person.withName("Darth","Vader");
		personDao.createPerson(darth);
		assertThat(personDao.findPeople(null)).contains(darth);
	}

	@Test
	public void shouldSearchWithLastname() throws Exception {
		Person ani = Person.withName("Anakin","Skywalker");
		Person luke = Person.withName("Luke","Skywalker");
		Person jarjar = Person.withName("JarJar","Binks");
		
		personDao.createPerson(luke);
		personDao.createPerson(ani);
		personDao.createPerson(jarjar);
		assertThat(personDao.findPeople("sky")).contains(ani,luke).excludes(jarjar);
	}

	@Test
	public void shouldSearchWithFirstname() throws Exception {
		Person anakin = Person.withName("Darth","Vader");
		Person maul = Person.withName("Darth","Maul");
		Person jarjar = Person.withName("JarJar","Binks");
		
		personDao.createPerson(maul);
		personDao.createPerson(anakin);
		personDao.createPerson(jarjar);
		assertThat(personDao.findPeople("art")).contains(anakin,maul).excludes(jarjar);
	}

	@Test
	public void searchByAge() throws Exception {
		Person unknownBirthday = createPerson("Unknown", "Birthdate", null);
		Person tooYoung = createPerson("Too", "Yong", new LocalDate().minusYears(18).plusDays(1));
		Person oldEnough = createPerson("Old", "Enough", new LocalDate().minusYears(18).minusDays(1));
		Person veryOld = createPerson("Very", "Old", new LocalDate().minusYears(100).minusDays(1));
		
		assertThat(personDao.searchByMinimumAge(18))
			.contains(oldEnough, veryOld)
			.excludes(unknownBirthday, tooYoung);
	}

	private Person createPerson(String firstName, String lastName, LocalDate birthDate) {
		Person person = Person.withName(firstName, lastName);
		person.setBirthDate(birthDate);
		personDao.createPerson(person);
		return person;
	}

	@Test
	public void shouldCommit() throws Exception {
		Person luke = Person.withName("Luke","Skywalker");
		Person jarjar = Person.withName("JarJar","Binks");
		
		personDao.createPerson(luke);
		personDao.endTransaction(true);
		personDao.beginTransaction();
		personDao.createPerson(jarjar);
		personDao.endTransaction(false);
	
		personDao.beginTransaction();
		assertThat(personDao.findPeople(null)).contains(luke).excludes(jarjar);
	}

}