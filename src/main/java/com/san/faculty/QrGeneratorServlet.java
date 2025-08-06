package com.san.faculty;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet("/QrGeneratorServlet")
public class QrGeneratorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");

    private SessionFactory sessionFactory;

    @Override
    public void init() throws ServletException {
        try {
            Configuration cfg = new Configuration();
            cfg.configure("hibernate.cfg.xml");
            cfg.addAnnotatedClass(Faculty.class);
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

        String facultyId = request.getParameter("facultyId");
        String department = request.getParameter("department");
        if (facultyId == null) facultyId = "";
        if (department == null) department = "";

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
        String qrContent = facultyId + "_" + department + "_" + timestamp;

        // Store in DB
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            Faculty faculty = session.get(Faculty.class, facultyId);
            if (faculty != null) {
                faculty.setLatestQrData(qrContent);
                faculty.setQrGeneratedTime(LocalDateTime.now());
                session.update(faculty);
            }

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Output QR code image
        response.setContentType("image/png");
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 300, 300);
            try (OutputStream os = response.getOutputStream()) {
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", os);
            }
        } catch (WriterException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "QR generation failed");
        }
    }
}
