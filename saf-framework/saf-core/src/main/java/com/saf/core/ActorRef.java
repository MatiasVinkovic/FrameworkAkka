package com.saf.core;

import java.util.concurrent.CompletableFuture;

/**
 * Référence vers un acteur. Permet d'envoyer un message.
 */
public interface ActorRef {
    String getName();

    //communication synchrone
    void tell(Message msg);
    void tell(Message msg, ActorContext ctx);  // surcharge
    void tell(Message msg, ActorRef sender);

    //communication asynchrone
    <T> CompletableFuture<T> ask(Object message, Class<T> responseType);

    Mailbox mailbox(); // Ajoute cette méthode
}