package global;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Horario de atencion del club (supuesto): lunes a sabado de 8:00 a 20:00.
 * Las reservas y partidos de practica se hacen por bloques de una hora en punto.
 */
public class Horario {

	public static final LocalTime APERTURA = LocalTime.of(8, 0);
	public static final LocalTime CIERRE = LocalTime.of(20, 0);
	public static final int MINUTOS_BLOQUE = 60;

	//El intervalo debe estar dentro de un mismo dia de atencion
	public static void validarIntervalo(LocalDateTime inicio, LocalDateTime fin) {
		if (inicio == null || fin == null) {
			throw new IllegalArgumentException("Debe indicar fecha y hora");
		}
		if (!inicio.isBefore(fin)) {
			throw new IllegalArgumentException("La hora de fin debe ser posterior a la de inicio");
		}
		if (inicio.getDayOfWeek() == DayOfWeek.SUNDAY) {
			throw new IllegalArgumentException("El club no atiende los domingos");
		}
		if (!inicio.toLocalDate().equals(fin.toLocalDate()) || inicio.toLocalTime().isBefore(APERTURA)
				|| fin.toLocalTime().isAfter(CIERRE)) {
			throw new IllegalArgumentException("Fuera del horario de atencion (" + APERTURA + " a " + CIERRE + ")");
		}
	}

	public static void validarBloque(LocalDateTime inicio) {
		if (inicio == null) {
			throw new IllegalArgumentException("Debe indicar el bloque horario");
		}
		if (inicio.getMinute() != 0 || inicio.getSecond() != 0) {
			throw new IllegalArgumentException("Los bloques empiezan en punto (ej. 9:00)");
		}
		validarIntervalo(inicio, finDeBloque(inicio));
	}

	public static LocalDateTime finDeBloque(LocalDateTime inicio) {
		return inicio.plusMinutes(MINUTOS_BLOQUE);
	}

	public static boolean seCruzan(LocalDateTime inicioA, LocalDateTime finA, LocalDateTime inicioB, LocalDateTime finB) {
		return inicioA.isBefore(finB) && inicioB.isBefore(finA);
	}
}
