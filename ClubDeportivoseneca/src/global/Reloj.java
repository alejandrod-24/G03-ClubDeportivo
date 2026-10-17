package global;

import java.time.LocalDate;

/**
 * Fecha "actual" del sistema. Por defecto es la fecha real, pero los
 * programas de prueba pueden fijarla o avanzarla para simular el paso
 * del tiempo (ej. registrar el resultado de un partido ya programado).
 */
public class Reloj {

	//Atributos
	private static LocalDate fechaFija = null;

	//Metodos
	public static LocalDate hoy() {
		return fechaFija != null ? fechaFija : LocalDate.now();
	}

	public static void fijar(LocalDate fecha) {
		fechaFija = fecha;
	}

	public static void avanzarDias(int dias) {
		fijar(hoy().plusDays(dias));
	}

	public static void restablecer() {
		fechaFija = null;
	}
}
