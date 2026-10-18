package personas;

public abstract class Empleado extends Persona{

	//Atributos
	protected final String codigoDescuento;

	//Constructor
	protected Empleado(String nombre, String login, String password, String codigoDescuento) {
		super(nombre, login, password);
		validarCodigoDescuento(codigoDescuento);
		this.codigoDescuento = codigoDescuento;
	}

	//Metodos
	public String getCodigoDescuento() {
		return codigoDescuento;
	}

	//Validar codigo de descuento
	private void validarCodigoDescuento(String codigoDescuento) {
		if (codigoDescuento == null || codigoDescuento.isEmpty()) {
			throw new IllegalArgumentException("El codigo de descuento no puede estar vacio");
		}
	}

}
