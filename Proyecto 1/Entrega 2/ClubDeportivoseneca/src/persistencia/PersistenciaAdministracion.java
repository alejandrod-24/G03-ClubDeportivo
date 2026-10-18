package persistencia;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import administracion.EstadoSolicitud;
import administracion.PagoMensualidad;
import administracion.SolicitudCambioTurno;
import administracion.SugerenciaProducto;
import administracion.Turno;
import global.Club;

/**
 * turnos.txt:             id;empleado;dia;horaInicio;horaFin
 * solicitudes_turno.txt:  id;turno;nuevaHoraInicio;nuevaHoraFin;estado
 * sugerencias.txt:        id;empleado;nombre;categoria;precioSugerido;estado;alergenos
 * pagos_mensualidad.txt:  id;socio;periodo;fecha;monto
 */
public class PersistenciaAdministracion {

	public static final String TURNOS = "turnos.txt";
	public static final String SOLICITUDES = "solicitudes_turno.txt";
	public static final String SUGERENCIAS = "sugerencias.txt";
	public static final String PAGOS = "pagos_mensualidad.txt";

	public void cargar(Club club, File carpeta) {
		ArchivoTexto.procesar(new File(carpeta, TURNOS), c -> {
			ArchivoTexto.exigirCampos(c, 5);
			club.agregarTurno(new Turno(c[0], club.buscarEmpleado(c[1]), DayOfWeek.valueOf(c[2]),
					LocalTime.parse(c[3]), LocalTime.parse(c[4])));
		});

		ArchivoTexto.procesar(new File(carpeta, SOLICITUDES), c -> {
			ArchivoTexto.exigirCampos(c, 5);
			SolicitudCambioTurno s = new SolicitudCambioTurno(c[0], club.buscarTurno(c[1]), LocalTime.parse(c[2]),
					LocalTime.parse(c[3]));
			s.restaurarEstado(EstadoSolicitud.valueOf(c[4]));
			club.agregarSolicitud(s);
		});

		ArchivoTexto.procesar(new File(carpeta, SUGERENCIAS), c -> {
			ArchivoTexto.exigirCampos(c, 7);
			SugerenciaProducto s = new SugerenciaProducto(c[0], club.buscarEmpleado(c[1]), c[2], c[3],
					Long.parseLong(c[4]), ArchivoTexto.separarLista(c[6]));
			s.restaurarEstado(EstadoSolicitud.valueOf(c[5]));
			club.agregarSugerencia(s);
		});

		ArchivoTexto.procesar(new File(carpeta, PAGOS), c -> {
			ArchivoTexto.exigirCampos(c, 5);
			club.agregarPago(new PagoMensualidad(c[0], club.buscarSocio(c[1]), YearMonth.parse(c[2]),
					LocalDate.parse(c[3]), Long.parseLong(c[4])));
		});
	}

	public void salvar(Club club, File carpeta) {
		List<String> turnos = new ArrayList<>();
		List<String> solicitudes = new ArrayList<>();
		List<String> sugerencias = new ArrayList<>();
		List<String> pagos = new ArrayList<>();

		for (Turno t : club.getTurnos()) {
			turnos.add(ArchivoTexto.unir(t.getId(), t.getEmpleado().getLogin(), t.getDia(), t.getHoraInicio(), t.getHoraFin()));
		}
		for (SolicitudCambioTurno s : club.getSolicitudes()) {
			solicitudes.add(ArchivoTexto.unir(s.getId(), s.getTurno().getId(), s.getNuevaHoraInicio(), s.getNuevaHoraFin(),
					s.getEstado()));
		}
		for (SugerenciaProducto s : club.getSugerencias()) {
			sugerencias.add(ArchivoTexto.unir(s.getId(), s.getEmpleado().getLogin(), s.getNombre(), s.getCategoria(),
					s.getPrecioSugerido(), s.getEstado(), ArchivoTexto.unirLista(s.getAlergenos())));
		}
		for (PagoMensualidad p : club.getPagos()) {
			pagos.add(ArchivoTexto.unir(p.getId(), p.getSocio().getLogin(), p.getPeriodo(), p.getFecha(), p.getMonto()));
		}

		ArchivoTexto.escribir(new File(carpeta, TURNOS), "id;empleado;dia;horaInicio;horaFin", turnos);
		ArchivoTexto.escribir(new File(carpeta, SOLICITUDES), "id;turno;nuevaHoraInicio;nuevaHoraFin;estado", solicitudes);
		ArchivoTexto.escribir(new File(carpeta, SUGERENCIAS), "id;empleado;nombre;categoria;precioSugerido;estado;alergenos", sugerencias);
		ArchivoTexto.escribir(new File(carpeta, PAGOS), "id;socio;periodo;fecha;monto", pagos);
	}
}
