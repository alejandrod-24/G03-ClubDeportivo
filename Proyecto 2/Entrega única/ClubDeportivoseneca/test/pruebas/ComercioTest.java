package pruebas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import comercio.ArticuloDeportivo;
import comercio.Venta;
import comercio.VentaCafeteria;
import comercio.VentaTienda;
import global.Club;
import global.Reloj;

/**
 * 8. Compras en tienda y cafeteria de socios y empleados: impuestos, codigo de descuento, alergenos,
 *    propina, puntos de fidelidad, bebida caliente y pago de la mensualidad.
 * RF-SOC-14 a 17, RF-EMP-04, 05 (HU de mensualidad)
 */
@DisplayName("8. Tienda, cafeteria, puntos y mensualidad")
class ComercioTest {

	private Club club;

	@BeforeEach
	void setUp() {
		club = Utilidades.clubDePrueba();
		club.iniciarSesion("admin", "admin123");
		club.desactivarProducto("Agua");
		club.iniciarSesion("ana", "1234");
	}

	//ana compra 2 balones y 1 tubo de pelotas, y un uniforme con el codigo de carlos: acumula mas de 6000 puntos
	private void comprasDeAna() {
		club.comprarEnTienda(Utilidades.items("Balon de futbol", 2, "Tubo de pelotas", 1), null, 0);
		club.comprarEnTienda(Utilidades.items("Uniforme Seneca", 1), "DESC-CARLOS", 0);
	}

	private List<Long> totales(Club c) {
		List<Long> totales = new ArrayList<>();
		for (Venta v : c.getVentas()) {
			totales.add(v.getTotal());
		}
		return totales;
	}

	@Test
	@DisplayName("RF-SOC-14: compra en la tienda con IVA y puntos generados")
	void comprarEnTienda() {
		VentaTienda v = club.comprarEnTienda(Utilidades.items("Balon de futbol", 2, "Tubo de pelotas", 1), null, 0);
		assertEquals(220150, v.getTotal(), "total con IVA 19%");
		assertEquals(4403, v.getPuntosGenerados(), "puntos generados");
	}

	@Test
	@DisplayName("RF-SOC-14: compra con el codigo de descuento de un empleado")
	void comprarConCodigoDescuento() {
		VentaTienda v = club.comprarEnTienda(Utilidades.items("Uniforme Seneca", 1), "DESC-CARLOS", 0);
		assertEquals(9600, v.getDescuento(), "descuento del 8%");
		assertEquals(131376, v.getTotal());
	}

	@Test
	@DisplayName("RF-SOC-14: rechazos en la tienda")
	void rechazosEnTienda() {
		comprasDeAna();
		assertThrows(RuntimeException.class, () -> club.comprarEnTienda(Utilidades.items("Balon de futbol", 1), "DESC-NADIE", 0),
				"codigo inexistente");
		assertThrows(RuntimeException.class, () -> club.comprarEnTienda(Utilidades.items("Balon de futbol", 1), "DESC-CARLOS", 1000),
				"codigo y puntos a la vez (no acumulable)");
		assertThrows(RuntimeException.class, () -> club.comprarEnTienda(Utilidades.items("Balon de futbol", 0), null, 0),
				"cantidad cero");
		assertThrows(RuntimeException.class, () -> club.comprarEnTienda(Utilidades.items("Tubo de pelotas", 5), null, 0),
				"mas tubos de los que hay en la tienda");
		assertThrows(RuntimeException.class, () -> club.comprarEnTienda(Utilidades.items("Patines", 1), null, 0),
				"articulo inexistente");
		assertThrows(RuntimeException.class, () -> club.comprarEnTienda(Utilidades.items("Cafe", 1), null, 0),
				"un producto de la cafeteria en la tienda");
		assertThrows(RuntimeException.class, () -> club.comprarEnTienda(Utilidades.items("Balon de futbol", 1), null, 10000),
				"redimir mas puntos de los que tiene");
		assertThrows(RuntimeException.class, () -> club.comprarEnTienda(Utilidades.items(), null, 0), "compra vacia");
	}

	@Test
	@DisplayName("RF-SOC-14: redimir puntos y descontar existencias")
	void redimirPuntos() {
		comprasDeAna();
		assertDoesNotThrow(() -> club.comprarEnTienda(Utilidades.items("Tubo de pelotas", 1), null, 5000));
		assertEquals(0, club.buscarArticulo("Tubo de pelotas").getExistencia(ArticuloDeportivo.TIENDA),
				"la tienda queda sin tubos de pelotas");
	}

	@Test
	@DisplayName("RF-EMP-04, 05: el descuento del empleado se aplica solo y no se acumula")
	void comprasDeEmpleados() {
		club.iniciarSesion("pedro", "pedro123");
		assertNotNull(club.consultarCodigoDescuento());
		VentaTienda v = club.comprarEnTienda(Utilidades.items("Raqueta de tenis", 1), null, 0);
		assertEquals(52500, v.getDescuento(), "su 15% se aplica solo");
		assertEquals(354025, v.getTotal());
		assertThrows(RuntimeException.class, () -> club.comprarEnTienda(Utilidades.items("Balon de futbol", 1), "DESC-LAURA", 0),
				"un empleado no usa otro codigo");
		VentaTienda conPuntos = club.comprarEnTienda(Utilidades.items("Balon de futbol", 1), null, 5000);
		assertEquals(0, conPuntos.getDescuento(), "si redime puntos no tiene el 15%");
		assertEquals(75000, conPuntos.getBase());
	}

	@Test
	@DisplayName("el administrador no compra")
	void administradorNoCompra() {
		club.iniciarSesion("admin", "admin123");
		assertThrows(RuntimeException.class, () -> club.comprarEnTienda(Utilidades.items("Balon de futbol", 1), null, 0));
	}

