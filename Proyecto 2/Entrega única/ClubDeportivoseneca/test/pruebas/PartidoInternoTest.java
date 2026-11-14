package pruebas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import deportes.Categoria;
import global.Club;
import global.Reloj;
import partidos.EstadisticaBaloncesto;
import partidos.EstadisticaFutbol;

/**
 * 10. Programacion de un partido interno, registro de alineacion, marcador,
 *     estadisticas y tarjetas, suspensiones y declaracion de partido no jugado.
 * RF-ENT-01 a 05
 */
@DisplayName("10. Partidos internos")
class PartidoInternoTest {

	private static final List<String> FC = List.of("ana", "luis", "camila", "jorge", "maria", "andres",
			"juliana", "felipe", "daniela", "santiago");
	private static final List<String> UNITED = List.of("paula", "sebastian", "natalia", "ricardo", "carolina",
			"miguel", "isabel", "nicolas");

	private Club club;
	private LocalDate hoy;

	@BeforeEach
	void setUp() {
		club = Utilidades.clubDePrueba();
		club.iniciarSesion("admin", "admin123");
		club.crearEquipo("Futbol", "Reservas", Categoria.MAYORES, 15);
		club.iniciarSesion("carlos", "carlos123");
		hoy = Reloj.hoy();
	}

	//Seneca FC 2 - 1 Seneca United, jugado dentro de 3 dias
	private String partidoJugado() {
		String id = club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", hoy.plusDays(3)).getId();
		Reloj.avanzarDias(3);
		club.registrarResultadoInterno(id, FC, UNITED, 2, 1);
		return id;
	}

	//El partido anterior con goles y tarjetas: maria con 2 amarillas y luis con roja
	private String partidoConTarjetas() {
		String id = partidoJugado();
		club.registrarEstadistica(id, new EstadisticaFutbol("felipe", 2, 1, 0, false));
		club.registrarEstadistica(id, new EstadisticaFutbol("maria", 0, 0, 2, false));
		club.registrarEstadistica(id, new EstadisticaFutbol("luis", 0, 0, 0, true));
		club.registrarEstadistica(id, new EstadisticaFutbol("isabel", 1, 0, 1, false));
		return id;
	}

