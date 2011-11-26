package no.steria.kata.javaee;

import static org.fest.assertions.Assertions.assertThat;

import javax.naming.NamingException;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.hibernate.cfg.Environment;
import org.hsqldb.jdbc.JDBCDataSource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class PersonWebTest {
	@Test
	public void shouldCreateAndDisplayPerson() throws Exception {
		DateTimeUtils.setCurrentMillisFixed(new DateTime(2011,11,25,10,10,10).getMillis());
		
		System.setProperty(Environment.HBM2DDL_AUTO, "create");
		createPersonDataSource("jdbc/personDs", "jdbc:hsqldb:mem:webTestDb");
		Server server = new Server(0);
		server.setHandler(new WebAppContext("src/main/webapp", "/"));
		server.start();
		
		WebDriver browser = createBrowser();
		String url = "http://localhost:" + server.getConnectors()[0].getLocalPort() + "/";
		
		createPerson(browser, url, "Darth", "Vader", "27.05.1977");
				
		browser.findElement(By.linkText("Find people")).click();
		browser.findElement(By.name("name_query")).sendKeys("vad");
		browser.findElement(By.name("findPeople")).click();
		
		assertThat(browser.getPageSource()).contains("Darth Vader [34 years old]");
	}

	private void createPerson(WebDriver browser, String url, String firstName, String lastName, String birthDate) {
		browser.get(url);
		browser.findElement(By.linkText("Create person")).click();
		browser.findElement(By.name("first_name")).sendKeys(firstName);
		browser.findElement(By.name("last_name")).sendKeys(lastName);
		browser.findElement(By.name("birth_date")).sendKeys(birthDate);
		browser.findElement(By.name("createPerson")).click();
	}
	
	@Test
	public void shouldFindOldPeople() throws Exception {
		DateTimeUtils.setCurrentMillisFixed(new DateTime(2011,11,25,10,10,10).getMillis());
		
		System.setProperty(Environment.HBM2DDL_AUTO, "create");
		createPersonDataSource("jdbc/personDs", "jdbc:hsqldb:mem:webTestDb");
		Server server = new Server(0);
		server.setHandler(new WebAppContext("src/main/webapp", "/"));
		server.start();
		
		WebDriver browser = createBrowser();
		String url = "http://localhost:" + server.getConnectors()[0].getLocalPort() + "/";

		createPerson(browser, url, "Unknown", "Birthdate", "");
		createPerson(browser, url, "Too", "Yong", new LocalDate().minusYears(18).plusDays(1).toString("dd.MM.yyyy"));
		createPerson(browser, url, "Old", "Enough", new LocalDate().minusYears(18).minusDays(1).toString("dd.MM.yyyy"));
		
		browser.get(url);
		
		browser.findElement(By.linkText("Find people who can drink")).click();
		assertThat(browser.getPageSource()).contains("Old Enough")
			.excludes("Too Yong")
			.excludes("Unknown Birthdate");		
	}

	private void createPersonDataSource(String dsName, String url) throws NamingException {
		JDBCDataSource ds = new JDBCDataSource();
		ds.setDatabase(url);
		ds.setUser("sa");
		ds.setPassword("");
		new EnvEntry(dsName, ds);
	}

	private HtmlUnitDriver createBrowser() {
		return new HtmlUnitDriver() {
			@Override
			public WebElement findElement(By by) {
				try {
					return super.findElement(by);
				} catch (NoSuchElementException e) {
					throw new NoSuchElementException("Did not find " + by + " in " + getPageSource());
				}
			}
		};
	}
}
