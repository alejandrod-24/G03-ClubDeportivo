package pruebas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import entrenamiento.Asistencia;
import global.Club;
import global.Reloj;
import instalaciones.Modalidad;

/**
 * 12. Planeacion, modificacion y cancelacion de sesiones de entrenamiento, registro de asistencia
 *     y evaluacion, y consulta de analiticas y agenda del entrenador.
 * RF-ENT-11 a 16
 */
@DisplayName("12. Sesiones de entrenamiento")
class EntrenamientoTest {

	private static final LocalDate LUNES = Utilidades.LUNES;
	private static final LocalDateTime LUNES_9 = LUNES.atTime(9, 0);
	private static final LocalDate OCTUBRE = LocalDate.of(2026, 10, 1);
	private static final LocalDate FIN_OCTUBRE = LocalDate.of(2026, 10, 31);

	private static final List<String> FC = List.of("ana", "luis", "camila", "jorge", "maria", "andres", "juliana");
	private static final List<String> TIGRES = List.of("ana", "luis", "maria", "felipe", "paula");

	private Club club;

	@BeforeEach
	void setUp() {
		club = Utilidades.clubDePrueba();
		club.iniciarSesion("carlos", "carlos123");
	}

	//carlos planifica futbol en FUT-1 el lunes de 8:00 a 10:00
	private String sesionFutbol() {
		club.iniciarSesion("carlos", "carlos123");
		return club.planificarSesion("FUT-1", LUNES.atTime(8, 0), 120, List.of("ana", "luis", "camila", "jorge", "felipe")).getId();
	}

	//laura planifica tenis en TEN-1 el martes a las 9:00
	private String sesionTenis() {
		club.iniciarSesion("laura", "laura123");
		return club.planificarSesion("TEN-1", LUNES.plusDays(1).atTime(9, 0), 60, List.of("diego", "valentina")).getId();
	}

	//La sesion de futbol ya paso, con asistencia y notas registradas por carlos
	private String sesionFutbolEvaluada() {
		String id = sesionFutbol();
		Reloj.fijar(LUNES.atTime(10, 5));
		club.registrarAsistencia(id, "ana", Asistencia.ASISTIO);
		club.registrarAsistencia(id, "luis", Asistencia.LLEGO_TARDE);
		club.registrarAsistencia(id, "camila", Asistencia.FALTO);
		club.registrarAsistencia(id, "jorge", Asistencia.FALTO);
		club.registrarAsistencia(id, "felipe", Asistencia.ASISTIO);
		club.registrarEvaluacion(id, "ana", 5, "Gran liderazgo");
		club.registrarEvaluacion(id, "luis", 3, "Mejorar la puntualidad");
		club.registrarEvaluacion(id, "felipe", 4, "");
		return id;
	}

	@Test
	@DisplayName("RF-ENT-11: planificar una sesion")
	void planificarSesion() {
		String id = assertDoesNotThrow(() -> club.planificarSesion("FUT-1", LUNES_9, 90, FC).getId());
		assertNotNull(club.buscarSesion(id));
		assertThrows(RuntimeException.class, () -> club.planificarSesion("FUT-1", LocalDate.of(2026, 10, 16).atTime(9, 0), 60, FC),
				"fecha pasada");
		assertThrows(RuntimeException.class, () -> club.planificarSesion("BAL-1", LUNES.atTime(14, 0), 0, TIGRES), "duracion cero");
		assertThrows(RuntimeException.class, () -> club.planificarSesion("BAL-1", LUNES.atTime(19, 30), 60, TIGRES),
				"termina despues del cierre");
		assertThrows(RuntimeException.class,
				() -> club.planificarSesion("BAL-1", LUNES.atTime(10, 0), 60, List.of("felipe", "paula")),
				"otra sesion de carlos se cruza");
		assertThrows(RuntimeException.class,
				() -> club.planificarSesion("BAL-1", LUNES.atTime(14, 0), 60, List.of("felipe", "diego")),
				"convocado no inscrito en baloncesto");
		assertThrows(RuntimeException.class,
				() -> club.planificarSesion("BAL-1", LUNES.atTime(14, 0), 60, List.of("felipe", "felipe")), "convocado repetido");

		club.iniciarSesion("laura", "laura123");
		assertThrows(RuntimeException.class, () -> club.planificarSesion("FUT-1", LUNES.atTime(10, 0), 60, List.of("santiago")),
				"instalacion ocupada");
		assertThrows(RuntimeException.class, () -> club.planificarSesion("BAL-1", LUNES.atTime(9, 30), 60, TIGRES),
				"ana ya esta en la sesion de carlos");
	}

	@Test
	@DisplayName("RF-ENT-11: la sesion bloquea la agenda de los convocados")
	void sesionBloqueaAgenda() {
		sesionTenis();
		club.iniciarSesion("diego", "1234");
		assertThrows(RuntimeException.class,
				() -> club.reservarInstalacion("Tenis", "TEN-2", LUNES.plusDays(1).atTime(9, 0), 2, Modalidad.SENCILLOS));
	}

