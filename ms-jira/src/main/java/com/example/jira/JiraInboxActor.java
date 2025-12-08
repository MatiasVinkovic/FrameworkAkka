package com.example.jira;

import com.example.messages.CreateTicket;
import com.saf.core.Actor;
import com.saf.core.ActorContext;
import com.saf.core.Message;

public class JiraInboxActor implements Actor {

    @Override
    public void onReceive(Message msg, ActorContext ctx) {
        System.out.println("[JIRA] message reçu : " + msg.getClass().getName());

        if (msg instanceof CreateTicket t) {
            System.out.println("  Nouveau ticket : " + t.getDescription());
            String senderName = (ctx.sender() != null) ? ctx.sender().getName() : "(inconnu)";
            System.out.println("  Expéditeur     : " + senderName);
        }
    }
}