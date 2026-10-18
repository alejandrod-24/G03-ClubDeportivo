package entrenamiento;

public enum Asistencia {
	ASISTIO,
	LLEGO_TARDE,
	FALTO;

	public boolean asistio() {
		return this != FALTO;
	}
}
