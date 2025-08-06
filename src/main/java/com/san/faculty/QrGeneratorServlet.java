package com.san.faculty;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
            cfg.addAnnotatedClass(Faculty.class);
            sessionFactory = cfg.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ServletException("Failed to build SessionFactory in QrGeneratorServlet.init()", ex);
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

        // Use epoch seconds to avoid format mismatch
        long epochSeconds = Instant.now().getEpochSecond();
        String qrContent = facultyId + "_" + department + "_" + epochSeconds;

        // Persist into DB (update faculty.latest_qr_data and qr_generated_time)
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Faculty faculty = session.get(Faculty.class, facultyId);
                if (faculty == null) {
                    tx.rollback();
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Faculty not found: " + facultyId);
                    return;
                }

                faculty.setLatestQrData(qrContent);
                // store LocalDateTime based on system default zone
                faculty.setQrGeneratedTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault()));

                session.merge(faculty);
                session.flush(); // ensure SQL executed before commit
                tx.commit();

                System.out.println("QrGeneratorServlet: saved latestQrData='" + qrContent + "' for faculty=" + facultyId);
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to save QR to DB");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DB session error");
            return;
        }

        // Return QR image
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
<<<<<<< HEAD
}
=======
}
>>>>>>> 1322caba89c84fcb26cf5626b808a789690c643c
