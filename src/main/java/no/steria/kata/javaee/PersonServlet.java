package no.steria.kata.javaee;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PersonServlet extends HttpServlet {

	private static final long serialVersionUID = 7744195856599544243L;
	private PersonDao personDao;
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		boolean commit = false;
		personDao.beginTransaction();
		try {
			super.service(req, resp);
			commit = true;
		} finally {
			personDao.endTransaction(commit);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (req.getPathInfo().equals("/findPeople.html")) {
			String minimumAge = req.getParameter("minimumAge");
			if (minimumAge != null) {
				displaySearchPage(resp, personDao.searchByMinimumAge(Integer.parseInt(minimumAge)), null);				
			} else {
				String query = req.getParameter("name_query");
				displaySearchPage(resp, personDao.findPeople(query), query);
			}
		} else {
			CreatePersonPage page = new CreatePersonPage();
			page.display(resp);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		CreatePersonPage page = new CreatePersonPage();
		page.setFirstName(req.getParameter("first_name"));
		page.setLastName(req.getParameter("last_name"));
		page.setBirthDate(req.getParameter("birth_date"));
		if (page.isValid()) {
			personDao.createPerson(page.createPerson());
			resp.sendRedirect("/");
		} else {
			page.display(resp);
		}
	}

	private void displaySearchPage(HttpServletResponse resp, List<Person> people, String query) throws IOException {
		resp.setContentType("text/html");
		PrintWriter writer = resp.getWriter();
		writer.append("<html><body>");
		writer
			.append("<form method='get' action='findPeople.html'>")
			.append("<input type='text' name='name_query' value='")
			.append(htmlEncode(query))
			.append("'/>")
			.append("<input type='submit' name='findPeople' value='Find people'/>")
			.append("</form>");
		
		writer.append("<ul>");
		for (Person person : people) {
			writer.append("<li>").append(person.getDisplayString()).append("</li>");
		}
		writer.append("</ul>");
		
		writer.append("</body></html>");
	}

	static String htmlEncode(String text) {
		if (text == null) return "";
		return text
				.replaceAll("&", "&amp;")
				.replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;")
				;
	}

	public void setPersonDao(PersonDao personDao) {
		this.personDao = personDao;
		
	}
	
	@Override
	public void init() throws ServletException {
		personDao = new HibernatePersonDao("jdbc/personDs");
	}
	
}
