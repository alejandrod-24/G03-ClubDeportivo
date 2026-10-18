package pruebas;

import java.time.LocalDate;
import java.util.List;

import entrenamiento.Asistencia;
import global.Club;
import global.Reloj;
import instalaciones.Modalidad;

/**
 * 7. Consulta de desempeno en entrenamiento y de calendario de un socio.
 * RF-SOC-12, 13
 */
public class PruebaDesempenoYCalendario {

	private static Club club;

	//Lunes 19 de octubre de 2026 (las pruebas empiezan el sabado 17 a las 7:00)
	private static final LocalDate LUNES = LocalDate.of(2026, 10, 19);

	private static final List<String> FC = List.of("ana", "luis", "camila", "jorge", "maria", "andres", "juliana");

	public static void main(String[] args) {
		ejecutar();
		Prueba.resumen();
	}

	public static void ejecutar() {
		Prueba.titulo("PRUEBA 7: Desempeno en entrenamiento y calendario del socio");
		club = Prueba.clubDePrueba();
		String[] sesiones = prepararDatos();

		Prueba.seccion("Calendario (RF-SOC-13). Ahora es " + Reloj.ahora());
		club.iniciarSesion("ana", "1234");
		Prueba.info("Calendario de ana (entrenamiento, partido interno, partido de liga y practica):");
		Prueba.lista(club.consultarCalendario());
		club.iniciarSesion("diego", "1234");
		Prueba.info("Calendario de diego (entrenamiento de tenis, reserva y practica):");
		Prueba.lista(club.consultarCalendario());
		Prueba.verificar("diego tiene 3 eventos en su calendario", club.consultarCalendario().size() == 3);

		Prueba.seccion("Pasa la sesion de futbol y el entrenador registra asistencia y notas");
		Reloj.fijar(LUNES.atTime(11, 0));
		club.iniciarSesion("carlos", "carlos123");
		club.registrarAsistencia(sesiones[0], "ana", Asistencia.ASISTIO);
		club.registrarAsistencia(sesiones[0], "luis", Asistencia.LLEGO_TARDE);
		club.registrarAsistencia(sesiones[0], "camila", Asistencia.FALTO);
		club.registrarEvaluacion(sesiones[0], "ana", 5, "Excelente lectura de juego");
		club.registrarEvaluacion(sesiones[0], "luis", 3, "Debe llegar a tiempo");
		Prueba.info(club.buscarSesion(sesiones[0]).generarDetalle());

		Prueba.seccion("Consultar desempeno en entrenamiento (RF-SOC-12)");
		club.iniciarSesion("ana", "1234");
		Prueba.info(club.consultarDesempeno());
		club.iniciarSesion("luis", "1234");
		Prueba.info(club.consultarDesempeno());
		club.iniciarSesion("camila", "1234");
		Prueba.info(club.consultarDesempeno());
		club.iniciarSesion("valentina", "1234");
		Prueba.info(club.consultarDesempeno());

		Prueba.seccion("El calendario solo muestra lo que viene");
		club.iniciarSesion("ana", "1234");
		Prueba.lista(club.consultarCalendario());
		Prueba.verificar("la sesion que ya paso no aparece",
				!club.consultarCalendario().toString().contains(sesiones[0] + "]"));
		club.iniciarSesion("admin", "admin123");
		Prueba.debeRechazar("el administrador no tiene calendario de socio", () -> club.consultarCalendario());
	}

	//Una sesion de futbol y una de tenis el lunes, un partido interno y uno de liga el martes,
	//una reserva y una practica de diego y una practica de ana
	private static String[] prepararDatos() {
		club.iniciarSesion("carlos", "carlos123");
		String futbol = club.planificarSesion("FUT-1", LUNES.atTime(9, 0), 90, FC).getId();
		club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", LUNES.plusDays(1));
		club.inscribirEquipoEnCompetencia("Liga de los Cerros", 2026, "Futbol", "Seneca FC");
		club.definirNomina("Liga de los Cerros", 2026, "Seneca FC", FC);
		club.programarPartidoCompetencia("Liga de los Cerros", 2026, "Seneca FC", "Universidad del Rosario", "Cancha Seneca",
				LUNES.plusDays(4), competencias.Fase.LIGA);
		club.iniciarSesion("laura", "laura123");
		String tenis = club.planificarSesion("TEN-1", LUNES.atTime(15, 0), 60, List.of("diego", "valentina")).getId();
		club.iniciarSesion("diego", "1234");
		club.reservarInstalacion("Tenis", "TEN-2", LUNES.plusDays(1).atTime(9, 0), 2, Modalidad.SENCILLOS);
		club.iniciarSesion("valentina", "1234");
		club.programarPartidoPractica("Tenis", "TEN-1", LUNES.plusDays(2).atTime(10, 0), List.of("diego"));
		club.iniciarSesion("ana", "1234");
		club.programarPartidoPractica("Tenis", "TEN-3", LUNES.plusDays(2).atTime(16, 0), List.of("camila"));
		club.cerrarSesion();
		return new String[] {futbol, tenis};
	}
}
