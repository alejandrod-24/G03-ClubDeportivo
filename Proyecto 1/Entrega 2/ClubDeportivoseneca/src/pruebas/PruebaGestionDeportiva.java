package pruebas;

import java.time.LocalDate;
import java.util.List;

import deportes.Categoria;
import global.Club;
import global.DatosIniciales;
import global.Reloj;
import instalaciones.Modalidad;

/**
 * 14. Creacion y baja de equipos, gestion de instalaciones y creacion de competencias.
 * RF-ADM-04 a 07
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

		Prueba.seccion("Gestionar instalaciones (RF-ADM-06)");
		Prueba.debeFuncionar("crear la cancha de tenis TEN-4 (capacidad 2)", () ->
				Prueba.info("Creada: " + club.crearInstalacionIndividual("TEN-4", "Cancha de tenis 4", "Tenis", 2, false)));
		Prueba.debeFuncionar("crear la cancha de futbol FUT-2 (capacidad 22)", () ->
				Prueba.info("Creada: " + club.crearInstalacionConjunto("FUT-2", "Cancha de futbol 2", "Futbol", 22)));
		Prueba.debeRechazar("codigo que ya existe (TEN-1)", () -> club.crearInstalacionIndividual("TEN-1", "Otra", "Tenis", 2, false));
		Prueba.debeRechazar("capacidad invalida para padel (siempre 4)", () -> club.crearInstalacionIndividual("PAD-3", "Padel 3", "Padel", 2, false));
		Prueba.debeRechazar("capacidad invalida para tenis (3)", () -> club.crearInstalacionIndividual("TEN-5", "Tenis 5", "Tenis", 3, false));
		Prueba.debeRechazar("cancha de futbol donde no caben dos equipos (10)", () -> club.crearInstalacionConjunto("FUT-3", "Mini", "Futbol", 10));
		Prueba.debeRechazar("deporte inexistente", () -> club.crearInstalacionIndividual("SQ-1", "Squash", "Squash", 2, false));
		Prueba.debeFuncionar("editar TEN-4: nuevo nombre y capacidad 4", () -> club.editarInstalacion("TEN-4", "Cancha de tenis 4 (renovada)", 4));
		Prueba.debeRechazar("editar TEN-4 con capacidad 5", () -> club.editarInstalacion("TEN-4", "Cancha de tenis 4", 5));
		club.iniciarSesion("valentina", "1234");
		club.reservarInstalacion("Tenis", "TEN-4", LocalDate.of(2026, 10, 19).atTime(9, 0), 2, Modalidad.SENCILLOS);
		club.iniciarSesion("admin", "admin123");
		Prueba.debeRechazar("desactivar TEN-4, que tiene una reserva futura", () -> club.desactivarInstalacion("TEN-4"));
		Prueba.debeFuncionar("desactivar MESA-2", () -> club.desactivarInstalacion("MESA-2"));
		Prueba.debeRechazar("desactivarla otra vez", () -> club.desactivarInstalacion("MESA-2"));
		club.iniciarSesion("sofia", "1234");
		Prueba.debeRechazar("no se puede reservar una instalacion desactivada", () -> club.reservarInstalacion("Tenis de mesa", "MESA-2",
				LocalDate.of(2026, 10, 19).atTime(9, 0), 2, Modalidad.SENCILLOS));
		club.iniciarSesion("admin", "admin123");
		Prueba.info("Inventario de instalaciones:");
		Prueba.lista(club.getInstalaciones());
	}
}
