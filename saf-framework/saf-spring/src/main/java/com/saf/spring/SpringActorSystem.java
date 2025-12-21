package com.saf.spring;

import com.saf.core.ActorRef;
import com.saf.core.ActorSystem;

/**
 * Spécialisation d'ActorSystem qui sait créer des acteurs distants via HTTP.
 *
 * Ici, on interprète le paramètre "target" de remoteActor comme une URL de base,
 * par ex : "http://localhost:8081".
 */
class SpringActorSystem extends ActorSystem {

    public SpringActorSystem(String name) {
        super(name);
    }

    @Override
    public ActorRef remoteActor(String baseUrl, String actorName) {
        // Utilise un nom d'expéditeur par défaut ou passe-le en paramètre
        return new RestRemoteActorRef(baseUrl, actorName, "defaultSender");
    }
}