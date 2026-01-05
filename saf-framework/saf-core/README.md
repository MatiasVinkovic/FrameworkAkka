# 📦 SAF-Core - Résumé des Classes

Ce document présente un résumé détaillé de toutes les classes du package `com.saf.core`, qui constitue le cœur du Simple Actor Framework (SAF).

---

## 🎭 Classes Principales

### 1. **Actor** (Interface)
**Fichier:** `Actor.java`

**Description:** Interface fondamentale qui définit le comportement d'un acteur dans le framework.

**Responsabilité:**
- Définit la méthode `onReceive(Message msg, ActorContext ctx)` que tout acteur doit implémenter pour traiter les messages reçus
- Permet la gestion des exceptions lors du traitement des messages

**Usage:** Toute classe acteur doit implémenter cette interface pour définir son comportement de traitement de messages.

---

### 2. **ActorRef** (Interface)
**Fichier:** `ActorRef.java`

**Description:** Représente une référence vers un acteur, permettant l'envoi de messages sans exposer directement l'instance de l'acteur.

**Responsabilités:**
- **Communication synchrone:** 
  - `tell(Message msg)` - Envoi simple sans spécifier l'expéditeur
  - `tell(Message msg, ActorContext ctx)` - Envoi avec contexte
  - `tell(Message msg, ActorRef sender)` - Envoi avec expéditeur explicite
- **Communication asynchrone:**
  - `ask(Object message, Class<T> responseType)` - Envoi avec attente de réponse via `CompletableFuture`
- **Accès aux ressources:**
  - `getName()` - Récupère le nom de l'acteur
  - `mailbox()` - Accès à la boîte aux lettres de l'acteur

**Pattern:** Proxy/Reference - Encapsule l'accès à un acteur en cachant les détails d'implémentation.

---

### 3. **LocalActorRef** (Classe)
**Fichier:** `LocalActorRef.java`

**Description:** Implémentation concrète de `ActorRef` pour les acteurs locaux (dans la même JVM).

**Responsabilités:**
- Gère l'instance de l'acteur et sa boîte aux lettres
- Implémente les méthodes `tell()` en enfilant les messages dans la mailbox
- Implémente `ask()` avec un callback temporaire pour capturer la réponse
- Gère le cycle de vie de l'acteur:
  - `restart()` - Réinitialise l'instance de l'acteur en cas de crash
  - `isBlocked()` / `setBlocked()` - Gestion du blocage d'un acteur
- Conserve la référence vers la classe de l'acteur pour pouvoir le recréer

**État:**
- `name` - Nom unique de l'acteur
- `actor` - Instance de l'acteur
- `mailbox` - File d'attente des messages
- `actorClass` - Classe de l'acteur (pour supervision/restart)
- `blocked` - État de blocage

---

### 4. **NullActorRef** (Classe)
**Fichier:** `NullActorRef.java`

**Description:** Implémentation du pattern Null Object pour `ActorRef`.

**Responsabilités:**
- Fournit une référence "vide" qui ne fait rien lors de l'envoi de messages
- Évite les vérifications null dans le code
- Lance `UnsupportedOperationException` pour `ask()` et `mailbox()`

**Pattern:** Null Object - Remplace les références null par un objet qui ne fait rien.

---

### 5. **ActorContext** (Interface)
**Fichier:** `ActorContext.java`

**Description:** Contexte fourni à un acteur lors du traitement d'un message.

**Responsabilités:**
- `self()` - Retourne la référence de l'acteur courant
- `sender()` / `getSender()` - Retourne la référence de l'expéditeur du message
- `reply(Message msg)` - Permet de répondre facilement à l'expéditeur

**Usage:** Passé en paramètre à `onReceive()` pour donner à l'acteur les informations contextuelles nécessaires.

---

### 6. **SimpleActorContext** (Classe)
**Fichier:** `SimpleActorContext.java`

**Description:** Implémentation concrète minimale de `ActorContext`.

**Responsabilités:**
- Stocke les références `self` et `sender`
- Implémente `reply()` en utilisant `sender.tell(msg, this)` pour permettre le chaînage de messages

**Pattern:** Value Object - Objet simple portant des valeurs sans logique métier complexe.

---

### 7. **ActorSystem** (Classe)
**Fichier:** `ActorSystem.java`

**Description:** Point d'entrée principal du framework. Gère le cycle de vie et la supervision de tous les acteurs.

**Responsabilités:**
- **Création d'acteurs:**
  - `createActor(Class<? extends Actor> actorClass, String actorName)` - Crée et enregistre un nouvel acteur
- **Recherche d'acteurs:**
  - `findLocal(String actorName)` - Trouve un acteur local par son nom
- **Supervision:**
  - `processOneCycle()` - Traite un cycle de messages pour tous les acteurs:
    - Parcourt tous les acteurs
    - Traite les messages en attente dans chaque mailbox
    - Gère les exceptions avec redémarrage automatique (self-healing)
    - Respecte l'état de blocage des acteurs
