package com.example.VetClinic.clientsandpatients;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * PLACEHOLDER - owned by Person 1 (Owners, pets and record locking).
 * Only the fields the appointment/reminder slice needs (owner contact for reminders).
 */
@Entity
public class Client {

    @Id
    private String id;

    private String name;

    private String email;

    private String phone;

    protected Client() {
    }

    public Client(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}
