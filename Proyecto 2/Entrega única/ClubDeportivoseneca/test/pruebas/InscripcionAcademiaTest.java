package pruebas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import deportes.Nivel;
import global.Club;
import instalaciones.Modalidad;

/**
 * 4. Inscripcion y retiro de un socio en una academia individual, con nivel y ELO inicial.
 * RF-SOC-04, 05 (y la parte de academias de RF-SOC-01)
 */
@DisplayName("4. Inscripcion y retiro de academias")
class InscripcionAcademiaTest {

	private static final LocalDate LUNES = Utilidades.LUNES;

	private Club club;

	@BeforeEach
	void setUp() {
		club = Utilidades.clubDePrueba();
		club.iniciarSesion("isabel", "1234");
	}

	@Test
	@DisplayName("RF-SOC-04: inscribirse en academias")
	void inscribirseEnAcademia() {
		assertNotNull(assertDoesNotThrow(() -> club.inscribirseEnAcademia("Tenis", Nivel.INTERMEDIO, 1500)));
		assertThrows(RuntimeException.class, () -> club.inscribirseEnAcademia("Tenis", Nivel.AVANZADO, 1600),
				"inscribirse otra vez en tenis");
		assertDoesNotThrow(() -> club.inscribirseEnAcademia("Padel", Nivel.PRINCIPIANTE, 1200), "otra academia");
	}

	@Test
	@DisplayName("RF-SOC-04: rechazos al inscribirse")
	void rechazosAlInscribirse() {
		assertThrows(RuntimeException.class, () -> club.inscribirseEnAcademia("Futbol", Nivel.INTERMEDIO, 1500),
				"futbol no tiene academia");
		assertThrows(RuntimeException.class, () -> club.inscribirseEnAcademia("Squash", Nivel.INTERMEDIO, 1500),
				"deporte inexistente");
		assertThrows(RuntimeException.class, () -> club.inscribirseEnAcademia("Padel", Nivel.INTERMEDIO, 5000),
				"ELO inicial fuera de rango");
		assertThrows(RuntimeException.class, () -> club.inscribirseEnAcademia("Padel", null, 1500), "sin nivel");
		club.iniciarSesion("admin", "admin123");
		assertThrows(RuntimeException.class, () -> club.inscribirseEnAcademia("Tenis", Nivel.INTERMEDIO, 1500),
				"el administrador no puede inscribirse");
	}

	@Test
	@DisplayName("RF-SOC-05: al retirarse se cancelan sus reservas y practicas futuras")
	void retirarseDeAcademia() {
		club.inscribirseEnAcademia("Tenis", Nivel.INTERMEDIO, 1500);
		String reserva = club.reservarInstalacion("Tenis", "TEN-1", LUNES.atTime(9, 0), 2, Modalidad.SENCILLOS).getId();
		String practica = club.programarPartidoPractica("Tenis", "TEN-2", LUNES.atTime(10, 0), List.of("valentina")).getId();

		assertDoesNotThrow(() -> club.retirarseDeAcademia("Tenis"));
		assertTrue(club.buscarReserva(reserva).estaCancelada(), "su reserva futura quedo cancelada");
		assertEquals("cancelado", club.buscarPartido(practica).getResultado(), "su practica futura quedo cancelada");
		assertThrows(RuntimeException.class, () -> club.retirarseDeAcademia("Tenis"), "retirarse otra vez");
		assertThrows(RuntimeException.class, () -> club.retirarseDeAcademia("Tenis de mesa"),
				"una academia en la que nunca estuvo");
		assertThrows(RuntimeException.class,
				() -> club.reservarInstalacion("Tenis", "TEN-1", LUNES.atTime(11, 0), 2, Modalidad.SENCILLOS),
				"ya no puede reservar tenis");
	}

	@Test
	@DisplayName("RF-SOC-04: al volver a inscribirse se conserva el historial del ranking")
	void volverAInscribirse() {
		club.inscribirseEnAcademia("Tenis", Nivel.INTERMEDIO, 1500);
		club.retirarseDeAcademia("Tenis");
		assertDoesNotThrow(() -> club.inscribirseEnAcademia("Tenis", Nivel.AVANZADO, 1550));
		assertEquals(2, club.buscarDeporteIndividual("Tenis").getAcademia().buscarInscripcion(club.buscarSocio("isabel"))
				.getRanking().getHistorial().size(), "el historial tiene la inscripcion y la reinscripcion");
	}

	@Test
	@DisplayName("las academias sobreviven a guardar y cargar")
	void persistencia(@TempDir Path carpeta) {
		club.inscribirseEnAcademia("Tenis", Nivel.INTERMEDIO, 1500);
		club.inscribirseEnAcademia("Padel", Nivel.PRINCIPIANTE, 1200);
		club.retirarseDeAcademia("Tenis");
		club.inscribirseEnAcademia("Tenis", Nivel.AVANZADO, 1550);

		Club recargado = Utilidades.guardarYRecargar(club, carpeta);
		recargado.iniciarSesion("isabel", "1234");
		assertEquals(club.consultarPartidosYRanking(), recargado.consultarPartidosYRanking());
	}
}
