package pruebas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import deportes.InscripcionAcademia;
import deportes.Nivel;
import global.Club;
import instalaciones.Modalidad;

/**
 * 4. Inscripcion y retiro de un socio en una academia individual, con nivel y ELO inicial.
 * RF-SOC-04, 05 (y la parte de academias de RF-SOC-01)
 */
public class PruebaInscripcionAcademia {

	private static Club club;

	//Lunes 19 de octubre de 2026 (las pruebas empiezan el sabado 17 a las 7:00)
	private static final LocalDate LUNES = LocalDate.of(2026, 10, 19);

	public static void main(String[] args) {
		ejecutar();
		Prueba.resumen();
	}

	public static void ejecutar() {
		Prueba.titulo("PRUEBA 4: Inscripcion y retiro de academias");
		club = Prueba.clubDePrueba();

		Prueba.seccion("Inscribirse a una academia (RF-SOC-04)");
		club.iniciarSesion("isabel", "1234");
		Prueba.debeFuncionar("isabel se inscribe en tenis como INTERMEDIO con ELO 1500", () -> {
			InscripcionAcademia i = club.inscribirseEnAcademia("Tenis", Nivel.INTERMEDIO, 1500);
			Prueba.info("Inscrita: " + i);
		});
		Prueba.debeRechazar("inscribirse otra vez en tenis", () -> club.inscribirseEnAcademia("Tenis", Nivel.AVANZADO, 1600));
		Prueba.debeRechazar("futbol no tiene academia", () -> club.inscribirseEnAcademia("Futbol", Nivel.INTERMEDIO, 1500));
		Prueba.debeRechazar("deporte inexistente", () -> club.inscribirseEnAcademia("Squash", Nivel.INTERMEDIO, 1500));
		Prueba.debeRechazar("ELO inicial fuera de rango", () -> club.inscribirseEnAcademia("Padel", Nivel.INTERMEDIO, 5000));
		Prueba.debeRechazar("sin nivel", () -> club.inscribirseEnAcademia("Padel", null, 1500));
		Prueba.debeFuncionar("isabel tambien se inscribe en padel (otra academia)",
				() -> club.inscribirseEnAcademia("Padel", Nivel.PRINCIPIANTE, 1200));
		Prueba.info(club.consultarPerfil());
		club.iniciarSesion("admin", "admin123");
		Prueba.debeRechazar("el administrador no puede inscribirse", () -> club.inscribirseEnAcademia("Tenis", Nivel.INTERMEDIO, 1500));

		Prueba.seccion("Retirarse de una academia (RF-SOC-05)");
		club.iniciarSesion("isabel", "1234");
		String[] ids = new String[2];
		Prueba.debeFuncionar("isabel reserva tenis el lunes a las 9:00 y programa una practica con valentina a las 10:00", () -> {
			ids[0] = club.reservarInstalacion("Tenis", "TEN-1", LUNES.atTime(9, 0), 2, Modalidad.SENCILLOS).getId();
			ids[1] = club.programarPartidoPractica("Tenis", "TEN-2", LUNES.atTime(10, 0), List.of("valentina")).getId();
		});
		Prueba.debeFuncionar("isabel se retira de la academia de tenis", () -> club.retirarseDeAcademia("Tenis"));
		Prueba.verificar("su reserva futura quedo cancelada", club.buscarReserva(ids[0]).estaCancelada());
		Prueba.verificar("su partido de practica futuro quedo cancelado",
				club.buscarPartido(ids[1]).getResultado().equals("cancelado"));
		Prueba.debeRechazar("retirarse otra vez", () -> club.retirarseDeAcademia("Tenis"));
		Prueba.debeRechazar("retirarse de una academia en la que nunca estuvo", () -> club.retirarseDeAcademia("Tenis de mesa"));
		Prueba.debeRechazar("ya no puede reservar tenis",
				() -> club.reservarInstalacion("Tenis", "TEN-1", LUNES.atTime(11, 0), 2, Modalidad.SENCILLOS));
		Prueba.info("El ranking historico se conserva:");
		Prueba.info(club.consultarPartidosYRanking());

		Prueba.seccion("Volver a inscribirse");
		Prueba.debeFuncionar("isabel vuelve a tenis como AVANZADO con ELO 1550", () -> club.inscribirseEnAcademia("Tenis", Nivel.AVANZADO, 1550));
		Prueba.verificar("el historial del ranking tiene la inscripcion y la reinscripcion",
				club.buscarDeporteIndividual("Tenis").getAcademia().buscarInscripcion(club.buscarSocio("isabel"))
						.getRanking().getHistorial().size() == 2);
		Prueba.info("Ranking actual de la academia de tenis:");
		Prueba.lista(club.buscarDeporteIndividual("Tenis").getAcademia().getRankingActual());

		Prueba.seccion("Las academias sobreviven a guardar y cargar");
		Club recargado = Prueba.guardarYRecargar(club, "_prueba_academias");
		recargado.iniciarSesion("isabel", "1234");
		Prueba.verificar("partidos y ranking de isabel identicos tras recargar",
				club.consultarPartidosYRanking().equals(recargado.consultarPartidosYRanking()));
	}
}
