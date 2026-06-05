# Pipeline CI — GitHub Actions

![Java CI](https://github.com/adoudi-mondher/my-recipe-api/actions/workflows/ci.yml/badge.svg)

---

## Déclenchement

La pipeline se lance automatiquement sur :
- `push` sur la branche `main`
- `pull_request` vers `main`

---

## Schéma

```
build-test  →  docker-build  →  docker-push
```

Les jobs s'enchaînent : chaque job attend la réussite du précédent (`needs`).

---

## Jobs

### 1. `build-test`

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk) ![Maven](https://img.shields.io/badge/Maven-build-blue?logo=apachemaven)

Compile et package l'application Java.

| Étape | Description |
|---|---|
| Checkout code | Récupère le code source |
| Set up Java | Installe Java 17 (Temurin) avec cache Maven |
| Package application | `mvn clean package -DskipTests` — produit le JAR |
| Upload JAR artifact | Dépose le JAR dans les artefacts GitHub Actions |

> Le JAR est partagé via les artefacts GitHub Actions pour être réutilisé par les jobs suivants sans recompiler.

---

### 2. `docker-build`

![Docker](https://img.shields.io/badge/Docker-build-2496ED?logo=docker&logoColor=white)

Vérifie que l'image Docker se construit correctement.

| Étape | Description |
|---|---|
| Checkout code | Récupère le code source (pour le Dockerfile) |
| Download JAR artifact | Récupère le JAR buildé dans `build-test` |
| Build Docker image | `docker build` — tag : `myrecipe-api:<commit-sha>` |

---

### 3. `docker-push`

![Docker Hub](https://img.shields.io/badge/Docker%20Hub-push-2496ED?logo=docker&logoColor=white)

Publie l'image sur Docker Hub.

| Étape | Description |
|---|---|
| Checkout code | Récupère le code source |
| Download JAR artifact | Récupère le JAR buildé dans `build-test` |
| Login to Docker Hub | Authentification via secrets GitHub |
| Build image | Rebuild l'image avec le tag Docker Hub |
| Push image | Pousse l'image sur Docker Hub |

---

## Secrets GitHub à configurer

Aller dans **Settings → Secrets and variables → Actions** du repo :

| Secret | Description |
|---|---|
| `DOCKERHUB_USERNAME` | Ton nom d'utilisateur Docker Hub |
| `DOCKERHUB_TOKEN` | Token d'accès Docker Hub (pas le mot de passe) |

> Générer le token sur hub.docker.com → Account Settings → Security → New Access Token

---

## Reproduire sur un autre projet Java

1. Copier le fichier `ci.yml` dans `.github/workflows/`
2. Adapter `java-version` si besoin
3. S'assurer que le projet a un `Dockerfile` et un `mvnw`
4. Configurer les deux secrets Docker Hub dans le repo GitHub