<<<<<<< HEAD
# lesbonsplans-api
partie backend de projet lesbonsplans
=======
# LeBonPlan — API Backend

Plateforme de partage de bons plans, événements, brocantes et bonnes adresses.

## Stack technique

- Java 21 + Spring Boot 3.2
- Spring Security + JWT
- PostgreSQL + Flyway
- Déployable gratuitement sur Railway / Render + Neon.tech

## Structure du projet

```
src/main/java/com/lebonplan/
├── LeBonPlanApplication.java
├── config/
│   └── SecurityConfig.java          # Configuration Spring Security + CORS
├── controller/
│   └── AuthController.java          # POST /auth/register, /login, /refresh, /logout
├── dto/
│   ├── request/
│   │   ├── RegisterRequest.java
│   │   └── LoginRequest.java
│   └── response/
│       ├── AuthResponse.java
│       └── UserResponse.java
├── entity/
│   ├── BaseEntity.java              # id, createdAt, updatedAt
│   ├── User.java
│   ├── Post.java
│   ├── Category.java
│   ├── RefreshToken.java
│   └── Entities.java               # Image, Tag, Favorite, Comment
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── EmailAlreadyExistsException.java
│   ├── UsernameAlreadyExistsException.java
│   └── InvalidTokenException.java
├── repository/
│   ├── UserRepository.java
│   └── RefreshTokenRepository.java
├── security/
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   └── UserDetailsServiceImpl.java
└── service/
    └── AuthService.java

src/main/resources/
├── application.yml
└── db/migration/
    └── V1__init_schema.sql          # Schéma complet (tables + catégories par défaut)
```

## Lancement en local

### Prérequis
- Java 21
- Maven 3.8+
- PostgreSQL 15+

### Étapes

```bash
# 1. Créer la base de données
createdb lebonplan

# 2. Copier la configuration
cp .env.example .env
# Éditer .env avec vos valeurs

# 3. Lancer l'application
./mvnw spring-boot:run
```

L'API sera disponible sur `http://localhost:8080/api`

### Avec Docker

```bash
docker build -t lebonplan-api .
docker run -p 8080:8080 --env-file .env lebonplan-api
```

## Endpoints d'authentification

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/auth/register` | Inscription |
| POST | `/api/auth/login` | Connexion |
| POST | `/api/auth/refresh` | Renouveler le token |
| POST | `/api/auth/logout` | Déconnexion |

### Exemples

**Inscription**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.fr","password":"motdepasse123","username":"testuser","city":"Paris"}'
```

**Connexion**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.fr","password":"motdepasse123"}'
```

**Appel authentifié**
```bash
curl http://localhost:8080/api/posts \
  -H "Authorization: Bearer <votre_access_token>"
```

## Déploiement gratuit (V1)

| Service | Usage | Free tier |
|---------|-------|-----------|
| [Railway](https://railway.app) | Backend Spring Boot | 500h/mois |
| [Neon.tech](https://neon.tech) | PostgreSQL | 0.5 GB |
| [Vercel](https://vercel.com) | Frontend React | Illimité |
| [Cloudinary](https://cloudinary.com) | Images | 25 GB |

### Déployer sur Railway

```bash
# Installer Railway CLI
npm install -g @railway/cli
railway login
railway init
railway up
```

Variables d'environnement à configurer dans le dashboard Railway :
- `DATABASE_URL` (fourni par Neon.tech)
- `JWT_SECRET` (générer avec `openssl rand -base64 64`)
- `CLOUDINARY_*`
- `CORS_ORIGINS`

## Prochaines étapes (Étape 2)

- [ ] PostRepository + PostService + PostController (CRUD annonces)
- [ ] CategoryController (liste des catégories)
- [ ] Recherche par ville et catégorie
- [ ] Upload d'images via Cloudinary
- [ ] FavoriteService
>>>>>>> 3d11af8 (first commit)
