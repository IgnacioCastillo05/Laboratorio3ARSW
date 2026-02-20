-- Limpiar tablas si existen (en orden correcto por FK)
DROP TABLE IF EXISTS points;
DROP TABLE IF EXISTS blueprints;

-- Tabla para almacenar los blueprints (planos) de los autores.
CREATE TABLE blueprints (
    id      BIGSERIAL    PRIMARY KEY,
    author  VARCHAR(100) NOT NULL,
    name    VARCHAR(100) NOT NULL
);

-- Evita que un mismo autor tenga dos blueprints con el mismo nombre.
ALTER TABLE blueprints
    ADD CONSTRAINT uk_author_name UNIQUE (author, name);

-- ÍNDICE para búsquedas de blueprints por autor
CREATE INDEX idx_blueprint_author ON blueprints(author);

-- Tabla para almacenar los puntos de cada blueprint.
CREATE TABLE points (
    id           BIGSERIAL PRIMARY KEY,
    x            INTEGER   NOT NULL,
    y            INTEGER   NOT NULL,
    point_order  INTEGER   NOT NULL,
    blueprint_id BIGINT    NOT NULL
);

-- FOREIGN KEY con DELETE CASCADE
ALTER TABLE points
    ADD CONSTRAINT fk_points_blueprint
    FOREIGN KEY (blueprint_id)
    REFERENCES blueprints(id)
    ON DELETE CASCADE;

-- ÍNDICE para búsquedas de puntos por blueprint_id
CREATE INDEX idx_point_blueprint ON points(blueprint_id);

-- Evita datos inválidos.
ALTER TABLE points
    ADD CONSTRAINT chk_point_order_positive
    CHECK (point_order >= 0);
