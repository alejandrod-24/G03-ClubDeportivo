package administracion;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;

public enum TipoPeriodo {
	DIARIO,
	SEMANAL,
	MENSUAL;

	public LocalDate inicioDe(LocalDate fecha) {
		switch (this) {
		case DIARIO:
			return fecha;
		case SEMANAL:
			return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		default:
			return fecha.withDayOfMonth(1);
		}
	}

	public String describir(LocalDate inicio) {
		switch (this) {
		case DIARIO:
			return "Dia " + inicio;
		case SEMANAL:
			return "Semana del " + inicio;
		default:
			return "Mes " + YearMonth.from(inicio);
		}
	}
}
