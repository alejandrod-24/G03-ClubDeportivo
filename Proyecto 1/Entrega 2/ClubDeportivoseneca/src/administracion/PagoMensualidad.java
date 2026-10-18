package administracion;

import java.time.LocalDate;
import java.time.YearMonth;

import personas.Socio;

public class PagoMensualidad {

	//Valor de la mensualidad (supuesto)
	public static final long VALOR = 150000;

	//Atributos
	private final String id;
	private final Socio socio;
	private final YearMonth periodo;
	private final LocalDate fecha;
	private final long monto;

	//Constructor
	public PagoMensualidad(String id, Socio socio, YearMonth periodo, LocalDate fecha, long monto) {
		this.id = id;
		this.socio = socio;
		this.periodo = periodo;
		this.fecha = fecha;
		this.monto = monto;
	}

	//Getters
	public String getId() {
		return id;
	}

	public Socio getSocio() {
		return socio;
	}

	public YearMonth getPeriodo() {
		return periodo;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public long getMonto() {
		return monto;
	}

	@Override
	public String toString() {
		return "[" + id + "] " + socio.getLogin() + " mensualidad " + periodo + " pagada el " + fecha + " $" + monto;
	}
}
