-- ============================================================
-- V1__init_schema.sql
-- Schéma initial LeBonPlan
-- ============================================================

-- Extension UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- USERS
-- ============================================================
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    username      VARCHAR(100) NOT NULL UNIQUE,
    avatar_url    VARCHAR(500),
    bio           TEXT,
    city          VARCHAR(100),
    latitude      DOUBLE PRECISION,
    longitude     DOUBLE PRECISION,
    role          VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email    ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_city     ON users(city);

-- ============================================================
-- CATEGORIES
-- ============================================================
CREATE TABLE categories (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(100) NOT NULL UNIQUE,
    icon        VARCHAR(50),
    description VARCHAR(255),
    parent_id   INTEGER REFERENCES categories(id) ON DELETE SET NULL,
    position    INTEGER NOT NULL DEFAULT 0,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE
);

-- Catégories de base
INSERT INTO categories (name, slug, icon, description, position) VALUES
    ('Bons plans',   'bons-plans',   '🎯', 'Réductions, promos et bons plans du quotidien', 1),
    ('Événements',   'evenements',   '🎪', 'Concerts, fêtes, expos et sorties culturelles',  2),
    ('Brocantes',    'brocantes',    '🏺', 'Vide-greniers, brocantes et marchés vintage',    3),
    ('Sorties',      'sorties',      '🍽️', 'Restaurants, bars et bonnes adresses',           4),
    ('Loisirs',      'loisirs',      '🎮', 'Sport, jeux et activités de loisirs',            5),
    ('Services',     'services',     '🔧', 'Échanges de services entre particuliers',        6);

-- Sous-catégories Événements
INSERT INTO categories (name, slug, icon, parent_id, position) VALUES
    ('Concerts',     'concerts',    '🎵', 2, 1),
    ('Expos',        'expos',       '🖼️', 2, 2),
    ('Festivals',    'festivals',   '🎡', 2, 3),
    ('Spectacles',   'spectacles',  '🎭', 2, 4);

-- ============================================================
-- POSTS (annonces / bons plans)
-- ============================================================
CREATE TABLE posts (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id  INTEGER NOT NULL REFERENCES categories(id),
    title        VARCHAR(200) NOT NULL,
    description  TEXT NOT NULL,
    address      VARCHAR(300),
    city         VARCHAR(100),
    latitude     DOUBLE PRECISION,
    longitude    DOUBLE PRECISION,
    event_date   DATE,
    event_time   TIME,
    price        DECIMAL(10, 2),
    price_label  VARCHAR(50),          -- ex: "Gratuit", "À partir de 5€"
    source_url   VARCHAR(500),         -- lien externe optionnel
    status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, EXPIRED, DELETED, PENDING
    is_featured  BOOLEAN NOT NULL DEFAULT FALSE,
    views_count  INTEGER NOT NULL DEFAULT 0,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    expires_at   TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_posts_user_id     ON posts(user_id);
CREATE INDEX idx_posts_category_id ON posts(category_id);
CREATE INDEX idx_posts_city        ON posts(city);
CREATE INDEX idx_posts_status      ON posts(status);
CREATE INDEX idx_posts_event_date  ON posts(event_date);
CREATE INDEX idx_posts_created_at  ON posts(created_at DESC);

-- ============================================================
-- IMAGES
-- ============================================================
CREATE TABLE images (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id    UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    url        VARCHAR(500) NOT NULL,
    public_id  VARCHAR(255),           -- identifiant Cloudinary
    position   INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_images_post_id ON images(post_id);

-- ============================================================
-- TAGS
-- ============================================================
CREATE TABLE tags (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    slug VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE post_tags (
    post_id UUID    NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    tag_id  INTEGER NOT NULL REFERENCES tags(id)  ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
);

-- ============================================================
-- FAVORITES
-- ============================================================
CREATE TABLE favorites (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id    UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, post_id)
);

CREATE INDEX idx_favorites_user_id ON favorites(user_id);
CREATE INDEX idx_favorites_post_id ON favorites(post_id);

-- ============================================================
-- COMMENTS
-- ============================================================
CREATE TABLE comments (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id    UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content    TEXT NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comments_post_id ON comments(post_id);

-- ============================================================
-- REFRESH TOKENS
-- ============================================================
CREATE TABLE refresh_tokens (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_token   ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
