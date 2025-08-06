package com.san.faculty;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.hibernate.*;
import org.hibernate.cfg.Configuration;

@WebServlet("/FacultyRegisterServlet")
public class FacultyRegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private SessionFactory factory;

    @Override
    public void init() throws ServletException {
        try {
            factory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .addAnnotatedClass(Faculty.class)
                    .buildSessionFactory();
        } catch (Throwable ex) {
            throw new ServletException("SessionFactory creation failed: " + ex);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String facultyName = request.getParameter("facultyName");
        String facultyId = request.getParameter("facultyId");
        String department = request.getParameter("department");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (!password.equals(confirmPassword)) {
            sendAlert(out, "Passwords do not match!", "facultyRegister.html");
            return;
        }

        Session session = factory.openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            Faculty existingFaculty = session.get(Faculty.class, facultyId);

            if (existingFaculty != null) {
                sendAlert(out, "Faculty ID already exists!", "facultyRegister.html");
            } else {
                Faculty newFaculty = new Faculty();
                newFaculty.setFacultyId(facultyId);
                newFaculty.setFacultyName(facultyName);
                newFaculty.setDepartment(department);
                newFaculty.setPassword(password); // You can hash it for real apps

                session.save(newFaculty);
                tx.commit();

                sendAlert(out, "Registration successful! Please login.", "facultyLogin.html");
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            sendAlert(out, "An error occurred. Please try again.", "facultyRegister.html");
        } finally {
            session.close();
        }
    }

    private void sendAlert(PrintWriter out, String message, String location) {
        out.println("<script type='text/javascript'>");
        out.println("alert('" + message + "');");
        out.println("location='" + location + "';");
        out.println("</script>");
    }

    @Override
    public void destroy() {
        if (factory != null) factory.close();
    }
}
