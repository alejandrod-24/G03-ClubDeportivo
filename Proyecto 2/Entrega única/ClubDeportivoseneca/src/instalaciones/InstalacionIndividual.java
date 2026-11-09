package instalaciones;

import deportes.DeporteIndividual;

public class InstalacionIndividual extends Instalacion{

	//Atributos
	private final boolean techada;

	//Constructor
	public InstalacionIndividual(String codigo, String nombre, DeporteIndividual deporte, int capacidad, boolean techada) {
		super(codigo, nombre, deporte, capacidad);
		this.techada = techada;
	}

	//Metodos
	public boolean estaTechada() {
		return techada;
	}

	@Override
	public DeporteIndividual getDeporte() {
		return (DeporteIndividual) deporte;
	}

	//Tenis y tenis de mesa: 2 o 4 jugadores; padel siempre 4
	@Override
	protected boolean esCapacidadValida(int capacidad) {
		return ((DeporteIndividual) deporte).esCapacidadValida(capacidad);
	}

	//Supuesto: la restriccion de bebida caliente aplica a canchas de padel y mesas de tenis de mesa techadas
	public boolean restringeBebidaCaliente() {
		String d = deporte.getNombre();
		return techada && (d.equalsIgnoreCase("Padel") || d.equalsIgnoreCase("Tenis de mesa"));
	}

	@Override
	public String toString() {
		return super.toString() + (techada ? " techada" : "");
	}
}
