package pruebas;

import java.util.List;

import competencias.Fase;
import deportes.Categoria;
import global.Club;
import global.DatosIniciales;
import global.Reloj;
import partidos.EstadisticaFutbol;

/**
 * 2. Consulta de competencias y equipos (cualquier usuario autenticado).
 * RF-GEN-05, 07. (La consulta de catalogos RF-GEN-06 llega con la etapa de comercio.)
 */
public class PruebaConsultasGenerales {

	private static Club club;

	public static void main(String[] args) {
		ejecutar();
		Prueba.resumen();
	}

	public static void ejecutar() {
		Prueba.titulo("PRUEBA 2: Consultas generales");
		club = Prueba.clubDePrueba();
		prepararDatos();

		Prueba.debeRechazar("consultar sin iniciar sesion", () -> club.consultarEquipos(null, null));
		club.iniciarSesion("valentina", "1234");

		Prueba.seccion("Consultar equipos (RF-GEN-07)");
		Prueba.info("Todos los equipos:");
		Prueba.lista(club.consultarEquipos(null, null));
		Prueba.info("Futbol, categoria MAYORES:");
		Prueba.lista(club.consultarEquipos("Futbol", Categoria.MAYORES));
		Prueba.info("Futbol, categoria SUB15:");
		Prueba.lista(club.consultarEquipos("Futbol", Categoria.SUB15));
		Prueba.debeRechazar("deporte inexistente", () -> club.consultarEquipos("Rugby", null));

		Prueba.info("Calendario y resultados de Seneca FC:");
		Prueba.lista(club.consultarCalendarioEquipo("Futbol", "Seneca FC"));
		Prueba.debeRechazar("calendario de un equipo inexistente", () -> club.consultarCalendarioEquipo("Futbol", "Galacticos"));

		Prueba.seccion("Consultar competencias (RF-GEN-05)");
		Prueba.info("Competencias del club:");
		Prueba.lista(club.getCompetencias());
		System.out.println();
		Prueba.info(club.consultarCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA).generarResumen());
		Prueba.debeRechazar("competencia inexistente", () -> club.consultarCompetencia("Copa America", 2026));
	}

	//Un partido interno jugado, uno programado y un partido de liga jugado
	private static void prepararDatos() {
		List<String> fc = List.of("ana", "luis", "camila", "jorge", "maria", "andres", "juliana", "felipe", "daniela");
		List<String> united = List.of("paula", "sebastian", "natalia", "ricardo", "carolina", "miguel", "isabel", "nicolas");
		club.iniciarSesion("carlos", "carlos123");
		String interno = club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", Reloj.hoy().plusDays(1)).getId();
		club.inscribirEquipoEnCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Futbol", "Seneca FC");
		club.definirNomina(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Seneca FC", fc);
		String liga = club.programarPartidoCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Seneca FC",
				"Universidad Externado", "Cancha Seneca", Reloj.hoy().plusDays(1), Fase.LIGA).getId();
		Reloj.avanzarDias(1);
		club.registrarResultadoInterno(interno, fc, united, 0, 2);
		club.registrarPartidoCompetencia(liga, fc, 2, 0, List.of(new EstadisticaFutbol("daniela", 2, 0, 0, false)));
		club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", Reloj.hoy().plusDays(7));
		club.cerrarSesion();
	}
}
