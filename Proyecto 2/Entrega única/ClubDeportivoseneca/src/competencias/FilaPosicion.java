package competencias;

/** Una fila de la tabla de posiciones. */
public class FilaPosicion {

	//Atributos
	private final String nombre;
	private int jugados;
	private int ganados;
	private int empatados;
	private int perdidos;
	private int aFavor;
	private int enContra;

	//Constructor
	public FilaPosicion(String nombre) {
		this.nombre = nombre;
	}

	//Metodos
	void registrar(int favor, int contra) {
		jugados++;
		aFavor += favor;
		enContra += contra;
		if (favor > contra) {
			ganados++;
		} else if (favor == contra) {
			empatados++;
		} else {
			perdidos++;
		}
	}

	public String getNombre() {
		return nombre;
	}

	//Victoria 3, empate 1, derrota 0
	public int getPuntos() {
		return ganados * 3 + empatados;
	}

	public int getDiferencia() {
		return aFavor - enContra;
	}

	@Override
	public String toString() {
		return String.format("%-28s PJ %2d  G %2d  E %2d  P %2d  F %3d  C %3d  Pts %3d",
				nombre, jugados, ganados, empatados, perdidos, aFavor, enContra, getPuntos());
	}
}
