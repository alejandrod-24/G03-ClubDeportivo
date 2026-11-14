package pruebas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import global.Club;
import global.Reloj;
import instalaciones.Modalidad;

/**
 * 5. Disponibilidad, reserva, cancelacion e ingreso/salida de una instalacion individual,
 *    con rechazo por cupo agotado y por restriccion de bebida caliente.
 * RF-SOC-06 a 09 y RF-SOC-17
 */
@DisplayName("5. Reservas e ingreso a instalaciones")
class ReservasTest {

	private static final LocalDate LUNES = Utilidades.LUNES;
	private static final LocalDateTime LUNES_9 = LUNES.atTime(9, 0);

	private Club club;

	@BeforeEach
	void setUp() {
		club = Utilidades.clubDePrueba();
		club.iniciarSesion("diego", "1234");
	}

	@Test
	@DisplayName("RF-SOC-06: consultar disponibilidad")
	void consultarDisponibilidad() {
		assertFalse(club.consultarDisponibilidad("Tenis", LUNES_9).isEmpty());
		assertThrows(RuntimeException.class, () -> club.consultarDisponibilidad("Tenis", LUNES.atTime(21, 0)),
				"bloque fuera del horario");
		assertThrows(RuntimeException.class, () -> club.consultarDisponibilidad("Tenis", LocalDate.of(2026, 10, 18).atTime(9, 0)),
				"domingo (el club no abre)");
		assertThrows(RuntimeException.class, () -> club.consultarDisponibilidad("Squash", LUNES_9), "deporte inexistente");
	}

	@Test
	@DisplayName("RF-SOC-07: reservar una instalacion")
	void reservarInstalacion() {
		String id = assertDoesNotThrow(
				() -> club.reservarInstalacion("Tenis", "TEN-1", LUNES_9, 2, Modalidad.SENCILLOS).getId());
		assertNotNull(club.buscarReserva(id));
		assertThrows(RuntimeException.class, () -> club.reservarInstalacion("Tenis", "TEN-2", LUNES_9, 2, Modalidad.SENCILLOS),
				"diego ya tiene una reserva en ese horario");

		club.iniciarSesion("valentina", "1234");
		assertThrows(RuntimeException.class, () -> club.reservarInstalacion("Tenis", "TEN-1", LUNES_9, 2, Modalidad.SENCILLOS),
				"instalacion ocupada");
		assertDoesNotThrow(() -> club.reservarInstalacion("Tenis", "TEN-2", LUNES_9, 4, Modalidad.DOBLES));

		club.iniciarSesion("luis", "1234");
		assertThrows(RuntimeException.class, () -> club.reservarInstalacion("Tenis", "TEN-3", LUNES_9, 2, Modalidad.SENCILLOS),
				"cupo agotado: ya hay 2 canchas de tenis en uso");
	}

	@Test
	@DisplayName("RF-SOC-07: rechazos al reservar")
	void rechazosAlReservar() {
		club.iniciarSesion("luis", "1234");
		assertThrows(RuntimeException.class,
				() -> club.reservarInstalacion("Tenis", "TEN-3", LUNES.atTime(10, 0), 4, Modalidad.DOBLES),
				"mas jugadores que la capacidad");
		assertThrows(RuntimeException.class,
				() -> club.reservarInstalacion("Tenis", "TEN-3", LocalDate.of(2026, 10, 16).atTime(9, 0), 2, Modalidad.SENCILLOS),
				"bloque pasado");
		assertThrows(RuntimeException.class,
				() -> club.reservarInstalacion("Tenis", "TEN-3", LUNES.atTime(10, 30), 2, Modalidad.SENCILLOS),
				"bloque que no empieza en punto");
		club.iniciarSesion("nicolas", "1234");
		assertThrows(RuntimeException.class,
				() -> club.reservarInstalacion("Tenis", "TEN-3", LUNES.atTime(10, 0), 2, Modalidad.SENCILLOS),
				"nicolas no esta inscrito en tenis");
		club.iniciarSesion("maria", "1234");
		assertThrows(RuntimeException.class, () -> club.reservarInstalacion("Padel", "PAD-2", LUNES_9, 2, Modalidad.SENCILLOS),
				"el padel no se juega en sencillos");
		assertThrows(RuntimeException.class, () -> club.reservarInstalacion("Padel", "TEN-1", LUNES_9, 4, Modalidad.DOBLES),
				"instalacion de otro deporte");
	}

