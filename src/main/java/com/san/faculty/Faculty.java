package com.san.faculty;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "faculty")
public class Faculty {

    @Id
    @Column(name = "faculty_id", nullable = false, length = 100)
    private String facultyId;

    @Column(name = "faculty_name", nullable = false, length = 150)
    private String facultyName;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "password", nullable = false)
    private String password;

    // persisted latest QR string
    @Column(name = "latest_qr_data")
    private String latestQrData;

    // when the latest QR was generated
    @Column(name = "qr_generated_time")
    private LocalDateTime qrGeneratedTime;

    // No-arg constructor required by JPA/Hibernate
    public Faculty() { }

    // Parameterized constructor (does NOT include latestQrData or qrGeneratedTime)
    public Faculty(String facultyId, String facultyName, String department, String password) {
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.department = department;
        this.password = password;
    }

    // Getters and setters
    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLatestQrData() {
        return latestQrData;
    }

    public void setLatestQrData(String latestQrData) {
        this.latestQrData = latestQrData;
    }

    public LocalDateTime getQrGeneratedTime() {
        return qrGeneratedTime;
    }

    public void setQrGeneratedTime(LocalDateTime qrGeneratedTime) {
        this.qrGeneratedTime = qrGeneratedTime;
    }

    // Optional: helpful toString for debugging (avoid logging passwords)
    @Override
    public String toString() {
        return "Faculty{" +
                "facultyId='" + facultyId + '\'' +
                ", facultyName='" + facultyName + '\'' +
                ", department='" + department + '\'' +
                ", latestQrData='" + latestQrData + '\'' +
                ", qrGeneratedTime=" + qrGeneratedTime +
                '}';
    }
}
