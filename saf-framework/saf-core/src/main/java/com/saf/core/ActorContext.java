package com.saf.core;

/**
 * Contexte fourni à l'acteur lors du traitement d'un message.
 */
public interface ActorContext {
    ActorRef self();
    ActorRef sender();
    ActorRef getSender();
    void reply(Message msg);
}