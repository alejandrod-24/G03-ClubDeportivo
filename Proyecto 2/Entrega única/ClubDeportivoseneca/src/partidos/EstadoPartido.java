package partidos;

public enum EstadoPartido {
	PROGRAMADO,
	JUGADO,
	NO_JUGADO,
	//Solo partidos de practica: el socio o el sistema lo cancela antes de jugarse
	CANCELADO
}
