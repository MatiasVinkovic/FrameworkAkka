package com.saf.core;

public class LocalActorRef implements ActorRef {
    private final String name;
    private final Actor actor;
    private final Mailbox mailbox;

    public LocalActorRef(String name, Actor actor, Mailbox mailbox) {
        this.name = name;
        this.actor = actor;
        this.mailbox = mailbox;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void tell(Message msg) {
        mailbox.enqueue(new MessageEnvelope(msg, null));
    }

    @Override
    public void tell(Message msg, ActorContext ctx) {

    }

    @Override
    public Mailbox mailbox() {  // Assure-toi que cette méthode est implémentée
        return mailbox;
    }

    public Actor actor() {
        return actor;
    }
}
