package partidos;

/** El padel registra los mismos campos que el tenis. */
public class EstadisticaPadel extends EstadisticaTenis {

	public static final String DEPORTE = "Padel";

	//Constructor
	public EstadisticaPadel(String loginJugador, int setsGanados, int gamesGanados, int aces, int erroresNoForzados) {
		super(loginJugador, setsGanados, gamesGanados, aces, erroresNoForzados);
	}

	@Override
	public String getNombreDeporte() {
		return DEPORTE;
	}
}
