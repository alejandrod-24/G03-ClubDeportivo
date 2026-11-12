package pruebas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import competencias.Fase;
import deportes.Categoria;
import global.Club;
import global.DatosIniciales;
import global.Reloj;
import partidos.EstadisticaBaloncesto;
import partidos.EstadisticaFutbol;

/**
 * 11. Inscripcion de equipo en competencia, definicion de nomina y registro de partidos de competencia.
 * RF-ENT-06, 07, 08 y consulta RF-GEN-05
 */
@DisplayName("11. Competencias externas")
class CompetenciaTest {

	private static final String LIGA = DatosIniciales.LIGA_CERROS;
	private static final String ASCUN = DatosIniciales.ASCUN_BALONCESTO;
	private static final String ASCUN_FUTBOL = "ASCUN Futbol";
	private static final int T = DatosIniciales.TEMPORADA;

	private static final List<String> NOMINA_FC = List.of("ana", "luis", "camila", "jorge", "maria", "andres",
			"juliana", "felipe", "daniela");
	private static final List<String> UNITED = List.of("paula", "sebastian", "natalia", "ricardo", "carolina",
			"miguel", "isabel", "nicolas");
	private static final List<String> QUINTETO_TIGRES = List.of("ana", "luis", "maria", "felipe", "paula");

	private Club club;

	@BeforeEach
	void setUp() {
		club = Utilidades.clubDePrueba();
		club.iniciarSesion("admin", "admin123");
		club.crearEquipo("Futbol", "Reservas", Categoria.MAYORES, 15);
		club.crearCompetenciaAscun(ASCUN_FUTBOL, T, "Futbol", Categoria.MAYORES,
				List.of("Universidad Nacional", "Universidad de los Andes"));
		club.iniciarSesion("carlos", "carlos123");
	}

	//Seneca FC inscrito en la liga con su nomina de 9 (santiago queda fuera)
	private void inscribirSenecaFC() {
		club.inscribirEquipoEnCompetencia(LIGA, T, "Futbol", "Seneca FC");
		club.definirNomina(LIGA, T, "Seneca FC", NOMINA_FC);
	}

	//Seneca FC vs Javeriana dentro de 2 dias
	private String programarContraJaveriana() {
		inscribirSenecaFC();
		return club.programarPartidoCompetencia(LIGA, T, "Seneca FC", "Universidad Javeriana", "Estadio El Campin",
				Reloj.hoy().plusDays(2), Fase.LIGA).getId();
	}

	private List<EstadisticaFutbol> estadisticasJaveriana() {
		return List.of(new EstadisticaFutbol("felipe", 2, 0, 0, false), new EstadisticaFutbol("daniela", 1, 2, 1, false),
				new EstadisticaFutbol("jorge", 0, 0, 0, true));
	}

	//Seneca FC 3 - 1 Javeriana, con roja para jorge
	private String jugarContraJaveriana() {
		String id = programarContraJaveriana();
		Reloj.avanzarDias(2);
		club.registrarPartidoCompetencia(id, NOMINA_FC, 3, 1, List.copyOf(estadisticasJaveriana()));
		return id;
	}

	//Tigres en ASCUN baloncesto, cuartos de final contra Nacional manana
	private String programarCuartosAscun() {
		club.inscribirEquipoEnCompetencia(ASCUN, T, "Baloncesto", "Tigres");
		club.definirNomina(ASCUN, T, "Tigres", List.of("ana", "luis", "maria", "felipe", "paula", "sebastian", "natalia"));
		return club.programarPartidoCompetencia(ASCUN, T, "Tigres", "Universidad Nacional", "Coliseo El Salitre",
				Reloj.hoy().plusDays(1), Fase.CUARTOS).getId();
	}

	//Seneca United en ASCUN futbol, semifinal contra los Andes manana
	private String programarSemifinalAscunFutbol() {
		club.inscribirEquipoEnCompetencia(ASCUN_FUTBOL, T, "Futbol", "Seneca United");
		club.definirNomina(ASCUN_FUTBOL, T, "Seneca United", UNITED);
		return club.programarPartidoCompetencia(ASCUN_FUTBOL, T, "Seneca United", "Universidad de los Andes",
				"Estadio Alfonso Lopez", Reloj.hoy().plusDays(1), Fase.SEMIFINAL).getId();
	}

