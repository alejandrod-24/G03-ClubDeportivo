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

import administracion.TipoPeriodo;
import competencias.Fase;
import entrenamiento.Asistencia;
import global.Club;
import global.DatosIniciales;
import global.Reloj;
import instalaciones.Modalidad;
import instalaciones.Reserva;
import partidos.EstadisticaFutbol;
import partidos.EstadisticaTenis;

/**
 * 17. Historiales y reportes del administrador.
 * Historiales de reservas, partidos y sesiones con filtros, y los reportes de ventas, asistencias,
 * resultados y rankings. RF-ADM-15 a 21
 */
@DisplayName("17. Historiales y reportes (administrador)")
class HistorialesYReportesTest {

	private static final List<String> FC = List.of("ana", "luis", "camila", "jorge", "maria", "andres", "juliana",
			"felipe", "daniela");
	private static final List<String> UNITED = List.of("paula", "sebastian", "natalia", "ricardo", "carolina",
			"miguel", "isabel", "nicolas");
	private static final LocalDate OCTUBRE = LocalDate.of(2026, 10, 1);
	private static final LocalDate FIN_OCTUBRE = LocalDate.of(2026, 10, 31);
	private static final LocalDate FIN_NOVIEMBRE = LocalDate.of(2026, 11, 30);

	private Club club;

	//Todas las pruebas parten del mismo historial: reservas, partidos, sesiones y ventas de octubre y noviembre
	@BeforeEach
	void setUp() {
		club = Utilidades.clubDePrueba();
		prepararReservasYSesiones();
		prepararDatos();
		registrarAsistenciaYPractica();
		club.iniciarSesion("admin", "admin123");
	}

	//Antes de que avance el reloj: reservas (una cancelada), dos sesiones y dos practicas
	private void prepararReservasYSesiones() {
		LocalDate lunes = Utilidades.LUNES;
		club.iniciarSesion("diego", "1234");
		club.reservarInstalacion("Tenis", "TEN-1", lunes.atTime(9, 0), 2, Modalidad.SENCILLOS);
		club.iniciarSesion("valentina", "1234");
		String cancelada = club.reservarInstalacion("Tenis", "TEN-2", lunes.atTime(9, 0), 4, Modalidad.DOBLES).getId();
		club.cancelarReserva(cancelada);
		club.programarPartidoPractica("Tenis", "TEN-1", lunes.plusDays(1).atTime(10, 0), List.of("diego"));
		club.iniciarSesion("maria", "1234");
		club.reservarInstalacion("Padel", "PAD-2", lunes.atTime(10, 0), 4, Modalidad.DOBLES);
		club.programarPartidoPractica("Padel", "PAD-1", lunes.plusDays(1).atTime(11, 0), List.of("mateo", "jorge", "juliana"));
		club.iniciarSesion("carlos", "carlos123");
		club.planificarSesion("FUT-1", lunes.plusDays(1).atTime(8, 0), 90, List.of("ana", "felipe", "daniela", "santiago"));
		club.iniciarSesion("laura", "laura123");
		club.planificarSesion("TEN-3", lunes.plusDays(1).atTime(14, 0), 60, List.of("luis", "camila"));
		club.cerrarSesion();
	}

