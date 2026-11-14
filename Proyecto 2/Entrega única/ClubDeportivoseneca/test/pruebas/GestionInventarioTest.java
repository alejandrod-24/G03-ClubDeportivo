package pruebas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import comercio.ArticuloDeportivo;
import global.Club;

/**
 * 15. Gestion del catalogo, reabastecimiento, movimiento entre ubicaciones, consulta de inventario
 *     y resolucion de sugerencias.
 * RF-ADM-08 a 12
 */
@DisplayName("15. Catalogo e inventario (administrador)")
class GestionInventarioTest {

	private Club club;

	@BeforeEach
	void setUp() {
		club = Utilidades.clubDePrueba();
		club.iniciarSesion("admin", "admin123");
	}

	@Test
	@DisplayName("RF-ADM-08: gestionar el catalogo")
	void gestionarCatalogo() {
		assertDoesNotThrow(() -> club.crearArticulo("Rodilleras", "Proteccion", 45000));
		assertDoesNotThrow(() -> club.crearProductoCafeteria("Te caliente", 4000, true, List.of()));
		assertThrows(RuntimeException.class, () -> club.crearArticulo("Cafe", "Bebidas", 5000), "nombre que ya existe");
		assertThrows(RuntimeException.class, () -> club.crearArticulo("Gorra", "Accesorios", 0), "precio cero");
		assertDoesNotThrow(() -> club.cambiarPrecioProducto("Cafe", 5500));
		assertEquals(5500, club.buscarProducto("Cafe").getPrecio());
		assertThrows(RuntimeException.class, () -> club.cambiarPrecioProducto("Cafe", -1), "precio negativo");
		assertDoesNotThrow(() -> club.desactivarProducto("Sandwich"));
		assertThrows(RuntimeException.class, () -> club.desactivarProducto("Sandwich"), "desactivarlo otra vez");
		assertFalse(club.consultarCatalogoCafeteria().contains(club.buscarProducto("Sandwich")),
				"el sandwich desactivado no aparece en el catalogo");
		club.iniciarSesion("carlos", "carlos123");
		assertThrows(RuntimeException.class, () -> club.crearArticulo("Gorra", "Accesorios", 30000),
				"un entrenador no gestiona el catalogo");
	}

	@Test
	@DisplayName("RF-ADM-09: reabastecer la tienda")
	void reabastecerTienda() {
		club.crearArticulo("Rodilleras", "Proteccion", 45000);
		assertDoesNotThrow(() -> club.reabastecerTienda("Rodilleras", 10));
		assertEquals(10, club.buscarArticulo("Rodilleras").getExistencia(ArticuloDeportivo.TIENDA));
		assertThrows(RuntimeException.class, () -> club.reabastecerTienda("Rodilleras", 0), "cantidad cero");
		assertThrows(RuntimeException.class, () -> club.reabastecerTienda("Patines", 5), "articulo inexistente");
		assertThrows(RuntimeException.class, () -> club.reabastecerTienda("Cafe", 5), "un producto de la cafeteria");
		club.desactivarProducto("Malla de voleibol");
		assertThrows(RuntimeException.class, () -> club.reabastecerTienda("Malla de voleibol", 5), "articulo desactivado");
	}

	@Test
	@DisplayName("RF-ADM-10, 11: mover articulos entre ubicaciones y consultar el inventario")
	void moverArticulos() {
		int tubosEnTienda = club.buscarArticulo("Tubo de pelotas").getExistencia(ArticuloDeportivo.TIENDA);
		assertDoesNotThrow(() -> club.moverArticulo("Tubo de pelotas", "Bodega", "Tienda", 10));
		assertEquals(tubosEnTienda + 10, club.buscarArticulo("Tubo de pelotas").getExistencia(ArticuloDeportivo.TIENDA));
		assertDoesNotThrow(() -> club.moverArticulo("Uniforme Seneca", "Tienda", "Bodega Norte", 3), "a una ubicacion nueva");
		assertEquals(3, club.buscarArticulo("Uniforme Seneca").getExistencia("Bodega Norte"));
		assertTrue(club.consultarInventario().toString().contains("Bodega Norte"), "el inventario muestra la ubicacion nueva");
		assertThrows(RuntimeException.class, () -> club.moverArticulo("Raqueta de tenis", "Bodega", "Tienda", 50),
				"mas raquetas de las que hay en la bodega");
		assertThrows(RuntimeException.class, () -> club.moverArticulo("Raqueta de tenis", "Bodega", "Tienda", -2),
				"cantidad negativa");
		assertThrows(RuntimeException.class, () -> club.moverArticulo("Raqueta de tenis", "Tienda", "Tienda", 1),
				"mismo origen y destino");
	}

	@Test
	@DisplayName("RF-ADM-12: aprobar o rechazar sugerencias")
	void resolverSugerencias() {
		club.iniciarSesion("pedro", "pedro123");
		String gel = club.sugerirProducto("Gel de recuperacion", "Recuperacion", 45000, List.of()).getId();
		String barra = club.sugerirProducto("Barra de proteina", "Snacks", 6000, List.of("Mani")).getId();
		String toalla = club.sugerirProducto("Toalla deportiva", "Accesorios", 35000, List.of()).getId();
		club.iniciarSesion("admin", "admin123");
		club.crearArticulo("Toalla deportiva", "Accesorios", 35000);

		assertDoesNotThrow(() -> club.aprobarSugerencia(gel));
		assertTrue(club.consultarCatalogoTienda().contains(club.buscarProducto("Gel de recuperacion")),
				"el gel aprobado entra al catalogo de la tienda");
		assertThrows(RuntimeException.class, () -> club.crearArticulo("Gel de recuperacion", "Recuperacion", 45000),
				"ya esta en el catalogo");
		assertDoesNotThrow(() -> club.rechazarSugerencia(barra));
		assertThrows(RuntimeException.class, () -> club.aprobarSugerencia(toalla), "la toalla ya existe en el catalogo");
		assertThrows(RuntimeException.class, () -> club.rechazarSugerencia(gel), "el gel ya fue aprobado");
		assertThrows(RuntimeException.class, () -> club.aprobarSugerencia("SG99"), "sugerencia inexistente");
	}

	@Test
	@DisplayName("el catalogo y el inventario sobreviven a guardar y cargar")
	void persistencia(@TempDir Path carpeta) {
		club.crearArticulo("Rodilleras", "Proteccion", 45000);
		club.reabastecerTienda("Rodilleras", 10);
		club.moverArticulo("Uniforme Seneca", "Tienda", "Bodega Norte", 3);
		club.desactivarProducto("Sandwich");
		club.iniciarSesion("pedro", "pedro123");
		String gel = club.sugerirProducto("Gel de recuperacion", "Recuperacion", 45000, List.of()).getId();
		club.sugerirProducto("Barra de proteina", "Snacks", 6000, List.of("Mani"));
		club.iniciarSesion("admin", "admin123");
		club.aprobarSugerencia(gel);

		Club recargado = Utilidades.guardarYRecargar(club, carpeta);
		recargado.iniciarSesion("admin", "admin123");
		assertEquals(club.consultarInventario(), recargado.consultarInventario(), "inventario");
		assertEquals(club.getProductos().size(), recargado.getProductos().size(), "productos");
		assertEquals(club.getSugerencias().toString(), recargado.getSugerencias().toString(), "sugerencias y su estado");
	}
}
