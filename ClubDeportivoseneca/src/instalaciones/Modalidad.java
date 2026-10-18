package instalaciones;

public enum Modalidad {
	SENCILLOS(2),
	DOBLES(4);

	private final int jugadores;

	Modalidad(int jugadores) {
		this.jugadores = jugadores;
	}

	public int getJugadores() {
		return jugadores;
	}

	public static Modalidad deJugadores(int jugadores) {
		for (Modalidad m : values()) {
			if (m.jugadores == jugadores) {
				return m;
			}
		}
		throw new IllegalArgumentException("Un partido de practica es de 2 (sencillos) o 4 (dobles) jugadores");
	}
}
