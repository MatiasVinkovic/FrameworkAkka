package com.saf.core;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Boîte aux lettres d'un acteur local.
 */
class Mailbox {

    private final Queue<MessageEnvelope> queue = new LinkedList<>();

    void enqueue(MessageEnvelope env) {
        queue.add(env);
    }

    MessageEnvelope dequeue() {
        return queue.poll();
    }

    boolean isEmpty() {
        return queue.isEmpty();
    }
}