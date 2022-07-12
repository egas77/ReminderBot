package ru.egas77.tgbot.finaltgspringboot.models;

import lombok.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "posts")
@Getter
@Setter
public class Post {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "title")
    private String title;

    @Column(name = "data")
    private String data;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date datecreated;

    @ManyToOne
    private User user;

    public static final String dateTimeFormat = "yyyy.MM.dd HH:mm";

    public Post() {
        datecreated = new LocalDateTime().toDate();
    }

    public void setTimeForUser(User user, DateTime userDateTime) {
        userDateTime = userDateTime.withZoneRetainFields(DateTimeZone.forTimeZone(user.getTimeZone()));
        DateTime dateTimePost = userDateTime.withZone(DateTimeZone.getDefault());
        setDatecreated(dateTimePost.toDate());
    }

    public DateTime getTimeForUser(User user) {
        return new DateTime(getDatecreated())
                .withZone(DateTimeZone.forTimeZone(user.getTimeZone()));
    }

    public String getStringTimeForUser(User user) {
        return getTimeForUser(user).toString(dateTimeFormat);
    }

    public void convertToNewTimeZone(DateTimeZone oldTimeZone, DateTimeZone newTimeZone) {
        DateTime dateTimePost = new DateTime(getDatecreated());
        DateTime dateTimePostUser = dateTimePost.withZoneRetainFields(newTimeZone);
        dateTimePostUser = dateTimePostUser.withZone(oldTimeZone);
        dateTimePost = dateTimePostUser.withZoneRetainFields(DateTimeZone.getDefault());
        setDatecreated(dateTimePost.toDate());
    }

    public String getPostView(Integer number, User user) {
        return "<b>" + number + ". " + getTitle() + "</b>\n" +
                "Описание: " + getData() + "\n" +
                "Дата и время: " + getStringTimeForUser(user);
    }
    public String getPostView(User user) {
        return "<b>" + getTitle() + "</b>\n" +
                "Описание: " + getData() + "\n" +
                "Дата и время: " + getStringTimeForUser(user);
    }
}
