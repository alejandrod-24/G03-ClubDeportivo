package comercio;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ArticuloDeportivo extends Producto {

	public static final String TIENDA = "Tienda";
	public static final int EXISTENCIA_MINIMA = 3;
	private final String categoria;
	private final Map<String, Integer> existencias;

	//Constructor
	public ArticuloDeportivo(String nombre, String categoria, long precio) {
		super(nombre, precio);
		validarTexto(categoria, "La categoria");
		this.categoria = categoria;
		this.existencias = new LinkedHashMap<>();
		this.existencias.put(TIENDA, 0);
	}

	//Getters
	public String getCategoria() {
		return categoria;
	}

	public Map<String, Integer> getExistencias() {
		return Collections.unmodifiableMap(existencias);
	}

	public int getExistencia(String ubicacion) {
		return existencias.getOrDefault(ubicacion, 0);
	}

	public boolean hayExistencia(int cantidad) {
		return cantidad <= getExistencia(TIENDA);
	}

	//Metodos
	public void reabastecer(int cantidad) {
		if (!activo) {
			throw new IllegalStateException(nombre + " esta desactivado");
		}
		validarCantidad(cantidad);
		existencias.merge(TIENDA, cantidad, Integer::sum);
	}

	public void mover(String origen, String destino, int cantidad) {
		validarCantidad(cantidad);
		validarTexto(destino, "La ubicacion de destino");
		if (destino.equals(origen)) {
			throw new IllegalArgumentException("El origen y el destino deben ser distintos");
		}
		if (cantidad > getExistencia(origen)) {
			throw new IllegalStateException("En " + origen + " solo hay " + getExistencia(origen) + " unidades de " + nombre);
		}
		existencias.put(origen, getExistencia(origen) - cantidad);
		existencias.merge(destino, cantidad, Integer::sum);
	}

	public void descontar(int cantidad) {
		if (!hayExistencia(cantidad)) {
			throw new IllegalStateException("No hay suficientes existencias de " + nombre + " en la tienda");
		}
		existencias.put(TIENDA, getExistencia(TIENDA) - cantidad);
	}

	private static void validarCantidad(int cantidad) {
		if (cantidad <= 0) {
			throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
		}
	}

	public void restaurarExistencia(String ubicacion, int cantidad) {
		existencias.put(ubicacion, cantidad);
	}

	@Override
	public String toString() {
		return nombre + " (" + categoria + ") $" + precio + " | en tienda: " + getExistencia(TIENDA)
				+ (activo ? "" : " [DESACTIVADO]");
	}
}
