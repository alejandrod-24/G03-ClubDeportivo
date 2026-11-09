package pruebas;

import java.time.LocalDate;
import java.util.List;

import global.Club;
import global.DatosIniciales;
import instalaciones.Modalidad;
import personas.Administrador;
import personas.Entrenador;

/**
 * 13. Creacion y baja de jugadores y empleados por parte del administrador.
 * RF-ADM-01, 02, 03
 */
public class PruebaGestionUsuarios {

	private static Club club;

	public static void main(String[] args) {
		ejecutar();
		Prueba.resumen();
	}

	public static void ejecutar() {
		Prueba.titulo("PRUEBA 13: Gestion de usuarios (administrador)");
		club = Prueba.clubDePrueba();
		club.iniciarSesion("admin", "admin123");

		Prueba.seccion("Crear jugador (RF-ADM-01)");
		Prueba.debeFuncionar("crear socio Elena (25 anios)", () -> {
			club.crearSocio("Elena Paz", "elena", "1234", Prueba.FECHA_INICIAL.minusYears(25));
			Prueba.info("Creado: " + club.buscarPersona("elena") + ", categoria " + club.buscarSocio("elena").getCategoria());
		});
		Prueba.debeRechazar("login repetido",
				() -> club.crearSocio("Otra Elena", "elena", "1234", Prueba.FECHA_INICIAL.minusYears(30)));
		Prueba.debeRechazar("fecha de nacimiento futura",
				() -> club.crearSocio("Futuro", "futuro", "1234", Prueba.FECHA_INICIAL.plusDays(5)));
		Prueba.debeRechazar("contrasena vacia",
				() -> club.crearSocio("Sin Clave", "sinclave", "", Prueba.FECHA_INICIAL.minusYears(20)));
		Prueba.debeRechazar("falta el nombre",
				() -> club.crearSocio("", "sinnombre", "1234", Prueba.FECHA_INICIAL.minusYears(20)));
		Prueba.debeRechazar("un segundo administrador",
				() -> club.registrarPersona(new Administrador("Otro Admin", "admin2", "admin123")));

		Prueba.seccion("Gestionar empleados (RF-ADM-03)");
		Prueba.debeFuncionar("crear entrenador Rosa con codigo de descuento generado", () -> {
			Entrenador e = club.crearEntrenador("Rosa Medina", "rosa", "rosa123");
			Prueba.info("Creado: " + e + " | codigo: " + e.getCodigoDescuento());
		});
		Prueba.debeFuncionar("crear fisioterapeuta Hugo", () -> {
			club.crearFisioterapeuta("Hugo Lara", "hugo", "hugo123");
			Prueba.info("Creado: " + club.buscarPersona("hugo"));
		});
		Prueba.debeRechazar("crear empleado con login existente", () -> club.crearEntrenador("Carlos 2", "carlos", "x"));
		Prueba.debeFuncionar("restablecer la contrasena de rosa a nueva456",
				() -> club.restablecerPassword("rosa", "nueva456"));
		Prueba.debeFuncionar("rosa entra con la contrasena restablecida", () -> {
			club.iniciarSesion("rosa", "nueva456");
			Prueba.info("Su codigo de descuento: " + club.consultarCodigoDescuento());
		});
		club.iniciarSesion("admin", "admin123");
		Prueba.debeFuncionar("dar de baja al fisioterapeuta hugo", () -> club.darDeBajaPersona("hugo"));
		Prueba.debeRechazar("hugo ya no puede iniciar sesion", () -> club.iniciarSesion("hugo", "hugo123"));

		Prueba.seccion("Dar de baja jugador (RF-ADM-02)");
		club.iniciarSesion("admin", "admin123");
		Prueba.info("Antes: felipe esta en " + club.buscarEquipo("Futbol", "Seneca FC") + " y " + club.buscarEquipo("Baloncesto", "Tigres"));
		Prueba.debeFuncionar("dar de baja a felipe", () -> club.darDeBajaPersona("felipe"));
		Prueba.verificar("felipe fue retirado de Seneca FC",
				!club.buscarEquipo("Futbol", "Seneca FC").estaInscrito(club.buscarSocio("felipe")));
		Prueba.verificar("felipe fue retirado de Tigres",
				!club.buscarEquipo("Baloncesto", "Tigres").estaInscrito(club.buscarSocio("felipe")));
		Prueba.info("Despues: " + club.buscarEquipo("Futbol", "Seneca FC") + " y " + club.buscarEquipo("Baloncesto", "Tigres"));
		Prueba.debeRechazar("felipe no puede iniciar sesion", () -> club.iniciarSesion("felipe", "1234"));
		club.iniciarSesion("admin", "admin123");
		Prueba.debeRechazar("dar de baja otra vez a felipe", () -> club.darDeBajaPersona("felipe"));
		Prueba.verificar("el historial se conserva: felipe sigue registrado en el club",
				club.buscarPersona("felipe") != null);

		//Preparar: Seneca FC inscrito en la liga con luis en la nomina
		club.iniciarSesion("carlos", "carlos123");
		club.inscribirEquipoEnCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Futbol", "Seneca FC");
		club.definirNomina(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Seneca FC",
				List.of("ana", "luis", "camila", "jorge", "maria", "andres", "juliana", "daniela"));
		club.iniciarSesion("admin", "admin123");
		Prueba.debeRechazar("dar de baja a luis, que esta en la nomina de una competencia en curso",
				() -> club.darDeBajaPersona("luis"));
		Prueba.debeRechazar("el administrador no puede darse de baja a si mismo", () -> club.darDeBajaPersona("admin"));

		Prueba.seccion("Bajas que afectan academias, reservas y sesiones (RF-ADM-02, 03)");
		LocalDate lunes = LocalDate.of(2026, 10, 19);
		club.iniciarSesion("diego", "1234");
		String reserva = club.reservarInstalacion("Tenis", "TEN-1", lunes.atTime(9, 0), 2, Modalidad.SENCILLOS).getId();
		club.iniciarSesion("carlos", "carlos123");
		club.planificarSesion("TEN-2", lunes.plusDays(1).atTime(9, 0), 60, List.of("valentina"));
		club.iniciarSesion("admin", "admin123");
		Prueba.debeFuncionar("dar de baja a diego, inscrito en la academia de tenis con una reserva futura",
				() -> club.darDeBajaPersona("diego"));
		Prueba.verificar("su reserva futura quedo cancelada", club.buscarReserva(reserva).estaCancelada());
		Prueba.verificar("ya no esta inscrito en la academia de tenis",
				!club.buscarDeporteIndividual("Tenis").getAcademia().estaInscrito(club.buscarSocio("diego")));
		Prueba.debeRechazar("dar de baja a carlos, que tiene una sesion de entrenamiento futura",
				() -> club.darDeBajaPersona("carlos"));
	}
}
