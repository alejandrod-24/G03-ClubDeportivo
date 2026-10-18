package pruebas;

import java.time.LocalDate;
import java.util.List;

import deportes.Categoria;
import global.Club;
import global.Reloj;
import partidos.EstadisticaBaloncesto;
import partidos.EstadisticaFutbol;
import partidos.PartidoInterno;

/**
 * 10. Programacion de un partido interno, registro de alineacion, marcador,
 *     estadisticas y tarjetas, suspensiones y declaracion de partido no jugado.
 * RF-ENT-01 a 05
 */
public class PruebaPartidoInterno {

	private static Club club;
	private static String id1;
	private static String id2;

	private static final List<String> FC = List.of("ana", "luis", "camila", "jorge", "maria", "andres",
			"juliana", "felipe", "daniela", "santiago");
	private static final List<String> UNITED = List.of("paula", "sebastian", "natalia", "ricardo", "carolina",
			"miguel", "isabel", "nicolas");

	public static void main(String[] args) {
		ejecutar();
		Prueba.resumen();
	}

	public static void ejecutar() {
		Prueba.titulo("PRUEBA 10: Partidos internos");
		club = Prueba.clubDePrueba();
		club.iniciarSesion("admin", "admin123");
		club.crearEquipo("Futbol", "Reservas", Categoria.MAYORES, 15);

		club.iniciarSesion("carlos", "carlos123");
		LocalDate hoy = Reloj.hoy();

		Prueba.seccion("Programar partido interno (RF-ENT-01). Hoy es " + hoy);
		Prueba.debeFuncionar("programar Seneca FC vs Seneca United en 3 dias", () -> {
			PartidoInterno p = club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", hoy.plusDays(3));
			id1 = p.getId();
			Prueba.info("Programado: " + p);
		});
		Prueba.debeRechazar("un equipo contra si mismo",
				() -> club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca FC", hoy.plusDays(3)));
		Prueba.debeRechazar("fecha pasada",
				() -> club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", hoy.minusDays(1)));
		Prueba.debeRechazar("distinta categoria (Seneca Sub15 vs Seneca FC)",
				() -> club.programarPartidoInterno("Futbol", "Seneca Sub15", "Seneca FC", hoy.plusDays(3)));
		Prueba.debeRechazar("distinto deporte (Tigres no es de futbol)",
				() -> club.programarPartidoInterno("Futbol", "Seneca FC", "Tigres", hoy.plusDays(3)));
		Prueba.debeRechazar("equipo sin el minimo de 7 jugadores (Reservas)",
				() -> club.programarPartidoInterno("Futbol", "Seneca FC", "Reservas", hoy.plusDays(3)));

		Prueba.seccion("Registrar alineacion y marcador (RF-ENT-02)");
		Prueba.debeRechazar("registrar antes de la fecha del partido",
				() -> club.registrarResultadoInterno(id1, FC, UNITED, 2, 1));
		Reloj.avanzarDias(3);
		Prueba.info("Avanzamos el reloj 3 dias: hoy es " + Reloj.hoy());
		Prueba.debeRechazar("alineado de otro equipo (paula en Seneca FC)",
				() -> club.registrarResultadoInterno(id1, List.of("ana", "luis", "camila", "jorge", "maria", "andres", "paula"), UNITED, 2, 1));
		Prueba.debeRechazar("alineacion con menos del minimo",
				() -> club.registrarResultadoInterno(id1, List.of("ana", "luis", "camila"), UNITED, 2, 1));
		Prueba.debeRechazar("marcador negativo", () -> club.registrarResultadoInterno(id1, FC, UNITED, -1, 0));
		Prueba.debeFuncionar("registrar Seneca FC 2 - 1 Seneca United",
				() -> club.registrarResultadoInterno(id1, FC, UNITED, 2, 1));
		Prueba.debeRechazar("registrar de nuevo un partido ya jugado",
				() -> club.registrarResultadoInterno(id1, FC, UNITED, 3, 1));

		Prueba.seccion("Registrar estadisticas y tarjetas (RF-ENT-03)");
		Prueba.debeFuncionar("felipe: 2 goles y 1 asistencia",
				() -> club.registrarEstadistica(id1, new EstadisticaFutbol("felipe", 2, 1, 0, false)));
		Prueba.debeFuncionar("maria: 2 amarillas",
				() -> club.registrarEstadistica(id1, new EstadisticaFutbol("maria", 0, 0, 2, false)));
		Prueba.debeFuncionar("luis: tarjeta roja",
				() -> club.registrarEstadistica(id1, new EstadisticaFutbol("luis", 0, 0, 0, true)));
		Prueba.debeFuncionar("isabel (United): 1 gol, 1 amarilla",
				() -> club.registrarEstadistica(id1, new EstadisticaFutbol("isabel", 1, 0, 1, false)));
		Prueba.debeRechazar("jugador no alineado (diego)",
				() -> club.registrarEstadistica(id1, new EstadisticaFutbol("diego", 1, 0, 0, false)));
		Prueba.debeRechazar("estadistica de otro deporte (baloncesto)",
				() -> club.registrarEstadistica(id1, new EstadisticaBaloncesto("ana", 10, 2)));
		Prueba.debeRechazar("valores negativos",
				() -> club.registrarEstadistica(id1, new EstadisticaFutbol("ana", -1, 0, 0, false)));
		Prueba.info("Partido: " + club.buscarPartido(id1));
		Prueba.lista(List.copyOf(club.buscarPartido(id1).getEstadisticas()));

		Prueba.seccion("Consultar sanciones (RF-ENT-05)");
		Prueba.info("Sanciones de Seneca FC:");
		Prueba.lista(club.consultarSanciones("Futbol", "Seneca FC"));
		Prueba.debeRechazar("consultar sanciones de un equipo inexistente",
				() -> club.consultarSanciones("Futbol", "Galacticos"));

		Prueba.seccion("Las suspensiones se cumplen en el siguiente partido del equipo");
		Prueba.debeFuncionar("programar el segundo partido para dentro de 4 dias", () ->
				id2 = club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", Reloj.hoy().plusDays(4)).getId());
		Reloj.avanzarDias(4);
		Prueba.debeRechazar("alinear a maria (suspendida por 2 amarillas)",
				() -> club.registrarResultadoInterno(id2, List.of("ana", "camila", "jorge", "maria", "andres", "juliana", "felipe", "daniela"), UNITED, 1, 1));
		Prueba.debeFuncionar("registrar sin maria ni luis: empate 1 - 1 (el futbol admite empates)",
				() -> club.registrarResultadoInterno(id2, List.of("ana", "camila", "jorge", "andres", "juliana", "felipe", "daniela", "santiago"), UNITED, 1, 1));
		Prueba.info("Sanciones de Seneca FC despues de cumplir la fecha:");
		Prueba.lista(club.consultarSanciones("Futbol", "Seneca FC"));

		Prueba.seccion("Baloncesto: no admite empates");
		String[] idBaloncesto = new String[1];
		Prueba.debeFuncionar("programar Tigres vs Panteras para manana", () ->
				idBaloncesto[0] = club.programarPartidoInterno("Baloncesto", "Tigres", "Panteras", Reloj.hoy().plusDays(1)).getId());
		Reloj.avanzarDias(1);
		List<String> tigres = List.of("ana", "luis", "maria", "felipe", "paula");
		List<String> panteras = List.of("camila", "jorge", "andres", "juliana", "daniela");
		Prueba.debeRechazar("empate 80 - 80", () -> club.registrarResultadoInterno(idBaloncesto[0], tigres, panteras, 80, 80));
		Prueba.debeFuncionar("Tigres 85 - 80 Panteras", () -> {
			club.registrarResultadoInterno(idBaloncesto[0], tigres, panteras, 85, 80);
			club.registrarEstadistica(idBaloncesto[0], new EstadisticaBaloncesto("ana", 30, 5));
		});

		Prueba.seccion("Declarar partido no jugado (RF-ENT-04)");
		String[] id3 = new String[1];
		Prueba.debeFuncionar("programar un tercer partido y declararlo no jugado", () -> {
			id3[0] = club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", Reloj.hoy().plusDays(7)).getId();
			club.declararNoJugado(id3[0]);
		});
		Prueba.debeRechazar("declarar no jugado un partido ya jugado", () -> club.declararNoJugado(id1));

		Prueba.seccion("Calendario y resultados de Seneca FC");
		Prueba.lista(club.consultarCalendarioEquipo("Futbol", "Seneca FC"));
	}
}