	@Test
	@DisplayName("RF-ENT-06: inscribir un equipo en una competencia")
	void inscribirEquipo() {
		assertDoesNotThrow(() -> club.inscribirEquipoEnCompetencia(LIGA, T, "Futbol", "Seneca FC"));
		assertThrows(RuntimeException.class, () -> club.inscribirEquipoEnCompetencia(LIGA, T, "Futbol", "Seneca FC"),
				"inscribirlo otra vez");
		assertThrows(RuntimeException.class, () -> club.inscribirEquipoEnCompetencia(LIGA, T, "Baloncesto", "Tigres"),
				"el deporte no corresponde");
		assertThrows(RuntimeException.class, () -> club.inscribirEquipoEnCompetencia(LIGA, T, "Futbol", "Seneca Sub15"),
				"la categoria no corresponde");
		assertThrows(RuntimeException.class, () -> club.inscribirEquipoEnCompetencia(LIGA, T, "Futbol", "Reservas"),
				"equipo sin la nomina minima");
		assertThrows(RuntimeException.class, () -> club.inscribirEquipoEnCompetencia("Copa Mundial", T, "Futbol", "Seneca FC"),
				"competencia inexistente");
	}

	@Test
	@DisplayName("RF-ENT-07: definir la nomina")
	void definirNomina() {
		club.inscribirEquipoEnCompetencia(LIGA, T, "Futbol", "Seneca FC");
		assertThrows(RuntimeException.class, () -> club.definirNomina(LIGA, T, "Seneca FC",
				List.of("ana", "luis", "camila", "jorge", "maria", "andres", "juliana", "felipe", "daniela", "santiago",
						"paula", "sebastian", "natalia")), "13 jugadores superan el cupo del equipo (12)");
		assertThrows(RuntimeException.class, () -> club.definirNomina(LIGA, T, "Seneca FC",
				List.of("ana", "luis", "camila", "jorge", "maria", "andres", "paula")), "un jugador que no es del equipo");
		assertDoesNotThrow(() -> club.definirNomina(LIGA, T, "Seneca FC", NOMINA_FC));
	}

	@Test
	@DisplayName("programar partidos de competencia")
	void programarPartidoCompetencia() {
		inscribirSenecaFC();
		assertThrows(RuntimeException.class, () -> club.programarPartidoCompetencia(LIGA, T, "Seneca FC",
				"Universidad de los Andes", "Estadio El Campin", Reloj.hoy().plusDays(2), Fase.LIGA),
				"rival que no participa en la competencia");
		assertThrows(RuntimeException.class, () -> club.programarPartidoCompetencia(LIGA, T, "Seneca FC",
				"Universidad Javeriana", "Estadio El Campin", Reloj.hoy().plusDays(2), Fase.GRUPOS), "fase de grupos en una liga");
		assertNotNull(assertDoesNotThrow(() -> club.programarPartidoCompetencia(LIGA, T, "Seneca FC",
				"Universidad Javeriana", "Estadio El Campin", Reloj.hoy().plusDays(2), Fase.LIGA)));
	}

	@Test
	@DisplayName("RF-ENT-08: registrar un partido de competencia")
	void registrarPartidoCompetencia() {
		String id = programarContraJaveriana();
		assertThrows(RuntimeException.class,
				() -> club.registrarPartidoCompetencia("P999", NOMINA_FC, 3, 1, List.copyOf(estadisticasJaveriana())),
				"partido no asignado");
		assertThrows(RuntimeException.class,
				() -> club.registrarPartidoCompetencia(id, NOMINA_FC, 3, 1, List.copyOf(estadisticasJaveriana())),
				"antes de la fecha del partido");
		Reloj.avanzarDias(2);
		assertThrows(RuntimeException.class, () -> club.registrarPartidoCompetencia(id,
				List.of("ana", "luis", "camila", "jorge", "maria", "andres", "santiago"), 3, 1, List.copyOf(estadisticasJaveriana())),
				"alineado que no esta en la nomina");
		assertThrows(RuntimeException.class,
				() -> club.registrarPartidoCompetencia(id, NOMINA_FC, -3, 1, List.copyOf(estadisticasJaveriana())),
				"marcador negativo");
		assertThrows(RuntimeException.class, () -> club.registrarPartidoCompetencia(id, NOMINA_FC, 3, 1,
				List.of(new EstadisticaFutbol("santiago", 1, 0, 0, false))), "estadistica de un jugador no alineado");
		assertDoesNotThrow(() -> club.registrarPartidoCompetencia(id, NOMINA_FC, 3, 1, List.copyOf(estadisticasJaveriana())));
		assertThrows(RuntimeException.class, () -> club.registrarPartidoCompetencia(id, NOMINA_FC, 3, 1, List.of()),
				"registrar de nuevo un partido ya jugado");
	}

