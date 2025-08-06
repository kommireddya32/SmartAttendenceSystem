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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet("/QrGeneratorServlet")
public class QrGeneratorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String facultyId = request.getParameter("facultyId");
        String department = request.getParameter("department");
        if (facultyId == null) facultyId = "";
        if (department == null) department = "";

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
        String qrContent = facultyId + "_" + department + "_" + timestamp;

        response.setContentType("image/png");
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 300, 300);
            try (OutputStream os = response.getOutputStream()) {
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", os);
            }

            publishToSessionServlet(request, qrContent);

        } catch (WriterException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "QR generation failed");
        }
    }

    private void publishToSessionServlet(HttpServletRequest request, String qrData) {
        HttpURLConnection conn = null;
        try {
            String urlStr = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                    + request.getContextPath() + "/SessionServlet?action=publish";

            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String body = "qrData=" + java.net.URLEncoder.encode(qrData, "UTF-8");
            byte[] postData = body.getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(postData.length);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
