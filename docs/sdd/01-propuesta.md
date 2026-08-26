# 01 — Propuesta

## Problema

LogiTrack ya centraliza bodegas, productos, inventario y movimientos, pero
la revisión de faltantes es manual: nadie calcula a diario qué productos
están por debajo de su punto de reorden, ni deja lista una propuesta de
compra. El riesgo se detecta tarde y la reposición depende de que alguien
revise las tablas a mano.

## Objetivo

Extender el backend de LogiTrack (sin reemplazar lo ya construido) con una
"torre de control" que:

1. calcule el stock real a partir de los movimientos (no de un campo
   estático);
2. detecte productos en riesgo según un punto de reorden basado en
   consumo real y tiempo de entrega del proveedor;
3. permita crear, aprobar y recibir una orden de compra, con la recepción
   generando automáticamente el movimiento `ENTRADA` correspondiente;
4. exponga esa información a un flujo automatizado (n8n vía MCP) que
   proponga como máximo una orden en `BORRADOR` por ejecución y publique
   un resumen diario;
5. muestre todo lo anterior en un dashboard que un `ADMIN` pueda operar.

## Alcance

- Nuevas entidades: `Proveedor`, `OrdenCompra`, `ResumenPanel`; relación
  `Producto.proveedorPrincipal`.
- Nuevo rol `AGENTE` con permisos limitados (solo lectura + crear
  borrador + publicar resumen).
- Endpoints nuevos: `/kpis`, `/productos/{id}/stock`, `/productos/riesgo`,
  `/bodegas/criticas`, `/proveedores`, `/ordenes*`, `/panel/resumen`.
- Generación de PDF de la orden (con marca de agua BORRADOR mientras esté
  en ese estado).
- Servidor MCP con exactamente 6 herramientas de solo consulta/creación
  limitada (sin aprobar/cancelar/recibir).
- Flujo n8n único "Resumen diario de inventario" (Schedule 6:00 a. m.
  America/Bogota + AI Agent + herramientas MCP).
- Skill `operacion-logitrack/SKILL.md` con las reglas operativas del
  flujo automatizado.
- Dashboard estático (HTML/CSS/JS puro) que consuma la API real.
- Pruebas automatizadas de las reglas nuevas, escritas antes de
  implementarlas (TDD), con evidencia roja→verde.

## Fuera de alcance

- No se reemplaza ni se reescribe el CRUD de bodegas/productos/movimientos
  ya existente; solo se reutiliza y se le agregan reglas de cálculo
  nuevas (stock derivado de movimientos, no del campo legado).
- No se implementa autenticación/roles nuevos más allá de `AGENTE`.
- No se migra el motor de base de datos (se mantiene PostgreSQL, ya
  configurado en el proyecto base; el enunciado menciona "MySQL" como
  término genérico para "una base relacional").
- No se automatiza la carga del `SKILL.md` dentro de n8n: se copia su
  contenido al prompt del nodo AI Agent, tal como permite el enunciado.
- No se graba el video de demostración ni se ejecuta n8n con un LLM real
  desde este entorno de desarrollo: eso queda a cargo del estudiante,
  quien sí cuenta con instancia de n8n y credenciales de modelo.
- Animaciones, diseño responsive avanzado o UI "bonita" del dashboard no
  se califican ni son objetivo de esta propuesta.