	@Test
	@DisplayName("RF-ENT-01: programar un partido interno")
	void programarPartidoInterno() {
		assertNotNull(assertDoesNotThrow(
				() -> club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", hoy.plusDays(3))));
		assertThrows(RuntimeException.class,
				() -> club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca FC", hoy.plusDays(3)), "un equipo contra si mismo");
		assertThrows(RuntimeException.class,
				() -> club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", hoy.minusDays(1)), "fecha pasada");
		assertThrows(RuntimeException.class,
				() -> club.programarPartidoInterno("Futbol", "Seneca Sub15", "Seneca FC", hoy.plusDays(3)), "distinta categoria");
		assertThrows(RuntimeException.class,
				() -> club.programarPartidoInterno("Futbol", "Seneca FC", "Tigres", hoy.plusDays(3)), "distinto deporte");
		assertThrows(RuntimeException.class,
				() -> club.programarPartidoInterno("Futbol", "Seneca FC", "Reservas", hoy.plusDays(3)),
				"equipo sin el minimo de 7 jugadores");
	}

	@Test
	@DisplayName("RF-ENT-02: registrar alineacion y marcador")
	void registrarResultado() {
		String id = club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", hoy.plusDays(3)).getId();
		assertThrows(RuntimeException.class, () -> club.registrarResultadoInterno(id, FC, UNITED, 2, 1),
				"antes de la fecha del partido");
		Reloj.avanzarDias(3);
		assertThrows(RuntimeException.class, () -> club.registrarResultadoInterno(id,
				List.of("ana", "luis", "camila", "jorge", "maria", "andres", "paula"), UNITED, 2, 1), "alineado de otro equipo");
		assertThrows(RuntimeException.class,
				() -> club.registrarResultadoInterno(id, List.of("ana", "luis", "camila"), UNITED, 2, 1),
				"alineacion con menos del minimo");
		assertThrows(RuntimeException.class, () -> club.registrarResultadoInterno(id, FC, UNITED, -1, 0), "marcador negativo");
		assertDoesNotThrow(() -> club.registrarResultadoInterno(id, FC, UNITED, 2, 1));
		assertThrows(RuntimeException.class, () -> club.registrarResultadoInterno(id, FC, UNITED, 3, 1),
				"registrar de nuevo un partido ya jugado");
	}

	@Test
	@DisplayName("RF-ENT-03: registrar estadisticas y tarjetas")
	void registrarEstadisticas() {
		String id = partidoJugado();
		assertDoesNotThrow(() -> club.registrarEstadistica(id, new EstadisticaFutbol("felipe", 2, 1, 0, false)));
		assertDoesNotThrow(() -> club.registrarEstadistica(id, new EstadisticaFutbol("maria", 0, 0, 2, false)));
		assertDoesNotThrow(() -> club.registrarEstadistica(id, new EstadisticaFutbol("luis", 0, 0, 0, true)));
		assertDoesNotThrow(() -> club.registrarEstadistica(id, new EstadisticaFutbol("isabel", 1, 0, 1, false)));
		assertThrows(RuntimeException.class, () -> club.registrarEstadistica(id, new EstadisticaFutbol("diego", 1, 0, 0, false)),
				"jugador no alineado");
		assertThrows(RuntimeException.class, () -> club.registrarEstadistica(id, new EstadisticaBaloncesto("ana", 10, 2)),
				"estadistica de otro deporte");
		assertThrows(RuntimeException.class, () -> club.registrarEstadistica(id, new EstadisticaFutbol("ana", -1, 0, 0, false)),
				"valores negativos");
		assertEquals(4, club.buscarPartido(id).getEstadisticas().size());
	}

	@Test
	@DisplayName("RF-ENT-05: consultar sanciones")
	void consultarSanciones() {
		partidoConTarjetas();
		assertEquals(2, club.consultarSanciones("Futbol", "Seneca FC").size(), "maria y luis quedan suspendidos");
		assertThrows(RuntimeException.class, () -> club.consultarSanciones("Futbol", "Galacticos"), "equipo inexistente");
	}

	@Test
	@DisplayName("RF-ENT-05: las suspensiones se cumplen en el siguiente partido del equipo")
	void cumplirSuspension() {
		partidoConTarjetas();
		String id2 = club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", Reloj.hoy().plusDays(4)).getId();
		Reloj.avanzarDias(4);
		assertThrows(RuntimeException.class, () -> club.registrarResultadoInterno(id2,
				List.of("ana", "camila", "jorge", "maria", "andres", "juliana", "felipe", "daniela"), UNITED, 1, 1),
				"maria esta suspendida");
		assertDoesNotThrow(() -> club.registrarResultadoInterno(id2,
				List.of("ana", "camila", "jorge", "andres", "juliana", "felipe", "daniela", "santiago"), UNITED, 1, 1),
				"sin maria ni luis: empate 1 - 1 (el futbol admite empates)");
	}

	@Test
	@DisplayName("RF-ENT-02: el baloncesto no admite empates")
	void baloncestoSinEmpates() {
		String id = club.programarPartidoInterno("Baloncesto", "Tigres", "Panteras", hoy.plusDays(1)).getId();
		Reloj.avanzarDias(1);
		List<String> tigres = List.of("ana", "luis", "maria", "felipe", "paula");
		List<String> panteras = List.of("camila", "jorge", "andres", "juliana", "daniela");
		assertThrows(RuntimeException.class, () -> club.registrarResultadoInterno(id, tigres, panteras, 80, 80), "empate");
		assertDoesNotThrow(() -> club.registrarResultadoInterno(id, tigres, panteras, 85, 80));
		assertDoesNotThrow(() -> club.registrarEstadistica(id, new EstadisticaBaloncesto("ana", 30, 5)));
	}

	@Test
	@DisplayName("RF-ENT-04: declarar un partido como no jugado")
	void declararNoJugado() {
		String jugado = partidoJugado();
		String id = club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", Reloj.hoy().plusDays(7)).getId();
		assertDoesNotThrow(() -> club.declararNoJugado(id));
		assertThrows(RuntimeException.class, () -> club.declararNoJugado(jugado), "un partido ya jugado");
	}

	@Test
	@DisplayName("RF-GEN-07: calendario y resultados del equipo")
	void calendarioEquipo() {
		partidoJugado();
		assertFalse(club.consultarCalendarioEquipo("Futbol", "Seneca FC").isEmpty());
	}
}
