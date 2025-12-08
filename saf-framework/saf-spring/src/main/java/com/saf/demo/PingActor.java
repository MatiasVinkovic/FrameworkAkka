package com.saf.demo;

import com.saf.core.Actor;
import com.saf.core.ActorContext;
import com.saf.core.Message;

public class PingActor implements Actor {

    @Override
    public void onReceive(Message msg, ActorContext ctx) {
        if (msg instanceof Ping p) {
            System.out.println("[PingActor] Message reçu : " + p.getText());
            System.out.println("  self  = " + ctx.self().getName());
            System.out.println("  sender = " + (ctx.sender() != null ? ctx.sender().getName() : "null"));
        } else {
            System.out.println("[PingActor] Message inconnu : " + msg.getClass().getSimpleName());
        }
    }
}