	@Test
	@DisplayName("RF-SOC-15: compra en la cafeteria con alergenos, impuesto y propina")
	void comprarEnCafeteria() {
		club.iniciarSesion("luis", "1234");
		assertFalse(club.consultarAlergenos(Utilidades.items("Galletas de mani", 1, "Jugo natural", 1)).isEmpty());
		assertThrows(RuntimeException.class, () -> club.comprarEnCafeteria(
				Utilidades.items("Galletas de mani", 1, "Jugo natural", 1), 0, VentaCafeteria.PROPINA_SUGERIDA, false),
				"comprar sin confirmar los alergenos");
		VentaCafeteria v = club.comprarEnCafeteria(Utilidades.items("Galletas de mani", 1, "Jugo natural", 1), 0,
				VentaCafeteria.PROPINA_SUGERIDA, true);
		assertEquals(880, v.getImpuesto(), "impuesto al consumo");
		assertEquals(1100, v.getPropina(), "propina sugerida del 10%");
		assertEquals(12980, v.getTotal());
		assertDoesNotThrow(() -> club.comprarEnCafeteria(Utilidades.items("Jugo natural", 1), 0, 0, false), "propina 0%");
	}

	@Test
	@DisplayName("RF-SOC-15: rechazos en la cafeteria")
	void rechazosEnCafeteria() {
		club.iniciarSesion("luis", "1234");
		assertThrows(RuntimeException.class, () -> club.comprarEnCafeteria(Utilidades.items("Jugo natural", 1), 0, -5, false),
				"propina negativa");
		assertThrows(RuntimeException.class, () -> club.comprarEnCafeteria(Utilidades.items("Agua", 1), 0, 10, false),
				"producto desactivado");
		assertThrows(RuntimeException.class, () -> club.comprarEnCafeteria(Utilidades.items("Pizza", 1), 0, 10, false),
				"producto inexistente");
		club.iniciarSesion("ana", "1234");
		comprasDeAna();
		assertThrows(RuntimeException.class, () -> club.comprarEnCafeteria(Utilidades.items("Cafe", 1), 6000, 10, false),
				"redimir mas puntos que el subtotal");
	}

	@Test
	@DisplayName("RF-SOC-15, 17: marca de bebida caliente")
	void bebidaCaliente() {
		assertDoesNotThrow(() -> club.comprarEnCafeteria(Utilidades.items("Cafe", 1), 0, 10, false));
		assertTrue(club.buscarSocio("ana").tieneBebidaCaliente(), "queda marcada con bebida caliente");
		assertDoesNotThrow(() -> club.declararFinBebidaCaliente());
		assertThrows(RuntimeException.class, () -> club.declararFinBebidaCaliente(), "declararlo sin tener bebida caliente");
		assertDoesNotThrow(() -> club.comprarEnCafeteria(Utilidades.items("Chocolate caliente", 1), 0, 10, true));
		Reloj.avanzarDias(1);
		assertFalse(club.buscarSocio("ana").tieneBebidaCaliente(), "al dia siguiente la marca vence");
	}

	@Test
	@DisplayName("RF-SOC-16: consultar puntos e historial de compras")
	void consultarPuntos() {
		VentaTienda v1 = club.comprarEnTienda(Utilidades.items("Balon de futbol", 2), null, 0);
		VentaTienda v2 = club.comprarEnTienda(Utilidades.items("Tubo de pelotas", 1), null, 0);
		long esperado = v1.getPuntosGenerados() + v2.getPuntosGenerados();
		assertTrue(club.consultarPuntos().startsWith("Saldo: " + esperado + " puntos"),
				"el saldo de ana es la suma de los puntos de sus compras");
		club.iniciarSesion("pedro", "pedro123");
		assertFalse(club.consultarPuntos().isBlank());
	}

	@Test
	@DisplayName("pagar la mensualidad una vez por mes")
	void pagarMensualidad() {
		assertNotNull(assertDoesNotThrow(() -> club.pagarMensualidad()), "ana paga octubre");
		assertThrows(RuntimeException.class, () -> club.pagarMensualidad(), "pagarla otra vez");
		Reloj.fijar(LocalDate.of(2026, 11, 2));
		assertDoesNotThrow(() -> club.pagarMensualidad(), "en noviembre ya puede pagar la siguiente");
		club.iniciarSesion("carlos", "carlos123");
		assertThrows(RuntimeException.class, () -> club.pagarMensualidad(), "un entrenador no paga mensualidad");
	}

	@Test
	@DisplayName("las ventas, existencias, puntos y la bebida caliente sobreviven a guardar y cargar")
	void persistencia(@TempDir Path carpeta) {
		comprasDeAna();
		club.pagarMensualidad();
		club.iniciarSesion("pedro", "pedro123");
		club.comprarEnTienda(Utilidades.items("Raqueta de tenis", 1), null, 0);
		club.iniciarSesion("ana", "1234");
		club.comprarEnCafeteria(Utilidades.items("Cafe", 1), 0, 10, false);

		Club recargado = Utilidades.guardarYRecargar(club, carpeta);
		recargado.iniciarSesion("ana", "1234");
		assertEquals(club.consultarPuntos(), recargado.consultarPuntos(), "puntos e historial de ana");
		assertEquals(club.consultarPerfil(), recargado.consultarPerfil(), "perfil de ana (mensualidad y bebida caliente)");
		assertEquals(club.buscarArticulo("Raqueta de tenis").getExistencias(),
				recargado.buscarArticulo("Raqueta de tenis").getExistencias(), "existencias de la raqueta");
		assertFalse(recargado.buscarProducto("Agua").estaActivo(), "el agua sigue desactivada");
		assertEquals(totales(club), totales(recargado), "cada venta conserva su total");
	}
}