	@Test
	@DisplayName("RF-ENT-12: modificar o cancelar una sesion")
	void modificarYCancelarSesion() {
		String futbol = club.planificarSesion("FUT-1", LUNES_9, 90, FC).getId();
		String tenis = sesionTenis();

		club.iniciarSesion("carlos", "carlos123");
		assertThrows(RuntimeException.class,
				() -> club.modificarSesion(tenis, "TEN-1", LUNES.plusDays(1).atTime(10, 0), 60, List.of("diego")),
				"carlos no puede modificar la sesion de laura");
		assertDoesNotThrow(() -> club.modificarSesion(futbol, "FUT-1", LUNES.atTime(8, 0), 120,
				List.of("ana", "luis", "camila", "jorge", "felipe")));
		assertEquals(LUNES.atTime(8, 0), club.buscarSesion(futbol).getInicio());

		club.iniciarSesion("laura", "laura123");
		assertDoesNotThrow(() -> club.cancelarSesion(tenis));
		assertThrows(RuntimeException.class,
				() -> club.modificarSesion(tenis, "TEN-1", LUNES.plusDays(1).atTime(10, 0), 60, List.of("diego")),
				"modificar una sesion cancelada");
	}

	@Test
	@DisplayName("RF-ENT-12: no se modifica ni cancela una sesion que ya empezo")
	void sesionYaEmpezada() {
		String id = sesionFutbol();
		Reloj.fijar(LUNES_9);
		assertThrows(RuntimeException.class, () -> club.registrarAsistencia(id, "ana", Asistencia.ASISTIO),
				"asistencia antes de que termine la sesion");
		assertThrows(RuntimeException.class, () -> club.modificarSesion(id, "FUT-1", LUNES.atTime(15, 0), 60, FC),
				"modificar una sesion que ya empezo");
		assertThrows(RuntimeException.class, () -> club.cancelarSesion(id), "cancelar una sesion que ya empezo");
	}

	@Test
	@DisplayName("RF-ENT-13, 14: registrar asistencia y evaluacion")
	void registrarAsistenciaYEvaluacion() {
		String id = sesionFutbol();
		Reloj.fijar(LUNES.atTime(10, 5));
		assertDoesNotThrow(() -> {
			club.registrarAsistencia(id, "ana", Asistencia.ASISTIO);
			club.registrarAsistencia(id, "luis", Asistencia.LLEGO_TARDE);
			club.registrarAsistencia(id, "camila", Asistencia.FALTO);
			club.registrarAsistencia(id, "jorge", Asistencia.FALTO);
			club.registrarAsistencia(id, "felipe", Asistencia.ASISTIO);
		});
		assertThrows(RuntimeException.class, () -> club.registrarAsistencia(id, "maria", Asistencia.ASISTIO),
				"alguien no convocado");
		assertThrows(RuntimeException.class, () -> club.registrarEvaluacion(id, "camila", 4, ""), "evaluar a quien falto");
		assertThrows(RuntimeException.class, () -> club.registrarEvaluacion(id, "ana", 6, ""), "nota fuera de 1 a 5");
		assertDoesNotThrow(() -> {
			club.registrarEvaluacion(id, "ana", 5, "Gran liderazgo");
			club.registrarEvaluacion(id, "luis", 3, "Mejorar la puntualidad");
			club.registrarEvaluacion(id, "felipe", 4, "");
		});
		assertTrue(club.buscarSesion(id).generarDetalle().contains("Gran liderazgo"));
	}

	@Test
	@DisplayName("RF-ENT-15: analiticas de jugador o grupo")
	void consultarAnaliticas() {
		sesionFutbolEvaluada();
		assertFalse(club.consultarAnaliticasJugador("ana", OCTUBRE, FIN_OCTUBRE).isBlank());
		assertFalse(club.consultarAnaliticasGrupo("Futbol", "Seneca FC", OCTUBRE, FIN_OCTUBRE).isBlank());
		assertFalse(club.consultarAnaliticasGrupo("Tenis", null, OCTUBRE, FIN_OCTUBRE).isBlank());
		assertThrows(RuntimeException.class, () -> club.consultarAnaliticasJugador("ana", FIN_OCTUBRE, OCTUBRE), "periodo invalido");
		assertThrows(RuntimeException.class, () -> club.consultarAnaliticasJugador("fantasma", OCTUBRE, FIN_OCTUBRE),
				"jugador inexistente");
		assertThrows(RuntimeException.class, () -> club.consultarAnaliticasGrupo("Futbol", "Galacticos", OCTUBRE, FIN_OCTUBRE),
				"grupo inexistente");
	}

	@Test
	@DisplayName("RF-ENT-16: la agenda del entrenador incluye sesiones, partidos internos y practicas")
	void consultarAgenda() {
		club.planificarSesion("BAL-1", LUNES.plusDays(2).atTime(17, 0), 60, TIGRES);
		club.programarPartidoInterno("Baloncesto", "Tigres", "Panteras", LUNES.plusDays(3));
		club.iniciarSesion("ana", "1234");
		club.programarPartidoPractica("Tenis", "TEN-1", LUNES.plusDays(1).atTime(15, 0), List.of("carlos"));
		club.iniciarSesion("carlos", "carlos123");
		assertEquals(3, club.consultarAgenda().size());
	}

	@Test
	@DisplayName("las sesiones sobreviven a guardar y cargar")
	void persistencia(@TempDir Path carpeta) {
		sesionFutbolEvaluada();
		String tenis = sesionTenis();
		club.cancelarSesion(tenis);
		club.iniciarSesion("admin", "admin123");
		Club recargado = Utilidades.guardarYRecargar(club, carpeta);
		recargado.iniciarSesion("admin", "admin123");
		assertEquals(club.historialSesiones(null, null, null, null, null), recargado.historialSesiones(null, null, null, null, null));
	}
}
