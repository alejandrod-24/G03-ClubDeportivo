package entrenamiento;

import personas.Socio;

/** Lo que paso con UN convocado en UNA sesion: asistencia y evaluacion opcional. */
public class RegistroEntrenamiento {

	//Atributos
	private final Socio socio;
	private Asistencia asistencia;
	private Integer nota;
	private String comentario;

	//Constructor (de paquete: lo crea la sesion por cada convocado)
	RegistroEntrenamiento(Socio socio) {
		this.socio = socio;
	}

	//Metodos
	public Socio getSocio() {
		return socio;
	}

	public Asistencia getAsistencia() {
		return asistencia;
	}

	public Integer getNota() {
		return nota;
	}

	public String getComentario() {
		return comentario;
	}

	void setAsistencia(Asistencia asistencia) {
		this.asistencia = asistencia;
	}

	void evaluar(int nota, String comentario) {
		if (asistencia == null) {
			throw new IllegalStateException("Primero debe registrarse la asistencia de " + socio.getLogin());
		}
		if (!asistencia.asistio()) {
			throw new IllegalStateException(socio.getLogin() + " falto a la sesion, no se puede evaluar");
		}
		if (nota < 1 || nota > 5) {
			throw new IllegalArgumentException("La nota debe estar entre 1 y 5");
		}
		if (comentario != null && (comentario.contains(";") || comentario.contains("\n"))) {
			throw new IllegalArgumentException("El comentario no puede contener ';' ni saltos de linea");
		}
		this.nota = nota;
		this.comentario = comentario == null ? "" : comentario;
	}

	//Para la persistencia
	public void restaurar(Asistencia asistencia, Integer nota, String comentario) {
		this.asistencia = asistencia;
		this.nota = nota;
		this.comentario = comentario;
	}

	@Override
	public String toString() {
		return socio.getLogin() + ": " + (asistencia == null ? "sin registrar" : asistencia)
				+ (nota == null ? "" : " | nota " + nota + (comentario.isEmpty() ? "" : " - " + comentario));
	}
}
