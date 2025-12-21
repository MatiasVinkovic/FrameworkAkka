package com.saf.spring;

public class IncomingMessageDTO {
    public String targetActor;
    public String messageType;
    public String payloadJson;
    public String senderActor;  //pour renvoie de confirmation de réception
}
