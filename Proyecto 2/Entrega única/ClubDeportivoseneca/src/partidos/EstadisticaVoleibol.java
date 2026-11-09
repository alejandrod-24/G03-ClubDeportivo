package partidos;

import java.util.List;

public class EstadisticaVoleibol extends EstadisticaPartido {

	public static final String DEPORTE = "Voleibol";

	//Atributos
	private final int aces;
	private final int bloqueos;

	//Constructor
	public EstadisticaVoleibol(String loginJugador, int aces, int bloqueos) {
		super(loginJugador);
		validarNoNegativo(aces, "Los aces");
		validarNoNegativo(bloqueos, "Los bloqueos");
		this.aces = aces;
		this.bloqueos = bloqueos;
	}

	//Metodos
	public int getAces() {
		return aces;
	}

	public int getBloqueos() {
		return bloqueos;
	}

	@Override
	public String getNombreDeporte() {
		return DEPORTE;
	}

	@Override
	public int getAnotaciones() {
		return aces;
	}

	@Override
	public int getValoracion() {
		return aces + bloqueos;
	}

	@Override
	public List<String> getValores() {
		return List.of("" + aces, "" + bloqueos);
	}

	@Override
	public String toString() {
		return loginJugador + ": " + aces + " aces, " + bloqueos + " bloqueos";
	}
}
