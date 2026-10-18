package global;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import administracion.Turno;
import comercio.ArticuloDeportivo;
import comercio.ProductoCafeteria;
import competencias.Competencia;
import deportes.Categoria;
import deportes.DeporteConjunto;
import deportes.DeporteIndividual;
import deportes.Equipo;
import deportes.Nivel;
import instalaciones.InstalacionConjunto;
import instalaciones.InstalacionIndividual;
import instalaciones.Modalidad;
import personas.Administrador;
import personas.Empleado;
import personas.Entrenador;
import personas.Fisioterapeuta;
import personas.Socio;

/**
 * Club de ejemplo que se usa cuando no hay datos guardados y en los programas de prueba.
 * Las edades se calculan respecto a Reloj.hoy() para que las categorias no cambien con el tiempo.
 *
 * Usuarios: admin/admin123, carlos/carlos123, laura/laura123 y jaime/jaime123 (entrenadores),
 * pedro/pedro123 (fisioterapeuta). Todos los socios tienen password 1234.
 */
public class DatosIniciales {

	public static final int TEMPORADA = 2026;
	public static final String LIGA_CERROS = "Liga de los Cerros";
	public static final String ASCUN_BALONCESTO = "ASCUN Baloncesto";

	public static Club crearClub() {
		Club club = new Club();

		// ---- Personas
		club.registrarPersona(new Administrador("Administrador General", "admin", "admin123"));
		club.registrarPersona(new Entrenador("Carlos Ruiz", "carlos", "carlos123", "DESC-CARLOS"));
		club.registrarPersona(new Entrenador("Laura Gomez", "laura", "laura123", "DESC-LAURA"));
		club.registrarPersona(new Fisioterapeuta("Pedro Diaz", "pedro", "pedro123", "DESC-PEDRO"));
		club.registrarPersona(new Entrenador("Jaime Ortiz", "jaime", "jaime123", "DESC-JAIME"));

		String[][] mayores = {
				{"ana", "Ana Torres"}, {"luis", "Luis Gomez"}, {"camila", "Camila Rojas"}, {"jorge", "Jorge Perez"},
				{"maria", "Maria Lopez"}, {"andres", "Andres Silva"}, {"juliana", "Juliana Castro"},
				{"felipe", "Felipe Moreno"}, {"daniela", "Daniela Vargas"}, {"santiago", "Santiago Herrera"},
				{"paula", "Paula Rios"}, {"sebastian", "Sebastian Ortiz"}, {"natalia", "Natalia Mendez"},
				{"ricardo", "Ricardo Pena"}, {"carolina", "Carolina Diaz"}, {"miguel", "Miguel Ramos"},
				{"isabel", "Isabel Cruz"}, {"nicolas", "Nicolas Vega"},
				{"diego", "Diego Navarro"}, {"valentina", "Valentina Ruiz"}};
		for (int i = 0; i < mayores.length; i++) {
			club.registrarPersona(new Socio(mayores[i][1], mayores[i][0], "1234", nacidoHace(20 + i % 10)));
		}
		club.registrarPersona(new Socio("Tomas Leon", "tomas", "1234", nacidoHace(14)));
		club.registrarPersona(new Socio("Martin Soto", "martin", "1234", nacidoHace(13)));
		club.registrarPersona(new Socio("Mateo Rincon", "mateo", "1234", nacidoHace(16)));
		club.registrarPersona(new Socio("Sofia Pardo", "sofia", "1234", nacidoHace(12)));

		// ---- Deportes (3 de conjunto y 3 academias individuales)
		DeporteConjunto futbol = new DeporteConjunto("Futbol", 7, true,
				List.of("Portero", "Defensa", "Mediocampista", "Delantero"));
		DeporteConjunto baloncesto = new DeporteConjunto("Baloncesto", 5, false,
				List.of("Base", "Escolta", "Alero", "Ala-pivot", "Pivot"));
		DeporteConjunto voleibol = new DeporteConjunto("Voleibol", 6, false,
				List.of("Armador", "Opuesto", "Central", "Punta", "Libero"));
		club.agregarDeporte(futbol);
		club.agregarDeporte(baloncesto);
		club.agregarDeporte(voleibol);
		DeporteIndividual tenis = new DeporteIndividual("Tenis", 2, List.of(Modalidad.SENCILLOS, Modalidad.DOBLES));
		DeporteIndividual padel = new DeporteIndividual("Padel", 2, List.of(Modalidad.DOBLES));
		DeporteIndividual tenisDeMesa = new DeporteIndividual("Tenis de mesa", 2, List.of(Modalidad.SENCILLOS, Modalidad.DOBLES));
		club.agregarDeporte(tenis);
		club.agregarDeporte(padel);
		club.agregarDeporte(tenisDeMesa);

		// ---- Equipos e inscripciones
		Equipo senecaFC = futbol.crearEquipo("Seneca FC", Categoria.MAYORES, 12);
		inscribir(club, senecaFC, new String[][] {
				{"ana", "Portero", "1"}, {"luis", "Defensa", "2"}, {"camila", "Defensa", "3"}, {"jorge", "Defensa", "4"},
				{"maria", "Mediocampista", "5"}, {"andres", "Mediocampista", "6"}, {"juliana", "Mediocampista", "8"},
				{"felipe", "Delantero", "9"}, {"daniela", "Delantero", "10"}, {"santiago", "Delantero", "11"}});

		Equipo senecaUnited = futbol.crearEquipo("Seneca United", Categoria.MAYORES, 12);
		inscribir(club, senecaUnited, new String[][] {
				{"paula", "Portero", "1"}, {"sebastian", "Defensa", "2"}, {"natalia", "Defensa", "3"},
				{"ricardo", "Defensa", "4"}, {"carolina", "Mediocampista", "5"}, {"miguel", "Mediocampista", "6"},
				{"isabel", "Delantero", "9"}, {"nicolas", "Delantero", "10"}});

		Equipo senecaSub15 = futbol.crearEquipo("Seneca Sub15", Categoria.SUB15, 1);
		inscribir(club, senecaSub15, new String[][] {{"tomas", "Delantero", "7"}});

		Equipo tigres = baloncesto.crearEquipo("Tigres", Categoria.MAYORES, 10);
		inscribir(club, tigres, new String[][] {
				{"ana", "Base", "4"}, {"luis", "Escolta", "5"}, {"maria", "Alero", "6"}, {"felipe", "Ala-pivot", "7"},
				{"paula", "Pivot", "8"}, {"sebastian", "Base", "9"}, {"natalia", "Alero", "10"}});

		Equipo panteras = baloncesto.crearEquipo("Panteras", Categoria.MAYORES, 10);
		inscribir(club, panteras, new String[][] {
				{"camila", "Base", "4"}, {"jorge", "Escolta", "5"}, {"andres", "Alero", "6"},
				{"juliana", "Ala-pivot", "7"}, {"daniela", "Pivot", "8"}});

		Equipo aguilas = voleibol.crearEquipo("Aguilas", Categoria.MAYORES, 10);
		inscribir(club, aguilas, new String[][] {
				{"carolina", "Armador", "1"}, {"miguel", "Opuesto", "2"}, {"isabel", "Central", "3"},
				{"nicolas", "Punta", "4"}, {"ricardo", "Libero", "5"}, {"santiago", "Punta", "6"}});

		// ---- Competencias (configuraciones previstas de los dos circuitos)
		club.agregarCompetencia(Competencia.torneoDeLosCerros(LIGA_CERROS, TEMPORADA, futbol, Categoria.MAYORES,
				List.of("Universidad Javeriana", "Universidad del Rosario", "Universidad Externado")));
		club.agregarCompetencia(Competencia.ascun(ASCUN_BALONCESTO, TEMPORADA, baloncesto, Categoria.MAYORES,
				List.of("Universidad Nacional", "Universidad de La Sabana", "Universidad EAFIT")));

		// ---- Instalaciones (3 canchas de tenis pero maximo 2 en uso a la vez)
		club.agregarInstalacion(new InstalacionIndividual("TEN-1", "Cancha de tenis 1", tenis, 4, false));
		club.agregarInstalacion(new InstalacionIndividual("TEN-2", "Cancha de tenis 2", tenis, 4, false));
		club.agregarInstalacion(new InstalacionIndividual("TEN-3", "Cancha de tenis 3", tenis, 2, false));
		club.agregarInstalacion(new InstalacionIndividual("PAD-1", "Cancha de padel techada", padel, 4, true));
		club.agregarInstalacion(new InstalacionIndividual("PAD-2", "Cancha de padel exterior", padel, 4, false));
		club.agregarInstalacion(new InstalacionIndividual("MESA-1", "Mesa de tenis de mesa techada", tenisDeMesa, 4, true));
		club.agregarInstalacion(new InstalacionIndividual("MESA-2", "Mesa de tenis de mesa exterior", tenisDeMesa, 2, false));
		club.agregarInstalacion(new InstalacionConjunto("FUT-1", "Cancha de futbol", futbol, 22));
		club.agregarInstalacion(new InstalacionConjunto("BAL-1", "Coliseo de baloncesto", baloncesto, 12));
		club.agregarInstalacion(new InstalacionConjunto("VOL-1", "Cancha de voleibol", voleibol, 12));

		// ---- Academias: socios inscritos con su nivel y ELO inicial
		academia(club, tenis, "diego", Nivel.AVANZADO, 1650);
		academia(club, tenis, "valentina", Nivel.INTERMEDIO, 1500);
		academia(club, tenis, "luis", Nivel.INTERMEDIO, 1480);
		academia(club, tenis, "ana", Nivel.PRINCIPIANTE, 1300);
		academia(club, tenis, "camila", Nivel.PRINCIPIANTE, 1320);
		academia(club, padel, "mateo", Nivel.INTERMEDIO, 1450);
		academia(club, padel, "maria", Nivel.INTERMEDIO, 1500);
		academia(club, padel, "jorge", Nivel.AVANZADO, 1600);
		academia(club, padel, "juliana", Nivel.AVANZADO, 1550);
		academia(club, tenisDeMesa, "sofia", Nivel.PRINCIPIANTE, 1200);
		academia(club, tenisDeMesa, "martin", Nivel.PRINCIPIANTE, 1250);
		academia(club, tenisDeMesa, "tomas", Nivel.INTERMEDIO, 1400);

		// ---- Tienda (existencias en Tienda y Bodega) y cafeteria
		agregarArticulo(club, "Balon de futbol", "Balones", 80000, 10, 20);
		agregarArticulo(club, "Raqueta de tenis", "Raquetas", 350000, 4, 6);
		agregarArticulo(club, "Tubo de pelotas", "Pelotas", 25000, 2, 30);
		agregarArticulo(club, "Uniforme Seneca", "Uniformes", 120000, 8, 0);
		agregarArticulo(club, "Malla de voleibol", "Mallas", 200000, 0, 3);
		club.agregarProducto(new ProductoCafeteria("Cafe", 5000, true, List.of()));
		club.agregarProducto(new ProductoCafeteria("Chocolate caliente", 6000, true, List.of("Lacteos")));
		club.agregarProducto(new ProductoCafeteria("Jugo natural", 7000, false, List.of()));
		club.agregarProducto(new ProductoCafeteria("Agua", 3000, false, List.of()));
		club.agregarProducto(new ProductoCafeteria("Galletas de mani", 4000, false, List.of("Mani", "Gluten")));
		club.agregarProducto(new ProductoCafeteria("Sandwich", 12000, false, List.of("Gluten", "Lacteos")));

		// ---- Turnos: carlos, laura y pedro cubren todo el horario; jaime refuerza lunes, miercoles y viernes en la manana
		for (DayOfWeek dia : DayOfWeek.values()) {
			if (dia != DayOfWeek.SUNDAY) {
				for (String login : List.of("carlos", "laura", "pedro")) {
					agregarTurno(club, login, dia, Turno.APERTURA, Turno.CIERRE);
				}
			}
		}
		for (DayOfWeek dia : List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)) {
			agregarTurno(club, "jaime", dia, Turno.APERTURA, LocalTime.of(14, 0));
		}

