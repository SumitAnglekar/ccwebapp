package com.cloud.ccwebapp.recipe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Entity(name = "User_Table")
public class User {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;
    private String first_name;
    private String last_name;

   //@Column(name="password")
    //@NotEmpty(message = "Password shouldn't be left blank")
    //@Size(min = 2, max = 30)
    @JsonIgnore
    private String password;

    @NotEmpty(message = "Email address shouldn't be left blank")
    @JsonProperty("email_address")
    @Pattern(regexp = "^(.+)@(.+)$")
    private String emailaddress;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "account_created")
    private Date account_created;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "account_updated")
    private Date account_updated;

    public User(){

    }

    public User(String first_name, String last_name, String password, String emailaddress, Date account_created, Date account_updated) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.password = password;
        this.emailaddress = emailaddress;
        this.account_created = account_created;
        this.account_updated = account_updated;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmailaddress() {
        return emailaddress;
    }

    public void setEmailaddress(String emailaddress) {
        this.emailaddress = emailaddress;
    }

    public Date getAccount_created() {
        return account_created;
    }

    public void setAccount_created(Date account_created) {
        this.account_created = account_created;
    }

    public Date getAccount_updated() {
        return account_updated;
    }

    public void setAccount_updated(Date account_updated) {
        this.account_updated = account_updated;
    }
}
