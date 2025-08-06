package com.san.attendance;

import com.san.student.Student;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.io.*;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/SessionServlet")
public class SessionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Store full qrData string for each facultyId
    private static final ConcurrentHashMap<String, String> publishedMap = new ConcurrentHashMap<>();

    private SessionFactory sessionFactory;

    @Override
    public void init() throws ServletException {
        try {
            Configuration cfg = new Configuration();
            cfg.configure("hibernate.cfg.xml");
            cfg.addAnnotatedClass(Student.class);
            cfg.addAnnotatedClass(Attendance.class);
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        resp.setContentType("text/plain; charset=utf-8");
        PrintWriter out = resp.getWriter();

        if ("publish".equalsIgnoreCase(action)) {
            String qrData = req.getParameter("qrData");

            if (qrData == null || !qrData.contains("_")) {
                out.println("ERR:Invalid QR data");
                return;
            }

            String[] parts = qrData.split("_");
            String facultyId = parts[0]; // format: facultyId_department_timestamp

            publishedMap.put(facultyId, qrData);
            out.println("OK:Published");
            return;
        }

        if ("verify".equalsIgnoreCase(action)) {
            String studentId = req.getParameter("studentId");
            String scannedQrData = req.getParameter("qrData");

            if (studentId == null || scannedQrData == null || !scannedQrData.contains("_")) {
                out.println("ERR:Missing or invalid studentId or qrData");
                return;
            }

            String facultyId = scannedQrData.split("_")[0];

            // Check if published QR matches scanned QR
            String expectedQr = publishedMap.get(facultyId);
            if (expectedQr == null || !expectedQr.equals(scannedQrData)) {
                out.println("ERR:QR mismatch or expired");
                return;
            }

            // Proceed to mark attendance
            try (Session session = sessionFactory.openSession()) {
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
}
