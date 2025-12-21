package com.saf.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saf.core.*;
import org.springframework.web.bind.annotation.*;

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

        ActorRef sender = system.findLocal(dto.senderActor);
        if (sender == null) {
            System.err.println("[SafMessageController] Sender actor not found: " + dto.senderActor);
            sender = new NullActorRef(); // Utilise un expéditeur par défaut si non trouvé
        }

        local.mailbox().enqueue(new MessageEnvelope(msg, sender));
    }

}
