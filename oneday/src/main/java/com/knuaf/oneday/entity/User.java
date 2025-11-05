package com.knuaf.oneday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@Entity
@Table(name ="USERS")
//@Getter
//@Setter
public class User {

    @Setter
    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public Long getId() { return id; }

    public void setId(Long id) {
        this.id = id;
    }
    //private Long StudentId;
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {return password;}
}
