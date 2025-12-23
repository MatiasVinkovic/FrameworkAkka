package com.saf.core;

import java.util.concurrent.CompletableFuture;

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
    public void tell(Message msg, ActorRef sender) {

    }

    /**
     * Implémentation obligatoire de ask pour la compilation.
     */
    @Override
    public <T> CompletableFuture<T> ask(Object message, Class<T> responseType) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Impossible de faire un ask() sur NullActorRef"));
    }

    @Override
    public Mailbox mailbox() {
        throw new UnsupportedOperationException("mailbox() not supported for null actor");
    }
}
