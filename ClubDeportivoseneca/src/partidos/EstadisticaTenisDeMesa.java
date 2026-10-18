package partidos;

import java.util.List;

public class EstadisticaTenisDeMesa extends EstadisticaRaqueta {

	public static final String DEPORTE = "Tenis de mesa";

	//Atributos
	private final int puntosTotales;

	//Constructor
	public EstadisticaTenisDeMesa(String loginJugador, int setsGanados, int puntosTotales) {
		super(loginJugador, setsGanados);
		validarNoNegativo(puntosTotales, "Los puntos");
		//Cada set ganado exige al menos 11 puntos
		if (puntosTotales < 11 * setsGanados) {
			throw new IllegalArgumentException("Valores inconsistentes: " + setsGanados + " sets ganados requieren al menos "
					+ (11 * setsGanados) + " puntos");
		}
		this.puntosTotales = puntosTotales;
	}

	//Metodos
	public int getPuntosTotales() {
		return puntosTotales;
	}

	@Override
	public String getNombreDeporte() {
		return DEPORTE;
	}

	@Override
	public int getValoracion() {
		return setsGanados * 3 + puntosTotales / 11;
	}

	@Override
	public List<String> getValores() {
		return List.of("" + setsGanados, "" + puntosTotales);
	}

	@Override
	public String toString() {
		return loginJugador + ": " + setsGanados + " sets, " + puntosTotales + " puntos";
	}
}
