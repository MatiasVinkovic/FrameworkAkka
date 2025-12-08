package com.example.messages;

import com.saf.core.Message;

public class CreateTicket implements Message {
    private String description;

    public CreateTicket() {}

    public CreateTicket(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}