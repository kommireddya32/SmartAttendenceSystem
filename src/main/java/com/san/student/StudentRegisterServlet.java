package com.san.student;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

@WebServlet("/StudentRegisterServlet")
public class StudentRegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private SessionFactory factory;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            factory = new Configuration().configure("hibernate.cfg.xml").addAnnotatedClass(Student.class).buildSessionFactory();
        } catch (Throwable ex) {
            throw new ServletException("Hibernate SessionFactory creation failed: " + ex);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String studentName = request.getParameter("studentName");
        String studentId = request.getParameter("studentId");
        String department = request.getParameter("department");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (!password.equals(confirmPassword)) {
            alertAndRedirect(out, "Passwords do not match!", "studentRegister.html");
            return;
        }

        Session session = factory.openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            // Check if student ID already exists
            Student existingStudent = session.get(Student.class, studentId);

            if (existingStudent != null) {
                alertAndRedirect(out, "Student ID already exists!", "studentRegister.html");
            } else {
                // Create and save new student
                Student newStudent = new Student();
                newStudent.setStudentId(studentId);
                newStudent.setStudentName(studentName);
                newStudent.setDepartment(department);
                newStudent.setPassword(password); // Note: hash in real app

                session.save(newStudent);
                tx.commit();

                alertAndRedirect(out, "Registration successful!", "studentLogin.html");
            }

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            alertAndRedirect(out, "Something went wrong! Try again.", "studentRegister.html");
        } finally {
            session.close();
        }
    }

    private void alertAndRedirect(PrintWriter out, String message, String location) {
        out.println("<script type='text/javascript'>");
        out.println("alert('" + message + "');");
        out.println("location='" + location + "';");
        out.println("</script>");
    }

    @Override
    public void destroy() {
        if (factory != null) {
            factory.close();
        }
    }
}
