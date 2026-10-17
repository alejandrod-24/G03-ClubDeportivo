package pruebas;

import java.util.List;

import deportes.Categoria;
import global.Club;
import global.DatosIniciales;
import global.Reloj;

/**
 * 14. Creacion y baja de equipos y creacion de competencias.
 * RF-ADM-04, 05, 07. (La gestion de instalaciones RF-ADM-06 llega en la etapa 3.)
 */
public class PruebaGestionDeportiva {

	private static Club club;

	public static void main(String[] args) {
		ejecutar();
		Prueba.resumen();
	}

	public static void ejecutar() {
		Prueba.titulo("PRUEBA 14: Gestion deportiva (administrador)");
		club = Prueba.clubDePrueba();
		club.iniciarSesion("admin", "admin123");

		Prueba.seccion("Crear equipo (RF-ADM-04)");
		Prueba.debeFuncionar("crear 'Halcones' de Voleibol SUB17 con cupo 12", () ->
				Prueba.info("Creado: " + club.crearEquipo("Voleibol", "Halcones", Categoria.SUB17, 12)));
		Prueba.debeRechazar("nombre repetido en el mismo deporte", () -> club.crearEquipo("Voleibol", "Halcones", Categoria.MAYORES, 12));
		Prueba.debeRechazar("deporte inexistente", () -> club.crearEquipo("Rugby", "Rinocerontes", Categoria.MAYORES, 15));
		Prueba.debeRechazar("deporte individual (Tenis)", () -> club.crearEquipo("Tenis", "Raquetas", Categoria.MAYORES, 4));
		Prueba.debeRechazar("sin categoria", () -> club.crearEquipo("Futbol", "Sin Categoria", null, 15));
		Prueba.debeRechazar("cupo maximo cero", () -> club.crearEquipo("Futbol", "Sin Cupo", Categoria.MAYORES, 0));

		Prueba.seccion("Dar de baja un equipo (RF-ADM-05)");
		club.iniciarSesion("carlos", "carlos123");
		club.programarPartidoInterno("Baloncesto", "Tigres", "Panteras", Reloj.hoy().plusDays(5));
		club.inscribirEquipoEnCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Futbol", "Seneca FC");
		club.iniciarSesion("admin", "admin123");
		Prueba.debeRechazar("Tigres tiene un partido interno programado", () -> club.darDeBajaEquipo("Baloncesto", "Tigres"));
		Prueba.debeRechazar("Seneca FC esta inscrito en una competencia en curso", () -> club.darDeBajaEquipo("Futbol", "Seneca FC"));
		Prueba.info("Antes: " + club.buscarEquipo("Voleibol", "Aguilas"));
		Prueba.debeFuncionar("dar de baja Aguilas (voleibol)", () -> club.darDeBajaEquipo("Voleibol", "Aguilas"));
		Prueba.info("Despues: " + club.buscarEquipo("Voleibol", "Aguilas"));
		Prueba.verificar("sus jugadores quedan libres (carolina ya no esta en el equipo)",
				!club.buscarEquipo("Voleibol", "Aguilas").estaInscrito(club.buscarSocio("carolina")));
		Prueba.debeRechazar("dar de baja otra vez", () -> club.darDeBajaEquipo("Voleibol", "Aguilas"));

		Prueba.seccion("Crear competencia (RF-ADM-07)");
		Prueba.debeFuncionar("crear 'Torneo Relampago' (Torneo de los Cerros, Voleibol SUB17)", () ->
				Prueba.info("Creada: " + club.crearCompetenciaCerros("Torneo Relampago", 2026, "Voleibol", Categoria.SUB17,
						List.of("Colegio San Bartolome", "Gimnasio Moderno"))));
		Prueba.debeFuncionar("crear 'ASCUN Voleibol' (fase de grupos y eliminacion)", () ->
				Prueba.info("Creada: " + club.crearCompetenciaAscun("ASCUN Voleibol", 2026, "Voleibol", Categoria.MAYORES,
						List.of("Universidad Nacional", "Universidad de los Andes", "Universidad del Valle"))));
		Prueba.debeRechazar("mismo nombre y temporada", () -> club.crearCompetenciaCerros("Torneo Relampago", 2026, "Voleibol",
				Categoria.SUB17, List.of("Colegio San Bartolome")));
		Prueba.debeFuncionar("mismo nombre en otra temporada si se permite", () -> club.crearCompetenciaCerros("Torneo Relampago", 2027,
				"Voleibol", Categoria.SUB17, List.of("Colegio San Bartolome")));
		Prueba.debeRechazar("institucion repetida", () -> club.crearCompetenciaCerros("Copa Norte", 2026, "Futbol",
				Categoria.MAYORES, List.of("Universidad Javeriana", "Universidad Javeriana")));
		Prueba.debeRechazar("deporte individual", () -> club.crearCompetenciaCerros("Copa Tenis", 2026, "Tenis",
				Categoria.MAYORES, List.of("Universidad Javeriana")));

		Prueba.info("Competencias del club:");
		Prueba.lista(club.getCompetencias());
	}
}
