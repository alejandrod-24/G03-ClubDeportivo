package administracion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import personas.Empleado;

public class SugerenciaProducto {

	//Atributos
	private final String id;
	private final Empleado empleado;
	private final String nombre;
	private final String categoria;
	private final long precioSugerido;
	private final List<String> alergenos;
	private EstadoSolicitud estado;

	//Constructor
	public SugerenciaProducto(String id, Empleado empleado, String nombre, String categoria, long precioSugerido,
			List<String> alergenos) {
		validarTexto(nombre, "El nombre");
		validarTexto(categoria, "La categoria");
		if (precioSugerido <= 0) {
			throw new IllegalArgumentException("El precio sugerido debe ser mayor a cero");
		}
		this.alergenos = new ArrayList<>();
		if (alergenos != null) {
			for (String alergeno : alergenos) {
				validarTexto(alergeno, "El alergeno");
				this.alergenos.add(alergeno);
			}
		}
		this.id = id;
		this.empleado = empleado;
		this.nombre = nombre;
		this.categoria = categoria;
		this.precioSugerido = precioSugerido;
		this.estado = EstadoSolicitud.PENDIENTE;
	}

	private static void validarTexto(String texto, String campo) {
		if (texto == null || texto.isBlank()) {
			throw new IllegalArgumentException(campo + " es obligatorio");
		}
		if (texto.contains(";") || texto.contains(",")) {
			throw new IllegalArgumentException(campo + " no puede contener ';' ni ','");
		}
	}

	//Getters
	public String getId() {
		return id;
	}

	public Empleado getEmpleado() {
		return empleado;
	}

	public String getNombre() {
		return nombre;
	}

	public String getCategoria() {
		return categoria;
	}

	public long getPrecioSugerido() {
		return precioSugerido;
	}

	public List<String> getAlergenos() {
		return Collections.unmodifiableList(alergenos);
	}

	public EstadoSolicitud getEstado() {
		return estado;
	}

	//Metodos
	public void aprobar() {
		validarPendiente();
		estado = EstadoSolicitud.APROBADA;
	}

	public void rechazar() {
		validarPendiente();
		estado = EstadoSolicitud.RECHAZADA;
	}

	private void validarPendiente() {
		if (estado != EstadoSolicitud.PENDIENTE) {
			throw new IllegalStateException("La sugerencia " + id + " ya fue resuelta (" + estado + ")");
		}
	}

	public void restaurarEstado(EstadoSolicitud estado) {
		this.estado = estado;
	}

	@Override
	public String toString() {
		return "[" + id + "] " + nombre + " (" + categoria + ") $" + precioSugerido
				+ (alergenos.isEmpty() ? "" : " | alergenos: " + String.join(", ", alergenos))
				+ " | sugerido por " + empleado.getLogin() + " | " + estado;
	}
}
