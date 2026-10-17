package pruebas;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import administracion.Turno;
import global.Club;

/**
 * 9. Consulta y solicitud de cambio de turno, y sugerencia de nuevos productos por parte de los empleados.
 * RF-EMP-01 a 03, 06, 07
 */
public class PruebaTurnosYSugerenciasEmpleado {

	private static Club club;

	public static void main(String[] args) {
		ejecutar();
		Prueba.resumen();
	}

	public static void ejecutar() {
		Prueba.titulo("PRUEBA 9: Turnos y sugerencias de los empleados");
		club = Prueba.clubDePrueba();
		club.iniciarSesion("admin", "admin123");
		club.asignarTurno("jaime", DayOfWeek.MONDAY, LocalTime.of(16, 0), LocalTime.of(20, 0));

		Prueba.seccion("Consultar mis turnos (RF-EMP-01)");
		club.iniciarSesion("laura", "laura123");
		Prueba.info("Turnos de laura:");
		Prueba.lista(club.consultarTurnos());
		club.iniciarSesion("jaime", "jaime123");
		Prueba.info("Turnos de jaime:");
		Prueba.lista(club.consultarTurnos());

		Prueba.seccion("Solicitar cambio de turno (RF-EMP-02)");
		club.iniciarSesion("laura", "laura123");
		String lunesLaura = turnoDe("laura", DayOfWeek.MONDAY);
		Prueba.debeFuncionar("laura pide que su turno del lunes sea de 14:00 a 20:00", () -> Prueba.info("Creada: "
				+ club.solicitarCambioTurno(lunesLaura, LocalTime.of(14, 0), LocalTime.of(20, 0))));
		Prueba.debeRechazar("pedir otro cambio para el mismo turno (ya hay uno pendiente)",
				() -> club.solicitarCambioTurno(lunesLaura, LocalTime.of(8, 0), LocalTime.of(14, 0)));
		Prueba.debeRechazar("pedir un cambio en el turno de carlos",
				() -> club.solicitarCambioTurno(turnoDe("carlos", DayOfWeek.MONDAY), LocalTime.of(8, 0), LocalTime.of(14, 0)));
		Prueba.debeRechazar("horario fuera de atencion (6:00 a 14:00)",
				() -> club.solicitarCambioTurno(turnoDe("laura", DayOfWeek.TUESDAY), LocalTime.of(6, 0), LocalTime.of(14, 0)));
		Prueba.debeRechazar("la hora de fin antes de la de inicio",
				() -> club.solicitarCambioTurno(turnoDe("laura", DayOfWeek.TUESDAY), LocalTime.of(15, 0), LocalTime.of(14, 0)));
		Prueba.debeRechazar("turno inexistente", () -> club.solicitarCambioTurno("T999", LocalTime.of(8, 0), LocalTime.of(14, 0)));
		club.iniciarSesion("jaime", "jaime123");
		Prueba.debeRechazar("jaime alarga su turno del lunes hasta las 17:00 y se cruza con su otro turno de ese dia",
				() -> club.solicitarCambioTurno(turnoDe("jaime", DayOfWeek.MONDAY), LocalTime.of(8, 0), LocalTime.of(17, 0)));
		club.iniciarSesion("pedro", "pedro123");
		Prueba.debeFuncionar("el fisioterapeuta pedro pide salir a las 14:00 el sabado", () -> club.solicitarCambioTurno(
				turnoDe("pedro", DayOfWeek.SATURDAY), LocalTime.of(8, 0), LocalTime.of(14, 0)));

		Prueba.seccion("Consultar mis solicitudes (RF-EMP-03)");
		club.iniciarSesion("laura", "laura123");
		Prueba.info("Solicitudes de laura:");
		Prueba.lista(club.consultarSolicitudes());
		club.iniciarSesion("jaime", "jaime123");
		Prueba.info("Solicitudes de jaime:");
		Prueba.lista(club.consultarSolicitudes());
		club.iniciarSesion("ana", "1234");
		Prueba.debeRechazar("un socio no tiene turnos", () -> club.consultarTurnos());

		Prueba.seccion("Sugerir articulo o snack (RF-EMP-06)");
		club.iniciarSesion("pedro", "pedro123");
		Prueba.debeFuncionar("sugerir 'Gel de recuperacion' (Recuperacion, $45000)", () -> Prueba.info("Creada: "
				+ club.sugerirProducto("Gel de recuperacion", "Recuperacion", 45000, List.of())));
		Prueba.debeFuncionar("sugerir el snack 'Barra de proteina' con alergenos", () -> Prueba.info("Creada: "
				+ club.sugerirProducto("Barra de proteina", "Snacks", 6000, List.of("Mani", "Lacteos"))));
		Prueba.debeRechazar("un producto que ya esta en el catalogo (Cafe)",
				() -> club.sugerirProducto("Cafe", "Bebidas", 5000, List.of()));
		Prueba.debeRechazar("ya hay una sugerencia pendiente con ese nombre",
				() -> club.sugerirProducto("Gel de recuperacion", "Recuperacion", 40000, List.of()));
		Prueba.debeRechazar("precio cero", () -> club.sugerirProducto("Banda elastica", "Recuperacion", 0, List.of()));
		Prueba.debeRechazar("falta la categoria", () -> club.sugerirProducto("Banda elastica", "", 15000, List.of()));
		Prueba.debeRechazar("falta el nombre", () -> club.sugerirProducto(null, "Recuperacion", 15000, List.of()));
		club.iniciarSesion("carlos", "carlos123");
		Prueba.debeFuncionar("el entrenador carlos tambien sugiere 'Conos de entrenamiento'",
				() -> club.sugerirProducto("Conos de entrenamiento", "Accesorios", 30000, List.of()));
		club.iniciarSesion("ana", "1234");
		Prueba.debeRechazar("un socio no puede sugerir productos",
				() -> club.sugerirProducto("Banda elastica", "Recuperacion", 15000, List.of()));

		Prueba.seccion("Consultar mis sugerencias (RF-EMP-07)");
		club.iniciarSesion("admin", "admin123");
		club.rechazarSugerencia("SG2");
		club.iniciarSesion("pedro", "pedro123");
		Prueba.info("Sugerencias de pedro (el administrador rechazo SG2):");
		Prueba.lista(club.consultarSugerencias());
		Prueba.debeFuncionar("despues del rechazo puede volver a sugerir 'Barra de proteina'",
				() -> club.sugerirProducto("Barra de proteina", "Snacks", 5000, List.of("Mani")));
	}

	private static String turnoDe(String login, DayOfWeek dia) {
		for (Turno t : club.getTurnos()) {
			if (t.getEmpleado().getLogin().equals(login) && t.getDia() == dia) {
				return t.getId();
			}
		}
		throw new IllegalArgumentException(login + " no tiene turno el " + dia);
	}
}
