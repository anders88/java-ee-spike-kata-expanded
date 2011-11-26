package no.steria.kata.javaee;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;


@Entity
public class Person {
	
	@SuppressWarnings("unused")
	@Id
	@GeneratedValue
	private Long id;

	private String lastName;

	private String firstName;

	private LocalDate birthDate;

	public static Person withName(String firstName, String lastName) {
		Person person = new Person();
		person.firstName = firstName;
		person.lastName = lastName;
		//person.fullName = firstName + " " + lastName;
		return person;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public String getFullName() {
		return firstName + " " + lastName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Person)) return false;
		return nullSafeEqual(getFullName(), ((Person)obj).getFullName());
	}

	private<T> boolean nullSafeEqual(T a, T b) {
		return (a != null) ? a.equals(b) : b == null;
	}
	
	@Override
	public String toString() {
		return "Person<" + getFullName() + ">";
	}
	
	@Override
	public int hashCode() {
		return getFullName().hashCode();
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
		
	}

	public int getYearsOld() {
		Period years = new Interval(getBirthDate().toDateTimeAtStartOfDay(),new DateTime()).toPeriod(PeriodType.years());
		return years.getYears();
	}

	public String getDisplayString() {
		return getFullName() + (birthDate != null ? (" [" + getYearsOld() + " years old]") : "");
	}

}
