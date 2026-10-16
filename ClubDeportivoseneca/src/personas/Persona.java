package personas;

public abstract class Persona {
	
	//Atributos
	protected String nombre;
	protected final String login;
	private String password;
	
	
	//Constructores
	protected Persona(String nombre, String login, String password) {
		this.nombre = nombre;
		this.login = login;
		validarPassword(password);
		this.password = password;
	}

	//Getters
	public String getNombre() {
		return nombre;
	}


	public String getLogin() {
		return login;
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

	//validar contrasena
	private void validarPassword(String password) {
		if (password == null || password.isEmpty()) {
			throw new IllegalArgumentException("La contraseña no puede estar vacia");
		}
	}
}
