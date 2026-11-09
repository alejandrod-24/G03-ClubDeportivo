package pruebas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import deportes.Nivel;
import global.Club;
import global.Reloj;
import partidos.EstadisticaFutbol;
import partidos.EstadisticaTenis;
import partidos.PartidoPractica;

/**
 * 6. Programacion de partidos de practica con validacion de nivel, asignacion de nivel
 *    y registro del resultado con actualizacion del ELO.
 * RF-SOC-08, 10, 11 y RF-ENT-09, 10
 */
public class PruebaPartidoPractica {

	private static Club club;
	private static String partidoValentina;
	private static String partidoAna;

	//Lunes 19 de octubre de 2026 (las pruebas empiezan el sabado 17 a las 7:00)
	private static final LocalDate LUNES = LocalDate.of(2026, 10, 19);
	private static final LocalDateTime LUNES_9 = LUNES.atTime(9, 0);

	public static void main(String[] args) {
		ejecutar();
		Prueba.resumen();
	}

	public static void ejecutar() {
		Prueba.titulo("PRUEBA 6: Partidos de practica y ranking ELO");
		club = Prueba.clubDePrueba();
		Prueba.info("Academia de tenis: " + club.buscarDeporteIndividual("Tenis").getAcademia().getRankingActual());

		Prueba.seccion("Programar partido de practica (RF-SOC-10)");
		club.iniciarSesion("valentina", "1234");
		Prueba.debeFuncionar("valentina (INTERMEDIO) vs diego (AVANZADO): niveles compatibles", () -> {
			PartidoPractica p = club.programarPartidoPractica("Tenis", "TEN-1", LUNES_9, List.of("diego"));
			partidoValentina = p.getId();
			Prueba.info("Programado: " + p);
		});
		Prueba.debeRechazar("valentina (INTERMEDIO) vs ana (PRINCIPIANTE)",
				() -> club.programarPartidoPractica("Tenis", "TEN-2", LUNES.atTime(10, 0), List.of("ana")));
		Prueba.debeRechazar("nicolas no esta inscrito en tenis",
				() -> club.programarPartidoPractica("Tenis", "TEN-2", LUNES.atTime(10, 0), List.of("nicolas")));
		Prueba.debeRechazar("participante repetido",
				() -> club.programarPartidoPractica("Tenis", "TEN-2", LUNES.atTime(10, 0), List.of("valentina")));
		Prueba.debeRechazar("3 participantes (no es sencillos ni dobles)",
				() -> club.programarPartidoPractica("Tenis", "TEN-2", LUNES.atTime(10, 0), List.of("luis", "diego")));
		club.iniciarSesion("luis", "1234");
		Prueba.debeRechazar("diego ya tiene otro compromiso a las 9:00",
				() -> club.programarPartidoPractica("Tenis", "TEN-2", LUNES_9, List.of("diego")));
		Prueba.debeRechazar("dobles en TEN-3 (capacidad 2): los jugadores superan la capacidad",
				() -> club.programarPartidoPractica("Tenis", "TEN-3", LUNES.atTime(11, 0), List.of("valentina", "diego", "carlos")));
		club.iniciarSesion("ana", "1234");
		Prueba.debeFuncionar("ana (PRINCIPIANTE) juega contra el entrenador carlos: no se exige nivel", () ->
				partidoAna = club.programarPartidoPractica("Tenis", "TEN-2", LUNES.atTime(10, 0), List.of("carlos")).getId());
		club.iniciarSesion("mateo", "1234");
		Prueba.debeFuncionar("dobles de padel: mateo y maria vs jorge y juliana", () ->
				Prueba.info("Programado: " + club.programarPartidoPractica("Padel", "PAD-2", LUNES_9, List.of("maria", "jorge", "juliana"))));

		Prueba.seccion("Asignar nivel en academia (RF-ENT-09)");
		club.iniciarSesion("carlos", "carlos123");
		Prueba.debeRechazar("bajar a diego a PRINCIPIANTE inhabilita su partido con valentina",
				() -> club.asignarNivel("Tenis", "diego", Nivel.PRINCIPIANTE));
		Prueba.debeFuncionar("subir a luis a AVANZADO", () -> club.asignarNivel("Tenis", "luis", Nivel.AVANZADO));
		Prueba.debeRechazar("socio no inscrito en la academia", () -> club.asignarNivel("Tenis", "nicolas", Nivel.INTERMEDIO));

		Prueba.seccion("Registrar resultado de partido de practica (RF-ENT-10)");
		Prueba.debeRechazar("el bloque aun no ha comenzado",
				() -> club.registrarResultadoPractica(partidoValentina, 2, 1, List.of()));
		Reloj.fijar(LUNES.atTime(10, 5));
		Prueba.info("Ahora es " + Reloj.ahora());
		Prueba.debeRechazar("empate en sets", () -> club.registrarResultadoPractica(partidoValentina, 1, 1, List.of()));
		Prueba.debeRechazar("valores negativos", () -> club.registrarResultadoPractica(partidoValentina, 2, 1,
				List.of(new EstadisticaTenis("valentina", 2, 14, -1, 3))));
		Prueba.debeRechazar("inconsistente: valentina registra 1 set pero su lado gano 2", () -> club.registrarResultadoPractica(partidoValentina,
				2, 1, List.of(new EstadisticaTenis("valentina", 1, 14, 2, 3))));
		Prueba.debeRechazar("inconsistente: 2 sets con solo 8 games", () -> club.registrarResultadoPractica(partidoValentina,
				2, 1, List.of(new EstadisticaTenis("valentina", 2, 8, 2, 3))));
		Prueba.debeRechazar("estadistica de otro deporte", () -> club.registrarResultadoPractica(partidoValentina,
				2, 1, List.of(new EstadisticaFutbol("valentina", 1, 0, 0, false))));
		Prueba.debeFuncionar("valentina gana 2 - 1 a diego, con aces y errores no forzados", () -> club.registrarResultadoPractica(
				partidoValentina, 2, 1, List.of(new EstadisticaTenis("valentina", 2, 15, 4, 6), new EstadisticaTenis("diego", 1, 11, 7, 12))));
		Prueba.debeRechazar("registrar otra vez", () -> club.registrarResultadoPractica(partidoValentina, 2, 0, List.of()));
		Prueba.verificar("valentina sube de ELO al ganarle a un rival de mayor puntaje",
				club.buscarDeporteIndividual("Tenis").getAcademia().buscarInscripcion(club.buscarSocio("valentina")).getRanking().getPuntaje() > 1500);
		Reloj.fijar(LUNES.atTime(11, 5));
		Prueba.debeFuncionar("ana pierde 0 - 2 contra el entrenador", () -> club.registrarResultadoPractica(partidoAna, 0, 2, List.of()));

		Prueba.seccion("Cancelar partido de practica (RF-SOC-08)");
		club.iniciarSesion("luis", "1234");
		String[] martes = new String[1];
		Prueba.debeFuncionar("luis programa una practica con valentina el martes",
				() -> martes[0] = club.programarPartidoPractica("Tenis", "TEN-1", LUNES.plusDays(1).atTime(9, 0), List.of("valentina")).getId());
		club.iniciarSesion("valentina", "1234");
		Prueba.debeRechazar("valentina no la programo, no la puede cancelar", () -> club.cancelarPartidoPractica(martes[0]));
		Prueba.debeRechazar("cancelar un partido que ya tiene resultado", () -> club.cancelarPartidoPractica(partidoValentina));
		club.iniciarSesion("luis", "1234");
		Prueba.debeFuncionar("luis la cancela", () -> club.cancelarPartidoPractica(martes[0]));

		Prueba.seccion("Consultar partidos y ranking (RF-SOC-11)");
		club.iniciarSesion("valentina", "1234");
		Prueba.info(club.consultarPartidosYRanking());
		club.iniciarSesion("ana", "1234");
		Prueba.info(club.consultarPartidosYRanking());

		Prueba.seccion("Historial de partidos del administrador incluye las practicas");
		club.iniciarSesion("admin", "admin123");
		Prueba.info("Partidos en los que jugo el entrenador carlos:");
		Prueba.lista(club.historialPartidos(null, null, null, "carlos"));

		Prueba.seccion("Las practicas y el ranking sobreviven a guardar y cargar");
		club.iniciarSesion("valentina", "1234");
		Club recargado = Prueba.guardarYRecargar(club, "_prueba_practicas");
		recargado.iniciarSesion("valentina", "1234");
		Prueba.verificar("partidos y ranking de valentina identicos tras recargar",
				club.consultarPartidosYRanking().equals(recargado.consultarPartidosYRanking()));
	}
}
