package pruebas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import deportes.Categoria;
import global.Club;
import global.DatosIniciales;
import global.Reloj;
import instalaciones.Modalidad;

/**
 * 14. Creacion y baja de equipos, gestion de instalaciones y creacion de competencias.
 * RF-ADM-04 a 07
 */
@DisplayName("14. Gestion deportiva (administrador)")
class GestionDeportivaTest {

	private Club club;

	@BeforeEach
	void setUp() {
		club = Utilidades.clubDePrueba();
		club.iniciarSesion("admin", "admin123");
	}

	@Test
	@DisplayName("RF-ADM-04: crear un equipo")
	void crearEquipo() {
		assertNotNull(assertDoesNotThrow(() -> club.crearEquipo("Voleibol", "Halcones", Categoria.SUB17, 12)));
		assertThrows(RuntimeException.class, () -> club.crearEquipo("Voleibol", "Halcones", Categoria.MAYORES, 12),
				"nombre repetido en el mismo deporte");
		assertThrows(RuntimeException.class, () -> club.crearEquipo("Rugby", "Rinocerontes", Categoria.MAYORES, 15),
				"deporte inexistente");
		assertThrows(RuntimeException.class, () -> club.crearEquipo("Tenis", "Raquetas", Categoria.MAYORES, 4),
				"deporte individual");
		assertThrows(RuntimeException.class, () -> club.crearEquipo("Futbol", "Sin Categoria", null, 15), "sin categoria");
		assertThrows(RuntimeException.class, () -> club.crearEquipo("Futbol", "Sin Cupo", Categoria.MAYORES, 0), "cupo cero");
	}

	@Test
	@DisplayName("RF-ADM-05: dar de baja un equipo")
	void darDeBajaEquipo() {
		club.iniciarSesion("carlos", "carlos123");
		club.programarPartidoInterno("Baloncesto", "Tigres", "Panteras", Reloj.hoy().plusDays(5));
		club.inscribirEquipoEnCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Futbol", "Seneca FC");
		club.iniciarSesion("admin", "admin123");

		assertThrows(RuntimeException.class, () -> club.darDeBajaEquipo("Baloncesto", "Tigres"),
				"Tigres tiene un partido interno programado");
		assertThrows(RuntimeException.class, () -> club.darDeBajaEquipo("Futbol", "Seneca FC"),
				"Seneca FC esta en una competencia en curso");
		assertDoesNotThrow(() -> club.darDeBajaEquipo("Voleibol", "Aguilas"));
		assertFalse(club.buscarEquipo("Voleibol", "Aguilas").estaInscrito(club.buscarSocio("carolina")),
				"sus jugadores quedan libres");
		assertThrows(RuntimeException.class, () -> club.darDeBajaEquipo("Voleibol", "Aguilas"), "darlo de baja otra vez");
	}

	@Test
	@DisplayName("RF-ADM-07: crear competencias")
	void crearCompetencia() {
		int antes = club.getCompetencias().size();
		assertDoesNotThrow(() -> club.crearCompetenciaCerros("Torneo Relampago", 2026, "Voleibol", Categoria.SUB17,
				List.of("Colegio San Bartolome", "Gimnasio Moderno")));
		assertDoesNotThrow(() -> club.crearCompetenciaAscun("ASCUN Voleibol", 2026, "Voleibol", Categoria.MAYORES,
				List.of("Universidad Nacional", "Universidad de los Andes", "Universidad del Valle")));
		assertThrows(RuntimeException.class, () -> club.crearCompetenciaCerros("Torneo Relampago", 2026, "Voleibol",
				Categoria.SUB17, List.of("Colegio San Bartolome")), "mismo nombre y temporada");
		assertDoesNotThrow(() -> club.crearCompetenciaCerros("Torneo Relampago", 2027, "Voleibol", Categoria.SUB17,
				List.of("Colegio San Bartolome")), "mismo nombre en otra temporada");
		assertThrows(RuntimeException.class, () -> club.crearCompetenciaCerros("Copa Norte", 2026, "Futbol", Categoria.MAYORES,
				List.of("Universidad Javeriana", "Universidad Javeriana")), "institucion repetida");
		assertThrows(RuntimeException.class, () -> club.crearCompetenciaCerros("Copa Tenis", 2026, "Tenis", Categoria.MAYORES,
				List.of("Universidad Javeriana")), "deporte individual");
		assertEquals(antes + 3, club.getCompetencias().size());
	}

	@Test
	@DisplayName("RF-ADM-06: crear y editar instalaciones")
	void crearYEditarInstalaciones() {
		assertDoesNotThrow(() -> club.crearInstalacionIndividual("TEN-4", "Cancha de tenis 4", "Tenis", 2, false));
		assertDoesNotThrow(() -> club.crearInstalacionConjunto("FUT-2", "Cancha de futbol 2", "Futbol", 22));
		assertThrows(RuntimeException.class, () -> club.crearInstalacionIndividual("TEN-1", "Otra", "Tenis", 2, false),
				"codigo que ya existe");
		assertThrows(RuntimeException.class, () -> club.crearInstalacionIndividual("PAD-3", "Padel 3", "Padel", 2, false),
				"el padel siempre es de 4");
		assertThrows(RuntimeException.class, () -> club.crearInstalacionIndividual("TEN-5", "Tenis 5", "Tenis", 3, false),
				"capacidad invalida para tenis");
		assertThrows(RuntimeException.class, () -> club.crearInstalacionConjunto("FUT-3", "Mini", "Futbol", 10),
				"no caben dos equipos");
		assertThrows(RuntimeException.class, () -> club.crearInstalacionIndividual("SQ-1", "Squash", "Squash", 2, false),
				"deporte inexistente");
		assertDoesNotThrow(() -> club.editarInstalacion("TEN-4", "Cancha de tenis 4 (renovada)", 4));
		assertThrows(RuntimeException.class, () -> club.editarInstalacion("TEN-4", "Cancha de tenis 4", 5), "capacidad 5");
	}

	@Test
	@DisplayName("RF-ADM-06: desactivar instalaciones")
	void desactivarInstalaciones() {
		club.crearInstalacionIndividual("TEN-4", "Cancha de tenis 4", "Tenis", 2, false);
		club.iniciarSesion("valentina", "1234");
		club.reservarInstalacion("Tenis", "TEN-4", Utilidades.LUNES.atTime(9, 0), 2, Modalidad.SENCILLOS);
		club.iniciarSesion("admin", "admin123");

		assertThrows(RuntimeException.class, () -> club.desactivarInstalacion("TEN-4"), "TEN-4 tiene una reserva futura");
		assertDoesNotThrow(() -> club.desactivarInstalacion("MESA-2"));
		assertThrows(RuntimeException.class, () -> club.desactivarInstalacion("MESA-2"), "desactivarla otra vez");
		club.iniciarSesion("sofia", "1234");
		assertThrows(RuntimeException.class, () -> club.reservarInstalacion("Tenis de mesa", "MESA-2",
				Utilidades.LUNES.atTime(9, 0), 2, Modalidad.SENCILLOS), "no se reserva una instalacion desactivada");
	}
}
