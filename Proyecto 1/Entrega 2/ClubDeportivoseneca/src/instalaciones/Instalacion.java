package instalaciones;
import deportes.Deporte;

public abstract class Instalacion {

	//Atributos
	protected final String codigo;
	protected String nombre;
	protected final Deporte deporte;
	protected int capacidad;
	protected boolean activa;

	//Constructor
	protected Instalacion(String codigo, String nombre, Deporte deporte, int capacidad) {
		validarTexto(codigo, "El codigo");
		this.codigo = codigo;
		validarTexto(nombre, "El nombre");
		this.nombre = nombre;
		validarDeporte(deporte);
		this.deporte = deporte;
		validarCapacidad(capacidad);
		this.capacidad = capacidad;
		this.activa = true;
	}


	//Getters
	public String getCodigo() {
		return codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public Deporte getDeporte() {
		return deporte;
	}

	public int getCapacidad() {
		return capacidad;
	}

	public boolean estaActiva() {
		return activa;
	}

	//Metodos
	public void editar(String nuevoNombre, int nuevaCapacidad) {
		validarTexto(nuevoNombre, "El nombre");
		validarCapacidad(nuevaCapacidad);
		this.nombre = nuevoNombre;
		this.capacidad = nuevaCapacidad;
	}

	public void desactivar() {
		if (!activa) {
			throw new IllegalStateException("La instalacion " + codigo + " ya esta desactivada");
		}
		activa = false;
	}

	//Cada tipo de instalacion define que capacidades son validas para su deporte
	protected abstract boolean esCapacidadValida(int capacidad);

	//Validadores
	private static void validarTexto(String texto, String campo) {
		if (texto == null || texto.isBlank())
			throw new IllegalArgumentException(campo + " no puede ir vacio");
		if (texto.contains(";") || texto.contains(","))
			throw new IllegalArgumentException(campo + " no puede contener ';' ni ','");
	}

	private void validarDeporte(Deporte deporte) {
		if (deporte == null)
			throw new IllegalArgumentException("La cancha debe tener un deporte asignado");
	}

	private void validarCapacidad(int capacidad) {
		if (capacidad <= 0)
			throw new IllegalArgumentException("La capacidad de la cancha no puede ser 0 o negativa");
		if (!esCapacidadValida(capacidad))
			throw new IllegalArgumentException("La capacidad " + capacidad + " no es valida para " + deporte.getNombre());
	}

	@Override
	public String toString() {
		return "[" + codigo + "] " + nombre + " (" + deporte.getNombre() + ", capacidad " + capacidad + ")"
				+ (activa ? "" : " [DESACTIVADA]");
	}
}
