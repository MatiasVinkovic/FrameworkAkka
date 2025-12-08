package com.saf.spring;

/**
 * Format JSON des messages échangés entre microservices.
 */
class IncomingMessageDTO {
    public String targetActor;
    public String messageType;
    public String payloadJson;
}