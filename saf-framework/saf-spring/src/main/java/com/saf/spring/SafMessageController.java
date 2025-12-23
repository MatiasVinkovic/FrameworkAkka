package com.saf.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saf.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;



/**
 * Contrôleur REST interne au framework SAF.
 * Gère la réception de messages distants (Asynchrones et Synchrones).
 */
@RestController
@RequestMapping("/actors")
public class SafMessageController {

    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private org.springframework.cloud.client.discovery.DiscoveryClient discoveryClient;

    @Autowired
    private ActorSystem actorSystem;

    /**
     * Endpoint pour le mode ASYNCHRONE (tell).
     * Libère le thread HTTP immédiatement après l'ajout en mailbox.
     */
    @PostMapping("/messages")
    public void receive(@RequestBody IncomingMessageDTO dto) throws Exception {
        if (actorSystem == null) return;

        ActorRef target = actorSystem.findLocal(dto.targetActor);
        if (target == null) {
            System.err.println("[SafMessageController] Acteur cible introuvable : " + dto.targetActor);
            return;
        }

        Message msg = deserialize(dto);
        ActorRef sender = resolveSender(dto.senderActor);

        // Injection classique dans la mailbox
        target.mailbox().enqueue(new MessageEnvelope(msg, sender));
        System.out.println("[REST-IN-ASYNC] " + msg.getClass().getSimpleName() + " -> " + dto.targetActor);
    }

    /**
     * Endpoint pour le mode SYNCHRONE (ask).
     * Attend que l'acteur réponde via context.reply() pour renvoyer le résultat.
     */
    @PostMapping("/messages/ask")
    public Object ask(@RequestBody IncomingMessageDTO dto) throws Exception {
        if (actorSystem == null) throw new RuntimeException("ActorSystem non initialisé");

        ActorRef target = actorSystem.findLocal(dto.targetActor);
        if (target == null) throw new RuntimeException("Acteur introuvable : " + dto.targetActor);

        Message msg = deserialize(dto);

        // On crée une promesse qui sera complétée quand l'acteur répondra
        CompletableFuture<Object> futureResponse = new CompletableFuture<>();

        // Création d'un expéditeur temporaire qui sert de "callback"
        ActorRef temporarySender = new ActorRef() {
            @Override public String getName() { return "temp-ask-handler"; }
            @Override public void tell(Message m) { futureResponse.complete(m); }
            @Override public void tell(Message m, ActorContext ctx) { futureResponse.complete(m); }

            @Override
            public void tell(Message msg, ActorRef sender) {

            }

            @Override public <T> CompletableFuture<T> ask(Object o, Class<T> c) { return null; }
            @Override public Mailbox mailbox() { return null; }
        };

        // On envoie le message avec notre expéditeur temporaire
        target.mailbox().enqueue(new MessageEnvelope(msg, temporarySender));

        // On attend la réponse (timeout de 5 secondes pour éviter de bloquer Spring indéfiniment)
        try {
            System.out.println("[REST-IN-SYNC] Attente réponse pour : " + msg.getClass().getSimpleName());
            return futureResponse.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Timeout : l'acteur n'a pas répondu dans les temps.");
        }
    }



    private Message deserialize(IncomingMessageDTO dto) throws Exception {
        Class<?> clazz = Class.forName(dto.messageType);
        return (Message) mapper.readValue(dto.payloadJson, clazz);
    }

    private ActorRef resolveSender(String senderName) {
        if (senderName == null || senderName.equals("defaultSender")) {
            return new NullActorRef();
        }

        // 1. On cherche si l'acteur est local (cas rare en inter-ms)
        ActorRef local = actorSystem.findLocal(senderName);
        if (local != null) return local;

        // 2. CORRECTION : Si pas local, on crée une référence DISTANTE vers le client
        // On suppose ici que l'expéditeur vient du MS-CLIENT (ou on passe le nom du service dans le DTO)
        return new RestRemoteActorRef(discoveryClient, "MS-CLIENT", senderName);
    }
}