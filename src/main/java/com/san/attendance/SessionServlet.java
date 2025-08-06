package com.san.attendance;

import com.san.student.Student;
import com.san.faculty.Faculty;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@WebServlet("/SessionServlet")
public class SessionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Validity window in seconds (3 minutes)
    private static final long VALIDITY_SECONDS = 3 * 60;

    private SessionFactory sessionFactory;

    @Override
    public void init() throws ServletException {
        try {
            Configuration cfg = new Configuration();
            cfg.configure("hibernate.cfg.xml");
            cfg.addAnnotatedClass(Faculty.class);
            cfg.addAnnotatedClass(Student.class);
            cfg.addAnnotatedClass(Attendance.class);
            sessionFactory = cfg.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ServletException("Failed to create SessionFactory in SessionServlet.init()", ex);
        }
    }

    @Override
    public void destroy() {
        if (sessionFactory != null) sessionFactory.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        resp.setContentType("text/plain; charset=utf-8");
        PrintWriter out = resp.getWriter();

        if ("verify".equalsIgnoreCase(action)) {
            String studentId = req.getParameter("studentId");
            String scannedQrData = req.getParameter("qrData");
            if (studentId == null || scannedQrData == null || !scannedQrData.contains("_")) {
                out.println("ERR:Missing or invalid studentId or qrData");
                return;
            }

            String[] parts = scannedQrData.split("_", 3);
            if (parts.length != 3) {
                out.println("ERR:Invalid QR format");
                return;
            }
            String facultyId = parts[0];

            try (Session session = sessionFactory.openSession()) {
                // fetch faculty row
                Faculty faculty = session.get(Faculty.class, facultyId);
                if (faculty == null) {
                    out.println("ERR:Invalid faculty");
                    return;
                }
                String expectedQr = faculty.getLatestQrData();
                LocalDateTime generatedTime = faculty.getQrGeneratedTime();

                if (expectedQr == null) {
                    out.println("ERR:No published QR");
                    return;
                }

                // exact-string check
                if (!expectedQr.equals(scannedQrData)) {
                    out.println("ERR:QR mismatch or expired");
                    return;
                }

                // time window check
                if (generatedTime == null) {
                    out.println("ERR:QR time missing");
                    return;
                }
                long ageSeconds = Math.abs(Duration.between(generatedTime, LocalDateTime.now()).getSeconds());
                if (ageSeconds > VALIDITY_SECONDS) {
                    out.println("ERR:QR expired");
                    return;
                }

                // mark attendance
                Transaction tx = session.beginTransaction();

                Student student = session.get(Student.class, studentId);
                if (student == null) {
                    out.println("ERR:Invalid student");
                    tx.commit();
                    return;
                }

                String today = LocalDate.now().toString();
                String hql = "FROM Attendance a WHERE a.studentId = :sid AND a.date = :d";
                Query<Attendance> query = session.createQuery(hql, Attendance.class);
                query.setParameter("sid", studentId);
                query.setParameter("d", today);

                Attendance existing = query.uniqueResult();
                if (existing != null) {
                    out.println("ERR:Attendance already marked for today");
                    tx.commit();
                    return;
                }

                Attendance attendance = new Attendance();
                attendance.setStudentId(studentId);
                attendance.setDate(today);
                session.persist(attendance);

                tx.commit();
                out.println("OK:Attendance marked");
            } catch (Exception e) {
                e.printStackTrace();
                out.println("ERR:Internal error");
            }
            return;
        }

        out.println("ERR:Unknown action");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
}
