package comercio;

public abstract class Producto {

	//Atributos
	protected final String nombre;
	protected long precio;
	protected boolean activo;

	//Constructor
	protected Producto(String nombre, long precio) {
		validarTexto(nombre, "El nombre");
		validarPrecio(precio);
		this.nombre = nombre;
		this.precio = precio;
		this.activo = true;
	}

	//Getters
	public String getNombre() {
		return nombre;
	}

	public long getPrecio() {
		return precio;
	}

	public boolean estaActivo() {
		return activo;
	}

	//Metodos
	public void cambiarPrecio(long nuevoPrecio) {
		validarPrecio(nuevoPrecio);
		this.precio = nuevoPrecio;
	}

	public void desactivar() {
		if (!activo) {
			throw new IllegalStateException(nombre + " ya esta desactivado");
		}
		activo = false;
	}

	private static void validarPrecio(long precio) {
		if (precio <= 0) {
			throw new IllegalArgumentException("El precio debe ser mayor a cero");
		}
	}

	protected static void validarTexto(String texto, String campo) {
		if (texto == null || texto.isBlank()) {
			throw new IllegalArgumentException(campo + " no puede estar vacio");
		}
		//';' y ',' se reservan como separadores en los archivos
		if (texto.contains(";") || texto.contains(",")) {
			throw new IllegalArgumentException(campo + " no puede contener ';' ni ','");
		}
	}
}
