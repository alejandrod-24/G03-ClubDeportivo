package deportes;

import personas.Socio;

public class InscripcionEquipo {

	//Atributos
	private final Socio socio;
	private final Equipo equipo;
	private final String posicion;
	private final int numeroCamiseta;

	//Constructor (de paquete: se crea con Equipo.inscribir)
	InscripcionEquipo(Socio socio, Equipo equipo, String posicion, int numeroCamiseta) {
		this.socio = socio;
		this.equipo = equipo;
		this.posicion = posicion;
		this.numeroCamiseta = numeroCamiseta;
	}

	//Metodos
	public Socio getSocio() {
		return socio;
	}

	public Equipo getEquipo() {
		return equipo;
	}

	public String getPosicion() {
		return posicion;
	}

	public int getNumeroCamiseta() {
		return numeroCamiseta;
	}

	@Override
	public String toString() {
		return "#" + numeroCamiseta + " " + socio.getNombre() + " (" + posicion + ")";
	}
}
