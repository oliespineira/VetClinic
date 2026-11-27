package com.example.vetclinic.clientsandpatients;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * PLACEHOLDER - to be replaced by the real Client from the clientsandpatients slice.
 * Only the fields the medical slice uses (owner contact details for reminders).
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
