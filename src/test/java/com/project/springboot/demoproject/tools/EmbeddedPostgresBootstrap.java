package com.project.springboot.demoproject.tools;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

/**
 * Utilidad de desarrollo (NO se usa en produccion ni en las pruebas
 * automatizadas): levanta un Postgres real embebido y deja el proceso
 * vivo, para poder apuntar `spring-boot:run` a una base real local
 * (por ejemplo, cuando la base remota de application.properties no esta
 * disponible) y generar evidencia real de la API/MCP. Imprime la URL JDBC
 * por stdout y no termina hasta que se mata el proceso.
 */
public final class EmbeddedPostgresBootstrap {

    private EmbeddedPostgresBootstrap() {
    }

    public static void main(String[] args) throws Exception {
        EmbeddedPostgres postgres = EmbeddedPostgres.start();
        System.out.println("JDBC_URL=" + postgres.getJdbcUrl("postgres", "postgres"));
        System.out.println("READY");
        Thread.currentThread().join();
    }
}
