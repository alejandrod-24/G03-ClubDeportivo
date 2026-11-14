package pruebas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import competencias.Fase;
import deportes.Categoria;
import deportes.Equipo;
import global.Club;
import global.DatosIniciales;
import global.Reloj;
import partidos.EstadisticaFutbol;

/**
 * 2. Consulta de competencias, catalogos y equipos (cualquier usuario autenticado).
 * RF-GEN-05, 06, 07
 */
@DisplayName("2. Consultas generales")
class ConsultasGeneralesTest {

	private Club club;

	@BeforeEach
	void setUp() {
		club = Utilidades.clubDePrueba();
		prepararDatos();
	}

	//Un partido interno jugado, uno programado y un partido de liga jugado
	private void prepararDatos() {
		List<String> fc = List.of("ana", "luis", "camila", "jorge", "maria", "andres", "juliana", "felipe", "daniela");
		List<String> united = List.of("paula", "sebastian", "natalia", "ricardo", "carolina", "miguel", "isabel", "nicolas");
		club.iniciarSesion("carlos", "carlos123");
		String interno = club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", Reloj.hoy().plusDays(1)).getId();
		club.inscribirEquipoEnCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Futbol", "Seneca FC");
		club.definirNomina(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Seneca FC", fc);
		String liga = club.programarPartidoCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA, "Seneca FC",
				"Universidad Externado", "Cancha Seneca", Reloj.hoy().plusDays(1), Fase.LIGA).getId();
		Reloj.avanzarDias(1);
		club.registrarResultadoInterno(interno, fc, united, 0, 2);
		club.registrarPartidoCompetencia(liga, fc, 2, 0, List.of(new EstadisticaFutbol("daniela", 2, 0, 0, false)));
		club.programarPartidoInterno("Futbol", "Seneca FC", "Seneca United", Reloj.hoy().plusDays(7));
		club.cerrarSesion();
	}

	@Test
	@DisplayName("las consultas exigen sesion iniciada")
	void consultarSinSesion() {
		assertThrows(RuntimeException.class, () -> club.consultarEquipos(null, null));
		assertThrows(RuntimeException.class, () -> club.consultarCatalogoTienda());
	}

	@Test
	@DisplayName("RF-GEN-07: consultar equipos con y sin filtros")
	void consultarEquipos() {
		club.iniciarSesion("valentina", "1234");
		assertFalse(club.consultarEquipos(null, null).isEmpty(), "todos los equipos");
		List<Equipo> mayores = club.consultarEquipos("Futbol", Categoria.MAYORES);
		assertFalse(mayores.isEmpty(), "hay equipos de futbol MAYORES");
		for (Equipo e : mayores) {
			assertTrue(e.getCategoria() == Categoria.MAYORES, "el filtro por categoria se respeta: " + e);
		}
		assertThrows(RuntimeException.class, () -> club.consultarEquipos("Rugby", null), "deporte inexistente");
	}

	@Test
	@DisplayName("RF-GEN-07: calendario y resultados de un equipo")
	void consultarCalendarioEquipo() {
		club.iniciarSesion("valentina", "1234");
		assertTrue(club.consultarCalendarioEquipo("Futbol", "Seneca FC").size() >= 3,
				"Seneca FC tiene el interno jugado, el de liga y el interno programado");
		assertThrows(RuntimeException.class, () -> club.consultarCalendarioEquipo("Futbol", "Galacticos"));
	}

	@Test
	@DisplayName("RF-GEN-05: consultar competencias")
	void consultarCompetencias() {
		club.iniciarSesion("valentina", "1234");
		assertFalse(club.getCompetencias().isEmpty());
		assertNotNull(club.consultarCompetencia(DatosIniciales.LIGA_CERROS, DatosIniciales.TEMPORADA).generarResumen());
		assertThrows(RuntimeException.class, () -> club.consultarCompetencia("Copa America", 2026), "competencia inexistente");
	}

	@Test
	@DisplayName("RF-GEN-06: consultar catalogos y productos")
	void consultarCatalogos() {
		club.iniciarSesion("valentina", "1234");
		assertFalse(club.consultarCatalogoTienda().isEmpty(), "catalogo de la tienda");
		assertFalse(club.consultarCatalogoCafeteria().isEmpty(), "catalogo de la cafeteria");
		assertNotNull(assertDoesNotThrow(() -> club.consultarProducto("Galletas de mani")));
		assertThrows(RuntimeException.class, () -> club.consultarProducto("Pizza"), "producto inexistente");
	}
}
