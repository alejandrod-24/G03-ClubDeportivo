package entrenamiento;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import global.Reloj;
import instalaciones.Actividad;
import instalaciones.Instalacion;
import personas.Entrenador;
import personas.Persona;
import personas.Socio;

/**
 * Sesion planeada por un entrenador: grupal para equipos de conjunto o
 * individual/grupal pequena para academias. Las reglas de cruces se validan en Club.
 */
public class SesionEntrenamiento implements Actividad {

	//Atributos
	private final String id;
	private final Entrenador entrenador;
	private Instalacion instalacion;
	private LocalDateTime inicio;
	private int duracionMinutos;
	private final List<RegistroEntrenamiento> registros;
	private boolean cancelada;

	//Constructor
	public SesionEntrenamiento(String id, Entrenador entrenador, Instalacion instalacion, LocalDateTime inicio,
			int duracionMinutos, List<Socio> convocados) {
		this.id = id;
		this.entrenador = entrenador;
		this.registros = new ArrayList<>();
		this.cancelada = false;
		cambiarDatos(instalacion, inicio, duracionMinutos, convocados);
	}

	private void cambiarDatos(Instalacion instalacion, LocalDateTime inicio, int duracionMinutos, List<Socio> convocados) {
		if (duracionMinutos <= 0) {
			throw new IllegalArgumentException("La duracion debe ser mayor a cero");
		}
		if (convocados == null || convocados.isEmpty()) {
			throw new IllegalArgumentException("La sesion debe tener al menos un convocado");
		}
		this.instalacion = instalacion;
		this.inicio = inicio;
		this.duracionMinutos = duracionMinutos;
		registros.clear();
		for (Socio s : convocados) {
			registros.add(new RegistroEntrenamiento(s));
		}
	}

	//Getters
	public String getId() {
		return id;
	}

	public Entrenador getEntrenador() {
		return entrenador;
	}

	@Override
	public Instalacion getInstalacion() {
		return instalacion;
	}

	@Override
	public LocalDateTime getInicio() {
		return inicio;
	}

	@Override
	public LocalDateTime getFin() {
		return inicio.plusMinutes(duracionMinutos);
	}

	public int getDuracionMinutos() {
		return duracionMinutos;
	}

	public boolean estaCancelada() {
		return cancelada;
	}

	public List<RegistroEntrenamiento> getRegistros() {
		return new ArrayList<>(registros);
	}

	public List<Socio> getConvocados() {
		List<Socio> convocados = new ArrayList<>();
		for (RegistroEntrenamiento r : registros) {
			convocados.add(r.getSocio());
		}
		return convocados;
	}

	public RegistroEntrenamiento buscarRegistro(Socio socio) {
		for (RegistroEntrenamiento r : registros) {
			if (r.getSocio() == socio) {
				return r;
			}
		}
		return null;
	}

	public boolean haEmpezado() {
		return !Reloj.ahora().isBefore(inicio);
	}

	public boolean haTerminado() {
		return !Reloj.ahora().isBefore(getFin());
	}

	// -------------------- Actividad --------------------

	@Override
	public boolean estaVigente() {
		return !cancelada;
	}

	@Override
	public boolean involucra(Persona persona) {
		return persona == entrenador || (persona instanceof Socio && buscarRegistro((Socio) persona) != null);
	}

	// -------------------- Metodos --------------------

	private void validarModificable() {
		if (cancelada) {
			throw new IllegalStateException("La sesion " + id + " esta cancelada");
		}
		if (haEmpezado()) {
			throw new IllegalStateException("La sesion " + id + " ya empezo o termino");
		}
	}

	public void modificar(Instalacion instalacion, LocalDateTime inicio, int duracionMinutos, List<Socio> convocados) {
		validarModificable();
		cambiarDatos(instalacion, inicio, duracionMinutos, convocados);
	}

	public void cancelar() {
		validarModificable();
		cancelada = true;
	}

	public void registrarAsistencia(Socio socio, Asistencia asistencia) {
		RegistroEntrenamiento r = registroParaCerrar(socio);
		if (asistencia == null) {
			throw new IllegalArgumentException("Debe indicar la asistencia");
		}
		r.setAsistencia(asistencia);
	}

	public void evaluar(Socio socio, int nota, String comentario) {
		registroParaCerrar(socio).evaluar(nota, comentario);
	}

	private RegistroEntrenamiento registroParaCerrar(Socio socio) {
		if (cancelada) {
			throw new IllegalStateException("La sesion " + id + " esta cancelada");
		}
		if (!haTerminado()) {
			throw new IllegalStateException("La sesion " + id + " no ha terminado (termina " + getFin() + ")");
		}
		RegistroEntrenamiento r = buscarRegistro(socio);
		if (r == null) {
			throw new IllegalArgumentException(socio.getLogin() + " no fue convocado a la sesion " + id);
		}
		return r;
	}

	//Para la persistencia
	public void restaurarCancelada(boolean cancelada) {
		this.cancelada = cancelada;
	}

	public String generarDetalle() {
		StringBuilder sb = new StringBuilder(toString());
		for (RegistroEntrenamiento r : registros) {
			sb.append("\n    ").append(r);
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return "[" + id + "] " + inicio.toLocalDate() + " " + inicio.toLocalTime() + " (" + duracionMinutos + " min) "
				+ instalacion.getNombre() + " - entrenador " + entrenador.getLogin() + " - " + registros.size()
				+ " convocados" + (cancelada ? " [CANCELADA]" : "");
	}
}
