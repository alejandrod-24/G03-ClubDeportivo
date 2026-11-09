package competencias;

public enum Fase {
	LIGA,
	GRUPOS,
	OCTAVOS,
	CUARTOS,
	SEMIFINAL,
	FINAL;

	public boolean esEliminatoria() {
		return this != LIGA && this != GRUPOS;
	}

	public boolean esValidaPara(FormatoCompetencia formato) {
		if (formato == FormatoCompetencia.LIGA) {
			return this == LIGA;
		}
		return this != LIGA;
	}
}