- **Gestion du cycle de vie:**
  - `blockActor(String name)` - Bloque un acteur (maintien des messages en attente)
  - `unblockActor(String name)` - Débloque un acteur
  - `killActor(String name)` - Détruit un acteur
- **Remote actors:** Non supporté dans saf-core (voir saf-spring)

**État:**
- `name` - Nom du système d'acteurs
- `actors` - Map thread-safe (`ConcurrentHashMap`) des acteurs enregistrés

**Pattern:** Registry/Container - Centralise la gestion des acteurs et orchestre leur exécution.

---

## 📬 Gestion des Messages

### 8. **Message** (Interface)
**Fichier:** `Message.java`

**Description:** Interface marqueur (marker interface) représentant un message échangé entre acteurs.

**Responsabilité:**
- Sert de type de base pour tous les messages
- Permet le typage fort des messages dans le framework

**Usage:** Toutes les classes de messages doivent implémenter cette interface vide.

---

### 9. **MessageEnvelope** (Classe)
**Fichier:** `MessageEnvelope.java`

**Description:** Enveloppe qui encapsule un message et son expéditeur.

**Responsabilités:**
- Transporte le message et la référence de l'expéditeur ensemble
- Permet de conserver l'information de provenance lors du routage des messages

**Champs:**
- `message` - Le message à transmettre
- `sender` - Référence de l'acteur expéditeur (peut être `null`)

**Pattern:** Data Transfer Object (DTO) - Simple conteneur de données.

---

### 10. **Mailbox** (Classe)
**Fichier:** `Mailbox.java`

**Description:** File d'attente (queue) des messages en attente de traitement pour un acteur.

**Responsabilités:**
- `enqueue(MessageEnvelope env)` - Ajoute un message à la file
- `dequeue()` - Récupère et retire le prochain message
- `isEmpty()` - Vérifie si la mailbox est vide

**Implémentation:** Utilise une `LinkedList` comme structure de données sous-jacente.

**Pattern:** Queue/FIFO - Garantit l'ordre de traitement des messages.

---

## 📝 Services Utilitaires

### 11. **LoggerService** (Classe)
**Fichier:** `LoggerService.java`

**Description:** Service de logging centralisé pour le framework.

**Responsabilités:**
- `log(String level, String actorName, String action, String details)` - Enregistre un événement
- Écrit simultanément dans la console et dans un fichier (`saf.log`)
- Formate les logs avec timestamp, niveau, nom de l'acteur, action et détails
- `close()` - Ferme proprement le fichier de log

**Niveaux de log utilisés:**
- `INFO` - Événements normaux (création, traitement de message, restart)
- `WARN` - Avertissements (blocage, supervision)
- `ERROR` - Erreurs lors du traitement (crash)
- `FATAL` - Erreurs critiques (échec de restart)

**Pattern:** Singleton (méthodes statiques) - Un seul service de logging pour toute l'application.

---

## 🏗️ Architecture Globale

Le package `saf-core` suit une architecture en couches:

1. **Couche Abstraction:** `Actor`, `ActorRef`, `ActorContext`, `Message`
2. **Couche Implémentation:** `LocalActorRef`, `SimpleActorContext`, `NullActorRef`
3. **Couche Infrastructure:** `Mailbox`, `MessageEnvelope`
4. **Couche Orchestration:** `ActorSystem`
5. **Couche Services:** `LoggerService`

### Flux de Traitement d'un Message
1. Un acteur A envoie un message via `actorRefB.tell(message, contextA)`
2. Le message est encapsulé dans une `MessageEnvelope` avec l'expéditeur
3. L'enveloppe est ajoutée à la `Mailbox` de l'acteur B
4. L'`ActorSystem` appelle `processOneCycle()` périodiquement
5. Le système défile les messages de chaque mailbox et appelle `onReceive()`
6. En cas d'exception, le système redémarre automatiquement l'acteur (supervision)

### Caractéristiques Clés
- ✅ **Isolation:** Chaque acteur a sa propre mailbox
- ✅ **Asynchronisme:** Les messages sont traités de manière non-bloquante
- ✅ **Supervision:** Redémarrage automatique en cas de crash
- ✅ **Traçabilité:** Logging complet du cycle de vie
- ✅ **Maintenabilité:** Possibilité de bloquer/débloquer des acteurs sans perte de messages

---

## 🎯 Résumé

Le package `saf-core` fournit une implémentation minimaliste mais complète du modèle d'acteurs, avec 11 classes/interfaces qui collaborent pour offrir:
- Un système de messagerie asynchrone fiable
- Une supervision automatique avec self-healing
- Une traçabilité complète des événements
- Une base solide pour la distribution (étendue dans `saf-spring`)
