package comercio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import personas.Persona;

public abstract class Venta {

	public static final int PORCENTAJE_PUNTOS = 2;
	protected final String id;
	protected final LocalDate fecha;
	protected final Persona comprador;
	protected final long puntosRedimidos;
	protected final List<DetalleVenta> detalles;

	//Constructor
	protected Venta(String id, LocalDate fecha, Persona comprador, long puntosRedimidos) {
		if (puntosRedimidos < 0) {
			throw new IllegalArgumentException("Los puntos a redimir no pueden ser negativos");
		}
		this.id = id;
		this.fecha = fecha;
		this.comprador = comprador;
		this.puntosRedimidos = puntosRedimidos;
		this.detalles = new ArrayList<>();
	}

	//Getters
	public String getId() {
		return id;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public Persona getComprador() {
		return comprador;
	}

	public long getPuntosRedimidos() {
		return puntosRedimidos;
	}

	public List<DetalleVenta> getDetalles() {
		return Collections.unmodifiableList(detalles);
	}

	public abstract String getRubro();

	public abstract String getNombreImpuesto();

	//Metodos
	public void agregarDetalle(Producto producto, int cantidad) {
		if (cantidad <= 0) {
			throw new IllegalArgumentException("La cantidad de " + producto.getNombre() + " debe ser mayor a cero");
		}
		detalles.add(new DetalleVenta(producto, cantidad, producto.getPrecio()));
	}

	public void agregarDetalleGuardado(Producto producto, int cantidad, long precioUnitario) {
		detalles.add(new DetalleVenta(producto, cantidad, precioUnitario));
	}

	public long getSubtotal() {
		long subtotal = 0;
		for (DetalleVenta detalle : detalles) {
			subtotal += detalle.getValor();
		}
		return subtotal;
	}

	public long getDescuento() {
		return 0;
	}

	public long getBase() {
		return getSubtotal() - getDescuento() - puntosRedimidos;
	}

	public abstract long getImpuesto();

	public long getPropina() {
		return 0;
	}

	public long getTotal() {
		return getBase() + getImpuesto() + getPropina();
	}

	public long getPuntosGenerados() {
		return porcentaje(getBase() + getImpuesto(), PORCENTAJE_PUNTOS);
	}

	protected static long porcentaje(long valor, int porcentaje) {
		return Math.round(valor * porcentaje / 100.0);
	}


	public String generarDesglose() {
		String resultado = "Venta " + id + " (" + getRubro() + ") del " + fecha
				+ " - comprador: " + comprador.getLogin() + "\n";

		for (DetalleVenta detalle : detalles) {
			resultado += "  " + detalle + "\n";
		}

		resultado += "  Subtotal: $" + getSubtotal() + "\n";
		resultado += "  Descuento: $" + getDescuento() + "\n";
		resultado += "  Puntos redimidos: $" + puntosRedimidos + "\n";
		resultado += "  Base: $" + getBase() + "\n";
		resultado += "  " + getNombreImpuesto() + ": $" + getImpuesto() + "\n";
		resultado += "  Propina: $" + getPropina() + "\n";
		resultado += "  Total: $" + getTotal() + "\n";
		resultado += "  Puntos generados: " + getPuntosGenerados();

		return resultado;
	}

	@Override
	public String toString() {
		return "[" + id + "] " + fecha + " " + getRubro() + " - " + comprador.getLogin() + " - total $" + getTotal();
	}
}