	//Partidos internos y de competencia, ventas en octubre y noviembre y mensualidades
	private void prepararDatos() {
		LocalDate dia1 = Reloj.hoy().plusDays(1);
		club.iniciarSesion("carlos", "carlos123");
		String interno = club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", dia1).getId();
		String baloncesto = club.programarPartidoInterno("Baloncesto", "Tigres", "Panteras", dia1).getId();
		club.inscribirEquipoEnCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Futbol", "Seneca FC");
		club.definirNomina(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Seneca FC", FC);
		String liga = club.programarPartidoCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Seneca FC",
				"Universidad Javeriana", "Cancha Seneca", Reloj.hoy().plusDays(2), Fase.LIGA).getId();
		club.iniciarSesion("ana", "1234");
		club.comprarEnTienda(Utilidades.items("Balon de futbol", 2), null, 0);
		club.pagarMensualidad();
		club.iniciarSesion("luis", "1234");
		club.comprarEnCafeteria(Utilidades.items("Cafe", 2, "Galletas de mani", 1), 0, 10, true);
		club.pagarMensualidad();

		Reloj.avanzarDias(1);
		club.iniciarSesion("carlos", "carlos123");
		club.registrarResultadoInterno(interno, FC, UNITED, 2, 1);
		club.registrarEstadistica(interno, new EstadisticaFutbol("felipe", 2, 0, 0, false));
		club.registrarResultadoInterno(baloncesto, List.of("ana", "luis", "maria", "felipe", "paula"),
				List.of("camila", "jorge", "andres", "juliana", "daniela"), 70, 75);
		club.iniciarSesion("pedro", "pedro123");
		club.comprarEnTienda(Utilidades.items("Raqueta de tenis", 1), null, 0);

		Reloj.avanzarDias(1);
		club.iniciarSesion("carlos", "carlos123");
		club.registrarPartidoCompetencia(liga, FC, 3, 1, List.of(new EstadisticaFutbol("daniela", 2, 1, 1, false)));
		club.iniciarSesion("ana", "1234");
		club.comprarEnCafeteria(Utilidades.items("Jugo natural", 1), 1000, 10, false);

		Reloj.fijar(LocalDate.of(2026, 11, 3));
		club.comprarEnTienda(Utilidades.items("Uniforme Seneca", 1), "DESC-LAURA", 0);
		club.pagarMensualidad();
		club.cerrarSesion();
	}

	//Ya en noviembre: asistencia y notas de las sesiones de octubre, y resultados de las practicas
	private void registrarAsistenciaYPractica() {
		club.iniciarSesion("carlos", "carlos123");
		club.registrarAsistencia("S1", "ana", Asistencia.ASISTIO);
		club.registrarAsistencia("S1", "felipe", Asistencia.LLEGO_TARDE);
		club.registrarAsistencia("S1", "daniela", Asistencia.ASISTIO);
		club.registrarAsistencia("S1", "santiago", Asistencia.FALTO);
		club.registrarEvaluacion("S1", "ana", 4, "Buena definicion");
		club.registrarEvaluacion("S1", "felipe", 3, "");
		club.registrarResultadoPractica(practica("Tenis"), 2, 0, List.of(new EstadisticaTenis("valentina", 2, 12, 3, 4),
				new EstadisticaTenis("diego", 0, 5, 1, 9)));
		club.registrarResultadoPractica(practica("Padel"), 1, 2, List.of());
		club.iniciarSesion("laura", "laura123");
		club.registrarAsistencia("S2", "luis", Asistencia.ASISTIO);
		club.registrarAsistencia("S2", "camila", Asistencia.ASISTIO);
		club.registrarEvaluacion("S2", "camila", 5, "Gran avance en el reves");
		club.cerrarSesion();
	}

	private String practica(String deporte) {
		return club.buscarDeporteIndividual(deporte).getPartidos().get(0).getId();
	}

	@Test
	@DisplayName("RF-ADM-15: historial de reservas con filtros, incluye cancelaciones")
	void historialReservas() {
		assertEquals(3, club.historialReservas(null, null, null, null, null).size(), "todas las reservas");
		assertEquals(1, club.historialReservas(null, null, null, "TEN-2", null).size(), "reservas de TEN-2");
		assertEquals(1, club.historialReservas("Padel", null, null, null, null).size(), "reservas de padel");
		assertEquals(1, club.historialReservas(null, OCTUBRE, FIN_OCTUBRE, null, "diego").size(), "diego en octubre");
		List<Reserva> deValentina = club.historialReservas(null, null, null, null, "valentina");
		assertTrue(deValentina.get(0).estaCancelada(), "la reserva cancelada de valentina aparece en el historial");
		assertThrows(RuntimeException.class, () -> club.historialReservas(null, null, null, "X-1", null), "instalacion inexistente");
		assertThrows(RuntimeException.class, () -> club.historialReservas(null, FIN_OCTUBRE, OCTUBRE, null, null), "periodo invalido");
	}

