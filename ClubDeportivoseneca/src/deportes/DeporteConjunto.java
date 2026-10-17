package deportes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import global.Reloj;
import partidos.EstadoPartido;
import partidos.PartidoInterno;
import personas.Socio;

public class DeporteConjunto extends Deporte {

	//Atributos
	private final int minimoJugadores;
	private final boolean permiteEmpate;
	private final List<String> posiciones;
	private final ArrayList<Equipo> equipos;
	private final ArrayList<PartidoInterno> partidos;

	//Constructor
	public DeporteConjunto(String nombre, int minimoJugadores, boolean permiteEmpate, List<String> posiciones) {
		super(nombre);
		if (minimoJugadores <= 0) {
			throw new IllegalArgumentException("El minimo de jugadores debe ser positivo");
		}
		if (posiciones == null || posiciones.isEmpty()) {
			throw new IllegalArgumentException("El deporte debe tener posiciones");
		}
		this.minimoJugadores = minimoJugadores;
		this.permiteEmpate = permiteEmpate;
		this.posiciones = new ArrayList<>(posiciones);
		this.equipos = new ArrayList<>();
		this.partidos = new ArrayList<>();
	}

	//Getters
	public int getMinimoJugadores() {
		return minimoJugadores;
	}

	public boolean permiteEmpate() {
		return permiteEmpate;
	}

	public List<String> getPosiciones() {
		return Collections.unmodifiableList(posiciones);
	}

	public List<Equipo> getEquipos() {
		return Collections.unmodifiableList(equipos);
	}

	public List<PartidoInterno> getPartidos() {
		return Collections.unmodifiableList(partidos);
	}

	//Metodos

	public Equipo crearEquipo(String nombre, Categoria categoria, int cupoMaximo) {
		if (buscarEquipo(nombre) != null) {
			throw new IllegalArgumentException("Ya existe un equipo de " + this.nombre + " llamado " + nombre);
		}
		Equipo equipo = new Equipo(this, nombre, categoria, cupoMaximo);
		equipos.add(equipo);
		return equipo;
	}

	public Equipo buscarEquipo(String nombre) {
		for (Equipo equipo : equipos) {
			if (equipo.getNombre().equalsIgnoreCase(nombre)) {
				return equipo;
			}
		}
		return null;
	}

	public boolean tienePosicion(String posicion) {
		for (String p : posiciones) {
			if (p.equalsIgnoreCase(posicion)) {
				return true;
			}
		}
		return false;
	}

	//Un socio pertenece a un unico equipo por deporte
	public Equipo getEquipoDe(Socio socio) {
		for (Equipo equipo : equipos) {
			if (equipo.estaInscrito(socio)) {
				return equipo;
			}
		}
		return null;
	}

	public PartidoInterno programarPartido(String id, Equipo local, Equipo visitante, LocalDate fecha) {
		if (local == visitante) {
			throw new IllegalArgumentException("Un equipo no puede jugar contra si mismo");
		}
		if (local.getDeporte() != this || visitante.getDeporte() != this) {
			throw new IllegalArgumentException("Los equipos deben ser de " + nombre);
		}
		if (local.getCategoria() != visitante.getCategoria()) {
			throw new IllegalArgumentException("Los equipos deben ser de la misma categoria");
		}
		if (!local.estaActivo() || !visitante.estaActivo()) {
			throw new IllegalStateException("Ambos equipos deben estar activos");
		}
		if (fecha == null || fecha.isBefore(Reloj.hoy())) {
			throw new IllegalArgumentException("La fecha del partido no puede ser pasada");
		}
		for (Equipo equipo : List.of(local, visitante)) {
			if (equipo.getJugadores().size() < minimoJugadores) {
				throw new IllegalStateException(equipo.getNombre() + " no tiene el minimo de "
						+ minimoJugadores + " jugadores");
			}
		}
		PartidoInterno partido = new PartidoInterno(id, this, local, visitante, fecha);
		partidos.add(partido);
		return partido;
	}

	//Usado por la persistencia (no valida fechas pasadas)
	public void agregarPartidoGuardado(PartidoInterno partido) {
		partidos.add(partido);
	}

	public List<PartidoInterno> getPartidosDe(Equipo equipo) {
		List<PartidoInterno> resultado = new ArrayList<>();
		for (PartidoInterno p : partidos) {
			if (p.getLocal() == equipo || p.getVisitante() == equipo) {
				resultado.add(p);
			}
		}
		return resultado;
	}

	public boolean tienePartidosProgramados(Equipo equipo) {
		for (PartidoInterno p : getPartidosDe(equipo)) {
			if (p.getEstado() == EstadoPartido.PROGRAMADO) {
				return true;
			}
		}
		return false;
	}
}
