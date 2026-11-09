package personas;

public class Entrenador extends Empleado {
	
	//Constructor
	public Entrenador(String nombre, String login, String password, String codigoDescuento) {
		super(nombre, login, password, codigoDescuento);
	}

	@Override
	public Rol getRol() {
		return Rol.ENTRENADOR;
	}
}
