package com.saf.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saf.core.ActorRef;
import com.saf.core.Message;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * Référence vers un acteur dans un autre microservice.
 * Envoie les messages en POST JSON sur {baseUrl}/actors/messages.
 */
class RestRemoteActorRef implements ActorRef {

    private final String baseUrl;
    private final String actorName;
    private final RestTemplate http = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    RestRemoteActorRef(String baseUrl, String actorName) {
        this.baseUrl = baseUrl;
        this.actorName = actorName;
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

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<IncomingMessageDTO> entity = new HttpEntity<>(dto, headers);

            String url = baseUrl + "/actors/messages";
            http.postForEntity(url, entity, Void.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send remote message to " + actorName, e);
        }
    }
}