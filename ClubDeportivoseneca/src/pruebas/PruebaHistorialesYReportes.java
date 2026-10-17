package pruebas;

import java.time.LocalDate;
import java.util.List;

import administracion.TipoPeriodo;
import competencias.Fase;
import global.Club;
import global.DatosIniciales;
import global.Reloj;
import partidos.EstadisticaFutbol;

/**
 * 17. Historial de partidos con filtros y reportes de ventas y de resultados del administrador.
 * RF-ADM-16, 18, 20. Integracion etapa 3: historial de reservas (RF-ADM-15) y de sesiones (RF-ADM-17),
 * reporte de asistencias (RF-ADM-19) y de rankings (RF-ADM-21), y partidos de practica en el historial.
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
		prepararDatos();
		club.iniciarSesion("admin", "admin123");
		LocalDate octubre = LocalDate.of(2026, 10, 1);
		LocalDate finOctubre = LocalDate.of(2026, 10, 31);

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

		club.iniciarSesion("ana", "1234");
		Prueba.debeRechazar("un socio no consulta reportes", () -> club.reporteVentas(octubre, finOctubre, TipoPeriodo.MENSUAL));
		Prueba.debeRechazar("un socio no consulta historiales", () -> club.historialPartidos(null, null, null, null));
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
