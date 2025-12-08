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
    public ActorRef sender() {
        return sender;
    }
}