package pruebas;

import java.io.File;
import java.util.List;

import competencias.Fase;
import deportes.Categoria;
import global.Club;
import global.DatosIniciales;
import global.Reloj;
import partidos.EstadisticaBaloncesto;
import partidos.EstadisticaFutbol;
import persistencia.Persistencia;

/**
 * 11. Inscripcion de equipo en competencia, definicion de nomina y registro de partidos de competencia.
 * RF-ENT-06, 07, 08 y consulta RF-GEN-05
 */
public class PruebaCompetencia {

	private static Club club;
	private static String id1;
	private static String id2;

	private static final String LIGA = DatosIniciales.LIGA_CERROS;
	private static final String ASCUN = DatosIniciales.ASCUN_BALONCESTO;
	private static final int T = DatosIniciales.TEMPORADA;

	private static final List<String> NOMINA_FC = List.of("ana", "luis", "camila", "jorge", "maria", "andres",
			"juliana", "felipe", "daniela");

	public static void main(String[] args) {
		ejecutar();
		Prueba.resumen();
	}

	public static void ejecutar() {
		Prueba.titulo("PRUEBA 11: Competencias externas");
		club = Prueba.clubDePrueba();
		club.iniciarSesion("admin", "admin123");
		club.crearEquipo("Futbol", "Reservas", Categoria.MAYORES, 15);
		club.crearCompetenciaAscun("ASCUN Futbol", T, "Futbol", Categoria.MAYORES,
				List.of("Universidad Nacional", "Universidad de los Andes"));

		club.iniciarSesion("carlos", "carlos123");

		Prueba.seccion("Inscribir equipo en competencia (RF-ENT-06)");
		Prueba.debeFuncionar("inscribir Seneca FC en " + LIGA, () -> club.inscribirEquipoEnCompetencia(LIGA, T, "Futbol", "Seneca FC"));
		Prueba.debeRechazar("inscribirlo otra vez", () -> club.inscribirEquipoEnCompetencia(LIGA, T, "Futbol", "Seneca FC"));
		Prueba.debeRechazar("deporte no corresponde (Tigres de baloncesto en liga de futbol)",
				() -> club.inscribirEquipoEnCompetencia(LIGA, T, "Baloncesto", "Tigres"));
		Prueba.debeRechazar("categoria no corresponde (Seneca Sub15)",
				() -> club.inscribirEquipoEnCompetencia(LIGA, T, "Futbol", "Seneca Sub15"));
		Prueba.debeRechazar("equipo sin la nomina minima (Reservas, 0 jugadores)",
				() -> club.inscribirEquipoEnCompetencia(LIGA, T, "Futbol", "Reservas"));
		Prueba.debeRechazar("competencia inexistente",
				() -> club.inscribirEquipoEnCompetencia("Copa Mundial", T, "Futbol", "Seneca FC"));

		Prueba.seccion("Definir nomina (RF-ENT-07)");
		Prueba.debeRechazar("nomina de 13 supera el cupo del equipo (12)", () -> club.definirNomina(LIGA, T, "Seneca FC",
				List.of("ana", "luis", "camila", "jorge", "maria", "andres", "juliana", "felipe", "daniela", "santiago",
						"paula", "sebastian", "natalia")));
		Prueba.debeRechazar("jugador que no es del equipo (paula)", () -> club.definirNomina(LIGA, T, "Seneca FC",
				List.of("ana", "luis", "camila", "jorge", "maria", "andres", "paula")));
		Prueba.debeFuncionar("nomina de 9 jugadores (santiago queda fuera)", () -> club.definirNomina(LIGA, T, "Seneca FC", NOMINA_FC));

		Prueba.seccion("Programar partidos de competencia");
		Prueba.debeRechazar("rival que no participa en la competencia", () -> club.programarPartidoCompetencia(LIGA, T,
				"Seneca FC", "Universidad de los Andes", "Estadio El Campin", Reloj.hoy().plusDays(2), Fase.LIGA));
		Prueba.debeRechazar("fase de grupos en una liga", () -> club.programarPartidoCompetencia(LIGA, T,
				"Seneca FC", "Universidad Javeriana", "Estadio El Campin", Reloj.hoy().plusDays(2), Fase.GRUPOS));
		Prueba.debeFuncionar("Seneca FC vs Universidad Javeriana en 2 dias", () -> id1 = club.programarPartidoCompetencia(LIGA, T,
				"Seneca FC", "Universidad Javeriana", "Estadio El Campin", Reloj.hoy().plusDays(2), Fase.LIGA).getId());

		Prueba.seccion("Registrar partido de competencia (RF-ENT-08)");
		List<EstadisticaFutbol> stats = List.of(new EstadisticaFutbol("felipe", 2, 0, 0, false),
				new EstadisticaFutbol("daniela", 1, 2, 1, false), new EstadisticaFutbol("jorge", 0, 0, 0, true));
		Prueba.debeRechazar("partido no asignado (P999)",
				() -> club.registrarPartidoCompetencia("P999", NOMINA_FC, 3, 1, List.copyOf(stats)));
		Prueba.debeRechazar("antes de la fecha del partido",
				() -> club.registrarPartidoCompetencia(id1, NOMINA_FC, 3, 1, List.copyOf(stats)));
		Reloj.avanzarDias(2);
		Prueba.debeRechazar("alineado que no esta en la nomina (santiago)", () -> club.registrarPartidoCompetencia(id1,
				List.of("ana", "luis", "camila", "jorge", "maria", "andres", "santiago"), 3, 1, List.copyOf(stats)));
		Prueba.debeRechazar("marcador negativo", () -> club.registrarPartidoCompetencia(id1, NOMINA_FC, -3, 1, List.copyOf(stats)));
		Prueba.debeRechazar("estadistica de un jugador no alineado", () -> club.registrarPartidoCompetencia(id1, NOMINA_FC, 3, 1,
				List.of(new EstadisticaFutbol("santiago", 1, 0, 0, false))));
		Prueba.debeFuncionar("Seneca FC 3 - 1 Javeriana con goles, asistencias y tarjetas",
				() -> club.registrarPartidoCompetencia(id1, NOMINA_FC, 3, 1, List.copyOf(stats)));
		Prueba.debeRechazar("registrar de nuevo un partido ya jugado",
				() -> club.registrarPartidoCompetencia(id1, NOMINA_FC, 3, 1, List.of()));

		Prueba.seccion("Suspension en competencia: jorge recibio roja");
		Prueba.debeFuncionar("programar Seneca FC vs Universidad del Rosario en 3 dias", () -> id2 = club.programarPartidoCompetencia(LIGA, T,
				"Seneca FC", "Universidad del Rosario", "Cancha Seneca", Reloj.hoy().plusDays(3), Fase.LIGA).getId());
		Reloj.avanzarDias(3);
		Prueba.debeRechazar("alinear a jorge (suspendido)", () -> club.registrarPartidoCompetencia(id2, NOMINA_FC, 0, 0, List.of()));
		Prueba.debeFuncionar("registrar sin jorge: empate 1 - 1", () -> club.registrarPartidoCompetencia(id2,
				List.of("ana", "luis", "camila", "maria", "andres", "juliana", "felipe", "daniela"), 1, 1,
				List.of(new EstadisticaFutbol("maria", 1, 0, 1, false))));

		Prueba.seccion("ASCUN: fase eliminatoria sin empates");
		Prueba.debeFuncionar("inscribir Tigres en " + ASCUN + " y definir su nomina", () -> {
			club.inscribirEquipoEnCompetencia(ASCUN, T, "Baloncesto", "Tigres");
			club.definirNomina(ASCUN, T, "Tigres", List.of("ana", "luis", "maria", "felipe", "paula", "sebastian", "natalia"));
		});
		String[] idCuartos = new String[1];
		Prueba.debeFuncionar("programar cuartos de final contra Universidad Nacional", () -> idCuartos[0] = club.programarPartidoCompetencia(
				ASCUN, T, "Tigres", "Universidad Nacional", "Coliseo El Salitre", Reloj.hoy().plusDays(1), Fase.CUARTOS).getId());
		Prueba.debeRechazar("fase LIGA en una competencia ASCUN", () -> club.programarPartidoCompetencia(
				ASCUN, T, "Tigres", "Universidad EAFIT", "Coliseo El Salitre", Reloj.hoy().plusDays(1), Fase.LIGA));
		Reloj.avanzarDias(1);
		List<String> quintetoTigres = List.of("ana", "luis", "maria", "felipe", "paula");
		Prueba.debeRechazar("empate en baloncesto", () -> club.registrarPartidoCompetencia(idCuartos[0], quintetoTigres, 70, 70, List.of()));
		Prueba.debeFuncionar("Tigres 78 - 70 Nacional", () -> club.registrarPartidoCompetencia(idCuartos[0], quintetoTigres, 78, 70,
				List.of(new EstadisticaBaloncesto("paula", 25, 12), new EstadisticaBaloncesto("ana", 18, 3))));

		List<String> united = List.of("paula", "sebastian", "natalia", "ricardo", "carolina", "miguel", "isabel", "nicolas");
		String[] idSemi = new String[1];
		Prueba.debeFuncionar("Seneca United en ASCUN Futbol: semifinal contra los Andes", () -> {
			club.inscribirEquipoEnCompetencia("ASCUN Futbol", T, "Futbol", "Seneca United");
			club.definirNomina("ASCUN Futbol", T, "Seneca United", united);
			idSemi[0] = club.programarPartidoCompetencia("ASCUN Futbol", T, "Seneca United", "Universidad de los Andes",
					"Estadio Alfonso Lopez", Reloj.hoy().plusDays(1), Fase.SEMIFINAL).getId();
		});
		Reloj.avanzarDias(1);
		Prueba.debeRechazar("empate en eliminatoria (futbol)", () -> club.registrarPartidoCompetencia(idSemi[0], united, 1, 1, List.of()));
		Prueba.debeFuncionar("Seneca United 2 - 0 Andes", () -> club.registrarPartidoCompetencia(idSemi[0], united, 2, 0,
				List.of(new EstadisticaFutbol("isabel", 2, 0, 0, false))));

		Prueba.seccion("Consultar competencias (RF-GEN-05)");
		Prueba.info(club.consultarCompetencia(LIGA, T).generarResumen());
		System.out.println();
		Prueba.info(club.consultarCompetencia(ASCUN, T).generarResumen());
		System.out.println();
		Prueba.info(club.consultarCompetencia("ASCUN Futbol", T).generarResumen());

		Prueba.seccion("Los partidos, nominas y estadisticas sobreviven a guardar y cargar");
		File carpeta = new File("datos/_prueba_competencia");
		try {
			Persistencia persistencia = new Persistencia(carpeta.getPath());
			persistencia.salvar(club);
			Club recargado = persistencia.cargar();
			recargado.iniciarSesion("carlos", "carlos123");
			for (String nombre : List.of(LIGA, ASCUN, "ASCUN Futbol")) {
				Prueba.verificar("resumen de " + nombre + " identico tras recargar",
						club.consultarCompetencia(nombre, T).generarResumen()
								.equals(recargado.consultarCompetencia(nombre, T).generarResumen()));
			}
			Prueba.verificar("las sanciones de Seneca FC se recalculan igual tras recargar",
					club.consultarSanciones("Futbol", "Seneca FC").equals(recargado.consultarSanciones("Futbol", "Seneca FC")));
		} finally {
			File[] archivos = carpeta.listFiles();
			if (archivos != null) {
				for (File f : archivos) {
					f.delete();
				}
			}
			carpeta.delete();
		}
	}
}
