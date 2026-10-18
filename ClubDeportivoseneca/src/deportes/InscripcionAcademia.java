package deportes;

import java.time.LocalDate;

import personas.Socio;

public class InscripcionAcademia {

	//Atributos
	private final Socio socio;
	private final Academia academia;
	private Nivel nivel;
	private boolean activa;
	private final Ranking ranking;

	//Constructor (de paquete: se crea con Academia.inscribir)
	InscripcionAcademia(Socio socio, Academia academia, Nivel nivel) {
		this.socio = socio;
		this.academia = academia;
		this.nivel = nivel;
		this.activa = true;
		this.ranking = new Ranking();
	}

	//Metodos
	public Socio getSocio() {
		return socio;
	}

	public Academia getAcademia() {
		return academia;
	}

	public Nivel getNivel() {
		return nivel;
	}

	public boolean estaActiva() {
		return activa;
	}

	public Ranking getRanking() {
		return ranking;
	}

	public void cambiarNivel(Nivel nuevo) {
		if (nuevo == null) {
			throw new IllegalArgumentException("Debe indicar el nivel");
		}
		this.nivel = nuevo;
	}

	//El ranking historico se conserva
	void retirar() {
		activa = false;
	}

	void reactivar(Nivel nivel, int puntajeInicial, LocalDate fecha) {
		cambiarNivel(nivel);
		activa = true;
		ranking.registrar(fecha, puntajeInicial, "Reinscripcion");
	}

	//Para la persistencia
	public void restaurarEstado(boolean activa) {
		this.activa = activa;
	}

	@Override
	public String toString() {
		return socio.getLogin() + " - " + nivel + " - ELO " + ranking.getPuntaje() + (activa ? "" : " [RETIRADO]");
	}
}
