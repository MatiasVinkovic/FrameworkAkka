package com.saf.core;

public class LocalActorRef implements ActorRef {
    private final String name;
    private  Actor actor;
    private final Mailbox mailbox;
    private final Class<? extends Actor> actorClass; // On stocke la recette de cuisine
    private boolean blocked = false; // L'état par défaut est "actif"

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
    public LocalActorRef(String name, Actor actor, Mailbox mailbox, Class<? extends Actor> actorClass) {
        this.name = name;
        this.actor = actor;
        this.actorClass = actorClass;
        this.mailbox = mailbox;
    }

    @Override
    public String getName() {
        return name;
    }

    // Méthode pour remplacer l'instance crashée par une neuve
    public void restart() throws Exception {
        this.actor = actorClass.getDeclaredConstructor().newInstance();
    }

    public Actor actor() { return actor; }
    public Class<? extends Actor> getActorClass() { return actorClass; }

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

}
