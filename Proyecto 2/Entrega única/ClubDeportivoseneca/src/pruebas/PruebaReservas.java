package pruebas;

import java.time.LocalDate;
import java.time.LocalDateTime;

import global.Club;
import global.Reloj;
import instalaciones.Modalidad;

/**
 * 5. Disponibilidad, reserva, cancelacion e ingreso/salida de una instalacion individual,
 *    con rechazo por cupo agotado y por restriccion de bebida caliente.
 * RF-SOC-06 a 09 y RF-SOC-17
 */
public class PruebaReservas {

	private static Club club;
	private static String reservaDiego;

	//Lunes 19 de octubre de 2026 (las pruebas empiezan el sabado 17 a las 7:00)
	private static final LocalDate LUNES = LocalDate.of(2026, 10, 19);
	private static final LocalDateTime LUNES_9 = LUNES.atTime(9, 0);

	public static void main(String[] args) {
		ejecutar();
		Prueba.resumen();
	}

	public static void ejecutar() {
		Prueba.titulo("PRUEBA 5: Reservas e ingreso a instalaciones");
		club = Prueba.clubDePrueba();

		Prueba.seccion("Consultar disponibilidad (RF-SOC-06). Ahora es " + Reloj.ahora());
		club.iniciarSesion("diego", "1234");
		Prueba.lista(club.consultarDisponibilidad("Tenis", LUNES_9));
		Prueba.debeRechazar("bloque fuera del horario (21:00)", () -> club.consultarDisponibilidad("Tenis", LUNES.atTime(21, 0)));
		Prueba.debeRechazar("domingo (el club no abre)", () -> club.consultarDisponibilidad("Tenis", LocalDate.of(2026, 10, 18).atTime(9, 0)));
		Prueba.debeRechazar("deporte inexistente", () -> club.consultarDisponibilidad("Squash", LUNES_9));

		Prueba.seccion("Reservar una instalacion (RF-SOC-07)");
		Prueba.debeFuncionar("diego reserva TEN-1 el lunes a las 9:00 (sencillos, 2 jugadores)", () -> {
			reservaDiego = club.reservarInstalacion("Tenis", "TEN-1", LUNES_9, 2, Modalidad.SENCILLOS).getId();
			Prueba.info("Reserva: " + club.buscarReserva(reservaDiego));
		});
		Prueba.debeRechazar("diego ya tiene una reserva en ese horario",
				() -> club.reservarInstalacion("Tenis", "TEN-2", LUNES_9, 2, Modalidad.SENCILLOS));
		club.iniciarSesion("valentina", "1234");
		Prueba.debeRechazar("instalacion ocupada (TEN-1)", () -> club.reservarInstalacion("Tenis", "TEN-1", LUNES_9, 2, Modalidad.SENCILLOS));
		Prueba.debeFuncionar("valentina reserva TEN-2 (dobles, 4 jugadores)",
				() -> club.reservarInstalacion("Tenis", "TEN-2", LUNES_9, 4, Modalidad.DOBLES));
		club.iniciarSesion("luis", "1234");
		Prueba.info("Disponibilidad despues de dos reservas:");
		Prueba.lista(club.consultarDisponibilidad("Tenis", LUNES_9));
		Prueba.debeRechazar("cupo agotado: TEN-3 esta libre pero ya hay 2 canchas de tenis en uso",
				() -> club.reservarInstalacion("Tenis", "TEN-3", LUNES_9, 2, Modalidad.SENCILLOS));
		Prueba.debeRechazar("mas jugadores que la capacidad (dobles en TEN-3, capacidad 2)",
				() -> club.reservarInstalacion("Tenis", "TEN-3", LUNES.atTime(10, 0), 4, Modalidad.DOBLES));
		Prueba.debeRechazar("bloque pasado (viernes 16)",
				() -> club.reservarInstalacion("Tenis", "TEN-3", LocalDate.of(2026, 10, 16).atTime(9, 0), 2, Modalidad.SENCILLOS));
		Prueba.debeRechazar("bloque que no empieza en punto",
				() -> club.reservarInstalacion("Tenis", "TEN-3", LUNES.atTime(10, 30), 2, Modalidad.SENCILLOS));
		club.iniciarSesion("nicolas", "1234");
		Prueba.debeRechazar("nicolas no esta inscrito en tenis",
				() -> club.reservarInstalacion("Tenis", "TEN-3", LUNES.atTime(10, 0), 2, Modalidad.SENCILLOS));
		club.iniciarSesion("maria", "1234");
		Prueba.debeRechazar("el padel no se juega en sencillos",
				() -> club.reservarInstalacion("Padel", "PAD-2", LUNES_9, 2, Modalidad.SENCILLOS));
		Prueba.debeRechazar("instalacion de otro deporte (TEN-1 para padel)",
				() -> club.reservarInstalacion("Padel", "TEN-1", LUNES_9, 4, Modalidad.DOBLES));

		Prueba.seccion("Cancelar reserva (RF-SOC-08)");
		Prueba.debeRechazar("maria intenta cancelar la reserva de diego", () -> club.cancelarReserva(reservaDiego));
		club.iniciarSesion("diego", "1234");
		Prueba.debeFuncionar("diego cancela su reserva", () -> club.cancelarReserva(reservaDiego));
		Prueba.debeRechazar("cancelarla otra vez", () -> club.cancelarReserva(reservaDiego));
		Prueba.info("Disponibilidad despues de cancelar:");
		Prueba.lista(club.consultarDisponibilidad("Tenis", LUNES_9));

		Prueba.seccion("Registrar ingreso y salida (RF-SOC-09)");
		String[] once = new String[1];
		Prueba.debeFuncionar("diego reserva TEN-1 el lunes a las 11:00",
				() -> once[0] = club.reservarInstalacion("Tenis", "TEN-1", LUNES.atTime(11, 0), 2, Modalidad.SENCILLOS).getId());
		Prueba.debeRechazar("entrar antes de la hora de su reserva", () -> club.registrarIngreso("TEN-1"));
		Reloj.fijar(LUNES.atTime(11, 10));
		Prueba.info("Ahora es " + Reloj.ahora());
		Prueba.debeRechazar("entrar a una cancha sin reserva (TEN-2)", () -> club.registrarIngreso("TEN-2"));
		Prueba.debeFuncionar("diego entra a TEN-1", () -> Prueba.info("Ingreso: " + club.registrarIngreso("TEN-1")));
		Prueba.debeRechazar("entrar otra vez: ya esta en una instalacion", () -> club.registrarIngreso("TEN-1"));
		Prueba.debeRechazar("cancelar una reserva cuyo bloque ya inicio", () -> club.cancelarReserva(once[0]));
		Prueba.debeFuncionar("diego sale de TEN-1", () -> club.registrarSalida());
		Prueba.debeRechazar("salir otra vez", () -> club.registrarSalida());

		Prueba.seccion("Restriccion de bebida caliente (padel techada)");
		club.iniciarSesion("maria", "1234");
		Prueba.debeFuncionar("maria reserva la cancha de padel techada a las 12:00 (dobles)",
				() -> club.reservarInstalacion("Padel", "PAD-1", LUNES.atTime(12, 0), 4, Modalidad.DOBLES));
		Prueba.debeFuncionar("maria compra un cafe (aun no esta en la cancha)",
				() -> club.comprarEnCafeteria(Prueba.items("Cafe", 1), 0, 10, true));
		Reloj.fijar(LUNES.atTime(12, 5));
		Prueba.debeRechazar("maria intenta entrar a PAD-1 con la bebida caliente", () -> club.registrarIngreso("PAD-1"));
		Prueba.debeFuncionar("maria declara que termino su bebida (RF-SOC-17)", () -> club.declararFinBebidaCaliente());
		Prueba.debeFuncionar("ahora si entra a PAD-1", () -> club.registrarIngreso("PAD-1"));
		Prueba.debeRechazar("dentro de PAD-1 no se le despacha un chocolate caliente",
				() -> club.comprarEnCafeteria(Prueba.items("Chocolate caliente", 1), 0, 10, true));
		Prueba.debeFuncionar("pero si un jugo natural", () -> club.comprarEnCafeteria(Prueba.items("Jugo natural", 1), 0, 10, true));
		Prueba.info(club.consultarPerfil());
		Prueba.debeFuncionar("maria sale de PAD-1", () -> club.registrarSalida());

		Prueba.seccion("Las reservas e ingresos sobreviven a guardar y cargar");
		club.iniciarSesion("admin", "admin123");
		Club recargado = Prueba.guardarYRecargar(club, "_prueba_reservas");
		recargado.iniciarSesion("admin", "admin123");
		Prueba.verificar("historial de reservas identico tras recargar", club.historialReservas(null, null, null, null, null).toString()
				.equals(recargado.historialReservas(null, null, null, null, null).toString()));
		Prueba.verificar("ingresos identicos tras recargar", club.getIngresos().toString().equals(recargado.getIngresos().toString()));
	}
}
