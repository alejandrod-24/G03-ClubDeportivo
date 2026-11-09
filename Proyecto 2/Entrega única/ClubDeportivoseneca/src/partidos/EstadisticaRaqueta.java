package partidos;

/**
 * Estadistica de un partido de practica de un deporte individual.
 * Los sets ganados deben coincidir con el marcador del lado del jugador.
 */
public abstract class EstadisticaRaqueta extends EstadisticaPartido {

	//Atributos
	protected final int setsGanados;

	//Constructor
	protected EstadisticaRaqueta(String loginJugador, int setsGanados) {
		super(loginJugador);
		validarNoNegativo(setsGanados, "Los sets ganados");
		this.setsGanados = setsGanados;
	}

	//Metodos
	public int getSetsGanados() {
		return setsGanados;
	}

	@Override
	public int getAnotaciones() {
		return setsGanados;
	}
}
