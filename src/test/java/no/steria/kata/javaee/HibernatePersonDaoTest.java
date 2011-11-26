package no.steria.kata.javaee;

import javax.naming.NamingException;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.hibernate.cfg.Environment;
import org.hsqldb.jdbc.JDBCDataSource;




public class HibernatePersonDaoTest extends AbstractPersonDaoTest {

	@Override
	protected PersonDao createPersonDao() throws Exception {
		System.setProperty(Environment.HBM2DDL_AUTO, "create");
		System.setProperty(Environment.SHOW_SQL, "true");
		createPersonDataSource();
		return new HibernatePersonDao("jdbc/testDs");
	}

	private void createPersonDataSource() throws NamingException {
		JDBCDataSource ds = new JDBCDataSource();
		ds.setDatabase("jdbc:hsqldb:mem:testDb");
		ds.setUser("sa");
		ds.setPassword("");
		new EnvEntry("jdbc/testDs", ds);
	}

}
