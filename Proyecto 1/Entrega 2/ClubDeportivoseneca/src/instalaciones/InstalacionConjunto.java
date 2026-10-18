package instalaciones;

import deportes.DeporteConjunto;

public class InstalacionConjunto extends Instalacion {

	//Constructor
	public InstalacionConjunto(String codigo, String nombre, DeporteConjunto deporte, int capacidad) {
		super(codigo, nombre, deporte, capacidad);
	}

	//Debe caber al menos un partido (dos equipos con el minimo de jugadores)
	@Override
	protected boolean esCapacidadValida(int capacidad) {
		return capacidad >= 2 * ((DeporteConjunto) deporte).getMinimoJugadores();
	}
}
