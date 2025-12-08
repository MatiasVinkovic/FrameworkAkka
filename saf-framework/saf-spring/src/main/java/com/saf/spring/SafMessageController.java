package com.saf.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saf.core.ActorRef;
import com.saf.core.ActorSystem;
import com.saf.core.Message;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint REST générique reçu par le framework.
 * Un autre microservice peut POSTer ici pour envoyer un message à un acteur local.
 */
@RestController
@RequestMapping("/actors")
public class SafMessageController {

    private final ActorSystem system = SAF.getActorSystem();
    private final ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/messages")
    public void receive(@RequestBody IncomingMessageDTO dto) throws Exception {
        if (system == null) {
            System.err.println("[SafMessageController] ActorSystem not initialized.");
            return;
        }

        ActorRef local = system.findLocal(dto.targetActor);
        if (local == null) {
            System.err.println("[SafMessageController] Actor not found: " + dto.targetActor);
            return;
        }

        Class<?> clazz = Class.forName(dto.messageType);
        Message msg = (Message) mapper.readValue(dto.payloadJson, clazz);

        local.tell(msg);
    }
}