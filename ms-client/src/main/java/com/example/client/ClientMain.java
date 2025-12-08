package com.example.client;

import com.example.messages.CreateTicket;
import com.saf.core.ActorRef;
import com.saf.core.ActorSystem;
import com.saf.spring.SAF;

public class ClientMain {

    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = SAF.start("client-service");

        ActorRef jiraInbox = system.remoteActor("http://localhost:8091", "jiraInbox");

        jiraInbox.tell(new CreateTicket("Bug critique : l'écran est tout noir"));

        System.out.println("✅ [CLIENT] Ticket envoyé au service Jira.");

        Thread.sleep(2000);
    }
}