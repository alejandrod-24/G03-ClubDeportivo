package pruebas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import global.Club;
import global.DatosIniciales;
import instalaciones.Modalidad;
import personas.Administrador;
import personas.Entrenador;

/**
 * 13. Creacion y baja de jugadores y empleados por parte del administrador.
 * RF-ADM-01, 02, 03
 */
@DisplayName("13. Gestion de usuarios (administrador)")
class GestionUsuariosTest {

	private static final LocalDate HOY = Utilidades.FECHA_INICIAL;

	private Club club;

	@BeforeEach
	void setUp() {
		club = Utilidades.clubDePrueba();
		club.iniciarSesion("admin", "admin123");
	}

	@Test
	@DisplayName("RF-ADM-01: crear un jugador")
	void crearSocio() {
		assertDoesNotThrow(() -> club.crearSocio("Elena Paz", "elena", "1234", HOY.minusYears(25)));
		assertNotNull(club.buscarSocio("elena"));
		assertThrows(RuntimeException.class, () -> club.crearSocio("Otra Elena", "elena", "1234", HOY.minusYears(30)),
				"login repetido");
		assertThrows(RuntimeException.class, () -> club.crearSocio("Futuro", "futuro", "1234", HOY.plusDays(5)),
				"fecha de nacimiento futura");
		assertThrows(RuntimeException.class, () -> club.crearSocio("Sin Clave", "sinclave", "", HOY.minusYears(20)),
				"contrasena vacia");
		assertThrows(RuntimeException.class, () -> club.crearSocio("", "sinnombre", "1234", HOY.minusYears(20)),
				"falta el nombre");
		assertThrows(RuntimeException.class, () -> club.registrarPersona(new Administrador("Otro Admin", "admin2", "admin123")),
				"un segundo administrador");
	}

	@Test
	@DisplayName("RF-ADM-03: crear empleados y restablecer su contrasena")
	void gestionarEmpleados() {
		Entrenador rosa = assertDoesNotThrow(() -> club.crearEntrenador("Rosa Medina", "rosa", "rosa123"));
		assertNotNull(rosa.getCodigoDescuento(), "se genera su codigo de descuento");
		assertDoesNotThrow(() -> club.crearFisioterapeuta("Hugo Lara", "hugo", "hugo123"));
		assertThrows(RuntimeException.class, () -> club.crearEntrenador("Carlos 2", "carlos", "x"), "login existente");
		assertDoesNotThrow(() -> club.restablecerPassword("rosa", "nueva456"));
		assertDoesNotThrow(() -> club.iniciarSesion("rosa", "nueva456"), "rosa entra con la contrasena restablecida");
		assertEquals(rosa.getCodigoDescuento(), club.consultarCodigoDescuento());
	}

	@Test
	@DisplayName("RF-ADM-03: dar de baja a un empleado")
	void darDeBajaEmpleado() {
		club.crearFisioterapeuta("Hugo Lara", "hugo", "hugo123");
		assertDoesNotThrow(() -> club.darDeBajaPersona("hugo"));
		assertThrows(RuntimeException.class, () -> club.iniciarSesion("hugo", "hugo123"), "hugo ya no puede iniciar sesion");
	}

	@Test
	@DisplayName("RF-ADM-02: dar de baja a un jugador lo retira de sus equipos y conserva su historial")
	void darDeBajaJugador() {
		assertDoesNotThrow(() -> club.darDeBajaPersona("felipe"));
		assertFalse(club.buscarEquipo("Futbol", "Seneca FC").estaInscrito(club.buscarSocio("felipe")), "retirado de Seneca FC");
		assertFalse(club.buscarEquipo("Baloncesto", "Tigres").estaInscrito(club.buscarSocio("felipe")), "retirado de Tigres");
		assertThrows(RuntimeException.class, () -> club.iniciarSesion("felipe", "1234"), "felipe no puede iniciar sesion");
		club.iniciarSesion("admin", "admin123");
		assertThrows(RuntimeException.class, () -> club.darDeBajaPersona("felipe"), "darlo de baja otra vez");
		assertNotNull(club.buscarPersona("felipe"), "felipe sigue registrado en el club");
	}

	@Test
	@DisplayName("RF-ADM-02: no se da de baja a quien esta en una nomina en curso, ni el administrador a si mismo")
	void bajasNoPermitidas() {
		club.iniciarSesion("carlos", "carlos123");
		club.inscribirEquipoEnCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Futbol", "Seneca FC");
		club.definirNomina(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Seneca FC",
				List.of("ana", "luis", "camila", "jorge", "maria", "andres", "juliana", "daniela"));
		club.iniciarSesion("admin", "admin123");
		assertThrows(RuntimeException.class, () -> club.darDeBajaPersona("luis"), "luis esta en la nomina");
		assertThrows(RuntimeException.class, () -> club.darDeBajaPersona("admin"), "el administrador no se da de baja");
	}

	@Test
	@DisplayName("RF-ADM-02, 03: bajas que afectan academias, reservas y sesiones")
	void bajasConAcademiasReservasYSesiones() {
		club.iniciarSesion("diego", "1234");
		String reserva = club.reservarInstalacion("Tenis", "TEN-1", Utilidades.LUNES.atTime(9, 0), 2, Modalidad.SENCILLOS).getId();
		club.iniciarSesion("carlos", "carlos123");
		club.planificarSesion("TEN-2", Utilidades.LUNES.plusDays(1).atTime(9, 0), 60, List.of("valentina"));
		club.iniciarSesion("admin", "admin123");

		assertDoesNotThrow(() -> club.darDeBajaPersona("diego"));
		assertTrue(club.buscarReserva(reserva).estaCancelada(), "su reserva futura quedo cancelada");
		assertFalse(club.buscarDeporteIndividual("Tenis").getAcademia().estaInscrito(club.buscarSocio("diego")),
				"ya no esta en la academia de tenis");
		assertThrows(RuntimeException.class, () -> club.darDeBajaPersona("carlos"),
				"carlos tiene una sesion de entrenamiento futura");
	}
}
