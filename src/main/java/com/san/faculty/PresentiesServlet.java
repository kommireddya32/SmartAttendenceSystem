package com.san.faculty;

import com.san.attendance.Attendance;
import com.san.student.Student;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/PresentiesServlet")
public class PresentiesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private SessionFactory sessionFactory;

    @Override
    public void init() throws ServletException {
        try {
            Configuration cfg = new Configuration();
            cfg.configure("hibernate.cfg.xml");
            cfg.addAnnotatedClass(Attendance.class);
            cfg.addAnnotatedClass(Student.class);
            sessionFactory = cfg.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ServletException("Hibernate init failed", ex);
        }
    }

    @Override
    public void destroy() {
        if (sessionFactory != null) sessionFactory.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // Get today’s date in YYYY-MM-DD format
        String today = LocalDate.now().toString();

        Session session = null;
        try {
            session = sessionFactory.openSession();

            // Fetch today's attendance records
            Query<Attendance> query = session.createQuery(
                "FROM Attendance WHERE date = :today ORDER BY studentId", Attendance.class);
            query.setParameter("today", today);
            List<Attendance> presenties = query.list();

            out.println("<html><head><title>Today's Presenties</title>");
            out.println("<style>");
            out.println("table { border-collapse: collapse; width: 60%; margin: auto; }");
            out.println("th, td { padding: 8px 12px; border: 1px solid #444; text-align: center; }");
            out.println("th { background-color: #f2f2f2; }");
            out.println("h2 { text-align: center; }");
            out.println("</style></head><body>");
            out.println("<h2>Today's Present Students (" + today + ")</h2>");

            if (presenties.isEmpty()) {
                out.println("<p style='text-align:center;'>No students marked present today.</p>");
            } else {
                out.println("<table>");
                out.println("<tr><th>Student ID</th><th>Student Name</th><th>Department</th></tr>");

                for (Attendance att : presenties) {
                    Student student = session.get(Student.class, att.getStudentId());
                    if (student != null) {
                        out.println("<tr>");
                        out.println("<td>" + student.getStudentId() + "</td>");
                        out.println("<td>" + student.getStudentName() + "</td>");
                        out.println("<td>" + student.getDepartment() + "</td>");
                        out.println("</tr>");
                    }
                }
                out.println("</table>");
            }

            out.println("</body></html>");

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<p>Error fetching attendance records: " + e.getMessage() + "</p>");
        } finally {
            if (session != null) session.close();
            out.close();
        }
    }
}
