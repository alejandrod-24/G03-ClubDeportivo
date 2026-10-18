package pruebas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import entrenamiento.Asistencia;
import global.Club;
import global.Reloj;

/**
 * 12. Planeacion, modificacion y cancelacion de sesiones de entrenamiento, registro de asistencia
 *     y evaluacion, y consulta de analiticas y agenda del entrenador.
 * RF-ENT-11 a 16
 */
public class PruebaEntrenamiento {

	private static Club club;
	private static String sesionFutbol;
	private static String sesionTenis;

	//Lunes 19 de octubre de 2026 (las pruebas empiezan el sabado 17 a las 7:00)
	private static final LocalDate LUNES = LocalDate.of(2026, 10, 19);
	private static final LocalDateTime LUNES_9 = LUNES.atTime(9, 0);

	private static final List<String> FC = List.of("ana", "luis", "camila", "jorge", "maria", "andres", "juliana");
	private static final List<String> TIGRES = List.of("ana", "luis", "maria", "felipe", "paula");

	public static void main(String[] args) {
		ejecutar();
		Prueba.resumen();
	}

	public static void ejecutar() {
		Prueba.titulo("PRUEBA 12: Sesiones de entrenamiento");
		club = Prueba.clubDePrueba();

		Prueba.seccion("Planificar sesion (RF-ENT-11). Ahora es " + Reloj.ahora());
		club.iniciarSesion("carlos", "carlos123");
		Prueba.debeFuncionar("carlos planifica futbol en FUT-1 el lunes 9:00 por 90 minutos", () -> {
			sesionFutbol = club.planificarSesion("FUT-1", LUNES_9, 90, FC).getId();
			Prueba.info("Planificada: " + club.buscarSesion(sesionFutbol));
		});
		Prueba.debeRechazar("fecha pasada", () -> club.planificarSesion("FUT-1", LocalDate.of(2026, 10, 16).atTime(9, 0), 60, FC));
		Prueba.debeRechazar("duracion cero", () -> club.planificarSesion("BAL-1", LUNES.atTime(14, 0), 0, TIGRES));
		Prueba.debeRechazar("termina despues del cierre (19:30 + 60 min)", () -> club.planificarSesion("BAL-1", LUNES.atTime(19, 30), 60, TIGRES));
		Prueba.debeRechazar("otra sesion de carlos se cruza (BAL-1 a las 10:00)",
				() -> club.planificarSesion("BAL-1", LUNES.atTime(10, 0), 60, List.of("felipe", "paula")));
		Prueba.debeRechazar("convocado no inscrito en baloncesto (diego)",
				() -> club.planificarSesion("BAL-1", LUNES.atTime(14, 0), 60, List.of("felipe", "diego")));
		Prueba.debeRechazar("convocado repetido", () -> club.planificarSesion("BAL-1", LUNES.atTime(14, 0), 60, List.of("felipe", "felipe")));
		club.iniciarSesion("laura", "laura123");
		Prueba.debeRechazar("instalacion ocupada (FUT-1 a las 10:00)",
				() -> club.planificarSesion("FUT-1", LUNES.atTime(10, 0), 60, List.of("santiago")));
		Prueba.debeRechazar("convocado con otro compromiso (ana esta en la sesion de carlos)",
				() -> club.planificarSesion("BAL-1", LUNES.atTime(9, 30), 60, TIGRES));
		Prueba.debeFuncionar("laura planifica tenis en TEN-1 el martes con diego y valentina", () ->
				sesionTenis = club.planificarSesion("TEN-1", LUNES.plusDays(1).atTime(9, 0), 60, List.of("diego", "valentina")).getId());
		club.iniciarSesion("diego", "1234");
		Prueba.debeRechazar("ahora diego no puede reservar a esa hora (tiene entrenamiento)",
				() -> club.reservarInstalacion("Tenis", "TEN-2", LUNES.plusDays(1).atTime(9, 0), 2, instalaciones.Modalidad.SENCILLOS));

		Prueba.seccion("Modificar o cancelar sesion (RF-ENT-12)");
		club.iniciarSesion("carlos", "carlos123");
		Prueba.debeRechazar("carlos no puede modificar la sesion de laura",
				() -> club.modificarSesion(sesionTenis, "TEN-1", LUNES.plusDays(1).atTime(10, 0), 60, List.of("diego")));
		Prueba.debeFuncionar("carlos mueve su sesion a las 8:00 y le agrega a felipe", () -> {
			club.modificarSesion(sesionFutbol, "FUT-1", LUNES.atTime(8, 0), 120, List.of("ana", "luis", "camila", "jorge", "felipe"));
			Prueba.info("Modificada: " + club.buscarSesion(sesionFutbol));
		});
		club.iniciarSesion("laura", "laura123");
		Prueba.debeFuncionar("laura cancela su sesion de tenis", () -> club.cancelarSesion(sesionTenis));
		Prueba.debeRechazar("modificar una sesion cancelada",
				() -> club.modificarSesion(sesionTenis, "TEN-1", LUNES.plusDays(1).atTime(10, 0), 60, List.of("diego")));

		Prueba.seccion("Registrar asistencia y evaluacion (RF-ENT-13, 14)");
		club.iniciarSesion("carlos", "carlos123");
		Reloj.fijar(LUNES_9);
		Prueba.info("Ahora es " + Reloj.ahora() + " (la sesion va de 8:00 a 10:00)");
		Prueba.debeRechazar("registrar asistencia antes de que termine la sesion",
				() -> club.registrarAsistencia(sesionFutbol, "ana", Asistencia.ASISTIO));
		Prueba.debeRechazar("modificar una sesion que ya empezo",
				() -> club.modificarSesion(sesionFutbol, "FUT-1", LUNES.atTime(15, 0), 60, FC));
		Prueba.debeRechazar("cancelar una sesion que ya empezo", () -> club.cancelarSesion(sesionFutbol));
		Reloj.fijar(LUNES.atTime(10, 5));
		Prueba.info("Ahora es " + Reloj.ahora());
		Prueba.debeFuncionar("asistencia: ana asistio, luis llego tarde, camila y jorge faltaron, felipe asistio", () -> {
			club.registrarAsistencia(sesionFutbol, "ana", Asistencia.ASISTIO);
			club.registrarAsistencia(sesionFutbol, "luis", Asistencia.LLEGO_TARDE);
			club.registrarAsistencia(sesionFutbol, "camila", Asistencia.FALTO);
			club.registrarAsistencia(sesionFutbol, "jorge", Asistencia.FALTO);
			club.registrarAsistencia(sesionFutbol, "felipe", Asistencia.ASISTIO);
		});
		Prueba.debeRechazar("asistencia de alguien no convocado (maria)",
				() -> club.registrarAsistencia(sesionFutbol, "maria", Asistencia.ASISTIO));
		Prueba.debeRechazar("evaluar a camila, que falto", () -> club.registrarEvaluacion(sesionFutbol, "camila", 4, ""));
		Prueba.debeRechazar("nota fuera de 1 a 5", () -> club.registrarEvaluacion(sesionFutbol, "ana", 6, ""));
		Prueba.debeFuncionar("notas: ana 5, luis 3, felipe 4", () -> {
			club.registrarEvaluacion(sesionFutbol, "ana", 5, "Gran liderazgo");
			club.registrarEvaluacion(sesionFutbol, "luis", 3, "Mejorar la puntualidad");
			club.registrarEvaluacion(sesionFutbol, "felipe", 4, "");
		});
		Prueba.info(club.buscarSesion(sesionFutbol).generarDetalle());

		Prueba.seccion("Analiticas de jugador o grupo (RF-ENT-15)");
		LocalDate octubre = LocalDate.of(2026, 10, 1);
		LocalDate finOctubre = LocalDate.of(2026, 10, 31);
		Prueba.info(club.consultarAnaliticasJugador("ana", octubre, finOctubre));
		Prueba.info(club.consultarAnaliticasGrupo("Futbol", "Seneca FC", octubre, finOctubre));
		Prueba.info(club.consultarAnaliticasGrupo("Tenis", null, octubre, finOctubre));
		Prueba.debeRechazar("periodo invalido", () -> club.consultarAnaliticasJugador("ana", finOctubre, octubre));
		Prueba.debeRechazar("jugador inexistente", () -> club.consultarAnaliticasJugador("fantasma", octubre, finOctubre));
		Prueba.debeRechazar("grupo inexistente", () -> club.consultarAnaliticasGrupo("Futbol", "Galacticos", octubre, finOctubre));

		Prueba.seccion("Consultar agenda (RF-ENT-16)");
		club.planificarSesion("BAL-1", LUNES.plusDays(2).atTime(17, 0), 60, TIGRES);
		club.programarPartidoInterno("Baloncesto", "Tigres", "Panteras", LUNES.plusDays(3));
		club.iniciarSesion("ana", "1234");
		club.programarPartidoPractica("Tenis", "TEN-1", LUNES.plusDays(1).atTime(15, 0), List.of("carlos"));
		club.iniciarSesion("carlos", "carlos123");
		Prueba.info("Agenda de carlos:");
		Prueba.lista(club.consultarAgenda());

		Prueba.seccion("Las sesiones sobreviven a guardar y cargar");
		club.iniciarSesion("admin", "admin123");
		Club recargado = Prueba.guardarYRecargar(club, "_prueba_entrenamiento");
		recargado.iniciarSesion("admin", "admin123");
		Prueba.verificar("historial de sesiones identico tras recargar", club.historialSesiones(null, null, null, null, null)
				.equals(recargado.historialSesiones(null, null, null, null, null)));
	}
}
