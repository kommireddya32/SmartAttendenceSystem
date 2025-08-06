package com.san.faculty;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "faculty")
public class Faculty {

    @Id
    @Column(name = "faculty_id", length = 50)
    private String facultyId;

    @Column(name = "faculty_name", nullable = false)
    private String facultyName;

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "latest_qr_data")
    private String latestQrData;

    @Column(name = "qr_generated_time")
    private LocalDateTime qrGeneratedTime;

    // ===== Constructors =====
    public Faculty() {
        // Required by Hibernate
    }

    // Parameterized constructor (without QR fields)
    public Faculty(String facultyId, String facultyName, String department, String password) {
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.department = department;
        this.password = password;
    }

    // ===== Getters / Setters =====
    public String getFacultyId() { return facultyId; }
    public void setFacultyId(String facultyId) { this.facultyId = facultyId; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getLatestQrData() { return latestQrData; }
    public void setLatestQrData(String latestQrData) { this.latestQrData = latestQrData; }

    public LocalDateTime getQrGeneratedTime() { return qrGeneratedTime; }
    public void setQrGeneratedTime(LocalDateTime qrGeneratedTime) { this.qrGeneratedTime = qrGeneratedTime; }
<<<<<<< HEAD
}
=======
}
>>>>>>> 1322caba89c84fcb26cf5626b808a789690c643c
