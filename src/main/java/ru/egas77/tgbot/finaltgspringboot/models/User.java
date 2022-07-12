package ru.egas77.tgbot.finaltgspringboot.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.joda.time.DateTimeZone;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.io.Serializable;
import java.util.List;
import java.util.TimeZone;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@ToString
@Table(name = "users")
@Validated
public class User implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "tgid", nullable = false, unique = true)
    private long tgid;

    @Column(name = "isblock", columnDefinition = "bit(1) default 0")
    private boolean isblock = false;

    @Column(name = "email", unique = true)
    @Email
    private String email;

    @Column(name = "nofemail", columnDefinition = "bit(1) default 0")
    private boolean nofemail = false;

    @Column(name = "timezone", columnDefinition = "varchar(255) default 'UTC'")
    private TimeZone timeZone = DateTimeZone.UTC.toTimeZone();
    
    @OneToMany(targetEntity = Post.class)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private List<Post> posts;

    public void delEmail() {
        email = null;
    }
}
