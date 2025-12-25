package com.saf.spring;

import com.saf.core.Actor;
import com.saf.core.ActorRef;
import com.saf.core.ActorSystem;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SAFShell {
    private final ActorSystem system;
    private final DiscoveryClient dc;
    private final Map<String, ActorRef> myRefs = new HashMap<>();
    private final Map<String, Object> myMessages = new HashMap<>();

    public SAFShell(ActorSystem system, DiscoveryClient dc) {
        this.system = system;
        this.dc = dc;
    }

    public void start() {
        Thread shellThread = new Thread(this::runLoop, "SAF-Shell-Thread");
        shellThread.setDaemon(true); // Pour ne pas bloquer l'arrêt de l'app
        shellThread.start();
    }

    private void runLoop() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                try {
                    System.out.print("\nSAF-LOGIC > ");
                    String line = scanner.nextLine();
                    if (line.trim().isEmpty()) continue;

                    String[] parts = line.split(" ");
                    String cmd = parts[0].toLowerCase();

                    handleCommand(cmd, parts);
                } catch (Exception e) {
                    System.err.println("[SHELL ERROR] " + e.getMessage());
                }
            }
        }
    }

    private void handleCommand(String cmd, String[] parts) throws Exception {
        switch (cmd) {
            case "spawn":
                // spawn client.ClientActor matias
                @SuppressWarnings("unchecked")
                Class<? extends Actor> actorClass = (Class<? extends Actor>) Class.forName(parts[1]);
                system.createActor(actorClass, parts[2]);
                System.out.println("[OK] Actor spawned.");
                break;
            case "ref":
                // ref MS-RESTAURANT extraCook r1
                ActorRef r = new RestRemoteActorRef(dc, parts[1].toUpperCase(), parts[2]);
                myRefs.put(parts[3], r);
                System.out.println("[OK] Reference " + parts[3] + " stored.");
                break;
            case "msg":
                // msg com.saf.messages.OrderRequest Pizza Matias m1
                Object m = Class.forName(parts[1]).getConstructor(String.class, String.class)
                        .newInstance(parts[2], parts[3]);
                myMessages.put(parts[4], m);
                System.out.println("[OK] Message " + parts[4] + " created.");
                break;
            case "tell":
                // tell r1 m1
                ActorRef target = myRefs.get(parts[1]);
                Object message = myMessages.get(parts[2]);
                target.tell((com.saf.core.Message) message);
                System.out.println("[OK] Sent.");
                break;
            case "list":
                System.out.println("Actors: " + system.getActors().keySet());
                break;
        }
    }
}