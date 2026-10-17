package deportes;

public class Equipo {
	
	//Atributos
	private final DeporteConjunto deporte;
	private final String nombre;
	private Categoria categoria;
	private final int cupoMaximo;
	
	//Constructor
	public Equipo(DeporteConjunto deporte, String nombre, Categoria categoria, int cupoMaximo) {
		super();
		this.deporte = deporte;
		validarNombre(nombre);
		this.nombre = nombre;
		validarCategoria(categoria);
		this.categoria = categoria;
		validarCupoMaximo(cupoMaximo);
		this.cupoMaximo = cupoMaximo;
	}

	//Validador
	private void validarNombre(String nombre) { 
		if (nombre == null || nombre.isEmpty())
			throw new IllegalArgumentException("El nombre no puede ir vacio");
	}
	
	private void validarCategoria(Categoria categoria) {
		if (categoria == null)
			throw new IllegalArgumentException("El equipo debe tener una categoria");
	}
	
	private void validarCupoMaximo(int cupoMaximo) {
		if (cupoMaximo <= 0 )
			throw new IllegalArgumentException("El equipo no puede estar vacio");
	}
	
	//Metodos
	public DeporteConjunto getDeporte() {
		return deporte;
	}
	
	public String getNombre() {
		return nombre;
	}
	
	public Categoria getCategoria() {
		return categoria;
	}
	
	public int getCupoMaximo() {
		return cupoMaximo;
	}
	
	//TODO Agregar metodo para cambiar de categoria
	
}	
