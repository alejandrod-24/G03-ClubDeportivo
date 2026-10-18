package deportes;

public enum Nivel {
	PRINCIPIANTE,
	INTERMEDIO,
	AVANZADO;

	//Supuesto, tomado del ejemplo del enunciado: Intermedio juega con Intermedio o Avanzado
	//pero no con Principiante. Por eso Principiante solo juega con Principiante.
	public boolean esCompatibleCon(Nivel otro) {
		if (this == otro) {
			return true;
		}
		return this != PRINCIPIANTE && otro != PRINCIPIANTE;
	}
}
