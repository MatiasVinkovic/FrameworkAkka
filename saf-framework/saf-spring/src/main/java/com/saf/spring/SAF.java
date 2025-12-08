package com.saf.spring;

import com.saf.core.ActorSystem;
import org.springframework.boot.SpringApplication;

/**
 * Façade simple pour l'utilisateur du framework.
 *
 * Exemple d'usage dans un microservice :
 *
 *   ActorSystem system = SAF.start("ms-client");
 *   ActorRef a = system.createActor(MyActor.class, "a");
 *   ActorRef b = system.remoteActor("http://localhost:8081", "otherActor");
 */
public final class SAF {

    private static ActorSystem system;

    private SAF() {
    }

    public static ActorSystem start(String serviceName) {
        // crée un ActorSystem "spécial Spring"
        system = new SpringActorSystem(serviceName);
        // démarre le serveur Spring (REST + superviseur)
        SpringApplication.run(SafSpringApplication.class);
        return system;
    }

    static ActorSystem getActorSystem() {
        return system;
    }
}