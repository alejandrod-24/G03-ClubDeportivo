package pruebas;

import java.util.ArrayList;
import java.util.List;

import deportes.Categoria;
import deportes.InscripcionEquipo;
import global.Club;
import global.DatosIniciales;

/**
 * 3. Inscripcion y retiro de un socio en un equipo de conjunto, con casos de rechazo.
 * RF-SOC-01, 02, 03
 */
public class PruebaInscripcionEquipos {

	private static Club club;

	public static void main(String[] args) {
		ejecutar();
		Prueba.resumen();
	}

	public static void ejecutar() {
		Prueba.titulo("PRUEBA 3: Inscripcion y retiro de equipos");
		club = Prueba.clubDePrueba();

		//Preparar: un equipo dado de baja
		club.iniciarSesion("admin", "admin123");
		club.crearEquipo("Futbol", "Veteranos", Categoria.MAYORES, 15);
		club.darDeBajaEquipo("Futbol", "Veteranos");

		Prueba.seccion("Inscribirse a un equipo (RF-SOC-02)");
		club.iniciarSesion("diego", "1234");
		Prueba.info("Socio: diego, " + club.buscarSocio("diego").getEdad() + " anios, categoria " + club.buscarSocio("diego").getCategoria());
		Prueba.info("Camisetas usadas en Seneca FC: " + camisetas("Seneca FC"));
		Prueba.debeRechazar("camiseta 10 ya usada", () -> club.inscribirseEnEquipo("Futbol", "Seneca FC", "Delantero", 10));
		Prueba.debeRechazar("posicion inexistente 'Arquero'", () -> club.inscribirseEnEquipo("Futbol", "Seneca FC", "Arquero", 20));
		Prueba.debeRechazar("edad no corresponde a la categoria (Seneca Sub15)",
				() -> club.inscribirseEnEquipo("Futbol", "Seneca Sub15", "Defensa", 20));
		Prueba.debeRechazar("equipo inactivo (Veteranos)", () -> club.inscribirseEnEquipo("Futbol", "Veteranos", "Defensa", 20));
		Prueba.debeRechazar("equipo inexistente", () -> club.inscribirseEnEquipo("Futbol", "Galacticos", "Defensa", 20));
		Prueba.debeFuncionar("diego se inscribe en Seneca FC como Delantero con la 20",
				() -> club.inscribirseEnEquipo("Futbol", "Seneca FC", "Delantero", 20));
		Prueba.debeRechazar("ya esta en un equipo de futbol (Seneca United)",
				() -> club.inscribirseEnEquipo("Futbol", "Seneca United", "Delantero", 20));
		Prueba.debeFuncionar("si puede inscribirse en otro deporte (Voleibol, Aguilas)",
				() -> club.inscribirseEnEquipo("Voleibol", "Aguilas", "Central", 7));

		club.iniciarSesion("martin", "1234");
		Prueba.info("Seneca Sub15: " + club.buscarEquipo("Futbol", "Seneca Sub15"));
		Prueba.debeRechazar("cupo lleno (martin, 13 anios, en Seneca Sub15)",
				() -> club.inscribirseEnEquipo("Futbol", "Seneca Sub15", "Defensa", 3));

		Prueba.seccion("Consultar perfil (RF-SOC-01)");
		club.iniciarSesion("diego", "1234");
		Prueba.info(club.consultarPerfil());

		Prueba.seccion("Retirarse de un equipo (RF-SOC-03)");
		Prueba.debeRechazar("retirarse de un equipo donde no esta inscrito",
				() -> club.retirarseDeEquipo("Futbol", "Seneca United"));

		//Preparar: Seneca FC entra a la liga con diego en la nomina
		club.iniciarSesion("carlos", "carlos123");
		club.inscribirEquipoEnCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Futbol", "Seneca FC");
		club.definirNomina(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Seneca FC",
				List.of("ana", "luis", "camila", "jorge", "maria", "andres", "juliana", "diego"));
		club.iniciarSesion("diego", "1234");
		Prueba.debeRechazar("retirarse estando en la nomina de una competencia en curso",
				() -> club.retirarseDeEquipo("Futbol", "Seneca FC"));

		club.iniciarSesion("admin", "admin123");
		club.finalizarCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA);
		club.iniciarSesion("diego", "1234");
		Prueba.debeFuncionar("con la competencia finalizada, diego se retira de Seneca FC",
				() -> club.retirarseDeEquipo("Futbol", "Seneca FC"));
		Prueba.verificar("la camiseta 20 queda libre", !camisetas("Seneca FC").contains(20));
		Prueba.info(club.consultarPerfil());
	}

	private static List<Integer> camisetas(String equipo) {
		List<Integer> numeros = new ArrayList<>();
		for (InscripcionEquipo i : club.buscarEquipo("Futbol", equipo).getInscripciones()) {
			numeros.add(i.getNumeroCamiseta());
		}
		return numeros;
	}
}
