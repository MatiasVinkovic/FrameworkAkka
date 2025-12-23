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
import java.util.concurrent.CompletableFuture;

/**
 * Implémentation d'ActorRef pour la communication inter-microservices[cite: 12, 19].
 * Utilise Eureka pour localiser dynamiquement les instances de services[cite: 43].
 */
public class RestRemoteActorRef implements ActorRef {
    private final DiscoveryClient discoveryClient;
    private final String serviceName;
    private final String actorName;
    private final String senderActorName;
    private String fixedBaseUrl;

    private final RestTemplate http = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public RestRemoteActorRef(DiscoveryClient discoveryClient, String serviceName, String actorName, String senderActorName) {
        this.discoveryClient = discoveryClient;
        this.serviceName = serviceName;
        this.actorName = actorName;
        this.senderActorName = senderActorName;
    }

    public RestRemoteActorRef(DiscoveryClient discoveryClient, String serviceName, String actorName) {
        this(discoveryClient, serviceName, actorName, "defaultSender");
    }

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

    /**
     * Résout l'URL cible via le DiscoveryClient (Eureka) ou une URL fixe[cite: 43].
     */
    private String resolveTargetUrl() {
        if (fixedBaseUrl != null) return fixedBaseUrl;

        if (discoveryClient != null) {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            if (!instances.isEmpty()) {
                // Stratégie de sélection de la première instance disponible [cite: 21]
                ServiceInstance instance = instances.get(0);
                return "http://" + instance.getHost() + ":" + instance.getPort();
            }
        }
        return null;
    }

    /**
     * Envoi asynchrone (Fire-and-Forget).
     */
    @Override
    public void tell(Message msg) {
        LoggerService.log("INFO", actorName, "REMOTE_SEND",
                "Sending " + msg.getClass().getSimpleName() + " via REST to " + serviceName);
        try {
            String baseUrl = resolveTargetUrl();
            if (baseUrl == null) {
                System.err.println("[SAF-REMOTE] Service '" + serviceName + "' introuvable sur Eureka.");
                return;
            }

            IncomingMessageDTO dto = createDTO(msg);
            HttpEntity<IncomingMessageDTO> entity = createHttpEntity(dto);

            // Appel HTTP POST sans attendre de corps de réponse [cite: 42, 44]
            http.postForEntity(baseUrl + "/actors/messages", entity, Void.class);

        } catch (Exception e) {
            System.err.println("[SAF-REMOTE] Échec de l'envoi asynchrone : " + e.getMessage());
        }
    }



    /**
     * Envoi synchrone (Request-Response).
     * Attend une réponse du microservice distant.
     */
    @Override
    public <T> CompletableFuture<T> ask(Object message, Class<T> responseType) {
        // Exécution asynchrone de la requête réseau pour ne pas bloquer le thread appelant
        return CompletableFuture.supplyAsync(() -> {
            try {
                String baseUrl = resolveTargetUrl();
                if (baseUrl == null) throw new RuntimeException("Service " + serviceName + " introuvable");

                IncomingMessageDTO dto = createDTO((Message) message);
                HttpEntity<IncomingMessageDTO> entity = createHttpEntity(dto);

                // Appel HTTP POST vers l'endpoint synchrone dédié [cite: 42]
                return http.postForObject(baseUrl + "/actors/messages/ask", entity, responseType);

            } catch (Exception e) {
                throw new RuntimeException("Échec de la communication synchrone (ask) : " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void tell(Message msg, ActorContext ctx) {
        // On extrait le nom de l'acteur actuel depuis le contexte
        String dynamicSender = (ctx != null && ctx.self() != null)
                ? ctx.self().getName()
                : this.senderActorName;

        try {
            String baseUrl = resolveTargetUrl();
            if (baseUrl == null) return;

            // On crée le DTO en passant explicitement le sender dynamique
            IncomingMessageDTO dto = createDTO(msg);
            dto.senderActor = dynamicSender; // ON FORCE LE SENDER DU CONTEXTE ICI

            HttpEntity<IncomingMessageDTO> entity = createHttpEntity(dto);
            http.postForEntity(baseUrl + "/actors/messages", entity, Void.class);
        } catch (Exception e) {
            System.err.println("[SAF-REMOTE] Erreur : " + e.getMessage());
        }
    }

    @Override
    public void tell(Message msg, ActorRef sender) {
        // 1. On récupère le nom du sender s'il existe, sinon on prend le nom par défaut
        String senderName = (sender != null) ? sender.getName() : this.senderActorName;

        LoggerService.log("INFO", actorName, "REMOTE_SEND",
                "Sending " + msg.getClass().getSimpleName() + " with sender " + senderName);

        try {
            String baseUrl = resolveTargetUrl();
            if (baseUrl == null) return;

            // 2. On crée le DTO
            IncomingMessageDTO dto = createDTO(msg);

            // 3. ON FORCE LE SENDER DYNAMIQUE ICI
            dto.senderActor = senderName;

            HttpEntity<IncomingMessageDTO> entity = createHttpEntity(dto);
            http.postForEntity(baseUrl + "/actors/messages", entity, Void.class);

        } catch (Exception e) {
            System.err.println("[SAF-REMOTE] Erreur lors de l'envoi : " + e.getMessage());
        }
    }

    private IncomingMessageDTO createDTO(Message msg) throws Exception {
        IncomingMessageDTO dto = new IncomingMessageDTO();
        dto.targetActor = actorName;
        dto.messageType = msg.getClass().getName();
        dto.payloadJson = mapper.writeValueAsString(msg);
        dto.senderActor = senderActorName;
        return dto;
    }

    private HttpEntity<IncomingMessageDTO> createHttpEntity(IncomingMessageDTO dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(dto, headers);
    }

    @Override
    public com.saf.core.Mailbox mailbox() {
        throw new UnsupportedOperationException("Les acteurs distants n'ont pas de Mailbox locale.");
    }
}