DROP TABLE IF EXISTS auditoria CASCADE;
DROP TABLE IF EXISTS movimiento_detalle CASCADE;
DROP TABLE IF EXISTS movimiento CASCADE;
DROP TABLE IF EXISTS inventario_bodega CASCADE;
DROP TABLE IF EXISTS producto CASCADE;
DROP TABLE IF EXISTS bodega CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;

DROP TYPE IF EXISTS rol_usuario;
DROP TYPE IF EXISTS tipo_movimiento;
DROP TYPE IF EXISTS tipo_operacion_auditoria;

CREATE TYPE rol_usuario AS ENUM ('SUPERADMIN', 'ADMIN', 'EMPLEADO');
CREATE TYPE tipo_movimiento AS ENUM ('ENTRADA', 'SALIDA', 'TRANSFERENCIA');
CREATE TYPE tipo_operacion_auditoria AS ENUM ('INSERT', 'UPDATE', 'DELETE');

CREATE TABLE usuario (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    rol         rol_usuario  NOT NULL DEFAULT 'EMPLEADO',
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bodega (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    ubicacion   VARCHAR(150) NOT NULL,
    capacidad   INTEGER      NOT NULL CHECK (capacidad >= 0),
    encargado   VARCHAR(100) NOT NULL
);

CREATE TABLE producto (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(150)   NOT NULL,
    categoria   VARCHAR(100)   NOT NULL,
    precio      NUMERIC(12,2)  NOT NULL CHECK (precio >= 0)
);

CREATE TABLE inventario_bodega (
    id           BIGSERIAL PRIMARY KEY,
    bodega_id    BIGINT NOT NULL REFERENCES bodega(id)   ON DELETE CASCADE,
    producto_id  BIGINT NOT NULL REFERENCES producto(id) ON DELETE CASCADE,
    stock        INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    CONSTRAINT uk_inventario_bodega_producto UNIQUE (bodega_id, producto_id)
);

CREATE INDEX idx_inventario_stock_bajo ON inventario_bodega (stock);

CREATE TABLE movimiento (
    id                  BIGSERIAL PRIMARY KEY,
    fecha               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo                tipo_movimiento NOT NULL,
    usuario_id          BIGINT NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    bodega_origen_id    BIGINT REFERENCES bodega(id) ON DELETE RESTRICT,
    bodega_destino_id   BIGINT REFERENCES bodega(id) ON DELETE RESTRICT,
    CONSTRAINT chk_movimiento_bodegas CHECK (
        (tipo = 'ENTRADA'       AND bodega_origen_id IS NULL     AND bodega_destino_id IS NOT NULL) OR
        (tipo = 'SALIDA'        AND bodega_origen_id IS NOT NULL AND bodega_destino_id IS NULL) OR
        (tipo = 'TRANSFERENCIA' AND bodega_origen_id IS NOT NULL AND bodega_destino_id IS NOT NULL
                                 AND bodega_origen_id <> bodega_destino_id)
    )
);

CREATE INDEX idx_movimiento_fecha ON movimiento (fecha);
CREATE INDEX idx_movimiento_usuario ON movimiento (usuario_id);

CREATE TABLE movimiento_detalle (
    id             BIGSERIAL PRIMARY KEY,
    movimiento_id  BIGINT NOT NULL REFERENCES movimiento(id) ON DELETE CASCADE,
    producto_id    BIGINT NOT NULL REFERENCES producto(id)   ON DELETE RESTRICT,
    cantidad       INTEGER NOT NULL CHECK (cantidad > 0)
);

CREATE INDEX idx_detalle_movimiento ON movimiento_detalle (movimiento_id);
CREATE INDEX idx_detalle_producto ON movimiento_detalle (producto_id);

CREATE TABLE auditoria (
    id                  BIGSERIAL PRIMARY KEY,
    tipo_operacion      tipo_operacion_auditoria NOT NULL,
    fecha_hora          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id          BIGINT NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    entidad_afectada    VARCHAR(100) NOT NULL,
    entidad_id          BIGINT,
    valores_anteriores  TEXT,
    valores_nuevos      TEXT
);

CREATE INDEX idx_auditoria_usuario ON auditoria (usuario_id);
CREATE INDEX idx_auditoria_tipo ON auditoria (tipo_operacion);
CREATE INDEX idx_auditoria_entidad ON auditoria (entidad_afectada);
CREATE INDEX idx_auditoria_fecha ON auditoria (fecha_hora);
