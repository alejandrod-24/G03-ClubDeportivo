package administracion;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import personas.Empleado;
import personas.Entrenador;
import personas.Fisioterapeuta;

// horario supuesto: lunes a sabado de 8:00 a 20:00. 
public class Turno {

	public static final LocalTime APERTURA = LocalTime.of(8, 0);
	public static final LocalTime CIERRE = LocalTime.of(20, 0);
	public static final int MINIMO_ENTRENADORES = 2;
	public static final int MINIMO_FISIOTERAPEUTAS = 1;

	//Atributos
	private final String id;
	private final Empleado empleado;
	private final DayOfWeek dia;
	private LocalTime horaInicio;
	private LocalTime horaFin;

	//Constructor
	public Turno(String id, Empleado empleado, DayOfWeek dia, LocalTime horaInicio, LocalTime horaFin) {
		validarHorario(dia, horaInicio, horaFin);
		this.id = id;
		this.empleado = empleado;
		this.dia = dia;
		this.horaInicio = horaInicio;
		this.horaFin = horaFin;
	}

	//Getters
	public String getId() {
		return id;
	}

	public Empleado getEmpleado() {
		return empleado;
	}

	public DayOfWeek getDia() {
		return dia;
	}

	public LocalTime getHoraInicio() {
		return horaInicio;
	}

	public LocalTime getHoraFin() {
		return horaFin;
	}

	//Metodos
	public static void validarHorario(DayOfWeek dia, LocalTime inicio, LocalTime fin) {
		if (dia == null || inicio == null || fin == null) {
			throw new IllegalArgumentException("El turno debe tener dia, hora de inicio y hora de fin");
		}
		if (dia == DayOfWeek.SUNDAY) {
			throw new IllegalArgumentException("El club no atiende los domingos");
		}
		if (inicio.isBefore(APERTURA) || fin.isAfter(CIERRE) || !inicio.isBefore(fin)) {
			throw new IllegalArgumentException("El turno debe estar dentro del horario de atencion (" + APERTURA + " a " + CIERRE + ")");
		}
	}

	public boolean seCruzaCon(DayOfWeek otroDia, LocalTime inicio, LocalTime fin) {
		return dia == otroDia && horaInicio.isBefore(fin) && inicio.isBefore(horaFin);
	}

	void cambiarHorario(LocalTime inicio, LocalTime fin) {
		validarHorario(dia, inicio, fin);
		this.horaInicio = inicio;
		this.horaFin = fin;
	}

	//Recorre el dia por franjas (cortes en cada inicio y fin de turno) y devuelve la primera
	//que no tiene la cobertura minima, o null si todo el dia esta cubierto. Solo cuentan empleados activos.
	public static String buscarFranjaSinCobertura(List<Turno> turnos, DayOfWeek dia) {
		List<LocalTime> cortes = new ArrayList<>(List.of(APERTURA, CIERRE));
		for (Turno t : turnos) {
			if (t.dia == dia) {
				cortes.add(t.horaInicio);
				cortes.add(t.horaFin);
			}
		}
		cortes.sort(null);
		for (int i = 0; i + 1 < cortes.size(); i++) {
			LocalTime desde = cortes.get(i);
			LocalTime hasta = cortes.get(i + 1);
			if (desde.isBefore(hasta)) {
				int entrenadores = 0;
				int fisioterapeutas = 0;
				for (Turno t : turnos) {
					if (t.dia == dia && t.empleado.estaActivo() && !t.horaInicio.isAfter(desde) && !t.horaFin.isBefore(hasta)) {
						if (t.empleado instanceof Entrenador) {
							entrenadores++;
						} else if (t.empleado instanceof Fisioterapeuta) {
							fisioterapeutas++;
						}
					}
				}
				if (entrenadores < MINIMO_ENTRENADORES || fisioterapeutas < MINIMO_FISIOTERAPEUTAS) {
					return dia + " de " + desde + " a " + hasta + " (" + entrenadores + " entrenadores, "
							+ fisioterapeutas + " fisioterapeutas)";
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "[" + id + "] " + empleado.getLogin() + " " + dia + " " + horaInicio + " - " + horaFin;
	}
}
