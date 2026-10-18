package partidos;

import java.util.List;

public class EstadisticaFutbol extends EstadisticaPartido {

	public static final String DEPORTE = "Futbol";

	//Atributos
	private final int goles;
	private final int asistencias;
	private final int tarjetasAmarillas;
	private final boolean tarjetaRoja;

	//Constructor
	public EstadisticaFutbol(String loginJugador, int goles, int asistencias, int tarjetasAmarillas, boolean tarjetaRoja) {
		super(loginJugador);
		validarNoNegativo(goles, "Los goles");
		validarNoNegativo(asistencias, "Las asistencias");
		validarNoNegativo(tarjetasAmarillas, "Las tarjetas amarillas");
		if (tarjetasAmarillas > 2) {
			throw new IllegalArgumentException("Un jugador no puede recibir mas de 2 amarillas en un partido");
		}
		this.goles = goles;
		this.asistencias = asistencias;
		this.tarjetasAmarillas = tarjetasAmarillas;
		this.tarjetaRoja = tarjetaRoja;
	}

	//Metodos
	public int getGoles() {
		return goles;
	}

	public int getAsistencias() {
		return asistencias;
	}

	@Override
	public int getTarjetasAmarillas() {
		return tarjetasAmarillas;
	}

	@Override
	public boolean tieneTarjetaRoja() {
		return tarjetaRoja;
	}

	@Override
	public String getNombreDeporte() {
		return DEPORTE;
	}

	@Override
	public int getAnotaciones() {
		return goles;
	}

	@Override
	public int getValoracion() {
		return goles * 3 + asistencias * 2 - tarjetasAmarillas - (tarjetaRoja ? 3 : 0);
	}

	@Override
	public List<String> getValores() {
		return List.of("" + goles, "" + asistencias, "" + tarjetasAmarillas, "" + tarjetaRoja);
	}

	@Override
	public String toString() {
		return loginJugador + ": " + goles + " goles, " + asistencias + " asistencias, "
				+ tarjetasAmarillas + " amarillas" + (tarjetaRoja ? ", ROJA" : "");
	}
}
