package com.example.jira;

import com.saf.core.ActorSystem;
import com.saf.spring.SAF;

public class JiraMain {

    public static void main(String[] args) {
        ActorSystem system = SAF.start("jira-service");

        system.createActor(JiraInboxActor.class, "jiraInbox");

        System.out.println("✅ Jira service running on port 8081.");
    }
}