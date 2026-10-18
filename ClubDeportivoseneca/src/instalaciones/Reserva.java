package instalaciones;

import java.time.LocalDateTime;

import global.Horario;
import global.Reloj;
import personas.Persona;
import personas.Socio;

/** Reserva de una instalacion individual por un bloque de una hora. */
public class Reserva implements Actividad {

	//Atributos
	private final String id;
	private final Socio socio;
	private final InstalacionIndividual instalacion;
	private final LocalDateTime inicio;
	private final int numeroJugadores;
	private final Modalidad modalidad;
	private boolean cancelada;

	//Constructor (las reglas del club se validan en Club.reservarInstalacion)
	public Reserva(String id, Socio socio, InstalacionIndividual instalacion, LocalDateTime inicio,
			int numeroJugadores, Modalidad modalidad) {
		if (modalidad == null) {
			throw new IllegalArgumentException("Debe indicar la modalidad");
		}
		if (numeroJugadores < 1 || numeroJugadores > modalidad.getJugadores()) {
			throw new IllegalArgumentException("En " + modalidad + " juegan entre 1 y " + modalidad.getJugadores() + " jugadores");
		}
		this.id = id;
		this.socio = socio;
		this.instalacion = instalacion;
		this.inicio = inicio;
		this.numeroJugadores = numeroJugadores;
		this.modalidad = modalidad;
		this.cancelada = false;
	}

	//Getters
	public String getId() {
		return id;
	}

	public Socio getSocio() {
		return socio;
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

	public int getNumeroJugadores() {
		return numeroJugadores;
	}

	public Modalidad getModalidad() {
		return modalidad;
	}

	public boolean estaCancelada() {
		return cancelada;
	}

	@Override
	public boolean estaVigente() {
		return !cancelada;
	}

	@Override
	public boolean involucra(Persona persona) {
		return socio == persona;
	}

	//Metodos
	public void cancelar(Socio quien) {
		if (quien != socio) {
			throw new IllegalStateException("La reserva " + id + " no le pertenece a " + quien.getLogin());
		}
		if (cancelada) {
			throw new IllegalStateException("La reserva " + id + " ya esta cancelada");
		}
		if (!Reloj.ahora().isBefore(inicio)) {
			throw new IllegalStateException("El bloque de la reserva " + id + " ya inicio o paso");
		}
		cancelada = true;
	}

	//Cancelacion automatica (retiro de academia o baja del socio)
	public void cancelarPorSistema() {
		cancelada = true;
	}

	//Para la persistencia
	public void restaurarCancelada(boolean cancelada) {
		this.cancelada = cancelada;
	}

	@Override
	public String toString() {
		return "[" + id + "] " + inicio.toLocalDate() + " " + inicio.toLocalTime() + " " + instalacion.getNombre()
				+ " (" + instalacion.getDeporte().getNombre() + ") - " + socio.getLogin() + ", " + numeroJugadores
				+ " jugadores, " + modalidad + (cancelada ? " [CANCELADA]" : "");
	}
}
