package com.knuaf.oneday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(unique = true) // ✅ 옵션: 아이디 중복 방지
    private String userId;

    private String password;

    // ✅ 1. studentId 필드 추가
    @Column(unique = true) // ✅ 옵션: 학번 중복 방지
    private String studentId;
    private String major;
    private String name;

    public Long getIdx() {
        return idx;
    }
    public void setId(Long idx) {
        this.idx = idx;
    }
    public String getUserId() {return userId;}
    public void setUserId(String userId) {this.userId = userId;}
    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    public String getStudentId() {return studentId;}
    public void setStudentId(String studentId) {this.studentId = studentId;}

    public String getMajor() {return major;}
    public void setMajor(String major) {this.major = major;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
}
