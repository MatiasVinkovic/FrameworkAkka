package com.saf.core;

import java.util.HashMap;
import java.util.Map;

public class ActorSystem {
    private final String name;
    private final Map<String, LocalActorRef> actors = new HashMap<>();

    public ActorSystem(String name) {
        this.name = name;
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
            LocalActorRef ref = new LocalActorRef(actorName, actor, mailbox);
            actors.put(actorName, ref);
            return ref;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create actor " + actorName, e);
        }
    }

    public ActorRef findLocal(String actorName) {
        return actors.get(actorName);
    }

    public void processOneCycle() {
        for (LocalActorRef ref : actors.values()) {
            Mailbox mailbox = ref.mailbox();
            while (!mailbox.isEmpty()) {
                MessageEnvelope env = mailbox.dequeue();
                try {
                    ref.actor().onReceive(env.message, new SimpleActorContext(ref, env.sender));
                } catch (Exception e) {
                    System.err.println("[ActorSystem] Error in actor " + ref.getName());
                    e.printStackTrace();
                }
            }
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
