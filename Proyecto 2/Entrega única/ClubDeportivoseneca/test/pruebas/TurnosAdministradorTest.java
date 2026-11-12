package pruebas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import administracion.Turno;
import global.Club;

/**
 * 16. Asignacion de turnos semanales y resolucion de solicitudes de cambio de turno,
 *     validando la cobertura minima (2 entrenadores y 1 fisioterapeuta en todo el horario de atencion).
 *     carlos, laura y pedro cubren todo el horario; jaime refuerza lunes, miercoles y viernes en la manana.
 * RF-ADM-13, 14
 */
@DisplayName("16. Turnos y cobertura minima (administrador)")
class TurnosAdministradorTest {

	private Club club;

	@BeforeEach
	void setUp() {
		club = Utilidades.clubDePrueba();
		club.iniciarSesion("admin", "admin123");
	}

	private String turnoDe(String login, DayOfWeek dia) {
		for (Turno t : club.getTurnos()) {
			if (t.getEmpleado().getLogin().equals(login) && t.getDia() == dia) {
				return t.getId();
			}
		}
		throw new IllegalArgumentException(login + " no tiene turno el " + dia);
	}

	//Solicitud de cambio de turno hecha por el propio empleado
	private String solicitar(String login, String password, DayOfWeek dia, int desde, int hasta) {
		club.iniciarSesion(login, password);
		String id = club.solicitarCambioTurno(turnoDe(login, dia), LocalTime.of(desde, 0), LocalTime.of(hasta, 0)).getId();
		club.iniciarSesion("admin", "admin123");
		return id;
	}

	@Test
	@DisplayName("RF-ADM-13: asignar turnos semanales")
	void asignarTurno() {
		assertDoesNotThrow(() -> club.asignarTurno("jaime", DayOfWeek.TUESDAY, LocalTime.of(14, 0), LocalTime.of(20, 0)));
		assertThrows(RuntimeException.class,
				() -> club.asignarTurno("jaime", DayOfWeek.SUNDAY, LocalTime.of(8, 0), LocalTime.of(14, 0)), "domingo");
		assertThrows(RuntimeException.class,
				() -> club.asignarTurno("jaime", DayOfWeek.THURSDAY, LocalTime.of(19, 0), LocalTime.of(21, 0)), "fuera del horario");
		assertThrows(RuntimeException.class,
				() -> club.asignarTurno("jaime", DayOfWeek.THURSDAY, LocalTime.of(14, 0), LocalTime.of(10, 0)),
				"la hora de fin antes de la de inicio");
		assertThrows(RuntimeException.class,
				() -> club.asignarTurno("jaime", DayOfWeek.TUESDAY, LocalTime.of(16, 0), LocalTime.of(18, 0)),
				"se cruza con otro turno de jaime");
		assertThrows(RuntimeException.class,
				() -> club.asignarTurno("ana", DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(10, 0)), "ana no es empleada");
		club.iniciarSesion("carlos", "carlos123");
		assertThrows(RuntimeException.class,
				() -> club.asignarTurno("jaime", DayOfWeek.THURSDAY, LocalTime.of(8, 0), LocalTime.of(10, 0)),
				"un entrenador no asigna turnos");
	}

	@Test
	@DisplayName("RF-ADM-14: se aprueba un cambio que mantiene la cobertura minima")
	void aprobarCambioConCobertura() {
		String lunesLaura = solicitar("laura", "laura123", DayOfWeek.MONDAY, 14, 20);
		assertDoesNotThrow(() -> club.aprobarSolicitudCambioTurno(lunesLaura), "el lunes en la manana quedan carlos y jaime");
		assertEquals(LocalTime.of(14, 0), club.buscarTurno(turnoDe("laura", DayOfWeek.MONDAY)).getHoraInicio());
		assertThrows(RuntimeException.class, () -> club.aprobarSolicitudCambioTurno(lunesLaura), "ya resuelta");
		assertThrows(RuntimeException.class, () -> club.aprobarSolicitudCambioTurno("C99"), "solicitud inexistente");
	}

	@Test
	@DisplayName("RF-ADM-14: no se aprueba un cambio que deja menos de 2 entrenadores")
	void rechazarPorFaltaDeEntrenadores() {
		String miercolesCarlos = solicitar("carlos", "carlos123", DayOfWeek.WEDNESDAY, 8, 14);
		assertThrows(RuntimeException.class, () -> club.aprobarSolicitudCambioTurno(miercolesCarlos),
				"el miercoles en la tarde quedaria solo laura");
		assertDoesNotThrow(() -> club.rechazarSolicitudCambioTurno(miercolesCarlos));
		club.iniciarSesion("carlos", "carlos123");
		assertEquals(1, club.consultarSolicitudes().size(), "carlos ve su solicitud rechazada");
	}

	@Test
	@DisplayName("RF-ADM-14: no se aprueba un cambio que deja sin fisioterapeuta")
	void rechazarPorFaltaDeFisioterapeuta() {
		String sabadoPedro = solicitar("pedro", "pedro123", DayOfWeek.SATURDAY, 8, 14);
		assertThrows(RuntimeException.class, () -> club.aprobarSolicitudCambioTurno(sabadoPedro),
				"el sabado en la tarde no quedaria ningun fisioterapeuta");
		assertDoesNotThrow(() -> club.rechazarSolicitudCambioTurno(sabadoPedro));
	}

	@Test
	@DisplayName("RF-ADM-14: se aprueba un cambio de un turno de refuerzo")
	void aprobarCambioDeRefuerzo() {
		club.asignarTurno("jaime", DayOfWeek.TUESDAY, LocalTime.of(14, 0), LocalTime.of(20, 0));
		String martesJaime = solicitar("jaime", "jaime123", DayOfWeek.TUESDAY, 15, 20);
		assertDoesNotThrow(() -> club.aprobarSolicitudCambioTurno(martesJaime), "el martes de 14:00 a 15:00 siguen carlos y laura");
	}

	@Test
	@DisplayName("RF-ADM-14: un empleado no aprueba solicitudes")
	void empleadoNoAprueba() {
		String miercolesCarlos = solicitar("carlos", "carlos123", DayOfWeek.WEDNESDAY, 8, 14);
		club.iniciarSesion("laura", "laura123");
		assertThrows(RuntimeException.class, () -> club.aprobarSolicitudCambioTurno(miercolesCarlos));
	}

	@Test
	@DisplayName("los turnos y las solicitudes sobreviven a guardar y cargar")
	void persistencia(@TempDir Path carpeta) {
		club.asignarTurno("jaime", DayOfWeek.TUESDAY, LocalTime.of(14, 0), LocalTime.of(20, 0));
		club.aprobarSolicitudCambioTurno(solicitar("laura", "laura123", DayOfWeek.MONDAY, 14, 20));
		club.rechazarSolicitudCambioTurno(solicitar("carlos", "carlos123", DayOfWeek.WEDNESDAY, 8, 14));
		solicitar("pedro", "pedro123", DayOfWeek.SATURDAY, 8, 14);

		Club recargado = Utilidades.guardarYRecargar(club, carpeta);
		assertEquals(club.getTurnos().toString(), recargado.getTurnos().toString(), "turnos");
		assertEquals(club.getSolicitudes().toString(), recargado.getSolicitudes().toString(), "solicitudes");
	}
}
