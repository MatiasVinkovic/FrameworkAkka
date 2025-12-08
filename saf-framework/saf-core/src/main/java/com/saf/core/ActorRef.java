package com.saf.core;

/**
 * Référence vers un acteur. Permet d'envoyer un message.
 */
public interface ActorRef {
    String getName();
    void tell(Message msg);
}