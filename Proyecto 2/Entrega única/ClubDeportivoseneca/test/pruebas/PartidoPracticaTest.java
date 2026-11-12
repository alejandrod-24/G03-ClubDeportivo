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

import deportes.Nivel;
import global.Club;
import global.Reloj;
import partidos.EstadisticaFutbol;
import partidos.EstadisticaTenis;

/**
 * 6. Programacion de partidos de practica con validacion de nivel, asignacion de nivel
 *    y registro del resultado con actualizacion del ELO.
 * RF-SOC-08, 10, 11 y RF-ENT-09, 10
 */
@DisplayName("6. Partidos de practica y ranking ELO")
class PartidoPracticaTest {

	private static final LocalDate LUNES = Utilidades.LUNES;
	private static final LocalDateTime LUNES_9 = LUNES.atTime(9, 0);

	private Club club;

	@BeforeEach
	void setUp() {
		club = Utilidades.clubDePrueba();
	}

	//valentina (INTERMEDIO) programa una practica con diego (AVANZADO) el lunes a las 9:00
	private String programarValentinaVsDiego() {
		club.iniciarSesion("valentina", "1234");
		return club.programarPartidoPractica("Tenis", "TEN-1", LUNES_9, List.of("diego")).getId();
	}

	//La practica anterior ya jugada: valentina gana 2 - 1 (registra el entrenador carlos)
	private String practicaJugada() {
		String id = programarValentinaVsDiego();
		club.iniciarSesion("carlos", "carlos123");
		Reloj.fijar(LUNES.atTime(10, 5));
		club.registrarResultadoPractica(id, 2, 1,
				List.of(new EstadisticaTenis("valentina", 2, 15, 4, 6), new EstadisticaTenis("diego", 1, 11, 7, 12)));
		return id;
	}

	private int eloDe(String login) {
		return club.buscarDeporteIndividual("Tenis").getAcademia().buscarInscripcion(club.buscarSocio(login))
				.getRanking().getPuntaje();
	}

	@Test
	@DisplayName("RF-SOC-10: programar partidos de practica con niveles compatibles")
	void programarPartidoPractica() {
		String id = assertDoesNotThrow(() -> programarValentinaVsDiego(), "INTERMEDIO vs AVANZADO son compatibles");
		assertNotNull(club.buscarPartido(id));
		assertThrows(RuntimeException.class,
				() -> club.programarPartidoPractica("Tenis", "TEN-2", LUNES.atTime(10, 0), List.of("ana")),
				"INTERMEDIO vs PRINCIPIANTE");
		assertThrows(RuntimeException.class,
				() -> club.programarPartidoPractica("Tenis", "TEN-2", LUNES.atTime(10, 0), List.of("nicolas")),
				"nicolas no esta inscrito en tenis");
		assertThrows(RuntimeException.class,
				() -> club.programarPartidoPractica("Tenis", "TEN-2", LUNES.atTime(10, 0), List.of("valentina")),
				"participante repetido");
		assertThrows(RuntimeException.class,
				() -> club.programarPartidoPractica("Tenis", "TEN-2", LUNES.atTime(10, 0), List.of("luis", "diego")),
				"3 participantes no es ni sencillos ni dobles");

		club.iniciarSesion("luis", "1234");
		assertThrows(RuntimeException.class, () -> club.programarPartidoPractica("Tenis", "TEN-2", LUNES_9, List.of("diego")),
				"diego ya tiene otro compromiso a las 9:00");
		assertThrows(RuntimeException.class,
				() -> club.programarPartidoPractica("Tenis", "TEN-3", LUNES.atTime(11, 0), List.of("valentina", "diego", "carlos")),
				"dobles en TEN-3 supera la capacidad");
	}

	@Test
	@DisplayName("RF-SOC-10: contra un entrenador no se exige nivel, y se pueden jugar dobles")
	void practicaContraEntrenadorYDobles() {
		club.iniciarSesion("ana", "1234");
		assertDoesNotThrow(() -> club.programarPartidoPractica("Tenis", "TEN-2", LUNES.atTime(10, 0), List.of("carlos")),
				"ana (PRINCIPIANTE) contra el entrenador carlos");
		club.iniciarSesion("mateo", "1234");
		assertDoesNotThrow(() -> club.programarPartidoPractica("Padel", "PAD-2", LUNES_9, List.of("maria", "jorge", "juliana")),
				"dobles de padel");
	}

	@Test
	@DisplayName("RF-ENT-09: asignar nivel en la academia")
	void asignarNivel() {
		programarValentinaVsDiego();
		club.iniciarSesion("carlos", "carlos123");
		assertThrows(RuntimeException.class, () -> club.asignarNivel("Tenis", "diego", Nivel.PRINCIPIANTE),
				"bajar a diego inhabilita su partido con valentina");
		assertDoesNotThrow(() -> club.asignarNivel("Tenis", "luis", Nivel.AVANZADO));
		assertThrows(RuntimeException.class, () -> club.asignarNivel("Tenis", "nicolas", Nivel.INTERMEDIO),
				"socio no inscrito en la academia");
	}

