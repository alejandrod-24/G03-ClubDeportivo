package personas;

public abstract class Persona {

	//Atributos
	protected String nombre;
	protected final String login;
	private String password;
	private boolean activo;


	//Constructores
	protected Persona(String nombre, String login, String password) {
		validarTexto(nombre, "El nombre");
		validarTexto(login, "El login");
		if (login.contains(" ")) {
			throw new IllegalArgumentException("El login no puede tener espacios");
		}
		validarPassword(password);
		this.nombre = nombre;
		this.login = login;
		this.password = password;
		this.activo = true;
	}

	//Getters
	public String getNombre() {
		return nombre;
	}


	public String getLogin() {
		return login;
	}

	public abstract Rol getRol();

	//Estado (dar de baja conserva el historial pero impide iniciar sesion)
	public boolean estaActivo() {
		return activo;
	}

	public void darDeBaja() {
		if (!activo) {
			throw new IllegalStateException(login + " ya esta dado de baja");
		}
		activo = false;
	}

	//Contrasena
	public boolean esPasswordCorrecta(String input) {
		return password.equals(input);
	}

	//cambiar contrasena
	public void cambiarPassword(String passwordActual, String newPass) {
		if (!esPasswordCorrecta(passwordActual)) {
			throw new IllegalArgumentException("La contraseña actual no es correcta");
		}
		validarPassword(newPass);
		this.password = newPass;
	}

	//restablecer contrasena (lo hace el administrador, no pide la actual)
	public void restablecerPassword(String newPass) {
		validarPassword(newPass);
		this.password = newPass;
	}

	//Solo para la persistencia
	public String getPasswordParaGuardar() {
		return password;
	}

	//validar contrasena
	private void validarPassword(String password) {
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("La contraseña no puede estar vacia");
		}
		validarTexto(password, "La contraseña");
	}

	//validar textos (el ';' se reserva como separador en los archivos)
	protected static void validarTexto(String texto, String campo) {
		if (texto == null || texto.isBlank()) {
			throw new IllegalArgumentException(campo + " no puede estar vacio");
		}
		if (texto.contains(";")) {
			throw new IllegalArgumentException(campo + " no puede contener ';'");
		}
	}

	@Override
	public String toString() {
		return nombre + " (" + login + ", " + getRol() + (activo ? "" : ", DADO DE BAJA") + ")";
	}
}
