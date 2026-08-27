-- Intencionalmente (casi) vacio: las pruebas siembran sus propios datos
-- via repositorios (ver AbstractLogiTrackIqTest). Este archivo existe solo
-- para que Spring Boot use el data.sql de test (src/test/resources) en vez
-- del de src/main/resources, que usa sintaxis especifica de PostgreSQL
-- (INTERVAL, etc.) incompatible con H2. Necesita al menos una sentencia
-- real (un script vacio hace fallar a ScriptUtils).
DELETE FROM usuario WHERE 1 = 0;
