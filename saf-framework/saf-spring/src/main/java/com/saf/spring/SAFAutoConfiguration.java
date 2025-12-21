package com.saf.spring;

import com.saf.core.ActorSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SAFAutoConfiguration {

    @Bean
    public ActorSystem actorSystem() {
        System.out.println("[SAF] Création du Bean ActorSystem par le framework...");
        return new ActorSystem("SafActorSystem");
    }
}