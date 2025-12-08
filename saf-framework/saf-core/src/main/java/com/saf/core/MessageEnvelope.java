package com.saf.core;

/**
 * Enveloppe interne qui associe un message à son expéditeur.
 */
class MessageEnvelope {

    final Message message;
    final ActorRef sender;

    MessageEnvelope(Message message, ActorRef sender) {
        this.message = message;
        this.sender = sender;
    }
}