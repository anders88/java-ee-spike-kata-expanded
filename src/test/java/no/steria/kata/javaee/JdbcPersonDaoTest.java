package no.steria.kata.javaee;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.hsqldb.jdbc.JDBCDataSource;

public class JdbcPersonDaoTest extends AbstractPersonDaoTest {

	@Override
	protected PersonDao createPersonDao() throws Exception {
		DataSource dataSource = createPersonDataSource();
		JdbcPersonDao jdbcPersonDao = new JdbcPersonDao(dataSource);
		jdbcPersonDao.beginTransaction();
		jdbcPersonDao.dropTables();
		jdbcPersonDao.createTables();
		jdbcPersonDao.endTransaction(true);
		return jdbcPersonDao;
	}


	private DataSource createPersonDataSource() throws NamingException {
		JDBCDataSource ds = new JDBCDataSource();
		ds.setDatabase("jdbc:hsqldb:mem:testDb");
		ds.setUser("sa");
		ds.setPassword("");
		return ds;
	}

}
