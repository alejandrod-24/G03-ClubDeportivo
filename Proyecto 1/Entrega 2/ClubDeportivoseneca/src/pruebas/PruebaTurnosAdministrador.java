package pruebas;

import java.time.DayOfWeek;
import java.time.LocalTime;

import administracion.Turno;
import global.Club;

/**
 * 16. Asignacion de turnos semanales y resolucion de solicitudes de cambio de turno,
 *     validando la cobertura minima (2 entrenadores y 1 fisioterapeuta en todo el horario de atencion).
 * RF-ADM-13, 14
 */
public class PruebaTurnosAdministrador {

	private static Club club;

	public static void main(String[] args) {
		ejecutar();
		Prueba.resumen();
	}

	public static void ejecutar() {
		Prueba.titulo("PRUEBA 16: Turnos y cobertura minima (administrador)");
		club = Prueba.clubDePrueba();
		club.iniciarSesion("admin", "admin123");

		Prueba.seccion("Asignar turnos semanales (RF-ADM-13)");
		Prueba.info("Horario de atencion: lunes a sabado de " + Turno.APERTURA + " a " + Turno.CIERRE
				+ ". carlos, laura y pedro cubren todo; jaime refuerza lunes, miercoles y viernes en la manana.");
		Prueba.debeFuncionar("asignar a jaime el martes de 14:00 a 20:00", () -> Prueba.info("Asignado: "
				+ club.asignarTurno("jaime", DayOfWeek.TUESDAY, LocalTime.of(14, 0), LocalTime.of(20, 0))));
		Prueba.debeRechazar("domingo (el club no atiende)",
				() -> club.asignarTurno("jaime", DayOfWeek.SUNDAY, LocalTime.of(8, 0), LocalTime.of(14, 0)));
		Prueba.debeRechazar("fuera del horario (19:00 a 21:00)",
				() -> club.asignarTurno("jaime", DayOfWeek.THURSDAY, LocalTime.of(19, 0), LocalTime.of(21, 0)));
		Prueba.debeRechazar("la hora de fin antes de la de inicio",
				() -> club.asignarTurno("jaime", DayOfWeek.THURSDAY, LocalTime.of(14, 0), LocalTime.of(10, 0)));
		Prueba.debeRechazar("se cruza con otro turno de jaime (martes 16:00 a 18:00)",
				() -> club.asignarTurno("jaime", DayOfWeek.TUESDAY, LocalTime.of(16, 0), LocalTime.of(18, 0)));
		Prueba.debeRechazar("ana no es empleada", () -> club.asignarTurno("ana", DayOfWeek.TUESDAY, LocalTime.of(8, 0),
				LocalTime.of(10, 0)));
		club.iniciarSesion("carlos", "carlos123");
		Prueba.debeRechazar("un entrenador no asigna turnos",
				() -> club.asignarTurno("jaime", DayOfWeek.THURSDAY, LocalTime.of(8, 0), LocalTime.of(10, 0)));

		//Preparar: solicitudes de varios empleados
		club.iniciarSesion("laura", "laura123");
		String lunesLaura = club.solicitarCambioTurno(turnoDe("laura", DayOfWeek.MONDAY), LocalTime.of(14, 0),
				LocalTime.of(20, 0)).getId();
		club.iniciarSesion("carlos", "carlos123");
		String miercolesCarlos = club.solicitarCambioTurno(turnoDe("carlos", DayOfWeek.WEDNESDAY), LocalTime.of(8, 0),
				LocalTime.of(14, 0)).getId();
		club.iniciarSesion("pedro", "pedro123");
		String sabadoPedro = club.solicitarCambioTurno(turnoDe("pedro", DayOfWeek.SATURDAY), LocalTime.of(8, 0),
				LocalTime.of(14, 0)).getId();
		club.iniciarSesion("jaime", "jaime123");
		String martesJaime = club.solicitarCambioTurno(turnoDe("jaime", DayOfWeek.TUESDAY), LocalTime.of(15, 0),
				LocalTime.of(20, 0)).getId();

		Prueba.seccion("Resolver solicitudes de cambio de turno (RF-ADM-14)");
		club.iniciarSesion("admin", "admin123");
		Prueba.info("Solicitudes:");
		Prueba.lista(club.getSolicitudes());
		Prueba.debeFuncionar("aprobar a laura: el lunes en la manana quedan carlos y jaime",
				() -> club.aprobarSolicitudCambioTurno(lunesLaura));
		Prueba.verificar("el turno del lunes de laura ahora es de 14:00 a 20:00",
				club.buscarTurno(turnoDe("laura", DayOfWeek.MONDAY)).getHoraInicio().equals(LocalTime.of(14, 0)));
		Prueba.debeRechazar("aprobar a carlos: el miercoles en la tarde quedaria solo laura",
				() -> club.aprobarSolicitudCambioTurno(miercolesCarlos));
		Prueba.debeFuncionar("rechazar la solicitud de carlos", () -> club.rechazarSolicitudCambioTurno(miercolesCarlos));
		Prueba.debeRechazar("aprobar a pedro: el sabado en la tarde no quedaria ningun fisioterapeuta",
				() -> club.aprobarSolicitudCambioTurno(sabadoPedro));
		Prueba.debeFuncionar("rechazar la solicitud de pedro", () -> club.rechazarSolicitudCambioTurno(sabadoPedro));
		Prueba.debeFuncionar("aprobar a jaime: el martes de 14:00 a 15:00 siguen carlos y laura",
				() -> club.aprobarSolicitudCambioTurno(martesJaime));
		Prueba.debeRechazar("aprobar otra vez una solicitud ya resuelta", () -> club.aprobarSolicitudCambioTurno(lunesLaura));
		Prueba.debeRechazar("solicitud inexistente", () -> club.aprobarSolicitudCambioTurno("C99"));
		Prueba.info("Solicitudes despues de resolverlas:");
		Prueba.lista(club.getSolicitudes());

		club.iniciarSesion("carlos", "carlos123");
		Prueba.info("carlos consulta sus solicitudes:");
		Prueba.lista(club.consultarSolicitudes());
		club.iniciarSesion("laura", "laura123");
		Prueba.debeRechazar("un empleado no aprueba solicitudes", () -> club.aprobarSolicitudCambioTurno(miercolesCarlos));

		Prueba.seccion("Los turnos y las solicitudes sobreviven a guardar y cargar");
		Club recargado = Prueba.guardarYRecargar(club, "_prueba_turnos");
		Prueba.verificar("turnos identicos tras recargar", club.getTurnos().toString().equals(recargado.getTurnos().toString()));
		Prueba.verificar("solicitudes identicas tras recargar",
				club.getSolicitudes().toString().equals(recargado.getSolicitudes().toString()));
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
