package pruebas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import administracion.Turno;
import global.Club;

/**
 * 9. Consulta y solicitud de cambio de turno, y sugerencia de nuevos productos por parte de los empleados.
 * RF-EMP-01 a 03, 06, 07
 */
@DisplayName("9. Turnos y sugerencias de los empleados")
class TurnosYSugerenciasEmpleadoTest {

	private Club club;

	@BeforeEach
	void setUp() {
		club = Utilidades.clubDePrueba();
		club.iniciarSesion("admin", "admin123");
		club.asignarTurno("jaime", DayOfWeek.MONDAY, LocalTime.of(16, 0), LocalTime.of(20, 0));
	}

	private String turnoDe(String login, DayOfWeek dia) {
		for (Turno t : club.getTurnos()) {
			if (t.getEmpleado().getLogin().equals(login) && t.getDia() == dia) {
				return t.getId();
			}
		}
		throw new IllegalArgumentException(login + " no tiene turno el " + dia);
	}

	@Test
	@DisplayName("RF-EMP-01: cada empleado consulta solo sus turnos")
	void consultarTurnos() {
		club.iniciarSesion("laura", "laura123");
		List<Turno> turnos = club.consultarTurnos();
		assertFalse(turnos.isEmpty());
		for (Turno t : turnos) {
			assertEquals("laura", t.getEmpleado().getLogin());
		}
		club.iniciarSesion("ana", "1234");
		assertThrows(RuntimeException.class, () -> club.consultarTurnos(), "un socio no tiene turnos");
	}

	@Test
	@DisplayName("RF-EMP-02: solicitar un cambio de turno")
	void solicitarCambioTurno() {
		club.iniciarSesion("laura", "laura123");
		String lunesLaura = turnoDe("laura", DayOfWeek.MONDAY);
		assertDoesNotThrow(() -> club.solicitarCambioTurno(lunesLaura, LocalTime.of(14, 0), LocalTime.of(20, 0)));
		assertThrows(RuntimeException.class, () -> club.solicitarCambioTurno(lunesLaura, LocalTime.of(8, 0), LocalTime.of(14, 0)),
				"ya hay una solicitud pendiente para ese turno");
		assertThrows(RuntimeException.class,
				() -> club.solicitarCambioTurno(turnoDe("carlos", DayOfWeek.MONDAY), LocalTime.of(8, 0), LocalTime.of(14, 0)),
				"un turno de otro empleado");
		assertThrows(RuntimeException.class,
				() -> club.solicitarCambioTurno(turnoDe("laura", DayOfWeek.TUESDAY), LocalTime.of(6, 0), LocalTime.of(14, 0)),
				"horario fuera de atencion");
		assertThrows(RuntimeException.class,
				() -> club.solicitarCambioTurno(turnoDe("laura", DayOfWeek.TUESDAY), LocalTime.of(15, 0), LocalTime.of(14, 0)),
				"la hora de fin antes de la de inicio");
		assertThrows(RuntimeException.class, () -> club.solicitarCambioTurno("T999", LocalTime.of(8, 0), LocalTime.of(14, 0)),
				"turno inexistente");
	}

	@Test
	@DisplayName("RF-EMP-02: el cambio no puede cruzarse con otro turno del mismo dia")
	void cambioQueSeCruza() {
		club.iniciarSesion("jaime", "jaime123");
		assertThrows(RuntimeException.class,
				() -> club.solicitarCambioTurno(turnoDe("jaime", DayOfWeek.MONDAY), LocalTime.of(8, 0), LocalTime.of(17, 0)));
		club.iniciarSesion("pedro", "pedro123");
		assertDoesNotThrow(() -> club.solicitarCambioTurno(turnoDe("pedro", DayOfWeek.SATURDAY), LocalTime.of(8, 0),
				LocalTime.of(14, 0)), "el fisioterapeuta tambien puede pedir cambios");
	}

	@Test
	@DisplayName("RF-EMP-03: consultar mis solicitudes")
	void consultarSolicitudes() {
		club.iniciarSesion("laura", "laura123");
		club.solicitarCambioTurno(turnoDe("laura", DayOfWeek.MONDAY), LocalTime.of(14, 0), LocalTime.of(20, 0));
		assertEquals(1, club.consultarSolicitudes().size());
		club.iniciarSesion("jaime", "jaime123");
		assertTrue(club.consultarSolicitudes().isEmpty(), "jaime no tiene solicitudes");
	}

	@Test
	@DisplayName("RF-EMP-06: sugerir articulos o snacks")
	void sugerirProducto() {
		club.iniciarSesion("pedro", "pedro123");
		assertDoesNotThrow(() -> club.sugerirProducto("Gel de recuperacion", "Recuperacion", 45000, List.of()));
		assertDoesNotThrow(() -> club.sugerirProducto("Barra de proteina", "Snacks", 6000, List.of("Mani", "Lacteos")),
				"un snack con alergenos");
		assertThrows(RuntimeException.class, () -> club.sugerirProducto("Cafe", "Bebidas", 5000, List.of()),
				"un producto que ya esta en el catalogo");
		assertThrows(RuntimeException.class, () -> club.sugerirProducto("Gel de recuperacion", "Recuperacion", 40000, List.of()),
				"ya hay una sugerencia pendiente con ese nombre");
		assertThrows(RuntimeException.class, () -> club.sugerirProducto("Banda elastica", "Recuperacion", 0, List.of()),
				"precio cero");
		assertThrows(RuntimeException.class, () -> club.sugerirProducto("Banda elastica", "", 15000, List.of()),
				"falta la categoria");
		assertThrows(RuntimeException.class, () -> club.sugerirProducto(null, "Recuperacion", 15000, List.of()),
				"falta el nombre");
		club.iniciarSesion("carlos", "carlos123");
		assertDoesNotThrow(() -> club.sugerirProducto("Conos de entrenamiento", "Accesorios", 30000, List.of()),
				"un entrenador tambien sugiere");
		club.iniciarSesion("ana", "1234");
		assertThrows(RuntimeException.class, () -> club.sugerirProducto("Banda elastica", "Recuperacion", 15000, List.of()),
				"un socio no puede sugerir productos");
	}

	@Test
	@DisplayName("RF-EMP-07: consultar mis sugerencias y volver a sugerir tras un rechazo")
	void consultarSugerencias() {
		club.iniciarSesion("pedro", "pedro123");
		club.sugerirProducto("Gel de recuperacion", "Recuperacion", 45000, List.of());
		String barra = club.sugerirProducto("Barra de proteina", "Snacks", 6000, List.of("Mani", "Lacteos")).getId();
		club.iniciarSesion("admin", "admin123");
		club.rechazarSugerencia(barra);

		club.iniciarSesion("pedro", "pedro123");
		assertEquals(2, club.consultarSugerencias().size());
		assertDoesNotThrow(() -> club.sugerirProducto("Barra de proteina", "Snacks", 5000, List.of("Mani")),
				"despues del rechazo puede volver a sugerirla");
	}
}
