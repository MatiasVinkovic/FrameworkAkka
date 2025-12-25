package com.saf.core;

import java.util.concurrent.CompletableFuture;

public class LocalActorRef implements ActorRef {
    private final String name;
    private Actor actor;
    private final Mailbox mailbox;
    private final Class<? extends Actor> actorClass;
    private boolean blocked = false;

    public LocalActorRef(String name, Actor actor, Mailbox mailbox, Class<? extends Actor> actorClass) {
        this.name = name;
        this.actor = actor;
        this.actorClass = actorClass;
        this.mailbox = mailbox;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    @Override
    public String getName() {
        return name;
    }

    public void restart() throws Exception {
        this.actor = actorClass.getDeclaredConstructor().newInstance();
    }

    public Actor actor() { return actor; }

    public Class<? extends Actor> getActorClass() { return actorClass; }

    @Override
    public void tell(Message msg) {
        // Envoi asynchrone sans expéditeur spécifié [cite: 19, 34]
        mailbox.enqueue(new MessageEnvelope(msg, null));
    }

//    @Override
//    public void tell(Message msg, ActorContext ctx) {
//        // Envoi asynchrone avec extraction de l'expéditeur depuis le contexte
//        mailbox.enqueue(new MessageEnvelope(msg, ctx.getSender()));
//    }

    @Override
    public void tell(Message msg, ActorContext ctx) {
        this.tell(msg, ctx.self()); // On extrait le "self" du contexte
    }


    public void tell(Message msg, ActorRef sender) {
        // On met le message et l'expéditeur directement dans l'enveloppe
        mailbox.enqueue(new MessageEnvelope(msg, sender));
    }

    /**
     * Implémentation du mode SYNCHRONE (ask) en local
     */
    @Override
    public <T> CompletableFuture<T> ask(Object message, Class<T> responseType) {
        CompletableFuture<T> future = new CompletableFuture<>();

        // On crée un expéditeur temporaire pour capturer la réponse
        ActorRef callback = new ActorRef() {
            @Override public String getName() { return "ask-callback"; }
            @Override @SuppressWarnings("unchecked") public void tell(Message m) { future.complete((T) m); }
            @Override @SuppressWarnings("unchecked") public void tell(Message m, ActorContext ctx) { future.complete((T) m); }

            @Override
            public void tell(Message msg, ActorRef sender) {

            }

            @Override public <T1> CompletableFuture<T1> ask(Object o, Class<T1> c) { return null; }
            @Override public Mailbox mailbox() { return null; }
        };

        // On envoie le message dans la mailbox avec notre callback comme expéditeur
        mailbox.enqueue(new MessageEnvelope((Message) message, callback));

        return future;
    }

    @Override
    public Mailbox mailbox() {
        return mailbox;
    }
}