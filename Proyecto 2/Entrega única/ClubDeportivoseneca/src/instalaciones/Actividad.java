package instalaciones;

import java.time.LocalDateTime;

import personas.Persona;

/**
 * Algo que ocupa una instalacion y a unas personas en un horario:
 * una reserva, un partido de practica o una sesion de entrenamiento.
 * Con esto el club revisa cruces de horario sin importar el tipo.
 */
public interface Actividad {

	LocalDateTime getInicio();

	LocalDateTime getFin();

	Instalacion getInstalacion();

	//false si fue cancelada (ya no ocupa nada)
	boolean estaVigente();

	boolean involucra(Persona persona);

	default boolean seCruzaCon(LocalDateTime inicio, LocalDateTime fin) {
		return getInicio().isBefore(fin) && inicio.isBefore(getFin());
	}

	default boolean estaEnCursoEn(LocalDateTime momento) {
		return !momento.isBefore(getInicio()) && momento.isBefore(getFin());
	}
}
