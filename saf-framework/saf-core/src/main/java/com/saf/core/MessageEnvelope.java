package com.saf.core;

public class MessageEnvelope {
    public final Message message;
    public final ActorRef sender;

    public MessageEnvelope(Message message, ActorRef sender) {
        this.message = message;
        this.sender = sender;
    }

}
