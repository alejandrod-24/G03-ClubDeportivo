package pruebas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import global.Club;
import persistencia.Persistencia;
import personas.Persona;
import personas.Rol;

/**
 * 1. Login exitoso y rechazo de credenciales incorrectas, cambio de contrasena,
 *    control de acceso por rol y carga/guardado del estado desde archivo.
 * RF-GEN-01, 02, 03, 04
 */
@DisplayName("1. Autenticacion, contrasenas y persistencia")
class AutenticacionTest {

	private Club club;

	@BeforeEach
	void setUp() {
		club = Utilidades.clubDePrueba();
	}

	@Test
	@DisplayName("RF-GEN-01: inicio de sesion exitoso y reconocimiento del rol")
	void iniciarSesion() {
		Persona admin = club.iniciarSesion("admin", "admin123");
		assertEquals(Rol.ADMINISTRADOR, admin.getRol());
		Persona ana = club.iniciarSesion("ana", "1234");
		assertEquals(Rol.SOCIO, ana.getRol(), "el sistema reconoce que ana es socia");
	}

	@Test
	@DisplayName("RF-GEN-01: rechaza credenciales incorrectas")
	void rechazarCredencialesIncorrectas() {
		assertThrows(RuntimeException.class, () -> club.iniciarSesion("ana", "clave-mala"), "password incorrecto");
		assertThrows(RuntimeException.class, () -> club.iniciarSesion("fantasma", "1234"), "usuario inexistente");
	}

	@Test
	@DisplayName("RF-GEN-02: cada rol solo puede hacer sus acciones")
	void controlDeAccesoPorRol() {
		club.iniciarSesion("ana", "1234");
		assertThrows(RuntimeException.class,
				() -> club.crearSocio("Intruso", "intruso", "1234", Utilidades.FECHA_INICIAL.minusYears(20)),
				"un socio intenta crear otro socio (accion de administrador)");
		assertThrows(RuntimeException.class,
				() -> club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", Utilidades.FECHA_INICIAL.plusDays(1)),
				"un socio intenta programar un partido (accion de entrenador)");
		club.iniciarSesion("carlos", "carlos123");
		assertThrows(RuntimeException.class, () -> club.inscribirseEnEquipo("Futbol", "Seneca FC", "Defensa", 30),
				"un entrenador intenta inscribirse a un equipo (accion de socio)");
	}

	@Test
	@DisplayName("RF-GEN-03: cambio de contrasena")
	void cambiarPassword() {
		club.iniciarSesion("ana", "1234");
		assertThrows(RuntimeException.class, () -> club.cambiarPassword("otra", "nueva123"), "contrasena actual incorrecta");
		assertThrows(RuntimeException.class, () -> club.cambiarPassword("1234", ""), "nueva contrasena vacia");
		assertDoesNotThrow(() -> club.cambiarPassword("1234", "nueva123"));
		assertThrows(RuntimeException.class, () -> club.iniciarSesion("ana", "1234"), "ya no entra con la contrasena vieja");
		assertDoesNotThrow(() -> club.iniciarSesion("ana", "nueva123"), "entra con la contrasena nueva");
	}

	@Test
	@DisplayName("RF-GEN-03: no se cambia la contrasena sin sesion iniciada")
	void cambiarPasswordSinSesion() {
		club.iniciarSesion("ana", "1234");
		club.cerrarSesion();
		assertThrows(RuntimeException.class, () -> club.cambiarPassword("1234", "otra123"));
	}

	@Test
	@DisplayName("RF-GEN-04: guardar el estado y recuperarlo en un club nuevo")
	void guardarYRecuperarEstado(@TempDir Path carpeta) {
		club.iniciarSesion("ana", "1234");
		club.cambiarPassword("1234", "nueva123");
		Persistencia persistencia = new Persistencia(carpeta.toString());

		assertDoesNotThrow(() -> persistencia.salvar(club));
		File[] archivos = carpeta.toFile().listFiles((dir, nombre) -> nombre.endsWith(".txt"));
		assertNotNull(archivos);
		assertTrue(archivos.length > 0, "se generan archivos .txt");

		Club otro = assertDoesNotThrow(() -> persistencia.cargar());
		assertEquals(club.getPersonas().size(), otro.getPersonas().size(), "se recuperan las personas");
		assertEquals(club.buscarDeporteConjunto("Futbol").getEquipos().size(),
				otro.buscarDeporteConjunto("Futbol").getEquipos().size(), "se recuperan los equipos de futbol");
		assertEquals(club.getProductos().size(), otro.getProductos().size(), "se recuperan los productos");
		assertEquals(club.getTurnos().size(), otro.getTurnos().size(), "se recuperan los turnos");
		assertEquals(club.getInstalaciones().size(), otro.getInstalaciones().size(), "se recuperan las instalaciones");
		assertDoesNotThrow(() -> otro.iniciarSesion("ana", "nueva123"), "la contrasena cambiada de ana se conserva");
	}

	@Test
	@DisplayName("RF-GEN-04: rechaza cargar desde una carpeta inexistente")
	void cargarCarpetaInexistente(@TempDir Path carpeta) {
		assertThrows(RuntimeException.class, () -> new Persistencia(carpeta.resolve("no_existe").toString()).cargar());
	}

	@Test
	@DisplayName("RF-GEN-04: rechaza cargar un archivo corrupto")
	void cargarArchivoCorrupto(@TempDir Path carpeta) throws IOException {
		Files.writeString(carpeta.resolve("personas.txt"), "SOCIO;Ana;ana;1234;quizas;no-es-fecha\n");
		assertThrows(RuntimeException.class, () -> new Persistencia(carpeta.toString()).cargar());
	}
}
