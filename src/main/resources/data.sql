-- ==========================================================
-- Datos de prueba para LogiTrack
-- Contraseñas ya cifradas con BCrypt:
--   superadmin -> superadmin123  (unico usuario SUPERADMIN, nace por seed, no por API)
--   admin      -> admin123
--   jperez     -> empleado123
-- ==========================================================

INSERT INTO usuario (username, password, email, rol, activo) VALUES
('superadmin', '$2b$10$0W/cZVLxlcjHCO8uQ9JBdOQVaZoZl1KT3FhFFfwRBL8lpawe0ct2C', 'superadmin@logitrack.com', 'SUPERADMIN', true),
('admin',  '$2b$10$qQyp8JwcSeuSJ2QvXAtvp./0ue.HE1bd.PQHemQc/lgAm8zK6M9c.', 'admin@logitrack.com',  'ADMIN',    true),
('jperez', '$2b$10$f.BSenCbRViwmSNylcXzQOUkyn.llXFHJ8q26pMlvQE6f39yVdaoK', 'jperez@logitrack.com', 'EMPLEADO', true);

INSERT INTO bodega (nombre, ubicacion, capacidad, encargado) VALUES
('Bodega Central',   'Bogotá - Zona Industrial Puente Aranda', 5000, 'Carlos Gómez'),
('Bodega Norte',     'Medellín - Guayabal',                    3000, 'Laura Restrepo'),
('Bodega Sur',       'Cali - Yumbo',                           2500, 'Andrés Torres');

INSERT INTO producto (nombre, categoria, precio) VALUES
('Laptop Lenovo ThinkPad E14',   'Tecnología',   3200000.00),
('Mouse inalámbrico Logitech',   'Tecnología',    65000.00),
('Silla ergonómica oficina',     'Mobiliario',   450000.00),
('Escritorio en L',              'Mobiliario',   680000.00),
('Resma papel carta x500',       'Papelería',      18000.00),
('Impresora multifuncional HP',  'Tecnología',   890000.00);

INSERT INTO inventario_bodega (bodega_id, producto_id, stock) VALUES
(1, 1, 25),
(1, 2, 120),
(1, 3, 8),
(2, 4, 15),
(2, 5, 300),
(3, 6, 5),
(3, 2, 40);

-- Movimiento de ejemplo: ENTRADA a Bodega Central realizada por el admin (usuario_id=2)
INSERT INTO movimiento (fecha, tipo, usuario_id, bodega_origen_id, bodega_destino_id) VALUES
(CURRENT_TIMESTAMP, 'ENTRADA', 2, NULL, 1);

INSERT INTO movimiento_detalle (movimiento_id, producto_id, cantidad) VALUES
(1, 1, 10),
(1, 2, 50);
