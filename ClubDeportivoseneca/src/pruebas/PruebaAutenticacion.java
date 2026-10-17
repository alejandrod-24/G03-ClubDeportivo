package pruebas;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import global.Club;
import persistencia.Persistencia;
import personas.Persona;

/**
 * 1. Login exitoso y rechazo de credenciales incorrectas, cambio de contrasena,
 *    control de acceso por rol y carga/guardado del estado desde archivo.
 * RF-GEN-01, 02, 03, 04
 */
public class PruebaAutenticacion {

	private static Club club;

	public static void main(String[] args) {
		ejecutar();
		Prueba.resumen();
	}

	public static void ejecutar() {
		Prueba.titulo("PRUEBA 1: Autenticacion, contrasenas y persistencia");
		club = Prueba.clubDePrueba();

		Prueba.seccion("Inicio de sesion (RF-GEN-01)");
		Prueba.debeFuncionar("admin inicia sesion con admin123", () -> {
			Persona p = club.iniciarSesion("admin", "admin123");
			Prueba.info("Sesion iniciada: " + p);
		});
		Prueba.debeFuncionar("el socio ana inicia sesion y el sistema reconoce su rol", () -> {
			Persona p = club.iniciarSesion("ana", "1234");
			Prueba.info("Rol detectado: " + p.getRol());
		});
		Prueba.debeRechazar("password incorrecto", () -> club.iniciarSesion("ana", "clave-mala"));
		Prueba.debeRechazar("usuario inexistente", () -> club.iniciarSesion("fantasma", "1234"));

		Prueba.seccion("Control de acceso por rol (RF-GEN-02)");
		club.iniciarSesion("ana", "1234");
		Prueba.debeRechazar("un socio intenta crear otro socio (accion de administrador)",
				() -> club.crearSocio("Intruso", "intruso", "1234", Prueba.FECHA_INICIAL.minusYears(20)));
		Prueba.debeRechazar("un socio intenta programar un partido (accion de entrenador)",
				() -> club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", Prueba.FECHA_INICIAL.plusDays(1)));
		club.iniciarSesion("carlos", "carlos123");
		Prueba.debeRechazar("un entrenador intenta inscribirse a un equipo (accion de socio)",
				() -> club.inscribirseEnEquipo("Futbol", "Seneca FC", "Defensa", 30));

		Prueba.seccion("Cambio de contrasena (RF-GEN-03)");
		club.iniciarSesion("ana", "1234");
		Prueba.debeRechazar("contrasena actual incorrecta", () -> club.cambiarPassword("otra", "nueva123"));
		Prueba.debeRechazar("nueva contrasena vacia", () -> club.cambiarPassword("1234", ""));
		Prueba.debeFuncionar("ana cambia su contrasena a nueva123", () -> club.cambiarPassword("1234", "nueva123"));
		Prueba.debeRechazar("ya no puede entrar con la contrasena vieja", () -> club.iniciarSesion("ana", "1234"));
		Prueba.debeFuncionar("entra con la contrasena nueva", () -> club.iniciarSesion("ana", "nueva123"));
		club.cerrarSesion();
		Prueba.debeRechazar("cambiar contrasena sin sesion iniciada", () -> club.cambiarPassword("nueva123", "otra123"));

		Prueba.seccion("Guardar y recuperar el estado (RF-GEN-04)");
		Persistencia persistencia = new Persistencia();
		Prueba.debeFuncionar("guardar el estado en la carpeta datos/", () -> persistencia.salvar(club));
		File[] archivos = new File(Persistencia.CARPETA_POR_DEFECTO).listFiles((dir, nombre) -> nombre.endsWith(".txt"));
		Prueba.info("Archivos generados: " + (archivos == null ? 0 : archivos.length));
		if (archivos != null) {
			for (File f : archivos) {
				Prueba.info("  datos/" + f.getName());
			}
		}
		Club[] recargado = new Club[1];
		Prueba.debeFuncionar("cargar el estado desde datos/ en un club nuevo", () -> recargado[0] = persistencia.cargar());
		if (recargado[0] != null) {
			Club otro = recargado[0];
			Prueba.verificar("se recuperan las " + club.getPersonas().size() + " personas",
					otro.getPersonas().size() == club.getPersonas().size());
			Prueba.verificar("se recuperan los equipos de futbol",
					otro.buscarDeporteConjunto("Futbol").getEquipos().size() == club.buscarDeporteConjunto("Futbol").getEquipos().size());
			Prueba.verificar("se recuperan los " + club.getProductos().size() + " productos y los "
					+ club.getTurnos().size() + " turnos", otro.getProductos().size() == club.getProductos().size()
							&& otro.getTurnos().size() == club.getTurnos().size());
			Prueba.debeFuncionar("la contrasena cambiada de ana se conserva tras recargar",
					() -> otro.iniciarSesion("ana", "nueva123"));
		}
		Prueba.debeRechazar("cargar desde una carpeta inexistente",
				() -> new Persistencia("datos/no_existe").cargar());

		File corrupta = new File("datos/_prueba_corrupta");
		try {
			corrupta.mkdirs();
			Files.writeString(new File(corrupta, "personas.txt").toPath(), "SOCIO;Ana;ana;1234;quizas;no-es-fecha\n");
			Prueba.debeRechazar("cargar un archivo corrupto", () -> new Persistencia(corrupta.getPath()).cargar());
		} catch (IOException e) {
			Prueba.verificar("no se pudo crear el archivo corrupto de prueba: " + e.getMessage(), false);
		} finally {
			new File(corrupta, "personas.txt").delete();
			corrupta.delete();
		}
	}
}
