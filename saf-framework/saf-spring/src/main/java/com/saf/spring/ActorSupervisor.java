package com.saf.spring;

import com.saf.core.ActorSystem;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Superviseur : toutes les 100 ms, on traite les messages en attente
 * dans les mailboxes de tous les acteurs.
 */
@Component
public class ActorSupervisor {

    @Scheduled(fixedDelay = 100)
    public void tick() {
        ActorSystem system = SAF.getActorSystem();
        if (system != null) {
            system.processOneCycle();
        }
    }
}