package administracion;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import personas.Empleado;

// Pide nuevas horas para un turno (el dia se mantiene). La revisa el administrador.
public class SolicitudCambioTurno {

	//Atributos
	private final String id;
	private final Turno turno;
	private final LocalTime nuevaHoraInicio;
	private final LocalTime nuevaHoraFin;
	private EstadoSolicitud estado;

	//Constructor
	public SolicitudCambioTurno(String id, Turno turno, LocalTime nuevaHoraInicio, LocalTime nuevaHoraFin) {
		Turno.validarHorario(turno.getDia(), nuevaHoraInicio, nuevaHoraFin);
		this.id = id;
		this.turno = turno;
		this.nuevaHoraInicio = nuevaHoraInicio;
		this.nuevaHoraFin = nuevaHoraFin;
		this.estado = EstadoSolicitud.PENDIENTE;
	}

	//Getters
	public String getId() {
		return id;
	}

	public Turno getTurno() {
		return turno;
	}

	public Empleado getEmpleado() {
		return turno.getEmpleado();
	}

	public LocalTime getNuevaHoraInicio() {
		return nuevaHoraInicio;
	}

	public LocalTime getNuevaHoraFin() {
		return nuevaHoraFin;
	}

	public EstadoSolicitud getEstado() {
		return estado;
	}

	//Metodos

	public void aprobar(List<Turno> turnosDelClub) {
		validarPendiente();
		List<Turno> simulados = new ArrayList<>();
		for (Turno t : turnosDelClub) {
			if (t == turno) {
				Turno turnoSimulado = new Turno(
					t.getId(),
					t.getEmpleado(),
					t.getDia(),
					nuevaHoraInicio,
					nuevaHoraFin);

				simulados.add(turnoSimulado);
			} else {{}
				simulados.add(t);
			}
		}
		String franja = Turno.buscarFranjaSinCobertura(simulados, turno.getDia());
		if (franja != null) {
			throw new IllegalStateException("El cambio deja sin cobertura minima la franja " + franja);
		}
		turno.cambiarHorario(nuevaHoraInicio, nuevaHoraFin);
		estado = EstadoSolicitud.APROBADA;
	}

	public void rechazar() {
		validarPendiente();
		estado = EstadoSolicitud.RECHAZADA;
	}

	private void validarPendiente() {
		if (estado != EstadoSolicitud.PENDIENTE) {
			throw new IllegalStateException("La solicitud " + id + " ya fue resuelta (" + estado + ")");
		}
	}

	
	public void restaurarEstado(EstadoSolicitud estado) {
		this.estado = estado;
	}

	@Override
	public String toString() {
		return "[" + id + "] " + getEmpleado().getLogin() + " turno " + turno.getId() + " (" + turno.getDia() + ") -> "
				+ nuevaHoraInicio + " - " + nuevaHoraFin + " | " + estado;
	}
}
