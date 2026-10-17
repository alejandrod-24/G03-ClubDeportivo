package partidos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import deportes.DeporteConjunto;
import deportes.Equipo;
import personas.Socio;

public class PartidoInterno extends Partido {

	//Atributos
	private final DeporteConjunto deporte;
	private final Equipo local;
	private final Equipo visitante;
	private List<Socio> alineacionLocal;
	private List<Socio> alineacionVisitante;

	//Constructor (las reglas de programacion estan en DeporteConjunto.programarPartido)
	public PartidoInterno(String id, DeporteConjunto deporte, Equipo local, Equipo visitante, LocalDate fecha) {
		super(id, fecha);
		this.deporte = deporte;
		this.local = local;
		this.visitante = visitante;
		this.alineacionLocal = new ArrayList<>();
		this.alineacionVisitante = new ArrayList<>();
	}

	//Getters
	@Override
	public DeporteConjunto getDeporte() {
		return deporte;
	}

	public Equipo getLocal() {
		return local;
	}

	public Equipo getVisitante() {
		return visitante;
	}

	public List<Socio> getAlineacionLocal() {
		return new ArrayList<>(alineacionLocal);
	}

	public List<Socio> getAlineacionVisitante() {
		return new ArrayList<>(alineacionVisitante);
	}

	@Override
	public List<Socio> getAlineados() {
		List<Socio> todos = new ArrayList<>(alineacionLocal);
		todos.addAll(alineacionVisitante);
		return todos;
	}

	@Override
	public String getDescripcion() {
		return local.getNombre() + " vs " + visitante.getNombre();
	}

	//Metodos

	//Registra alineacion y marcador. Las suspensiones se "consumen" porque
	//solo cuentan para el siguiente partido jugado del equipo.
	public void registrarResultado(List<Socio> alineacionLocal, List<Socio> alineacionVisitante,
			int marcadorLocal, int marcadorVisitante) {
		validarPuedeRegistrarse();
		validarAlineacion(alineacionLocal, local);
		validarAlineacion(alineacionVisitante, visitante);
		validarMarcador(marcadorLocal, marcadorVisitante, deporte.permiteEmpate());
		this.alineacionLocal = new ArrayList<>(alineacionLocal);
		this.alineacionVisitante = new ArrayList<>(alineacionVisitante);
		this.marcadorLocal = marcadorLocal;
		this.marcadorVisitante = marcadorVisitante;
		this.estado = EstadoPartido.JUGADO;
	}

	private void validarAlineacion(List<Socio> alineacion, Equipo equipo) {
		if (alineacion.size() < deporte.getMinimoJugadores()) {
			throw new IllegalArgumentException(equipo.getNombre() + " necesita al menos "
					+ deporte.getMinimoJugadores() + " jugadores habilitados");
		}
		validarSinRepetidos(alineacion);
		for (Socio socio : alineacion) {
			if (!equipo.estaInscrito(socio)) {
				throw new IllegalArgumentException(socio.getLogin() + " no pertenece a " + equipo.getNombre());
			}
			if (!socio.estaActivo()) {
				throw new IllegalArgumentException(socio.getLogin() + " no esta habilitado (dado de baja)");
			}
			if (estaSuspendido(socio, equipo)) {
				throw new IllegalArgumentException(socio.getLogin() + " esta suspendido para este partido");
			}
		}
	}

	public boolean estaSuspendido(Socio socio, Equipo equipo) {
		Partido anterior = ultimoJugadoAntesDe(deporte.getPartidosDe(equipo), fecha);
		return fueExpulsadoEn(anterior, socio);
	}

	//Para la persistencia
	public void restaurarAlineaciones(List<Socio> alineacionLocal, List<Socio> alineacionVisitante) {
		this.alineacionLocal = new ArrayList<>(alineacionLocal);
		this.alineacionVisitante = new ArrayList<>(alineacionVisitante);
	}
}
