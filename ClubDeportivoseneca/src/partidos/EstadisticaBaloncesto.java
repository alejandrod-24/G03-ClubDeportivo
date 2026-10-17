package partidos;

import java.util.List;

public class EstadisticaBaloncesto extends EstadisticaPartido {

	public static final String DEPORTE = "Baloncesto";

	//Atributos
	private final int puntos;
	private final int rebotes;

	//Constructor
	public EstadisticaBaloncesto(String loginJugador, int puntos, int rebotes) {
		super(loginJugador);
		validarNoNegativo(puntos, "Los puntos");
		validarNoNegativo(rebotes, "Los rebotes");
		this.puntos = puntos;
		this.rebotes = rebotes;
	}

	//Metodos
	public int getPuntos() {
		return puntos;
	}

	public int getRebotes() {
		return rebotes;
	}

	@Override
	public String getNombreDeporte() {
		return DEPORTE;
	}

	@Override
	public int getAnotaciones() {
		return puntos;
	}

	@Override
	public int getValoracion() {
		return puntos + rebotes;
	}

	@Override
	public List<String> getValores() {
		return List.of("" + puntos, "" + rebotes);
	}

	@Override
	public String toString() {
		return loginJugador + ": " + puntos + " puntos, " + rebotes + " rebotes";
	}
}
