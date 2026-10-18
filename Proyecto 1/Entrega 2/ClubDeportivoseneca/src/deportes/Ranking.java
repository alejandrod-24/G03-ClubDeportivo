package deportes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Puntaje ELO de un socio en una academia, con su historial (nunca se borra). */
public class Ranking {

	//Atributos
	private final List<RegistroRanking> historial;

	//Constructor
	public Ranking() {
		this.historial = new ArrayList<>();
	}

	//Metodos
	public void registrar(LocalDate fecha, int puntaje, String motivo) {
		if (puntaje < 0) {
			throw new IllegalArgumentException("El puntaje ELO no puede ser negativo");
		}
		historial.add(new RegistroRanking(fecha, puntaje, motivo));
	}

	public int getPuntaje() {
		return historial.isEmpty() ? 0 : historial.get(historial.size() - 1).getPuntaje();
	}

	public List<RegistroRanking> getHistorial() {
		return Collections.unmodifiableList(historial);
	}

	//Puntaje vigente al final de esa fecha (null si aun no habia registros)
	public Integer getPuntajeEn(LocalDate fecha) {
		Integer puntaje = null;
		for (RegistroRanking r : historial) {
			if (!r.getFecha().isAfter(fecha)) {
				puntaje = r.getPuntaje();
			}
		}
		return puntaje;
	}
}