	@Test
	@DisplayName("RF-ADM-16: historial de partidos con filtros")
	void historialPartidos() {
		assertEquals(5, club.historialPartidos(null, null, null, null).size(),
				"2 internos, 1 de liga y 2 practicas");
		assertEquals(1, club.historialPartidos("Baloncesto", null, null, null).size(), "solo baloncesto");
		assertEquals(3, club.historialPartidos(null, null, null, "daniela").size(), "partidos en los que jugo daniela");
		assertFalse(club.historialPartidos(null, LocalDate.of(2026, 10, 18), LocalDate.of(2026, 10, 18), null).isEmpty(),
				"partidos del 18 de octubre");
		assertThrows(RuntimeException.class, () -> club.historialPartidos(null, FIN_OCTUBRE, OCTUBRE, null), "periodo invalido");
		assertThrows(RuntimeException.class, () -> club.historialPartidos("Rugby", null, null, null), "deporte inexistente");
		assertThrows(RuntimeException.class, () -> club.historialPartidos(null, null, null, "fantasma"), "socio inexistente");
	}

	@Test
	@DisplayName("RF-ADM-17: historial de sesiones con asistencia y evaluaciones")
	void historialSesiones() {
		assertEquals(2, club.historialSesiones(null, null, null, null, null).size(), "todas las sesiones");
		assertEquals(1, club.historialSesiones("Tenis", null, null, null, null).size(), "sesiones de tenis");
		assertEquals(1, club.historialSesiones(null, null, null, null, "felipe").size(), "sesiones de felipe");
		assertThrows(RuntimeException.class, () -> club.historialSesiones(null, FIN_OCTUBRE, OCTUBRE, null, null),
				"periodo invalido");
	}

	@Test
	@DisplayName("RF-ADM-18: reporte de ventas por rubro y periodo")
	void reporteVentas() {
		for (TipoPeriodo tipo : TipoPeriodo.values()) {
			assertFalse(club.reporteVentas(OCTUBRE, FIN_NOVIEMBRE, tipo).isBlank(), "reporte " + tipo);
		}
		assertThrows(RuntimeException.class, () -> club.reporteVentas(OCTUBRE, null, TipoPeriodo.MENSUAL), "sin fecha de fin");
		assertThrows(RuntimeException.class, () -> club.reporteVentas(OCTUBRE, FIN_OCTUBRE, null), "sin tipo de periodo");
	}

	@Test
	@DisplayName("RF-ADM-19: reporte de asistencias")
	void reporteAsistencias() {
		assertFalse(club.reporteAsistencias(OCTUBRE, FIN_OCTUBRE).isBlank());
		assertFalse(club.reporteAsistencias(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)).isBlank(),
				"un periodo sin sesiones tambien da reporte");
		assertThrows(RuntimeException.class, () -> club.reporteAsistencias(FIN_OCTUBRE, OCTUBRE), "periodo invalido");
	}

	@Test
	@DisplayName("RF-ADM-20: reporte de resultados de partidos")
	void reporteResultados() {
		assertFalse(club.reporteResultados(OCTUBRE, FIN_OCTUBRE).isBlank());
		assertThrows(RuntimeException.class, () -> club.reporteResultados(FIN_OCTUBRE, OCTUBRE), "periodo invalido");
	}

	@Test
	@DisplayName("RF-ADM-21: reporte de evolucion de rankings")
	void reporteRankings() {
		assertFalse(club.reporteRankings(OCTUBRE, FIN_NOVIEMBRE).isBlank());
		assertThrows(RuntimeException.class, () -> club.reporteRankings(null, FIN_OCTUBRE), "periodo invalido");
	}

	@Test
	@DisplayName("un socio no consulta reportes ni historiales")
	void socioSinAcceso() {
		club.iniciarSesion("ana", "1234");
		assertThrows(RuntimeException.class, () -> club.reporteVentas(OCTUBRE, FIN_OCTUBRE, TipoPeriodo.MENSUAL));
		assertThrows(RuntimeException.class, () -> club.historialPartidos(null, null, null, null));
	}
}
