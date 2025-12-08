package com.saf.demo;

import com.saf.core.ActorRef;
import com.saf.core.ActorSystem;
import com.saf.spring.SAF;

public class LocalTestApp {

    public static void main(String[] args) throws InterruptedException {

        // 1. On démarre le framework (Spring + superviseur + ActorSystem)
        ActorSystem system = SAF.start("local-test");

        // 2. On crée un acteur local
        ActorRef pingActor = system.createActor(PingActor.class, "ping");

        // 3. On lui envoie un message
        pingActor.tell(new Ping("Hello depuis LocalTestApp"));

        // 4. On laisse le superviseur tourner un peu
        Thread.sleep(2000);

        System.out.println("Fin du test local (tu peux stopper l'app).");
    }
}