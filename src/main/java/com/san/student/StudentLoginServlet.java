package com.san.student;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

@WebServlet("/StudentLoginServlet")
public class StudentLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private SessionFactory sessionFactory;

    @Override
    public void init() throws ServletException {
        try {
            Configuration cfg = new Configuration();
            cfg.configure("hibernate.cfg.xml");
            cfg.addAnnotatedClass(Student.class); // Your Student entity
            this.sessionFactory = cfg.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ServletException("Failed to create SessionFactory", ex);
        }
    }

    @Override
    public void destroy() {
        if (this.sessionFactory != null) {
            this.sessionFactory.close();
        }
        super.destroy();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username"); // student_id
        String password = request.getParameter("password");

        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            response.setContentType("text/html");
            response.getWriter().println("<script>alert('Please enter username and password');window.history.back();</script>");
            return;
        }

        Session session = null;
        Transaction tx = null;
        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();

            String hql = "FROM Student s WHERE s.studentId = :id";
            Query<Student> query = session.createQuery(hql, Student.class);
            query.setParameter("id", username);
            Student student = query.uniqueResult();

            tx.commit();

            if (student != null && password.equals(student.getPassword())) {
                // Redirect with studentId
                String redirectUrl = request.getContextPath() + "/student.html"
                        + "?studentId=" + student.getStudentId();
                response.sendRedirect(redirectUrl);
                return;
            }

            response.setContentType("text/html");
            response.getWriter().println("<script>alert('Invalid Username or Password');window.history.back();</script>");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Internal Server Error");
        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Redirect to login page
        resp.sendRedirect(req.getContextPath() + "/studentLogin.html");
    }
}
