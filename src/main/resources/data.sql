-- ==========================================================
-- Datos de prueba para LogiTrack
-- Contraseñas ya cifradas con BCrypt:
--   superadmin -> superadmin123  (unico usuario SUPERADMIN, nace por seed, no por API)
--   admin      -> admin123
--   jperez     -> empleado123
--   agente     -> agente123      (rol AGENTE, usado por el servidor MCP / flujo n8n)
-- ==========================================================

INSERT INTO usuario (username, password, email, rol, activo) VALUES
('superadmin', '$2b$10$0W/cZVLxlcjHCO8uQ9JBdOQVaZoZl1KT3FhFFfwRBL8lpawe0ct2C', 'superadmin@logitrack.com', 'SUPERADMIN', true),
('admin',  '$2b$10$qQyp8JwcSeuSJ2QvXAtvp./0ue.HE1bd.PQHemQc/lgAm8zK6M9c.', 'admin@logitrack.com',  'ADMIN',    true),
('jperez', '$2b$10$f.BSenCbRViwmSNylcXzQOUkyn.llXFHJ8q26pMlvQE6f39yVdaoK', 'jperez@logitrack.com', 'EMPLEADO', true),
('agente', '$2a$10$3/WwbWF1.jkQwAjBBOLfu.Xj3PKpr9mm4ds0Hd3ypTwtCJYk4gNei', 'agente@logitrack.com', 'AGENTE',   true);

INSERT INTO bodega (nombre, ubicacion, capacidad, encargado) VALUES
('Bodega Central',   'Bogotá - Zona Industrial Puente Aranda', 5000, 'Carlos Gómez'),
('Bodega Norte',     'Medellín - Guayabal',                    3000, 'Laura Restrepo'),
('Bodega Sur',       'Cali - Yumbo',                           2500, 'Andrés Torres'),
('Bodega Este',      'Bucaramanga - Chimitá',                    50, 'Marta Higuera');

-- LogiTrack IQ: proveedores (diasEntrega alimenta el punto de reorden)
INSERT INTO proveedor (nombre, contacto, dias_entrega) VALUES
('Distribuidora Tecnológica SAS', 'ventas@distectec.com',      7),
('Muebles y Oficina Ltda',        'contacto@mueblesoficina.com', 15),
('Papelería Central',             'pedidos@papelcentral.com',  3);

-- proveedor_principal_id: Laptop/Mouse/Impresora -> proveedor 1 (tecnología),
-- Silla -> proveedor 2 (muebles). Escritorio, Resma y Teclado quedan SIN
-- proveedor principal a propósito: no pueden aparecer como "en riesgo".
INSERT INTO producto (nombre, categoria, precio, proveedor_principal_id) VALUES
('Laptop Lenovo ThinkPad E14',   'Tecnología',   3200000.00, 1),
('Mouse inalámbrico Logitech',   'Tecnología',    65000.00, 1),
('Silla ergonómica oficina',     'Mobiliario',   450000.00, 2),
('Escritorio en L',              'Mobiliario',   680000.00, NULL),
('Resma papel carta x500',       'Papelería',      18000.00, NULL),
('Impresora multifuncional HP',  'Tecnología',   890000.00, 1),
('Teclado mecánico Redragon',    'Tecnología',   180000.00, NULL);

-- Producto 7 (Teclado) queda sin fila en inventario_bodega a propósito:
-- stock total = 0 => cuenta para el indicador "productos en quiebre".
INSERT INTO inventario_bodega (bodega_id, producto_id, stock) VALUES
(1, 1, 25),
(1, 2, 120),
(1, 3, 8),
(2, 4, 15),
(2, 5, 300),
(3, 6, 5),
(3, 2, 40),
(4, 2, 48); -- Bodega Este: 48/50 = 96% => bodega crítica (>= 90%)

