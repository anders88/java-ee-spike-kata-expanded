package no.steria.kata.javaee;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

public class CreatePersonPage {

	private String firstName;
	private String lastName;
	private String birthDate;
	private List<String> errors = new ArrayList<String>();

	public void setFirstName(String firstName) {
		this.firstName = firstName;
		validateName(firstName,"First name");
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
		validateName(lastName, "Last name");
	}

	public void setBirthDate(String birthDate) {
		if (birthDate != null) {
			this.birthDate = birthDate;
			try {
				getBirthDate();
			} catch (IllegalArgumentException e) {
				this.errors.add("Birth date is not a valid date");
			}
		}
		
	}

	private LocalDate getBirthDate() {
		return LocalDate.parse(birthDate,DateTimeFormat.forPattern("dd.MM.yyyy"));
	}

	public boolean isValid() {
		return errors.isEmpty();
	}

	public Person createPerson() {
		Person person = Person.withName(firstName, lastName);
		if (birthDate != null) {
			person.setBirthDate(getBirthDate());
		}
		return person;
	}

	public void display(HttpServletResponse resp) throws IOException {
		resp.setContentType("text/html");
		PrintWriter writer = resp.getWriter();
		writer.append("<html><body>");
		for (String error : errors) {
			writer.append("<i>" + error + "</i><br/>");
		}
		writer
			.append("<form method='post' action='createPerson.html'>")
			.append("<input type='text' name='first_name' value='")
			.append(PersonServlet.htmlEncode(firstName))
			.append("'/>")
			.append("<input type='text' name='last_name' value='")
			.append(PersonServlet.htmlEncode(lastName))
			.append("'/>")
			.append("<input type='text' name='birth_date' value='")
			.append(PersonServlet.htmlEncode(birthDate))
			.append("'/>")
			.append("<input type='submit' name='createPerson' value='Create person'/>")
			.append("</form>");
		writer.append("</body></html>");
	}

	String validateName(String name, String field) {
		if (name == null) return null;
		if (name.isEmpty()) {
			errors.add(field + " must be provided");
		}
		if (name.contains("&") || name.contains("<") || name.contains(">")) {
			errors.add(field + " contains illegal characters");
		}
		return null;
	}

}
