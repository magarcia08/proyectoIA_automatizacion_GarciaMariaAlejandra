package com.project.springboot.demoproject.logitrackiq.support;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import com.project.springboot.demoproject.entities.Bodega;
import com.project.springboot.demoproject.entities.InventarioBodega;
import com.project.springboot.demoproject.entities.Movimiento;
import com.project.springboot.demoproject.entities.MovimientoDetalle;
import com.project.springboot.demoproject.entities.Producto;
import com.project.springboot.demoproject.entities.Proveedor;
import com.project.springboot.demoproject.entities.Usuario;
import com.project.springboot.demoproject.enums.Rol;
import com.project.springboot.demoproject.enums.TipoMovimiento;
import com.project.springboot.demoproject.repositories.BodegaRepository;
import com.project.springboot.demoproject.repositories.InventarioBodegaRepository;
import com.project.springboot.demoproject.repositories.MovimientoDetalleRepository;
import com.project.springboot.demoproject.repositories.MovimientoRepository;
import com.project.springboot.demoproject.repositories.ProductoRepository;
import com.project.springboot.demoproject.repositories.ProveedorRepository;
import com.project.springboot.demoproject.repositories.UsuarioRepository;

/**
 * Base comun para las pruebas de LogiTrack IQ: helpers para sembrar
 * usuarios/bodegas/proveedores/productos/movimientos contra la base H2 de
 * pruebas (ver src/test/resources/application.properties) y para simular
 * un usuario autenticado sin pasar por HTTP.
 *
 * Un unico Postgres embebido se comparte entre TODAS las clases de prueba
 * de este paquete (el contexto de Spring se cachea porque las propiedades
 * dinamicas resuelven al mismo valor). Cada prueba corre en su propia
 * transaccion (@Transactional de Spring Test) que se revierte al terminar,
 * asi que los datos que siembra un metodo nunca contaminan otro.
 */
@SpringBootTest
@Transactional
public abstract class AbstractLogiTrackIqTest {

    @Autowired protected UsuarioRepository usuarioRepository;
    @Autowired protected BodegaRepository bodegaRepository;
    @Autowired protected ProveedorRepository proveedorRepository;
    @Autowired protected ProductoRepository productoRepository;
    @Autowired protected InventarioBodegaRepository inventarioBodegaRepository;
    @Autowired protected MovimientoRepository movimientoRepository;
    @Autowired protected MovimientoDetalleRepository movimientoDetalleRepository;
    @Autowired protected PasswordEncoder passwordEncoder;

    /**
     * Un unico Postgres real embebido (sin Docker) para toda la JVM de
     * pruebas: mas rapido que levantar uno por clase, y evita el problema
     * de los enums nativos de Usuario/Movimiento/Auditoria/OrdenCompra en
     * H2 (ver docs/sdd/03-diseno.md).
     */
    private static EmbeddedPostgres postgres;

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", AbstractLogiTrackIqTest::jdbcUrl);
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
    }

    private static synchronized String jdbcUrl() {
        try {
            if (postgres == null) {
                postgres = EmbeddedPostgres.start();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        postgres.close();
                    } catch (IOException ignored) {
                        // JVM se esta cerrando de todas formas
                    }
                }));
            }
            return postgres.getJdbcUrl("postgres", "postgres");
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo iniciar Postgres embebido para las pruebas", e);
        }
    }

    private static int contador = 0;

    private String unico(String prefijo) {
        return prefijo + (++contador);
    }

    protected Usuario crearUsuario(Rol rol) {
        Usuario u = new Usuario();
        String nombre = unico(rol.name().toLowerCase());
        u.setUsername(nombre);
        u.setPassword(passwordEncoder.encode("clave123"));
        u.setEmail(nombre + "@logitrack.test");
        u.setRol(rol);
        u.setActivo(true);
        return usuarioRepository.save(u);
    }

    protected Bodega crearBodega(int capacidad) {
        Bodega b = new Bodega();
        b.setNombre(unico("Bodega"));
        b.setUbicacion("Ciudad de prueba");
        b.setCapacidad(capacidad);
        b.setEncargado("Encargado de prueba");
        return bodegaRepository.save(b);
    }

    protected Proveedor crearProveedor(int diasEntrega) {
        Proveedor p = new Proveedor();
        p.setNombre(unico("Proveedor"));
        p.setContacto("contacto@proveedor.test");
        p.setDiasEntrega(diasEntrega);
        return proveedorRepository.save(p);
    }

    protected Producto crearProducto(Proveedor proveedorPrincipal) {
        Producto p = new Producto();
        p.setNombre(unico("Producto"));
        p.setCategoria("General");
        p.setPrecio(new java.math.BigDecimal("10000.00"));
        p.setProveedorPrincipal(proveedorPrincipal);
        return productoRepository.save(p);
    }

    protected void asignarStock(Bodega bodega, Producto producto, int stock) {
        InventarioBodega inv = new InventarioBodega();
        inv.setBodega(bodega);
        inv.setProducto(producto);
        inv.setStock(stock);
        inventarioBodegaRepository.save(inv);
    }

    /** Crea una SALIDA (consumo) de un producto desde una bodega, hace "diasAtras" dias. */
    protected void registrarSalidaHace(Usuario usuario, Bodega origen, Producto producto, int cantidad, long diasAtras) {
        Movimiento m = new Movimiento();
        m.setFecha(LocalDateTime.now().minusDays(diasAtras));
        m.setTipo(TipoMovimiento.SALIDA);
        m.setUsuario(usuario);
        m.setBodegaOrigen(origen);
        m.setBodegaDestino(null);
        Movimiento guardado = movimientoRepository.save(m);

        MovimientoDetalle detalle = new MovimientoDetalle();
        detalle.setMovimiento(guardado);
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        movimientoDetalleRepository.save(detalle);
    }

    /** Autentica el hilo de prueba actual como el Usuario dado (sin pasar por HTTP/JWT). */
    protected void autenticarComo(Usuario usuario) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario.getUsername(), null, authorities));
    }

    @AfterEach
    void limpiarContextoSeguridad() {
        SecurityContextHolder.clearContext();
    }
}
