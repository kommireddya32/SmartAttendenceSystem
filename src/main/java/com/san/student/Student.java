package com.san.student;

import jakarta.persistence.*;

@Entity
@Table(name = "student")
public class Student {

    @Id
    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "password", nullable = false)
    private String password;

    // Default constructor
    public Student() {
    }

    // Parameterized constructor
    public Student(String studentId, String studentName, String department, String password) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.department = department;
        this.password = password;
    }

    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
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
}
