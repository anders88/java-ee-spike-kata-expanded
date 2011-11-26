package no.steria.kata.javaee;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.fest.assertions.Fail;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

public class PersonServletTest {
	private PersonServlet servlet = new PersonServlet();
	private HttpServletRequest req = mock(HttpServletRequest.class);
	private HttpServletResponse resp = mock(HttpServletResponse.class);
	private PersonDao personDao = mock(PersonDao.class);
	private StringWriter htmlSource = new StringWriter();
	
	@Before
	public void setupServlet() throws IOException {
		servlet.setPersonDao(personDao);		
		when(resp.getWriter()).thenReturn(new PrintWriter(htmlSource));
	}
	
	@After
	public void checkHtml() throws DocumentException {
		if (!htmlSource.toString().isEmpty())
			DocumentHelper.parseText(htmlSource.toString());
	}
	
	@Test
	public void shouldDisplayCreatePage() throws Exception {
		when(req.getMethod()).thenReturn("GET");
		when(req.getPathInfo()).thenReturn("/createPerson.html");
		
		servlet.service(req, resp);
		
		verify(resp).setContentType("text/html");
		assertThat(htmlSource.toString())
			.contains("<form method='post' action='createPerson.html'")
			.contains("<input type='text' name='first_name' value=''")
			.contains("<input type='text' name='last_name' value=''")
			.contains("<input type='text' name='birth_date' value=''")
			.contains("<input type='submit' name='createPerson' value='Create person'");
	}
	
	@Test
	public void shouldSavePerson() throws Exception {
		when(req.getMethod()).thenReturn("POST");
		when(req.getParameter("first_name")).thenReturn("Obi-Wan");
		when(req.getParameter("last_name")).thenReturn("Kenobi");
		when(req.getParameter("birth_date")).thenReturn("10.11.2011");
		
		servlet.service(req, resp);
		
		InOrder order = inOrder(personDao);
		order.verify(personDao).beginTransaction();
		ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
		order.verify(personDao).createPerson(personCaptor.capture());
		order.verify(personDao).endTransaction(true);
		verify(resp).sendRedirect("/");
		
		assertThat(personCaptor.getValue().getFullName()).isEqualTo("Obi-Wan Kenobi");
		assertThat(personCaptor.getValue().getBirthDate()).isEqualTo(new LocalDate(2011, 11, 10));
	}
	
	@Test
	public void shouldValidateLegalName() throws Exception {
		when(req.getMethod()).thenReturn("POST");
		when(req.getParameter("first_name")).thenReturn("Obi-Wan<");
		when(req.getParameter("last_name")).thenReturn(">Ken&bi");
		when(req.getParameter("birth_date")).thenReturn("yesterday");
		
		servlet.service(req, resp);
		
		verify(personDao, never()).createPerson(any(Person.class));
		verify(resp).setContentType("text/html");
		assertThat(htmlSource.toString())
			.contains("First name contains illegal characters")
			.contains("Last name contains illegal characters")
			.contains("Birth date is not a valid date")
			.contains("<input type='text' name='first_name' value='Obi-Wan&lt;'")
			.contains("<input type='text' name='last_name' value='&gt;Ken&amp;bi'")
			.contains("<input type='text' name='birth_date' value='yesterday'")
			;
	}
	
	@Test
	public void shouldNotAllowEmptyName() throws Exception {
		when(req.getMethod()).thenReturn("POST");
		when(req.getParameter("first_name")).thenReturn("");
		when(req.getParameter("last_name")).thenReturn("");
		
		servlet.service(req, resp);
		
		verify(personDao, never()).createPerson(any(Person.class));
		verify(resp).setContentType("text/html");
		assertThat(htmlSource.toString())
			.contains("First name must be provided")
			.contains("Last name must be provided")
			.contains("<input type='text' name='first_name' value=''")
			.contains("<input type='text' name='last_name' value=''")
			;
	}
	
	@Test
	public void shouldAcceptNoBirthDateGiven() throws Exception {
		when(req.getMethod()).thenReturn("POST");
		
		when(req.getParameter("first_name")).thenReturn("The");
		when(req.getParameter("last_name")).thenReturn("Name");
		
		servlet.service(req, resp);

		ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
		verify(personDao).createPerson(personCaptor.capture());
		
		assertThat(personCaptor.getValue().getBirthDate()).isNull();
	}
	
	@Test
	public void shouldRollbackOnException() throws Exception {
		doThrow(new RuntimeException("Hello world"))
			.when(personDao).createPerson(any(Person.class));
		
		when(req.getMethod()).thenReturn("POST");
		when(req.getParameter("first_name")).thenReturn("The");
		when(req.getParameter("last_name")).thenReturn("Name");
		
		try {
			servlet.service(req, resp);
			Fail.fail("Expected exception");
		} catch (RuntimeException expected) {
			assertThat(expected.getMessage()).isEqualTo("Hello world");
		}
		verify(personDao).endTransaction(false);
	}
	
	@Test
	public void shouldDisplaySearchPage() throws Exception {
		createGetRequest("/findPeople.html");

		servlet.service(req, resp);
		
		verify(resp).setContentType("text/html");
		assertThat(htmlSource.toString())
			.contains("<form method='get' action='findPeople.html'")
			.contains("<input type='text' name='name_query' value=''")
			.contains("<input type='submit' name='findPeople' value='Find people'");
	}

	@Test
	public void shouldSearchForPeople() throws Exception {
		createGetRequest("/findPeople.html");
		when(req.getParameter("name_query")).thenReturn("vader");
		servlet.service(req, resp);
		
		verify(personDao).findPeople("vader");
	}
	
	@Test
	public void shouldSearchByAge() throws Exception {
		createGetRequest("/findPeople.html");
		when(req.getParameter("minimumAge")).thenReturn("28");
		servlet.service(req, resp);
		
		verify(personDao).searchByMinimumAge(28);
		verify(personDao, never()).findPeople(anyString());
	}

	
	@Test
	public void shouldDisplayResult() throws Exception {
		createGetRequest("/findPeople.html");
		when(personDao.findPeople(anyString())).thenReturn(Arrays.asList(Person.withName("Anakin","Skywalker"),Person.withName("Luke","Skywalker")));
		
		servlet.service(req, resp);
		
		assertThat(htmlSource.toString())
			.contains("<li>Anakin Skywalker</li>")
			.contains("<li>Luke Skywalker</li>");
	}
	
	@Test
	public void shouldDisplayPersonAge() throws Exception {
		createGetRequest("/findPeople.html");
		Person person = Person.withName("Luke", "Skywalker");
		person.setBirthDate(new LocalDate().minusYears(17).plusDays(1));
		when(personDao.findPeople(anyString())).thenReturn(Arrays.asList(person));
		
		servlet.service(req, resp);
		assertThat(htmlSource.toString()).contains("Luke Skywalker [16 years old]");
	}
	
	@Test
	public void shouldEchoSearchParameter() throws Exception {
		createGetRequest("/findPeople.html");
		when(req.getParameter("name_query")).thenReturn("vader");
		servlet.service(req, resp);
		
		assertThat(htmlSource.toString())
			.contains("name='name_query' value='vader'");
	}
	
	private void createGetRequest(String pathInfo) {
		when(req.getMethod()).thenReturn("GET");
		when(req.getPathInfo()).thenReturn(pathInfo);
	}
}

