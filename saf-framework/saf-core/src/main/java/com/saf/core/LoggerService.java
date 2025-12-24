package com.saf.core;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerService {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_FILE = "saf.log"; // Nom du fichier de log
    private static PrintWriter fileWriter;

    static {
        try {
            // Initialisation du PrintWriter pour écrire dans le fichier
            fileWriter = new PrintWriter(new FileWriter(LOG_FILE, true), true);
        } catch (IOException e) {
            System.err.println("Impossible d'ouvrir le fichier de log : " + e.getMessage());
        }
    }

    public static void log(String level, String actorName, String action, String details) {
        String timestamp = dtf.format(LocalDateTime.now());
        String logMessage = String.format("[%s] [%s] [%s] %s -> %s%n",
                timestamp, level, actorName, action.toUpperCase(), details);

        // Écriture dans la console
        System.out.printf(logMessage);

        // Écriture dans le fichier
        if (fileWriter != null) {
            fileWriter.printf(logMessage);
        }
    }

    // Méthode pour fermer le fichier à la fin du programme (optionnel, mais recommandé)
    public static void close() {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }
}
