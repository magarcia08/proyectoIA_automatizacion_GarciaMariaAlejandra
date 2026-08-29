# Frontend

El dashboard de LogiTrack (reto anterior + LogiTrack IQ) vive en
[`../src/main/resources/static/`](../src/main/resources/static/) y se
sirve directamente desde el propio backend Spring Boot (mismo origen,
sin CORS que configurar para la demo). Es HTML/CSS/JS sin framework, tal
como pide el enunciado.

Se mantuvo ahí (en vez de duplicarlo en esta carpeta) para no romper el
login/JWT y el resto de módulos ya construidos en el reto anterior — el
enunciado permite adaptar la estructura "siempre que las
responsabilidades estén separadas de forma clara".

## Qué hay de LogiTrack IQ

- [`static/js/torre-control.js`](../src/main/resources/static/js/torre-control.js) +
  [`static/pages/torre-control.html`](../src/main/resources/static/pages/torre-control.html):
  módulo nuevo "Torre de control" — los 4 indicadores, ocupación por
  bodega, movimientos de ayer, el último resumen del panel, productos en
  riesgo, y las órdenes en `BORRADOR` (generar/ver PDF con marca de agua,
  botón **Aprobar** visible solo para `ADMIN`/`SUPERADMIN`).
- [`static/js/api.js`](../src/main/resources/static/js/api.js): el JWT
  ahora se guarda solo en `sessionStorage` (antes `localStorage`), y se
  agregó `peticionApiBinaria` para pedir el PDF con el header
  `Authorization` (no se puede usar un `<a href>` directo).

## Ejecutar

```bash
./mvnw spring-boot:run
```

y abre `http://localhost:8085` (o el puerto configurado). Usuarios de
prueba en [`../README.md`](../README.md).
