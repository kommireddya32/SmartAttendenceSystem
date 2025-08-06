package com.san.faculty;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.Session;
import org.hibernate.Transaction;

@WebServlet("/QrGeneratorServlet")
public class QrGeneratorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int QR_WIDTH = 300;
    private static final int QR_HEIGHT = 300;

    private SessionFactory sessionFactory;

    @Override
    public void init() throws ServletException {
        try {
            Configuration cfg = new Configuration();
            cfg.configure("hibernate.cfg.xml");
            // register entities used
            cfg.addAnnotatedClass(Faculty.class);
            cfg.addAnnotatedClass(com.san.student.Student.class);
            cfg.addAnnotatedClass(com.san.attendance.Attendance.class);
            sessionFactory = cfg.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ServletException("Failed to create SessionFactory in QrGeneratorServlet.init()", ex);
        }
    }

    @Override
    public void destroy() {
        if (sessionFactory != null) sessionFactory.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String facultyId = request.getParameter("facultyId");
        String department = request.getParameter("department");
        if (facultyId == null || facultyId.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing facultyId");
            return;
        }
        if (department == null) department = "";

        long epochSeconds = Instant.now().getEpochSecond();
        String qrContent = facultyId + "_" + department + "_" + epochSeconds;

        // Save latest QR in DB
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            Faculty f = session.get(Faculty.class, facultyId);
            if (f == null) {
                tx.rollback();
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Faculty not found: " + facultyId);
                return;
            }
            f.setLatestQrData(qrContent);
            f.setQrGeneratedTime(LocalDateTime.now());
            session.merge(f);

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to save QR to DB");
            return;
        }

        // Generate QR PNG and return
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        try (OutputStream os = response.getOutputStream()) {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", os);
            os.flush();
        } catch (WriterException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "QR generation failed");
        }
    }
}
