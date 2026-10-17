package instalaciones;

import deportes.DeporteIndividual;

public class InstalacionIndividual extends Instalacion{
	
	//Atributos
	private final boolean techada;

	//Constructor
	public InstalacionIndividual(String nombre, DeporteIndividual deporte, int capacidad, boolean techada) {
		super(nombre, deporte, capacidad);
		this.techada = techada;
	}
	
	//Metodos
	public boolean estaTechada() {
		return techada;
	}
	
}
