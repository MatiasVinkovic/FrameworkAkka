package com.saf.spring;

import com.saf.core.ActorSystem;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Façade simple pour l'utilisateur du framework.
 * Gère le démarrage de Spring Boot et la synchronisation avec Eureka.
 */
public final class SAF {

    private static ConfigurableApplicationContext context;
    private static ActorSystem system;

    private SAF() {}

    /**
     * Démarre le microservice et le système d'acteurs SAF.
     */
    public static ActorSystem start(Class<?> primarySource, String serviceName, boolean enableConsole, String[] args) {
        // 1. Lancement de Spring
        context = SpringApplication.run(primarySource, args);

        // 2. Récupération automatique du bean ActorSystem géré par Spring
        ActorSystem system = context.getBean(ActorSystem.class);

        // 3. --- AJOUT CRITIQUE : Démarrage du moteur de traitement des Mailboxes ---
        // Sans cela, processOneCycle() n'est jamais appelé et les messages restent bloqués.
        Thread engineThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    system.processOneCycle(); // On vide les mailboxes de tous les acteurs
                    Thread.sleep(2000);       // Pause de 100ms pour ne pas saturer le CPU
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "SAF-Engine-Thread");
        engineThread.setDaemon(true); // Le thread s'arrête si l'app s'arrête
        engineThread.start();
        // --------------------------------------------------------------------------

        // 4. Attente optionnelle de synchronisation (propre au réseau)
        waitForNetworkDiscovery();

        // 5. Lancement de la console si demandé
        if (enableConsole) {
            DiscoveryClient dc = context.getBean(DiscoveryClient.class);
            new SAFShell(system, dc).start();
        }

        return system;
    }

    public static ConfigurableApplicationContext getContext() {
        return context;
    }

    static ActorSystem getActorSystem() {
        return system;
    }

    private static void waitForNetworkDiscovery() {
        System.out.println("[SAF] Initialisation du réseau et synchronisation Eureka...");
        try {
            Thread.sleep(4000);
            System.out.println("[SAF] Système d'acteurs distribués prêt.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}


