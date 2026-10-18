package comercio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import personas.Persona;

public class CuentaFidelidad {

	//Atributos
	private final Persona titular;
	private final List<Venta> ventas;

	//Constructor
	public CuentaFidelidad(Persona titular) {
		this.titular = titular;
		this.ventas = new ArrayList<>();
	}

	//Metodos
	public Persona getTitular() {
		return titular;
	}

	public List<Venta> getVentas() {
		return Collections.unmodifiableList(ventas);
	}

	public void registrarVenta(Venta venta) {
		if (venta.getComprador() != titular) {
			throw new IllegalArgumentException("La venta " + venta.getId() + " no es de " + titular.getLogin());
		}
		ventas.add(venta);
	}

	public long getSaldo() {
		long saldo = 0;
		for (Venta venta : ventas) {
			saldo += venta.getPuntosGenerados() - venta.getPuntosRedimidos();
		}
		return saldo;
	}

	public List<String> getMovimientos() {
		List<String> movimientos = new ArrayList<>();
		for (Venta venta : ventas) {
			movimientos.add(venta + " | +" + venta.getPuntosGenerados() + " puntos"
					+ (venta.getPuntosRedimidos() > 0 ? ", -" + venta.getPuntosRedimidos() + " redimidos" : ""));
		}
		return movimientos;
	}
}
