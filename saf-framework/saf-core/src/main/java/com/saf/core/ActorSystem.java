package com.saf.core;

import java.util.HashMap;
import java.util.Map;

public class ActorSystem {
    private final String name;
    private final Map<String, LocalActorRef> actors = new HashMap<>();

    public ActorSystem(String name) {
        this.name = name;
    }

    // Ajoutez cette méthode
    public Map<String, LocalActorRef> getActors() {
        return this.actors;
    }

    public String getName() {
        return name;
    }

    public ActorRef createActor(Class<? extends Actor> actorClass, String actorName) {
        if (actors.containsKey(actorName)) {
            throw new IllegalArgumentException("Actor already exists: " + actorName);
        }
        try {
            Actor actor = actorClass.getDeclaredConstructor().newInstance();
            Mailbox mailbox = new Mailbox();
            LocalActorRef ref = new LocalActorRef(actorName, actor, mailbox, actorClass);
            LoggerService.log("INFO", ref.getName(), "BORN", "Actor created successfully ");
            actors.put(actorName, ref);
            return ref;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create actor " + actorName, e);
        }
    }

    public ActorRef findLocal(String actorName) {
        return actors.get(actorName);
    }


    // méthode de supervision
    public void processOneCycle() {
        for (LocalActorRef ref : actors.values()) {

            if (ref.isBlocked()) {
                // On saute cet acteur pour ce tour, ses messages restent dans la mailbox
                LoggerService.log("INFO", ref.getName(), "BLOCKED", "Acteur bloqué, impossible de communiquer");
                continue;
            }

            Mailbox mailbox = ref.mailbox();

            // On traite les messages en attente
            while (!mailbox.isEmpty()) {
                MessageEnvelope env = mailbox.dequeue();
                String actorName = ref.getName();
                String msgType = env.message.getClass().getSimpleName();

                try {
                    // 1. LOG : Trace de l'activité (Audit utilisateur)
                    LoggerService.log("INFO", actorName, "RECEIVE", "Processing message: " + msgType);

                    // 2. EXÉCUTION DU MESSAGE
                    ref.actor().onReceive(env.message, new SimpleActorContext(ref, env.sender));

                } catch (Exception e) {
                    // 3. --- SUPERVISION : Gestion de l'erreur ---
                    LoggerService.log("ERROR", actorName, "CRASH", "Exception during " + msgType + " : " + e.getMessage());

                    try {
                        LoggerService.log("WARN", actorName, "SUPERVISION", "Restarting actor instance...");

                        // Réinitialisation de l'instance d'acteur (Réflexion)
                        ref.restart();

                        LoggerService.log("INFO", actorName, "RESTART", "Actor successfully healed and ready.");

                    } catch (Exception restartEx) {
                        LoggerService.log("FATAL", actorName, "SUPERVISION", "Restart failed. Removing actor from system.");
                        //stopActor(actorName);
                    }

                    // On arrête de traiter la mailbox pour cet acteur dans ce cycle après un crash
                    // pour laisser le temps à la nouvelle instance de se stabiliser
                    break;
                }
            }
        }
    }


    public void blockActor(String name) {
        if (actors.containsKey(name)) {
            actors.get(name).setBlocked(true);
            LoggerService.log("WARN", name, "LIFECYCLE", "Actor has been BLOCKED");
        }
    }

    public void unblockActor(String name) {
        if (actors.containsKey(name)) {
            actors.get(name).setBlocked(false);
            LoggerService.log("INFO", name, "LIFECYCLE", "Actor has been UNBLOCKED");
        }
    }

    public void killActor(String name) {
        actors.remove(name);
    }

    public ActorRef remoteActor(String target, String actorName) {
        throw new UnsupportedOperationException(
                "Remote actors not supported in saf-core. Use saf-spring."
        );
    }
}
