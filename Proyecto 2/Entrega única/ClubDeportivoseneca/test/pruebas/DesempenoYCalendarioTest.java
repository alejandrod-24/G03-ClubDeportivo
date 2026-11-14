package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import competencias.Fase;
import entrenamiento.Asistencia;
import global.Club;
import global.Reloj;
import instalaciones.Modalidad;

/**
 * 7. Consulta de desempeno en entrenamiento y de calendario de un socio.
 * RF-SOC-12, 13
 */
@DisplayName("7. Desempeno en entrenamiento y calendario del socio")
class DesempenoYCalendarioTest {

	private static final LocalDate LUNES = Utilidades.LUNES;
	private static final List<String> FC = List.of("ana", "luis", "camila", "jorge", "maria", "andres", "juliana");

	private Club club;
	private String sesionFutbol;

	@BeforeEach
	void setUp() {
		club = Utilidades.clubDePrueba();
		prepararDatos();
	}

	//Una sesion de futbol y una de tenis el lunes, un partido interno el martes, uno de liga el viernes,
	//una reserva y una practica de diego y una practica de ana
	private void prepararDatos() {
		club.iniciarSesion("carlos", "carlos123");
		sesionFutbol = club.planificarSesion("FUT-1", LUNES.atTime(9, 0), 90, FC).getId();
		club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", LUNES.plusDays(1));
		club.inscribirEquipoEnCompetencia("Liga de los Cerros", 2026, "Futbol", "Seneca FC");
		club.definirNomina("Liga de los Cerros", 2026, "Seneca FC", FC);
		club.programarPartidoCompetencia("Liga de los Cerros", 2026, "Seneca FC", "Universidad del Rosario", "Cancha Seneca",
				LUNES.plusDays(4), Fase.LIGA);
		club.iniciarSesion("laura", "laura123");
		club.planificarSesion("TEN-1", LUNES.atTime(15, 0), 60, List.of("diego", "valentina"));
		club.iniciarSesion("diego", "1234");
		club.reservarInstalacion("Tenis", "TEN-2", LUNES.plusDays(1).atTime(9, 0), 2, Modalidad.SENCILLOS);
		club.iniciarSesion("valentina", "1234");
		club.programarPartidoPractica("Tenis", "TEN-1", LUNES.plusDays(2).atTime(10, 0), List.of("diego"));
		club.iniciarSesion("ana", "1234");
		club.programarPartidoPractica("Tenis", "TEN-3", LUNES.plusDays(2).atTime(16, 0), List.of("camila"));
		club.cerrarSesion();
	}

	//Pasa la sesion de futbol y el entrenador registra asistencia y notas
	private void registrarSesionFutbol() {
		Reloj.fijar(LUNES.atTime(11, 0));
		club.iniciarSesion("carlos", "carlos123");
		club.registrarAsistencia(sesionFutbol, "ana", Asistencia.ASISTIO);
		club.registrarAsistencia(sesionFutbol, "luis", Asistencia.LLEGO_TARDE);
		club.registrarAsistencia(sesionFutbol, "camila", Asistencia.FALTO);
		club.registrarEvaluacion(sesionFutbol, "ana", 5, "Excelente lectura de juego");
		club.registrarEvaluacion(sesionFutbol, "luis", 3, "Debe llegar a tiempo");
	}

	@Test
	@DisplayName("RF-SOC-13: el calendario muestra entrenamientos, partidos, reservas y practicas")
	void consultarCalendario() {
		club.iniciarSesion("ana", "1234");
		assertEquals(4, club.consultarCalendario().size(),
				"ana: entrenamiento, partido interno, partido de liga y practica");
		club.iniciarSesion("diego", "1234");
		assertEquals(3, club.consultarCalendario().size(), "diego: entrenamiento de tenis, reserva y practica");
	}

	@Test
	@DisplayName("RF-SOC-12: consultar el desempeno en entrenamiento")
	void consultarDesempeno() {
		registrarSesionFutbol();
		club.iniciarSesion("ana", "1234");
		assertTrue(club.consultarDesempeno().contains("Excelente lectura de juego"), "ana ve la nota de su entrenador");
		club.iniciarSesion("luis", "1234");
		assertTrue(club.consultarDesempeno().contains("Debe llegar a tiempo"));
		club.iniciarSesion("valentina", "1234");
		assertFalse(club.consultarDesempeno().isBlank(), "valentina, sin sesiones evaluadas, tambien puede consultar");
	}

	@Test
	@DisplayName("RF-SOC-13: el calendario solo muestra lo que viene")
	void calendarioSoloFuturo() {
		registrarSesionFutbol();
		club.iniciarSesion("ana", "1234");
		assertFalse(club.consultarCalendario().toString().contains(sesionFutbol + "]"), "la sesion que ya paso no aparece");
	}

	@Test
	@DisplayName("RF-SOC-13: el administrador no tiene calendario de socio")
	void administradorSinCalendario() {
		club.iniciarSesion("admin", "admin123");
		assertThrows(RuntimeException.class, () -> club.consultarCalendario());
	}
}
