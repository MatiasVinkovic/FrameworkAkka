package com.saf.core;

/**
 * Implémentation d’un ActorRef pour les acteurs locaux (dans la même JVM)
 */
class LocalActorRef implements ActorRef {

    private final String name;
    private final Actor actor;
    private final Mailbox mailbox;

    LocalActorRef(String name, Actor actor, Mailbox mailbox) {
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

    Mailbox mailbox() {
        return mailbox;
    }

    Actor actor() {
        return actor;
    }
}