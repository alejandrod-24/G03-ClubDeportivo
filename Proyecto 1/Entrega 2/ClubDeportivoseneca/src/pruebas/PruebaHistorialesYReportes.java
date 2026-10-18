package pruebas;

import java.time.LocalDate;
import java.util.List;

import administracion.TipoPeriodo;
import competencias.Fase;
import global.Club;
import global.DatosIniciales;
import global.Reloj;
import entrenamiento.Asistencia;
import instalaciones.Modalidad;
import partidos.EstadisticaFutbol;
import partidos.EstadisticaTenis;

/**
 * 17. Historiales y reportes del administrador.
 * Historiales de reservas, partidos y sesiones con filtros, y los reportes de ventas, asistencias,
 * resultados y rankings. RF-ADM-15 a 21
 */
public class PruebaHistorialesYReportes {

	private static Club club;

	private static final List<String> FC = List.of("ana", "luis", "camila", "jorge", "maria", "andres", "juliana",
			"felipe", "daniela");
	private static final List<String> UNITED = List.of("paula", "sebastian", "natalia", "ricardo", "carolina",
			"miguel", "isabel", "nicolas");

	public static void main(String[] args) {
		ejecutar();
		Prueba.resumen();
	}

	public static void ejecutar() {
		Prueba.titulo("PRUEBA 17: Historiales y reportes (administrador)");
		club = Prueba.clubDePrueba();
		prepararReservasYSesiones();
		prepararDatos();
		registrarAsistenciaYPractica();
		club.iniciarSesion("admin", "admin123");
		LocalDate octubre = LocalDate.of(2026, 10, 1);
		LocalDate finOctubre = LocalDate.of(2026, 10, 31);

		Prueba.seccion("Historial de reservas, incluye cancelaciones (RF-ADM-15). Hoy es " + Reloj.hoy());
		Prueba.info("Todas las reservas:");
		Prueba.lista(club.historialReservas(null, null, null, null, null));
		Prueba.info("Reservas de la cancha TEN-2:");
		Prueba.lista(club.historialReservas(null, null, null, "TEN-2", null));
		Prueba.info("Reservas de padel:");
		Prueba.lista(club.historialReservas("Padel", null, null, null, null));
		Prueba.info("Reservas de diego en octubre:");
		Prueba.lista(club.historialReservas(null, octubre, finOctubre, null, "diego"));
		Prueba.verificar("la reserva cancelada de valentina aparece en el historial",
				club.historialReservas(null, null, null, null, "valentina").get(0).estaCancelada());
		Prueba.debeRechazar("instalacion inexistente", () -> club.historialReservas(null, null, null, "X-1", null));
		Prueba.debeRechazar("periodo invalido", () -> club.historialReservas(null, finOctubre, octubre, null, null));

		Prueba.seccion("Historial de partidos (RF-ADM-16). Hoy es " + Reloj.hoy());
		Prueba.info("Todos los partidos:");
		Prueba.lista(club.historialPartidos(null, null, null, null));
		Prueba.info("Solo Baloncesto:");
		Prueba.lista(club.historialPartidos("Baloncesto", null, null, null));
		Prueba.info("Partidos en los que jugo daniela:");
		Prueba.lista(club.historialPartidos(null, null, null, "daniela"));
		Prueba.info("Partidos del 19 de octubre:");
		Prueba.lista(club.historialPartidos(null, LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 19), null));
		Prueba.debeRechazar("periodo invalido", () -> club.historialPartidos(null, finOctubre, octubre, null));
		Prueba.debeRechazar("deporte inexistente", () -> club.historialPartidos("Rugby", null, null, null));
		Prueba.debeRechazar("socio inexistente", () -> club.historialPartidos(null, null, null, "fantasma"));

		Prueba.seccion("Historial de sesiones con asistencia y evaluaciones (RF-ADM-17)");
		Prueba.info("Todas las sesiones:");
		Prueba.lista(club.historialSesiones(null, null, null, null, null));
		Prueba.info("Sesiones de tenis:");
		Prueba.lista(club.historialSesiones("Tenis", null, null, null, null));
		Prueba.info("Sesiones en las que estuvo convocado felipe:");
		Prueba.lista(club.historialSesiones(null, null, null, null, "felipe"));
		Prueba.debeRechazar("periodo invalido", () -> club.historialSesiones(null, finOctubre, octubre, null, null));

