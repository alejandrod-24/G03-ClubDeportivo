package instalaciones;
import deportes.Deporte;

public abstract class Instalacion {
	
	//Atributos
	protected final String nombre;
	protected final Deporte deporte;
	protected final int capacidad;
	
	//Constructor
	protected Instalacion(String nombre, Deporte deporte, int capacidad) {
		validarNombre(nombre);
		this.nombre = nombre;
		validarDeporte(deporte);
		this.deporte = deporte;
		validarCapacidad(capacidad);
		this.capacidad = capacidad;
	}

	
	//Getters
	public String getNombre() {
		return nombre;
	}

	public Deporte getDeporte() {
		return deporte;
	}

	public int getCapacidad() {
		return capacidad;
	}
	
	//Validadores
	private void validarNombre(String nombre) {
		if (nombre == null || nombre.isBlank())
			throw new IllegalArgumentException("El nombre no puede ir vacio");
		if (nombre.contains(";"))
			throw new IllegalArgumentException("El nombre no puede contener ';'");
	}
	
	private void validarDeporte(Deporte deporte) {
		if (deporte == null)
			throw new IllegalArgumentException("La cancha debe tener un deporte asignado");
	}
	
	private void validarCapacidad(int capacidad) {
		if (capacidad <= 0)
			throw new IllegalArgumentException("La capacidad de la cancha no puede ser 0 o negativa");
	}
	
}
