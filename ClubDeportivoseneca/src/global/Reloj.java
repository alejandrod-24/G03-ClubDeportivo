package global;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Fecha y hora "actuales" del sistema. Por defecto son las reales, pero los
 * programas de prueba pueden fijarlas o avanzarlas para simular el paso
 * del tiempo (ej. registrar el resultado de un partido ya programado).
 */
public class Reloj {

	//Hora a la que queda el reloj cuando se fija solo una fecha (antes de abrir el club)
	public static final LocalTime HORA_AL_FIJAR_FECHA = LocalTime.of(7, 0);

	//Atributos
	private static LocalDateTime momentoFijo = null;

	//Metodos
	public static LocalDateTime ahora() {
		return momentoFijo != null ? momentoFijo : LocalDateTime.now().withSecond(0).withNano(0);
	}

	public static LocalDate hoy() {
		return ahora().toLocalDate();
	}

	public static void fijar(LocalDate fecha) {
		momentoFijo = fecha.atTime(HORA_AL_FIJAR_FECHA);
	}

	public static void fijar(LocalDateTime momento) {
		momentoFijo = momento;
	}

	public static void avanzarDias(int dias) {
		fijar(ahora().plusDays(dias));
	}

	public static void avanzarHoras(int horas) {
		fijar(ahora().plusHours(horas));
	}

	public static void restablecer() {
		momentoFijo = null;
	}
}
