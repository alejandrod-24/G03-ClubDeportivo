package partidos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import deportes.DeporteIndividual;
import global.Horario;
import global.Reloj;
import instalaciones.Actividad;
import instalaciones.InstalacionIndividual;
import instalaciones.Modalidad;
import personas.Persona;
import personas.Socio;

/**
 * Partido de practica de una academia, en una instalacion y un bloque de una hora.
 * Participan 2 (sencillos) o 4 (dobles) personas: socios inscritos y, opcionalmente, un entrenador.
 * La primera mitad de la lista es el lado A (marcador local) y la segunda el lado B.
 * El marcador es en sets.
 */
public class PartidoPractica extends Partido implements Actividad {

	//Atributos
	private final DeporteIndividual deporte;
	private final InstalacionIndividual instalacion;
	private final LocalDateTime inicio;
	private final Socio programadoPor;
	private final List<Persona> participantes;

	//Constructor (las reglas del club se validan en Club.programarPartidoPractica)
	public PartidoPractica(String id, DeporteIndividual deporte, InstalacionIndividual instalacion, LocalDateTime inicio,
			Socio programadoPor, List<Persona> participantes) {
		super(id, inicio.toLocalDate());
		Modalidad.deJugadores(participantes.size());
		this.deporte = deporte;
		this.instalacion = instalacion;
		this.inicio = inicio;
		this.programadoPor = programadoPor;
		this.participantes = new ArrayList<>(participantes);
	}

	//Getters
	@Override
	public DeporteIndividual getDeporte() {
		return deporte;
	}

	@Override
	public InstalacionIndividual getInstalacion() {
		return instalacion;
	}

	@Override
	public LocalDateTime getInicio() {
		return inicio;
	}

	@Override
	public LocalDateTime getFin() {
		return Horario.finDeBloque(inicio);
	}

	public Socio getProgramadoPor() {
		return programadoPor;
	}

	public List<Persona> getParticipantes() {
		return new ArrayList<>(participantes);
	}

	public List<Persona> getLadoA() {
		return new ArrayList<>(participantes.subList(0, participantes.size() / 2));
	}

	public List<Persona> getLadoB() {
		return new ArrayList<>(participantes.subList(participantes.size() / 2, participantes.size()));
	}

	public Modalidad getModalidad() {
		return Modalidad.deJugadores(participantes.size());
	}

	//Solo los socios (el entrenador no tiene ranking)
	@Override
	public List<Socio> getAlineados() {
		List<Socio> socios = new ArrayList<>();
		for (Persona p : participantes) {
			if (p instanceof Socio) {
				socios.add((Socio) p);
			}
		}
		return socios;
	}

	//Incluye al entrenador, para el historial filtrado por persona
	@Override
	public boolean estaAlineado(String login) {
		for (Persona p : participantes) {
			if (p.getLogin().equals(login)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getDescripcion() {
		return nombres(getLadoA()) + " vs " + nombres(getLadoB()) + " en " + instalacion.getNombre() + " a las " + inicio.toLocalTime();
	}

	private static String nombres(List<Persona> personas) {
		List<String> logins = new ArrayList<>();
		for (Persona p : personas) {
			logins.add(p.getLogin());
		}
		return String.join(" y ", logins);
	}

	public boolean estaEnLadoA(Persona persona) {
		return getLadoA().contains(persona);
	}

	// -------------------- Actividad --------------------

	//Ocupa la instalacion mientras no se cancele ni se declare no jugado
	@Override
	public boolean estaVigente() {
		return estado == EstadoPartido.PROGRAMADO || estado == EstadoPartido.JUGADO;
	}

	@Override
	public boolean involucra(Persona persona) {
		return participantes.contains(persona);
	}

	// -------------------- Metodos --------------------

	public void cancelar(Socio quien) {
		if (quien != programadoPor) {
			throw new IllegalStateException("El partido " + id + " no lo programo " + quien.getLogin());
		}
		if (estado == EstadoPartido.JUGADO) {
			throw new IllegalStateException("El partido " + id + " ya tiene resultado");
		}
		if (estado != EstadoPartido.PROGRAMADO) {
			throw new IllegalStateException("El partido " + id + " no esta programado (" + estado + ")");
		}
		if (!Reloj.ahora().isBefore(inicio)) {
			throw new IllegalStateException("El bloque del partido " + id + " ya inicio o paso");
		}
		estado = EstadoPartido.CANCELADO;
	}

	//Cancelacion automatica de partidos futuros (retiro de academia o baja del socio)
	public void cancelarPorSistema() {
		if (estado == EstadoPartido.PROGRAMADO && Reloj.ahora().isBefore(inicio)) {
			estado = EstadoPartido.CANCELADO;
		}
	}

	public boolean esFuturoProgramado() {
		return estado == EstadoPartido.PROGRAMADO && Reloj.ahora().isBefore(inicio);
	}

	//Marcador en sets (lado A - lado B) y estadisticas de cada socio
	public void registrarResultado(int setsA, int setsB, List<EstadisticaPartido> estadisticasJugadores) {
		if (estado == EstadoPartido.JUGADO) {
			throw new IllegalStateException("El partido " + id + " ya tiene resultado");
		}
		if (estado != EstadoPartido.PROGRAMADO) {
			throw new IllegalStateException("El partido " + id + " no esta programado (" + estado + ")");
		}
		if (Reloj.ahora().isBefore(inicio)) {
			throw new IllegalStateException("El bloque del partido " + id + " aun no ha comenzado (" + inicio + ")");
		}
		validarMarcador(setsA, setsB, false);
		this.marcadorLocal = setsA;
		this.marcadorVisitante = setsB;
		//Se valida todo antes de dejar el partido como jugado
		for (EstadisticaPartido e : estadisticasJugadores) {
			validarEstadistica(e);
		}
		this.estado = EstadoPartido.JUGADO;
		for (EstadisticaPartido e : estadisticasJugadores) {
			estadisticas.put(e.getLoginJugador(), e);
		}
	}

	@Override
	protected void validarEstadistica(EstadisticaPartido estadistica) {
		super.validarEstadistica(estadistica);
		EstadisticaRaqueta e = (EstadisticaRaqueta) estadistica;
		boolean ladoA = false;
		for (Persona p : getLadoA()) {
			ladoA = ladoA || p.getLogin().equals(e.getLoginJugador());
		}
		int setsDelLado = ladoA ? marcadorLocal : marcadorVisitante;
		if (e.getSetsGanados() != setsDelLado) {
			throw new IllegalArgumentException("Valores inconsistentes: " + e.getLoginJugador() + " registra "
					+ e.getSetsGanados() + " sets pero su lado gano " + setsDelLado);
		}
	}

	//Gano el lado de la persona (solo si el partido se jugo)
	public boolean gano(Persona persona) {
		boolean ganoA = marcadorLocal > marcadorVisitante;
		return estado == EstadoPartido.JUGADO && (estaEnLadoA(persona) == ganoA);
	}

	@Override
	public String toString() {
		return "[" + id + "] " + inicio.toLocalDate() + " " + deporte.getNombre() + " (practica, " + getModalidad() + "): "
				+ getDescripcion() + " -> " + getResultado();
	}
}
