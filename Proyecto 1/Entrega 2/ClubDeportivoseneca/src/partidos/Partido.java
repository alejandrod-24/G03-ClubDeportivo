package partidos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import deportes.Deporte;
import global.Reloj;
import personas.Socio;

public abstract class Partido {

	//Atributos
	protected final String id;
	protected final LocalDate fecha;
	protected EstadoPartido estado;
	protected int marcadorLocal;
	protected int marcadorVisitante;
	//Clase de asociacion Partido-Socio: una estadistica por jugador (clave: login)
	protected final Map<String, EstadisticaPartido> estadisticas;

	//Constructor
	protected Partido(String id, LocalDate fecha) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("El partido debe tener id");
		}
		if (fecha == null) {
			throw new IllegalArgumentException("El partido debe tener fecha");
		}
		this.id = id;
		this.fecha = fecha;
		this.estado = EstadoPartido.PROGRAMADO;
		this.estadisticas = new LinkedHashMap<>();
	}

	//Getters
	public String getId() {
		return id;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public EstadoPartido getEstado() {
		return estado;
	}

	public int getMarcadorLocal() {
		return marcadorLocal;
	}

	public int getMarcadorVisitante() {
		return marcadorVisitante;
	}

	public abstract Deporte getDeporte();

	public abstract List<Socio> getAlineados();

	//Texto con los contrincantes, ej. "Seneca FC vs Seneca United"
	public abstract String getDescripcion();

	public Collection<EstadisticaPartido> getEstadisticas() {
		return Collections.unmodifiableCollection(estadisticas.values());
	}

	public EstadisticaPartido getEstadistica(String login) {
		return estadisticas.get(login);
	}

	//Metodos
	public boolean estaAlineado(String login) {
		for (Socio socio : getAlineados()) {
			if (socio.getLogin().equals(login)) {
				return true;
			}
		}
		return false;
	}

	public void registrarEstadistica(EstadisticaPartido estadistica) {
		if (estado != EstadoPartido.JUGADO) {
			throw new IllegalStateException("Primero debe registrarse el resultado del partido " + id);
		}
		validarEstadistica(estadistica);
		estadisticas.put(estadistica.getLoginJugador(), estadistica);
	}

	protected void validarEstadistica(EstadisticaPartido estadistica) {
		if (!estadistica.getNombreDeporte().equalsIgnoreCase(getDeporte().getNombre())) {
			throw new IllegalArgumentException("La estadistica es de " + estadistica.getNombreDeporte()
					+ " pero el partido es de " + getDeporte().getNombre());
		}
		if (!estaAlineado(estadistica.getLoginJugador())) {
			throw new IllegalArgumentException(estadistica.getLoginJugador() + " no fue alineado en el partido " + id);
		}
	}

	public void declararNoJugado() {
		if (estado == EstadoPartido.JUGADO) {
			throw new IllegalStateException("El partido " + id + " ya fue jugado");
		}
		if (estado == EstadoPartido.NO_JUGADO) {
			throw new IllegalStateException("El partido " + id + " ya fue declarado no jugado");
		}
		if (estado == EstadoPartido.CANCELADO) {
			throw new IllegalStateException("El partido " + id + " fue cancelado");
		}
		estado = EstadoPartido.NO_JUGADO;
	}

	protected void validarPuedeRegistrarse() {
		if (estado == EstadoPartido.JUGADO) {
			throw new IllegalStateException("El partido " + id + " ya fue jugado");
		}
		if (estado != EstadoPartido.PROGRAMADO) {
			throw new IllegalStateException("El partido " + id + " no esta programado (" + estado + ")");
		}
		if (fecha.isAfter(Reloj.hoy())) {
			throw new IllegalStateException("El partido " + id + " es el " + fecha + ", aun no se ha jugado");
		}
	}

	protected static void validarMarcador(int local, int visitante, boolean permiteEmpate) {
		if (local < 0 || visitante < 0) {
			throw new IllegalArgumentException("El marcador no puede ser negativo");
		}
		if (local == visitante && !permiteEmpate) {
			throw new IllegalArgumentException("Este deporte no admite empates");
		}
	}

	//Revisa que no haya jugadores repetidos en una lista de alineados
	protected static void validarSinRepetidos(List<Socio> jugadores) {
		List<Socio> vistos = new ArrayList<>();
		for (Socio socio : jugadores) {
			if (vistos.contains(socio)) {
				throw new IllegalArgumentException(socio.getLogin() + " esta repetido en la alineacion");
			}
			vistos.add(socio);
		}
	}

	//Para la persistencia
	public void restaurarEstado(EstadoPartido estado, int marcadorLocal, int marcadorVisitante) {
		this.estado = estado;
		this.marcadorLocal = marcadorLocal;
		this.marcadorVisitante = marcadorVisitante;
	}

	public void restaurarEstadistica(EstadisticaPartido estadistica) {
		estadisticas.put(estadistica.getLoginJugador(), estadistica);
	}

	// -------------------- Suspensiones (se calculan, no se guardan) --------------------

	//Ultimo partido jugado de la lista con fecha anterior a la indicada
	public static Partido ultimoJugadoAntesDe(List<? extends Partido> partidos, LocalDate fecha) {
		Partido ultimo = null;
		for (Partido p : partidos) {
			if (p.estado == EstadoPartido.JUGADO && p.fecha.isBefore(fecha)
					&& (ultimo == null || p.fecha.isAfter(ultimo.fecha))) {
				ultimo = p;
			}
		}
		return ultimo;
	}

	//El jugador recibio 2 amarillas o una roja en ese partido
	public static boolean fueExpulsadoEn(Partido partido, Socio socio) {
		if (partido == null) {
			return false;
		}
		EstadisticaPartido e = partido.getEstadistica(socio.getLogin());
		return e != null && e.generaSuspension();
	}

	public String getResultado() {
		switch (estado) {
		case JUGADO:
			return marcadorLocal + " - " + marcadorVisitante;
		case NO_JUGADO:
			return "no jugado";
		case CANCELADO:
			return "cancelado";
		default:
			return "programado";
		}
	}

	@Override
	public String toString() {
		return "[" + id + "] " + fecha + " " + getDeporte().getNombre() + ": " + getDescripcion() + " -> " + getResultado();
	}
}
