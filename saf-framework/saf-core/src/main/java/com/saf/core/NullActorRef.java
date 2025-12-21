package com.saf.core;

public class NullActorRef implements ActorRef {
    @Override
    public String getName() {
        return "null";
    }

    @Override
    public void tell(Message msg) {
        // Ne fait rien
    }

    @Override
    public void tell(Message msg, ActorContext ctx) {

    }

    @Override
    public Mailbox mailbox() {
        throw new UnsupportedOperationException("mailbox() not supported for null actor");
    }
}
