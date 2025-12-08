package com.saf.demo;

import com.saf.core.Message;

public class Ping implements Message {

    private String text;

    public Ping() {
        // constructeur vide requis pour Jackson si un jour on envoie ce message en REST
    }

    public Ping(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) { // pour Jackson aussi
        this.text = text;
    }
}