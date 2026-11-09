package partidos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import competencias.Competencia;
import competencias.Fase;
import competencias.InscripcionCompetencia;
import deportes.DeporteConjunto;
import deportes.Equipo;
import personas.Socio;

/**
 * Partido oficial de un equipo del club contra una institucion rival.
 * Marcador local = club, marcador visitante = rival.
 */
public class PartidoCompetencia extends Partido {

	//Atributos
	private final Competencia competencia;
	private final Equipo equipo;
	private final String institucionRival;
	private final String escenario;
	private final Fase fase;
	private List<Socio> alineacion;

	//Constructor (las reglas de programacion estan en Competencia.programarPartido)
	public PartidoCompetencia(String id, Competencia competencia, Equipo equipo, String institucionRival,
			String escenario, LocalDate fecha, Fase fase) {
		super(id, fecha);
		if (escenario == null || escenario.isBlank() || escenario.contains(";") || escenario.contains(",")) {
			throw new IllegalArgumentException("El escenario no puede estar vacio ni contener ';' o ','");
		}
		this.competencia = competencia;
		this.equipo = equipo;
		this.institucionRival = institucionRival;
		this.escenario = escenario;
		this.fase = fase;
		this.alineacion = new ArrayList<>();
	}

	//Getters
	@Override
	public DeporteConjunto getDeporte() {
		return competencia.getDeporte();
	}

	public Competencia getCompetencia() {
		return competencia;
	}

	public Equipo getEquipo() {
		return equipo;
	}

	public String getInstitucionRival() {
		return institucionRival;
	}

	public String getEscenario() {
		return escenario;
	}

	public Fase getFase() {
		return fase;
	}

	@Override
	public List<Socio> getAlineados() {
		return new ArrayList<>(alineacion);
	}

	@Override
	public String getDescripcion() {
		return equipo.getNombre() + " vs " + institucionRival + " (" + fase + ", " + escenario + ")";
	}

	//"Gana X", "Empate" o "-" si no se ha jugado
	public String getGanador() {
		if (estado != EstadoPartido.JUGADO) {
			return "-";
		}
		if (marcadorLocal == marcadorVisitante) {
			return "Empate";
		}
		return marcadorLocal > marcadorVisitante ? equipo.getNombre() : institucionRival;
	}

	//Metodos
	public void registrarResultado(List<Socio> alineacion, int marcadorClub, int marcadorRival,
			List<EstadisticaPartido> estadisticasJugadores) {
		validarPuedeRegistrarse();
		InscripcionCompetencia inscripcion = competencia.buscarInscripcion(equipo);
		if (inscripcion == null) {
			throw new IllegalStateException(equipo.getNombre() + " no esta inscrito en " + competencia.getNombre());
		}
		DeporteConjunto deporte = getDeporte();
		if (alineacion.size() < deporte.getMinimoJugadores()) {
			throw new IllegalArgumentException("Se necesitan al menos " + deporte.getMinimoJugadores() + " jugadores");
		}
		validarSinRepetidos(alineacion);
		for (Socio socio : alineacion) {
			if (!inscripcion.estaEnNomina(socio)) {
				throw new IllegalArgumentException(socio.getLogin() + " no esta en la nomina de la competencia");
			}
			if (competencia.estaSuspendido(socio, equipo, fecha)) {
				throw new IllegalArgumentException(socio.getLogin() + " esta suspendido para este partido");
			}
		}
		validarMarcador(marcadorClub, marcadorRival, deporte.permiteEmpate());
		if (marcadorClub == marcadorRival && fase.esEliminatoria()) {
			throw new IllegalArgumentException("No puede haber empate en fase eliminatoria");
		}
		//Se valida todo antes de modificar el partido
		this.alineacion = new ArrayList<>(alineacion);
		for (EstadisticaPartido e : estadisticasJugadores) {
			try {
				validarEstadistica(e);
			} catch (RuntimeException error) {
				this.alineacion = new ArrayList<>();
				throw error;
			}
		}
		this.marcadorLocal = marcadorClub;
		this.marcadorVisitante = marcadorRival;
		this.estado = EstadoPartido.JUGADO;
		for (EstadisticaPartido e : estadisticasJugadores) {
			estadisticas.put(e.getLoginJugador(), e);
		}
	}

	//Para la persistencia
	public void restaurarAlineacion(List<Socio> alineacion) {
		this.alineacion = new ArrayList<>(alineacion);
	}
}
