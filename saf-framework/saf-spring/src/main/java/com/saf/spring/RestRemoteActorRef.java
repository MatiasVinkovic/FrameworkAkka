package com.saf.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saf.core.ActorContext;
import com.saf.core.ActorRef;
import com.saf.core.LoggerService;
import com.saf.core.Message;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Implémentation d'ActorRef pour la communication inter-microservices[cite: 19].
 * Utilise Eureka pour localiser dynamiquement les instances[cite: 43].
 */
public class RestRemoteActorRef implements ActorRef {
    private final DiscoveryClient discoveryClient;
    private final String serviceName;
    private final String actorName;
    private final String senderActorName;
    private String fixedBaseUrl; // Utilisé si on passe une URL en dur au lieu d'un serviceId

    private final RestTemplate http = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    // Constructeur avec DiscoveryClient : RÉSOLUTION LAZY (Pas de .get(0) ici !)
    public RestRemoteActorRef(DiscoveryClient discoveryClient, String serviceName, String actorName, String senderActorName) {
        this.discoveryClient = discoveryClient;
        this.serviceName = serviceName;
        this.actorName = actorName;
        this.senderActorName = senderActorName;
    }

    public RestRemoteActorRef(DiscoveryClient discoveryClient, String serviceName, String actorName) {
        this(discoveryClient, serviceName, actorName, "defaultSender");
    }

    // Constructeur avec URL fixe (pour tests locaux ou adresses connues)
    public RestRemoteActorRef(String baseUrl, String actorName, String senderActorName) {
        this.discoveryClient = null;
        this.serviceName = null;
        this.fixedBaseUrl = baseUrl;
        this.actorName = actorName;
        this.senderActorName = senderActorName;
    }

    @Override
    public String getName() {
        return actorName;
    }

    @Override
    public void tell(Message msg) {
        String targetUrl = "";
        LoggerService.log("INFO", actorName, "REMOTE_SEND",
                "Sending " + msg.getClass().getSimpleName() + " via REST to " + serviceName);
        try {
            // 1. Détermination de l'URL de base
            if (fixedBaseUrl != null) {
                targetUrl = fixedBaseUrl;
            } else if (discoveryClient != null) {
                // Résolution dynamique via Eureka au moment de l'envoi (Résilience) [cite: 14]
                List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
                if (instances.isEmpty()) {
                    System.err.println("[SAF-REMOTE] Abandon : Le service '" + serviceName + "' est introuvable sur Eureka.");
                    return;
                }
                // Stratégie simple : on prend la première instance (Load Balancing possible ici) [cite: 21, 36]
                ServiceInstance instance = instances.get(0);
                targetUrl = "http://" + instance.getHost() + ":" + instance.getPort();
            }

            // 2. Préparation du DTO
            IncomingMessageDTO dto = new IncomingMessageDTO();
            dto.targetActor = actorName;
            dto.messageType = msg.getClass().getName();
            dto.payloadJson = mapper.writeValueAsString(msg);
            dto.senderActor = senderActorName;

            // 3. Envoi HTTP asynchrone (via REST) [cite: 34, 44]
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<IncomingMessageDTO> entity = new HttpEntity<>(dto, headers);

            String fullPath = targetUrl + "/actors/messages";
            http.postForEntity(fullPath, entity, Void.class);

        } catch (Exception e) {
            // Log de l'erreur sans faire crasher l'application appelante (Tolérance aux pannes) [cite: 20, 35]
            System.err.println("[SAF-REMOTE] Échec critique lors de l'envoi vers " + actorName + " : " + e.getMessage());
        }
    }

    @Override
    public void tell(Message msg, ActorContext ctx) {
        // Le contexte distant est géré via le champ senderActorName du DTO
        tell(msg);
    }

    @Override
    public com.saf.core.Mailbox mailbox() {
        throw new UnsupportedOperationException("Les acteurs distants n'ont pas de Mailbox locale.");
    }
}