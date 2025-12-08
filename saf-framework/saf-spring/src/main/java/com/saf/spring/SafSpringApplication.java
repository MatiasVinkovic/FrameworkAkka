package com.saf.spring;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application Spring interne du framework.
 * On la démarre via SAF.start().
 */
@SpringBootApplication
@EnableScheduling
public class SafSpringApplication {
    // pas de main ici : Spring est démarré depuis SAF.start()
}