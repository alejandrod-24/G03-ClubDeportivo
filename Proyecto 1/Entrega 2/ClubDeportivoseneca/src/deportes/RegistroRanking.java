package deportes;

import java.time.LocalDate;

/** Un valor del puntaje ELO en una fecha, con el motivo del cambio. */
public class RegistroRanking {

	//Atributos
	private final LocalDate fecha;
	private final int puntaje;
	private final String motivo;

	//Constructor
	public RegistroRanking(LocalDate fecha, int puntaje, String motivo) {
		this.fecha = fecha;
		this.puntaje = puntaje;
		this.motivo = motivo;
	}

	//Metodos
	public LocalDate getFecha() {
		return fecha;
	}

	public int getPuntaje() {
		return puntaje;
	}

	public String getMotivo() {
		return motivo;
	}

	@Override
	public String toString() {
		return fecha + ": " + puntaje + " (" + motivo + ")";
	}
}
