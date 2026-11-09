package partidos;

import java.util.List;

/**
 * Todo lo que hizo UN jugador en UN partido. Cada deporte define sus campos.
 * Agregar un deporte = agregar una subclase.
 */
public abstract class EstadisticaPartido {

	//Atributos
	protected final String loginJugador;

	//Constructor
	protected EstadisticaPartido(String loginJugador) {
		if (loginJugador == null || loginJugador.isBlank()) {
			throw new IllegalArgumentException("La estadistica debe indicar el jugador");
		}
		this.loginJugador = loginJugador;
	}

	//Metodos
	public String getLoginJugador() {
		return loginJugador;
	}

	//Nombre del deporte al que pertenece (debe coincidir con Deporte.getNombre())
	public abstract String getNombreDeporte();

	//Goles, puntos o aces segun el deporte (para el goleador)
	public abstract int getAnotaciones();

	//Puntaje simple para elegir al jugador destacado
	public abstract int getValoracion();

	//Valores en orden, para la persistencia
	public abstract List<String> getValores();

	public int getTarjetasAmarillas() {
		return 0;
	}

	public boolean tieneTarjetaRoja() {
		return false;
	}

	//Dos amarillas o una roja en el partido -> fuera el siguiente partido del equipo
	public boolean generaSuspension() {
		return getTarjetasAmarillas() >= 2 || tieneTarjetaRoja();
	}

	protected static void validarNoNegativo(int valor, String campo) {
		if (valor < 0) {
			throw new IllegalArgumentException(campo + " no puede ser negativo");
		}
	}
}