		Prueba.seccion("Reporte de ventas por rubro y periodo (RF-ADM-18)");
		Prueba.info(club.reporteVentas(octubre, LocalDate.of(2026, 11, 30), TipoPeriodo.DIARIO));
		System.out.println();
		Prueba.info(club.reporteVentas(octubre, LocalDate.of(2026, 11, 30), TipoPeriodo.SEMANAL));
		System.out.println();
		Prueba.info(club.reporteVentas(octubre, LocalDate.of(2026, 11, 30), TipoPeriodo.MENSUAL));
		Prueba.debeRechazar("periodo invalido (sin fecha de fin)", () -> club.reporteVentas(octubre, null, TipoPeriodo.MENSUAL));
		Prueba.debeRechazar("sin tipo de periodo", () -> club.reporteVentas(octubre, finOctubre, null));

		Prueba.seccion("Reporte de resultados de partidos (RF-ADM-20)");
		Prueba.info(club.reporteResultados(octubre, finOctubre));
		Prueba.debeRechazar("periodo invalido", () -> club.reporteResultados(finOctubre, octubre));

		Prueba.seccion("Reporte de asistencias (RF-ADM-19)");
		Prueba.info(club.reporteAsistencias(octubre, finOctubre));
		Prueba.info(club.reporteAsistencias(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)));
		Prueba.debeRechazar("periodo invalido", () -> club.reporteAsistencias(finOctubre, octubre));

		Prueba.seccion("Reporte de evolucion de rankings (RF-ADM-21)");
		Prueba.info(club.reporteRankings(octubre, LocalDate.of(2026, 11, 30)));
		Prueba.debeRechazar("periodo invalido", () -> club.reporteRankings(null, finOctubre));

		club.iniciarSesion("ana", "1234");
		Prueba.debeRechazar("un socio no consulta reportes", () -> club.reporteVentas(octubre, finOctubre, TipoPeriodo.MENSUAL));
		Prueba.debeRechazar("un socio no consulta historiales", () -> club.historialPartidos(null, null, null, null));
	}

	//Antes de que avance el reloj: reservas (una cancelada), dos sesiones y dos practicas
	private static void prepararReservasYSesiones() {
		LocalDate lunes = LocalDate.of(2026, 10, 19);
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

	//Ya en noviembre: asistencia y notas de las sesiones de octubre, y resultados de las practicas
	private static void registrarAsistenciaYPractica() {
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

	private static String practica(String deporte) {
		return club.buscarDeporteIndividual(deporte).getPartidos().get(0).getId();
	}

	//Partidos internos y de competencia, ventas en octubre y noviembre y mensualidades
	private static void prepararDatos() {
		LocalDate dia1 = Reloj.hoy().plusDays(1);
		club.iniciarSesion("carlos", "carlos123");
		String interno = club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", dia1).getId();
		String baloncesto = club.programarPartidoInterno("Baloncesto", "Tigres", "Panteras", dia1).getId();
		club.inscribirEquipoEnCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Futbol", "Seneca FC");
		club.definirNomina(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Seneca FC", FC);
		String liga = club.programarPartidoCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Seneca FC",
				"Universidad Javeriana", "Cancha Seneca", Reloj.hoy().plusDays(2), Fase.LIGA).getId();
		club.iniciarSesion("ana", "1234");
		club.comprarEnTienda(Prueba.items("Balon de futbol", 2), null, 0);
		club.pagarMensualidad();
		club.iniciarSesion("luis", "1234");
		club.comprarEnCafeteria(Prueba.items("Cafe", 2, "Galletas de mani", 1), 0, 10, true);
		club.pagarMensualidad();

		Reloj.avanzarDias(1);
		club.iniciarSesion("carlos", "carlos123");
		club.registrarResultadoInterno(interno, FC, UNITED, 2, 1);
		club.registrarEstadistica(interno, new EstadisticaFutbol("felipe", 2, 0, 0, false));
		club.registrarResultadoInterno(baloncesto, List.of("ana", "luis", "maria", "felipe", "paula"),
				List.of("camila", "jorge", "andres", "juliana", "daniela"), 70, 75);
		club.iniciarSesion("pedro", "pedro123");
		club.comprarEnTienda(Prueba.items("Raqueta de tenis", 1), null, 0);

		Reloj.avanzarDias(1);
		club.iniciarSesion("carlos", "carlos123");
		club.registrarPartidoCompetencia(liga, FC, 3, 1, List.of(new EstadisticaFutbol("daniela", 2, 1, 1, false)));
		club.iniciarSesion("ana", "1234");
		club.comprarEnCafeteria(Prueba.items("Jugo natural", 1), 1000, 10, false);

		Reloj.fijar(LocalDate.of(2026, 11, 3));
		club.comprarEnTienda(Prueba.items("Uniforme Seneca", 1), "DESC-LAURA", 0);
		club.pagarMensualidad();
		club.cerrarSesion();
	}
}