-- Movimiento de ejemplo: ENTRADA a Bodega Central realizada por el admin (usuario_id=2)
INSERT INTO movimiento (fecha, tipo, usuario_id, bodega_origen_id, bodega_destino_id) VALUES
(CURRENT_TIMESTAMP, 'ENTRADA', 2, NULL, 1);

INSERT INTO movimiento_detalle (movimiento_id, producto_id, cantidad) VALUES
(1, 1, 10),
(1, 2, 50);

-- ==========================================================
-- LogiTrack IQ: movimientos "de ayer" (para el indicador movimientosAyer:
-- entrada=2, salida=3, transferencia=1, igual al ejemplo del enunciado).
-- Se usan fechas relativas (CURRENT_TIMESTAMP - INTERVAL) para que el
-- seed siga siendo válido sin importar cuándo se levante el backend.
-- Nota: CURRENT_DATE/CURRENT_TIMESTAMP dependen de la zona horaria del
-- servidor de PostgreSQL, no del backend; el cálculo de "ayer" y "últimos
-- 30 días" que SÍ se califica vive en el servicio Java y usa
-- ZoneId.of("America/Bogota") explícitamente (ver RiesgoService/KpiService),
-- así que este seed es solo una aproximación razonable para la demo.
-- ==========================================================
INSERT INTO movimiento (fecha, tipo, usuario_id, bodega_origen_id, bodega_destino_id) VALUES
((CURRENT_DATE - 1) + TIME '08:00:00', 'ENTRADA', 3, NULL, 1),
((CURRENT_DATE - 1) + TIME '08:30:00', 'ENTRADA', 3, NULL, 2),
((CURRENT_DATE - 1) + TIME '09:00:00', 'SALIDA',  3, 1, NULL),
((CURRENT_DATE - 1) + TIME '09:15:00', 'SALIDA',  3, 2, NULL),
((CURRENT_DATE - 1) + TIME '09:30:00', 'SALIDA',  3, 3, NULL),
((CURRENT_DATE - 1) + TIME '10:00:00', 'TRANSFERENCIA', 3, 1, 3);

INSERT INTO movimiento_detalle (movimiento_id, producto_id, cantidad) VALUES
(2, 1, 10),
(3, 5, 50),
(4, 2, 5),
(5, 4, 2),
(6, 6, 1),
(7, 2, 10);

-- ==========================================================
-- LogiTrack IQ: historial de SALIDA del producto 3 (Silla ergonómica) en
-- los últimos 30 días para que tenga consumo real y quede EN RIESGO:
-- 6 salidas x 5 unidades = 30 unidades / 30 días = consumoDiarioPromedio 1.0
-- puntoReorden = 1.0 * diasEntrega(proveedor 2 = 15) * 1.5 = 22.5
-- stockTotal (bodega 1) = 8  =>  8 < 22.5  =>  EN_RIESGO, diasCobertura = 8.0
-- ==========================================================
INSERT INTO movimiento (fecha, tipo, usuario_id, bodega_origen_id, bodega_destino_id) VALUES
(CURRENT_TIMESTAMP - INTERVAL '2 days',  'SALIDA', 3, 1, NULL),
(CURRENT_TIMESTAMP - INTERVAL '6 days',  'SALIDA', 3, 1, NULL),
(CURRENT_TIMESTAMP - INTERVAL '10 days', 'SALIDA', 3, 1, NULL),
(CURRENT_TIMESTAMP - INTERVAL '14 days', 'SALIDA', 3, 1, NULL),
(CURRENT_TIMESTAMP - INTERVAL '18 days', 'SALIDA', 3, 1, NULL),
(CURRENT_TIMESTAMP - INTERVAL '22 days', 'SALIDA', 3, 1, NULL);

INSERT INTO movimiento_detalle (movimiento_id, producto_id, cantidad) VALUES
(8, 3, 5),
(9, 3, 5),
(10, 3, 5),
(11, 3, 5),
(12, 3, 5),
(13, 3, 5);
