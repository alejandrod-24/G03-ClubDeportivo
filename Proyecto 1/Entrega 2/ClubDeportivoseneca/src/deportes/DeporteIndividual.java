package deportes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import instalaciones.Modalidad;
import partidos.PartidoPractica;

public class DeporteIndividual extends Deporte {

	//Atributos
	//Maximo de instalaciones de la disciplina en uso al mismo tiempo
	private final int maximoSimultaneas;
	private final List<Modalidad> modalidades;
	private final Academia academia;
	private final List<PartidoPractica> partidos;

	//Constructor
	public DeporteIndividual(String nombre, int maximoSimultaneas, List<Modalidad> modalidades) {
		super(nombre);
		if (maximoSimultaneas <= 0) {
			throw new IllegalArgumentException("El maximo de instalaciones simultaneas debe ser positivo");
		}
		if (modalidades == null || modalidades.isEmpty()) {
			throw new IllegalArgumentException("El deporte debe tener al menos una modalidad");
		}
		this.maximoSimultaneas = maximoSimultaneas;
		this.modalidades = new ArrayList<>(modalidades);
		this.academia = new Academia(this);
		this.partidos = new ArrayList<>();
	}

	public DeporteIndividual(String nombre) {
		this(nombre, 2, List.of(Modalidad.SENCILLOS, Modalidad.DOBLES));
	}

	//Metodos
	public int getMaximoSimultaneas() {
		return maximoSimultaneas;
	}

	public List<Modalidad> getModalidades() {
		return Collections.unmodifiableList(modalidades);
	}

	public boolean permiteModalidad(Modalidad modalidad) {
		return modalidades.contains(modalidad);
	}

	//Una instalacion de este deporte solo puede tener la capacidad de alguna modalidad (2 o 4)
	public boolean esCapacidadValida(int capacidad) {
		for (Modalidad m : modalidades) {
			if (m.getJugadores() == capacidad) {
				return true;
			}
		}
		return false;
	}

	public Academia getAcademia() {
		return academia;
	}

	public List<PartidoPractica> getPartidos() {
		return Collections.unmodifiableList(partidos);
	}

	public void agregarPartido(PartidoPractica partido) {
		partidos.add(partido);
	}
}
