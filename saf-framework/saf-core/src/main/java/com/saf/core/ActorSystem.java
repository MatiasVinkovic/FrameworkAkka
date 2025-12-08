package com.saf.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Système d'acteurs local :
 * - stocke les acteurs
 * - gère leurs boîtes aux lettres
 * - distribue les messages via processOneCycle()
 */
public class ActorSystem {

    private final String name;
    private final Map<String, LocalActorRef> actors = new HashMap<>();

    public ActorSystem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Crée un acteur local simple
     */
    public ActorRef createActor(Class<? extends Actor> actorClass, String actorName) {
        if (actors.containsKey(actorName)) {
            throw new IllegalArgumentException("Actor already exists: " + actorName);
        }
        try {
            Actor actor = actorClass.getDeclaredConstructor().newInstance();
            Mailbox mailbox = new Mailbox();
            LocalActorRef ref = new LocalActorRef(actorName, actor, mailbox);
            actors.put(actorName, ref);
            return ref;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create actor " + actorName, e);
        }
    }

    /** Pour retrouver un acteur local par son nom (utilisé par la couche REST). */
    public ActorRef findLocal(String actorName) {
        return actors.get(actorName);
    }

    /**
     * Superviseur : traite les messages en attente.
     */
    public void processOneCycle() {
        for (LocalActorRef ref : actors.values()) {
            Mailbox mailbox = ref.mailbox();
            while (!mailbox.isEmpty()) {
                MessageEnvelope env = mailbox.dequeue();
                try {
                    ref.actor().onReceive(env.message,
                            new SimpleActorContext(ref, env.sender));
                } catch (Exception e) {
                    System.err.println("[ActorSystem] Error in actor " + ref.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Remote actor → pas dans le module core.
     * Implémenté dans saf-spring.
     */
    public ActorRef remoteActor(String target, String actorName) {
        throw new UnsupportedOperationException(
                "Remote actors not supported in saf-core. Use saf-spring."
        );
    }
}