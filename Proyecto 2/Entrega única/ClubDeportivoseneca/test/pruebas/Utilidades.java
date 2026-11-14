package pruebas;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import global.Club;
import global.DatosIniciales;
import global.Reloj;
import persistencia.Persistencia;

/**
 * Utilidades compartidas por las pruebas JUnit.
 */
public final class Utilidades {

	//Fecha fija para que las pruebas den siempre el mismo resultado (sabado 17 de octubre, 7:00)
	public static final LocalDate FECHA_INICIAL = LocalDate.of(2026, 10, 17);

	//Lunes 19 de octubre de 2026, el primer dia habil despues de la fecha inicial
	public static final LocalDate LUNES = LocalDate.of(2026, 10, 19);

	private Utilidades() {
	}

	//Club nuevo con los datos iniciales (no toca la carpeta datos/)
	public static Club clubDePrueba() {
		Reloj.fijar(FECHA_INICIAL);
		return DatosIniciales.crearClub();
	}

	//Arma el mapa producto -> cantidad a partir de pares nombre, cantidad
	public static Map<String, Integer> items(Object... pares) {
		Map<String, Integer> items = new LinkedHashMap<>();
		for (int i = 0; i + 1 < pares.length; i += 2) {
			items.put((String) pares[i], (Integer) pares[i + 1]);
		}
		return items;
	}

	//Guarda el club en una carpeta temporal y lo carga de nuevo en un club nuevo
	public static Club guardarYRecargar(Club club, Path carpeta) {
		Persistencia persistencia = new Persistencia(carpeta.toString());
		persistencia.salvar(club);
		return persistencia.cargar();
	}
}
