package pruebas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import deportes.Categoria;
import deportes.InscripcionEquipo;
import global.Club;
import global.DatosIniciales;

/**
 * 3. Inscripcion y retiro de un socio en un equipo de conjunto, con casos de rechazo.
 * RF-SOC-01, 02, 03
 */
@DisplayName("3. Inscripcion y retiro de equipos")
class InscripcionEquiposTest {

	private Club club;

	@BeforeEach
	void setUp() {
		club = Utilidades.clubDePrueba();
		//Un equipo dado de baja
		club.iniciarSesion("admin", "admin123");
		club.crearEquipo("Futbol", "Veteranos", Categoria.MAYORES, 15);
		club.darDeBajaEquipo("Futbol", "Veteranos");
		club.iniciarSesion("diego", "1234");
	}

	private List<Integer> camisetas(String equipo) {
		List<Integer> numeros = new ArrayList<>();
		for (InscripcionEquipo i : club.buscarEquipo("Futbol", equipo).getInscripciones()) {
			numeros.add(i.getNumeroCamiseta());
		}
		return numeros;
	}

	@Test
	@DisplayName("RF-SOC-02: rechazos al inscribirse")
	void rechazosAlInscribirse() {
		assertThrows(RuntimeException.class, () -> club.inscribirseEnEquipo("Futbol", "Seneca FC", "Delantero", 10),
				"camiseta 10 ya usada");
		assertThrows(RuntimeException.class, () -> club.inscribirseEnEquipo("Futbol", "Seneca FC", "Arquero", 20),
				"posicion inexistente");
		assertThrows(RuntimeException.class, () -> club.inscribirseEnEquipo("Futbol", "Seneca Sub15", "Defensa", 20),
				"edad no corresponde a la categoria");
		assertThrows(RuntimeException.class, () -> club.inscribirseEnEquipo("Futbol", "Veteranos", "Defensa", 20),
				"equipo inactivo");
		assertThrows(RuntimeException.class, () -> club.inscribirseEnEquipo("Futbol", "Galacticos", "Defensa", 20),
				"equipo inexistente");
	}

	@Test
	@DisplayName("RF-SOC-02: inscribirse en un equipo por deporte")
	void inscribirseEnEquipo() {
		assertDoesNotThrow(() -> club.inscribirseEnEquipo("Futbol", "Seneca FC", "Delantero", 20));
		assertTrue(camisetas("Seneca FC").contains(20), "diego queda con la camiseta 20");
		assertThrows(RuntimeException.class, () -> club.inscribirseEnEquipo("Futbol", "Seneca United", "Delantero", 20),
				"ya esta en un equipo de futbol");
		assertDoesNotThrow(() -> club.inscribirseEnEquipo("Voleibol", "Aguilas", "Central", 7),
				"si puede inscribirse en otro deporte");
	}

	@Test
	@DisplayName("RF-SOC-02: rechaza la inscripcion si el cupo esta lleno")
	void cupoLleno() {
		club.iniciarSesion("martin", "1234");
		assertThrows(RuntimeException.class, () -> club.inscribirseEnEquipo("Futbol", "Seneca Sub15", "Defensa", 3));
	}

	@Test
	@DisplayName("RF-SOC-01: el perfil muestra el equipo del socio")
	void consultarPerfil() {
		club.inscribirseEnEquipo("Futbol", "Seneca FC", "Delantero", 20);
		assertTrue(club.consultarPerfil().contains("Seneca FC"));
	}

	@Test
	@DisplayName("RF-SOC-03: no se retira de un equipo donde no esta")
	void retirarseSinEstarInscrito() {
		assertThrows(RuntimeException.class, () -> club.retirarseDeEquipo("Futbol", "Seneca United"));
	}

	@Test
	@DisplayName("RF-SOC-03: el retiro se bloquea mientras este en una nomina de competencia en curso")
	void retirarseDeEquipo() {
		club.inscribirseEnEquipo("Futbol", "Seneca FC", "Delantero", 20);
		club.iniciarSesion("carlos", "carlos123");
		club.inscribirEquipoEnCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Futbol", "Seneca FC");
		club.definirNomina(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Seneca FC",
				List.of("ana", "luis", "camila", "jorge", "maria", "andres", "juliana", "diego"));
		club.iniciarSesion("diego", "1234");
		assertThrows(RuntimeException.class, () -> club.retirarseDeEquipo("Futbol", "Seneca FC"),
				"esta en la nomina de una competencia en curso");

		club.iniciarSesion("admin", "admin123");
		club.finalizarCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA);
		club.iniciarSesion("diego", "1234");
		assertDoesNotThrow(() -> club.retirarseDeEquipo("Futbol", "Seneca FC"), "con la competencia finalizada si se retira");
		assertFalse(camisetas("Seneca FC").contains(20), "la camiseta 20 queda libre");
	}
}
