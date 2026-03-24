package com.jongsoft.finance.project.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

@Entity
@Introspected
@Table(name = "client")
public class ClientJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String address;
    private boolean archived;

    @ManyToOne
    private UserAccountJpa user;

    public ClientJpa() {}

    private ClientJpa(
            String name, String email, String phone, String address, UserAccountJpa user) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.archived = false;
        this.user = user;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public UserAccountJpa getUser() {
        return user;
    }

    public static ClientJpa of(
            String name, String email, String phone, String address, UserAccountJpa user) {
        return new ClientJpa(name, email, phone, address, user);
    }
}
