package com.knuaf.oneday.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "activity")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;
    private String title;
    private String detail;
    private String year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id")
    private User user;

    public Activity(String category, String title, String detail, String year) {
        this.category = category;
        this.title = title;
        this.detail = detail;
        this.year = year;
        this.user = user;
    }

    @Builder
    public Activity(User user) {
        this.user = user;
    }

    public Activity(String category, String title, String detail, String year, User user) {
        this.category = category;
        this.title = title;
        this.detail = detail;
        this.year = year;
        this.user = user; // 👈 이게 있어야 student_id가 저장됩니다!
    }
}