	@Test
	@DisplayName("RF-ENT-10: registrar el resultado de una practica y actualizar el ELO")
	void registrarResultadoPractica() {
		String id = programarValentinaVsDiego();
		int eloInicial = eloDe("valentina");
		club.iniciarSesion("carlos", "carlos123");
		assertThrows(RuntimeException.class, () -> club.registrarResultadoPractica(id, 2, 1, List.of()),
				"el bloque aun no ha comenzado");

		Reloj.fijar(LUNES.atTime(10, 5));
		assertThrows(RuntimeException.class, () -> club.registrarResultadoPractica(id, 1, 1, List.of()), "empate en sets");
		assertThrows(RuntimeException.class,
				() -> club.registrarResultadoPractica(id, 2, 1, List.of(new EstadisticaTenis("valentina", 2, 14, -1, 3))),
				"valores negativos");
		assertThrows(RuntimeException.class,
				() -> club.registrarResultadoPractica(id, 2, 1, List.of(new EstadisticaTenis("valentina", 1, 14, 2, 3))),
				"valentina registra 1 set pero su lado gano 2");
		assertThrows(RuntimeException.class,
				() -> club.registrarResultadoPractica(id, 2, 1, List.of(new EstadisticaTenis("valentina", 2, 8, 2, 3))),
				"2 sets con solo 8 games");
		assertThrows(RuntimeException.class,
				() -> club.registrarResultadoPractica(id, 2, 1, List.of(new EstadisticaFutbol("valentina", 1, 0, 0, false))),
				"estadistica de otro deporte");
		assertDoesNotThrow(() -> club.registrarResultadoPractica(id, 2, 1,
				List.of(new EstadisticaTenis("valentina", 2, 15, 4, 6), new EstadisticaTenis("diego", 1, 11, 7, 12))));
		assertThrows(RuntimeException.class, () -> club.registrarResultadoPractica(id, 2, 0, List.of()), "registrar otra vez");
		assertTrue(eloDe("valentina") > eloInicial, "valentina sube de ELO al ganarle a un rival de mayor puntaje");
	}

	@Test
	@DisplayName("RF-ENT-10: registrar una practica contra el entrenador")
	void registrarPracticaContraEntrenador() {
		club.iniciarSesion("ana", "1234");
		String id = club.programarPartidoPractica("Tenis", "TEN-2", LUNES.atTime(10, 0), List.of("carlos")).getId();
		club.iniciarSesion("carlos", "carlos123");
		Reloj.fijar(LUNES.atTime(11, 5));
		assertDoesNotThrow(() -> club.registrarResultadoPractica(id, 0, 2, List.of()));
	}

	@Test
	@DisplayName("RF-SOC-08: cancelar un partido de practica")
	void cancelarPartidoPractica() {
		String jugado = practicaJugada();
		club.iniciarSesion("luis", "1234");
		String martes = club.programarPartidoPractica("Tenis", "TEN-1", LUNES.plusDays(1).atTime(9, 0), List.of("valentina")).getId();

		club.iniciarSesion("valentina", "1234");
		assertThrows(RuntimeException.class, () -> club.cancelarPartidoPractica(martes), "valentina no la programo");
		assertThrows(RuntimeException.class, () -> club.cancelarPartidoPractica(jugado), "ya tiene resultado");
		club.iniciarSesion("luis", "1234");
		assertDoesNotThrow(() -> club.cancelarPartidoPractica(martes));
	}

	@Test
	@DisplayName("RF-SOC-11: consultar partidos y ranking")
	void consultarPartidosYRanking() {
		practicaJugada();
		club.iniciarSesion("valentina", "1234");
		assertFalse(club.consultarPartidosYRanking().isBlank());
	}

	@Test
	@DisplayName("el historial de partidos del administrador incluye las practicas")
	void historialIncluyePracticas() {
		club.iniciarSesion("ana", "1234");
		club.programarPartidoPractica("Tenis", "TEN-2", LUNES.atTime(10, 0), List.of("carlos"));
		club.iniciarSesion("admin", "admin123");
		assertEquals(1, club.historialPartidos(null, null, null, "carlos").size());
	}

	@Test
	@DisplayName("las practicas y el ranking sobreviven a guardar y cargar")
	void persistencia(@TempDir Path carpeta) {
		practicaJugada();
		club.iniciarSesion("valentina", "1234");
		Club recargado = Utilidades.guardarYRecargar(club, carpeta);
		recargado.iniciarSesion("valentina", "1234");
		assertEquals(club.consultarPartidosYRanking(), recargado.consultarPartidosYRanking());
	}
}
