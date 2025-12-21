package com.saf.core;

import java.util.LinkedList;
import java.util.Queue;

public class Mailbox {
    private final Queue<MessageEnvelope> queue = new LinkedList<>();

    public void enqueue(MessageEnvelope env) {  // Assure-toi que cette méthode est publique
        queue.add(env);
    }

    public MessageEnvelope dequeue() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
