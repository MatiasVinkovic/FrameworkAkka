package com.saf.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saf.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST interne au framework SAF.
 * Il permet la réception de messages distants et leur injection dans le système d'acteurs local.
 */
@RestController
@RequestMapping("/actors")
public class SafMessageController {

    private final ObjectMapper mapper = new ObjectMapper();

    // Injection de l'ActorSystem unique géré par Spring
    @Autowired
    private ActorSystem actorSystem;

    @PostMapping("/messages")
    public void receive(@RequestBody IncomingMessageDTO dto) throws Exception {

        // 1. Sécurité : Vérifier que le moteur d'acteurs est bien démarré
        if (actorSystem == null) {
            System.err.println("[SafMessageController] ERREUR : ActorSystem non injecté par Spring.");
            return;
        }

        // 2. Recherche de l'acteur cible dans le registre local (ceux créés par spawn ou au start)
        ActorRef target = actorSystem.findLocal(dto.targetActor);

        if (target == null) {
            System.err.println("[SafMessageController] Acteur cible introuvable : " + dto.targetActor);
            return;
        }

        // 3. Transformation du JSON en véritable objet Message Java via la réflexion
        // Utilise le nom de classe complet envoyé par le client (ex: com.saf.messages.OrderRequest)
        Class<?> clazz = Class.forName(dto.messageType);
        Message msg = (Message) mapper.readValue(dto.payloadJson, clazz);

        // 4. Identification de l'expéditeur (Sender)
        // Si l'expéditeur n'est pas trouvé localement, on utilise un NullActorRef pour éviter les NullPointerException
        ActorRef sender = actorSystem.findLocal(dto.senderActor);
        if (sender == null) {
            sender = new NullActorRef();
        }

        // 5. Injection du message dans la Mailbox de l'acteur
        // Note : On passe par target.mailbox().enqueue() car target.tell() depuis un contrôleur
        // ne dispose pas de l'ActorContext interne requis par la signature standard.
        target.mailbox().enqueue(new MessageEnvelope(msg, sender));

        // Logging pour le monitoring en temps réel dans la console
        System.out.println("[REST-IN] " + msg.getClass().getSimpleName() + " -> " + dto.targetActor);
    }
}