		return club;
	}

	private static LocalDate nacidoHace(int anios) {
		return Reloj.hoy().minusYears(anios).minusDays(30);
	}

	private static void inscribir(Club club, Equipo equipo, String[][] jugadores) {
		for (String[] j : jugadores) {
			equipo.inscribir(club.buscarSocio(j[0]), j[1], Integer.parseInt(j[2]));
		}
	}

	private static void academia(Club club, DeporteIndividual deporte, String login, Nivel nivel, int elo) {
		deporte.getAcademia().inscribir(club.buscarSocio(login), nivel, elo);
	}

	private static void agregarArticulo(Club club, String nombre, String categoria, long precio, int enTienda, int enBodega) {
		ArticuloDeportivo articulo = new ArticuloDeportivo(nombre, categoria, precio);
		articulo.restaurarExistencia(ArticuloDeportivo.TIENDA, enTienda);
		if (enBodega > 0) {
			articulo.restaurarExistencia("Bodega", enBodega);
		}
		club.agregarProducto(articulo);
	}

	private static void agregarTurno(Club club, String login, DayOfWeek dia, LocalTime inicio, LocalTime fin) {
		Empleado empleado = club.buscarEmpleado(login);
		club.agregarTurno(new Turno("T" + (club.getTurnos().size() + 1), empleado, dia, inicio, fin));
	}
}
