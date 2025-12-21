package com.saf.core;

/**
 * Référence vers un acteur. Permet d'envoyer un message.
 */
public interface ActorRef {
    String getName();
    void tell(Message msg);
    void tell(Message msg, ActorContext ctx);  // surcharge
    Mailbox mailbox(); // Ajoute cette méthode
}