	@Test
	@DisplayName("RF-SOC-08: cancelar una reserva")
	void cancelarReserva() {
		String id = club.reservarInstalacion("Tenis", "TEN-1", LUNES_9, 2, Modalidad.SENCILLOS).getId();
		club.iniciarSesion("maria", "1234");
		assertThrows(RuntimeException.class, () -> club.cancelarReserva(id), "maria no puede cancelar la reserva de diego");
		club.iniciarSesion("diego", "1234");
		assertDoesNotThrow(() -> club.cancelarReserva(id));
		assertThrows(RuntimeException.class, () -> club.cancelarReserva(id), "cancelarla otra vez");
	}

	@Test
	@DisplayName("RF-SOC-09: registrar ingreso y salida")
	void ingresoYSalida() {
		String id = club.reservarInstalacion("Tenis", "TEN-1", LUNES.atTime(11, 0), 2, Modalidad.SENCILLOS).getId();
		assertThrows(RuntimeException.class, () -> club.registrarIngreso("TEN-1"), "antes de la hora de su reserva");

		Reloj.fijar(LUNES.atTime(11, 10));
		assertThrows(RuntimeException.class, () -> club.registrarIngreso("TEN-2"), "cancha sin reserva");
		assertDoesNotThrow(() -> club.registrarIngreso("TEN-1"));
		assertThrows(RuntimeException.class, () -> club.registrarIngreso("TEN-1"), "ya esta en una instalacion");
		assertThrows(RuntimeException.class, () -> club.cancelarReserva(id), "el bloque ya inicio");
		assertDoesNotThrow(() -> club.registrarSalida());
		assertThrows(RuntimeException.class, () -> club.registrarSalida(), "salir otra vez");
	}

	@Test
	@DisplayName("RF-SOC-17: no se entra a la cancha de padel techada con bebida caliente")
	void restriccionBebidaCaliente() {
		club.iniciarSesion("maria", "1234");
		club.reservarInstalacion("Padel", "PAD-1", LUNES.atTime(12, 0), 4, Modalidad.DOBLES);
		//La marca de bebida caliente vence al dia siguiente, por eso la compra es el mismo lunes
		Reloj.fijar(LUNES.atTime(11, 50));
		assertDoesNotThrow(() -> club.comprarEnCafeteria(Utilidades.items("Cafe", 1), 0, 10, true),
				"compra un cafe antes de estar en la cancha");

		Reloj.fijar(LUNES.atTime(12, 5));
		assertThrows(RuntimeException.class, () -> club.registrarIngreso("PAD-1"), "entrar con la bebida caliente");
		assertDoesNotThrow(() -> club.declararFinBebidaCaliente());
		assertDoesNotThrow(() -> club.registrarIngreso("PAD-1"));
		assertThrows(RuntimeException.class,
				() -> club.comprarEnCafeteria(Utilidades.items("Chocolate caliente", 1), 0, 10, true),
				"dentro de la cancha no se despacha bebida caliente");
		assertDoesNotThrow(() -> club.comprarEnCafeteria(Utilidades.items("Jugo natural", 1), 0, 10, true));
		assertDoesNotThrow(() -> club.registrarSalida());
	}

	@Test
	@DisplayName("las reservas e ingresos sobreviven a guardar y cargar")
	void persistencia(@TempDir Path carpeta) {
		String cancelada = club.reservarInstalacion("Tenis", "TEN-1", LUNES_9, 2, Modalidad.SENCILLOS).getId();
		club.cancelarReserva(cancelada);
		club.reservarInstalacion("Tenis", "TEN-1", LUNES.atTime(11, 0), 2, Modalidad.SENCILLOS);
		Reloj.fijar(LUNES.atTime(11, 10));
		club.registrarIngreso("TEN-1");
		club.registrarSalida();

		club.iniciarSesion("admin", "admin123");
		Club recargado = Utilidades.guardarYRecargar(club, carpeta);
		recargado.iniciarSesion("admin", "admin123");
		assertEquals(club.historialReservas(null, null, null, null, null).toString(),
				recargado.historialReservas(null, null, null, null, null).toString(), "historial de reservas");
		assertEquals(club.getIngresos().toString(), recargado.getIngresos().toString(), "ingresos");
	}
}
