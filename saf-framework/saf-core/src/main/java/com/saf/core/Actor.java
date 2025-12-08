package com.saf.core;

/**
 * Un acteur possède un comportement : traiter un message.
 */
public interface Actor {
    void onReceive(Message msg, ActorContext ctx) throws Exception;
}