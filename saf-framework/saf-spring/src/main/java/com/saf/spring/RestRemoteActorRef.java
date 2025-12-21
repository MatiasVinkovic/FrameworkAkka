package com.saf.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saf.core.ActorContext;
import com.saf.core.ActorRef;
import com.saf.core.Message;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class RestRemoteActorRef implements ActorRef {
    private final String baseUrl;
    private final String actorName;
    private final String senderActorName;
    private final RestTemplate http = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    // Constructeur avec expéditeur
    public RestRemoteActorRef(String baseUrl, String actorName, String senderActorName) {
        this.baseUrl = baseUrl;
        this.actorName = actorName;
        this.senderActorName = senderActorName;
    }

    // Constructeur sans expéditeur (utilise un expéditeur par défaut)
    public RestRemoteActorRef(String baseUrl, String actorName) {
        this(baseUrl, actorName, "defaultSender");
    }

    @Override
    public String getName() {
        return actorName;
    }

    @Override
    public void tell(Message msg) {
        try {
            IncomingMessageDTO dto = new IncomingMessageDTO();
            dto.targetActor = actorName;
            dto.messageType = msg.getClass().getName();
            dto.payloadJson = mapper.writeValueAsString(msg);
            dto.senderActor = senderActorName;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<IncomingMessageDTO> entity = new HttpEntity<>(dto, headers);

            String url = baseUrl + "/actors/messages";
            http.postForEntity(url, entity, Void.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send remote message to " + actorName, e);
        }
    }

    @Override
    public void tell(Message msg, ActorContext ctx) {

    }

    @Override
    public com.saf.core.Mailbox mailbox() {
        throw new UnsupportedOperationException("mailbox() not supported for remote actors");
    }
}
