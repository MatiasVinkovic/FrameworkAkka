package com.saf.core;

/**
 * Contexte minimal passé à onReceive de l’acteur.
 */
class SimpleActorContext implements ActorContext {

    private final ActorRef self;
    private final ActorRef sender;

    SimpleActorContext(ActorRef self, ActorRef sender) {
        this.self = self;
        this.sender = sender;
    }

    @Override
    public ActorRef self() {
        return self;
    }

    @Override
    public ActorRef getSender() {
        return sender;
    }

    @Override
    public void reply(Message msg) {
        // On renvoie le message à l'expéditeur en passant le contexte actuel
        // Cela permet de faire du chaînage de messages
        sender.tell(msg, this);
    }

    public void reply(Object response) {
        if (sender != null) {
            sender.tell((Message) response, (ActorContext) self);
        }
    }

    @Override
    public ActorRef sender() {
        return sender;
    }
}