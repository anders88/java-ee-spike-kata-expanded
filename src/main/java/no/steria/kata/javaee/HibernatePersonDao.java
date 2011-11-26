package no.steria.kata.javaee;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;
import org.joda.time.contrib.hibernate.PersistentLocalDate;

public class HibernatePersonDao implements PersonDao {

	private SessionFactory sessionFactory;

	public HibernatePersonDao(String dsName) {
		Configuration configuration = new Configuration();
		configuration.setProperty(Environment.DATASOURCE, dsName);
		configuration.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
		configuration.registerTypeOverride(new PersistentLocalDate(), new String[] { LocalDate.class.getName() });
		configuration.addAnnotatedClass(Person.class);
		sessionFactory = configuration.buildSessionFactory();
	}

	@Override
	public void createPerson(Person person) {
		getSession().save(person);
	}

	@Override
	public void beginTransaction() {
		getSession().beginTransaction();
	}

	@Override
	public void endTransaction(boolean commit) {
		if (commit) {
			getSession().getTransaction().commit();
		} else {
			getSession().getTransaction().rollback();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Person> findPeople(String nameQuery) {
		Criteria criteria = getSession().createCriteria(Person.class);
		if (nameQuery != null) {
			criteria.add(Restrictions.or(
					Restrictions.ilike("firstName", nameQuery, MatchMode.ANYWHERE),
					Restrictions.ilike("lastName", nameQuery, MatchMode.ANYWHERE)));
		}
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Person> searchByMinimumAge(int minimumAge) {
		Criteria criteria = getSession().createCriteria(Person.class);
		criteria.add(Restrictions.lt("birthDate", new LocalDate().minusYears(minimumAge)));
		return criteria.list();
	}

	private Session getSession() {
		return sessionFactory.getCurrentSession();
	}

}
