package partidos;

import java.util.List;

public class EstadisticaTenis extends EstadisticaRaqueta {

	public static final String DEPORTE = "Tenis";

	//Atributos
	private final int gamesGanados;
	private final int aces;
	private final int erroresNoForzados;

	//Constructor
	public EstadisticaTenis(String loginJugador, int setsGanados, int gamesGanados, int aces, int erroresNoForzados) {
		super(loginJugador, setsGanados);
		validarNoNegativo(gamesGanados, "Los games ganados");
		validarNoNegativo(aces, "Los aces");
		validarNoNegativo(erroresNoForzados, "Los errores no forzados");
		//Cada set ganado exige al menos 6 games ganados
		if (gamesGanados < 6 * setsGanados) {
			throw new IllegalArgumentException("Valores inconsistentes: " + setsGanados + " sets ganados requieren al menos "
					+ (6 * setsGanados) + " games");
		}
		this.gamesGanados = gamesGanados;
		this.aces = aces;
		this.erroresNoForzados = erroresNoForzados;
	}

	//Metodos
	public int getGamesGanados() {
		return gamesGanados;
	}

	public int getAces() {
		return aces;
	}

	public int getErroresNoForzados() {
		return erroresNoForzados;
	}

	@Override
	public String getNombreDeporte() {
		return DEPORTE;
	}

	@Override
	public int getValoracion() {
		return setsGanados * 3 + gamesGanados + aces - erroresNoForzados;
	}

	@Override
	public List<String> getValores() {
		return List.of("" + setsGanados, "" + gamesGanados, "" + aces, "" + erroresNoForzados);
	}

	@Override
	public String toString() {
		return loginJugador + ": " + setsGanados + " sets, " + gamesGanados + " games, " + aces + " aces, "
				+ erroresNoForzados + " errores no forzados";
	}
}