	@Test
	@DisplayName("RF-ENT-08: la roja de jorge lo suspende para el siguiente partido de la competencia")
	void suspensionEnCompetencia() {
		jugarContraJaveriana();
		String id2 = club.programarPartidoCompetencia(LIGA, T, "Seneca FC", "Universidad del Rosario", "Cancha Seneca",
				Reloj.hoy().plusDays(3), Fase.LIGA).getId();
		Reloj.avanzarDias(3);
		assertThrows(RuntimeException.class, () -> club.registrarPartidoCompetencia(id2, NOMINA_FC, 0, 0, List.of()),
				"jorge esta suspendido");
		assertDoesNotThrow(() -> club.registrarPartidoCompetencia(id2,
				List.of("ana", "luis", "camila", "maria", "andres", "juliana", "felipe", "daniela"), 1, 1,
				List.of(new EstadisticaFutbol("maria", 1, 0, 1, false))), "sin jorge: empate 1 - 1");
	}

	@Test
	@DisplayName("ASCUN baloncesto: fase eliminatoria sin empates")
	void ascunBaloncesto() {
		String id = assertDoesNotThrow(() -> programarCuartosAscun());
		assertThrows(RuntimeException.class, () -> club.programarPartidoCompetencia(ASCUN, T, "Tigres", "Universidad EAFIT",
				"Coliseo El Salitre", Reloj.hoy().plusDays(1), Fase.LIGA), "fase LIGA en una competencia ASCUN");
		Reloj.avanzarDias(1);
		assertThrows(RuntimeException.class, () -> club.registrarPartidoCompetencia(id, QUINTETO_TIGRES, 70, 70, List.of()),
				"empate en baloncesto");
		assertDoesNotThrow(() -> club.registrarPartidoCompetencia(id, QUINTETO_TIGRES, 78, 70,
				List.of(new EstadisticaBaloncesto("paula", 25, 12), new EstadisticaBaloncesto("ana", 18, 3))));
	}

	@Test
	@DisplayName("ASCUN futbol: en eliminatoria tampoco hay empates")
	void ascunFutbolEliminatoria() {
		String id = assertDoesNotThrow(() -> programarSemifinalAscunFutbol());
		Reloj.avanzarDias(1);
		assertThrows(RuntimeException.class, () -> club.registrarPartidoCompetencia(id, UNITED, 1, 1, List.of()),
				"empate en eliminatoria");
		assertDoesNotThrow(() -> club.registrarPartidoCompetencia(id, UNITED, 2, 0,
				List.of(new EstadisticaFutbol("isabel", 2, 0, 0, false))));
	}

	@Test
	@DisplayName("RF-GEN-05 y persistencia: partidos, nominas y estadisticas sobreviven a guardar y cargar")
	void persistencia(@TempDir Path carpeta) {
		jugarContraJaveriana();
		String cuartos = programarCuartosAscun();
		String semifinal = programarSemifinalAscunFutbol();
		Reloj.avanzarDias(1);
		club.registrarPartidoCompetencia(cuartos, QUINTETO_TIGRES, 78, 70, List.of(new EstadisticaBaloncesto("paula", 25, 12)));
		club.registrarPartidoCompetencia(semifinal, UNITED, 2, 0, List.of(new EstadisticaFutbol("isabel", 2, 0, 0, false)));

		Club recargado = Utilidades.guardarYRecargar(club, carpeta);
		recargado.iniciarSesion("carlos", "carlos123");
		for (String nombre : List.of(LIGA, ASCUN, ASCUN_FUTBOL)) {
			assertNotNull(club.consultarCompetencia(nombre, T).generarResumen());
			assertEquals(club.consultarCompetencia(nombre, T).generarResumen(),
					recargado.consultarCompetencia(nombre, T).generarResumen(), "resumen de " + nombre);
		}
		assertEquals(club.consultarSanciones("Futbol", "Seneca FC"), recargado.consultarSanciones("Futbol", "Seneca FC"),
				"las sanciones se recalculan igual");
	}
}
