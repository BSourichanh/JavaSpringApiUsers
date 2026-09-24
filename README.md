# 👤 JavaSpringUsers — Microservice de Gestion des Utilisateurs

> Microservice d'authentification et de gestion des utilisateurs développé avec **Spring Boot 3.3.4**, **Java 21**, **Spring Security 6**, **JJWT**, **Spring Data JPA** et base de données **H2 in-memory**.

---

## 🎯 Fonctionnalités

- **Gestion des comptes** : Inscription avec mot de passe haché de manière sécurisée en **BCrypt**.
- **Authentification Stateless** : Émission de jetons cryptographiques **JWT (HMAC-SHA256)** sur `POST /auth/login`.
- **Contrôle d'accès par rôles (RBAC)** :
  - `ROLE_USER` : Accès aux endpoints de base et à son propre profil.
  - `ROLE_ADMIN` : Droit exclusif de lister tous les utilisateurs et de supprimer des comptes (`@PreAuthorize`).
- **Validation inter-services** : Endpoint technique `GET /users/{id}/valid` permettant à l'API [Square Games](https://github.com/BSourichanh/JavaSpring.git) de vérifier l'existence des joueurs.

---

## 🛠️ Stack Technique

- **Java** : 21 (Records, pattern matching)
- **Framework** : Spring Boot 3.3.4 (Spring Web, Spring Security, Spring Data JPA, Spring Validation)
- **Sécurité & Cryptographie** : BCrypt, JJWT (`io.jsonwebtoken:jjwt-api:0.12.6`)
- **Base de Données** : H2 Database (in-memory, console accessible sur `/h2-console`)
- **Port d'écoute** : `8081`

---

## 🌐 Endpoints REST

| Méthode | URL | Accès / Rôle | Description |
|---|---|---|---|
| `POST` | `/users` | Public | Inscription d'un nouvel utilisateur |
| `POST` | `/auth/login` | Public | Connexion (username + password) ➔ Jeton JWT |
| `GET` | `/users/{id}` | `ADMIN` ou Propriétaire | Consultation d'un profil par ID |
| `GET` | `/users` | `ADMIN` | Liste de tous les utilisateurs |
| `DELETE` | `/users/{id}` | `ADMIN` | Suppression d'un compte utilisateur |
| `GET` | `/users/{id}/valid` | Public (Technique) | Validation d'existence (`true` / `404 false`) |

---

## 🚀 Démarrage & Tests

```bash
# Démarrage du microservice (port 8081)
./mvnw spring-boot:run

# Lancer la suite de tests automatisés (9 tests d'intégration)
./mvnw clean test
```
