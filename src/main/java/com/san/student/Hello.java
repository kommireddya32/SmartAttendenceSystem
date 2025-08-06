package com.san.student;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class Hello {
	public static void main(String args[]) {
		SessionFactory factory = new Configuration().configure().addAnnotatedClass(Student.class).buildSessionFactory();
		Session session=factory.openSession();
		Transaction transaction=session.beginTransaction();
		Student s= new Student("1","charan","csd","1234");
		session.save(s);
		
		
		transaction.commit();
	}
}
