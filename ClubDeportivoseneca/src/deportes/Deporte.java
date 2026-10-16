package deportes;

public abstract class Deporte {
	
	//Atributos
	protected final String nombre;

	//Constructor
	protected Deporte(String nombre) {
		this.nombre = nombre;
	}
	
	//Validar nombre
	private void validarNombre(String nombre) {
		if (nombre == null || nombre.isEmpty())	 
			throw new IllegalArgumentException("Nombre no puede estar vacio");
	}
	
	//Metodos
	public String getNombre() {
		return nombre;
	}
	
	
	
	
}
