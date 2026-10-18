package persistencia;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import entrenamiento.Asistencia;
import entrenamiento.RegistroEntrenamiento;
import entrenamiento.SesionEntrenamiento;
import global.Club;
import instalaciones.Instalacion;
import instalaciones.InstalacionConjunto;
import instalaciones.InstalacionIndividual;
import instalaciones.Modalidad;
import instalaciones.RegistroIngreso;
import instalaciones.Reserva;
import personas.Entrenador;
import personas.Persona;
import personas.Socio;

/**
 * Instalaciones y entrenamiento. Referencia deportes, socios y entrenadores por clave.
 * instalaciones.txt:            INDIVIDUAL;codigo;nombre;deporte;capacidad;activa;techada | CONJUNTO;codigo;nombre;deporte;capacidad;activa
 * reservas.txt:                 id;socio;instalacion;inicio;numeroJugadores;modalidad;cancelada
 * sesiones.txt:                 id;entrenador;instalacion;inicio;duracionMinutos;cancelada;convocados
 * registros_entrenamiento.txt:  idSesion;socio;asistencia;nota;comentario  (vacio = sin registrar)
 * ingresos.txt:                 socio;instalacion;entrada;salida  (salida vacia = sigue adentro)
 */
public class PersistenciaInstalaciones {

	public static final String INSTALACIONES = "instalaciones.txt";
	public static final String RESERVAS = "reservas.txt";
	public static final String SESIONES = "sesiones.txt";
	public static final String REGISTROS = "registros_entrenamiento.txt";
	public static final String INGRESOS = "ingresos.txt";

	public void cargar(Club club, File carpeta) {
		ArchivoTexto.procesar(new File(carpeta, INSTALACIONES), c -> {
			ArchivoTexto.exigirCampos(c, 6);
			Instalacion instalacion;
			if (c[0].equals("INDIVIDUAL")) {
				ArchivoTexto.exigirCampos(c, 7);
				instalacion = new InstalacionIndividual(c[1], c[2], club.buscarDeporteIndividual(c[3]), Integer.parseInt(c[4]),
						ArchivoTexto.leerBooleano(c[6]));
			} else if (c[0].equals("CONJUNTO")) {
				instalacion = new InstalacionConjunto(c[1], c[2], club.buscarDeporteConjunto(c[3]), Integer.parseInt(c[4]));
			} else {
				throw new IllegalArgumentException("tipo de instalacion desconocido: " + c[0]);
			}
			if (!ArchivoTexto.leerBooleano(c[5])) {
				instalacion.desactivar();
			}
			club.agregarInstalacion(instalacion);
		});

		ArchivoTexto.procesar(new File(carpeta, RESERVAS), c -> {
			ArchivoTexto.exigirCampos(c, 7);
			Reserva r = new Reserva(c[0], club.buscarSocio(c[1]), club.buscarInstalacionIndividual(c[2]),
					LocalDateTime.parse(c[3]), Integer.parseInt(c[4]), Modalidad.valueOf(c[5]));
			r.restaurarCancelada(ArchivoTexto.leerBooleano(c[6]));
			club.agregarReserva(r);
		});

		ArchivoTexto.procesar(new File(carpeta, SESIONES), c -> {
			ArchivoTexto.exigirCampos(c, 7);
			Persona entrenador = club.buscarPersona(c[1]);
			if (!(entrenador instanceof Entrenador)) {
				throw new IllegalArgumentException(c[1] + " no es entrenador");
			}
			List<Socio> convocados = new ArrayList<>();
			for (String login : ArchivoTexto.separarLista(c[6])) {
				convocados.add(club.buscarSocio(login));
			}
			SesionEntrenamiento s = new SesionEntrenamiento(c[0], (Entrenador) entrenador, club.buscarInstalacion(c[2]),
					LocalDateTime.parse(c[3]), Integer.parseInt(c[4]), convocados);
			s.restaurarCancelada(ArchivoTexto.leerBooleano(c[5]));
			club.agregarSesion(s);
		});

		ArchivoTexto.procesar(new File(carpeta, REGISTROS), c -> {
			ArchivoTexto.exigirCampos(c, 5);
			RegistroEntrenamiento r = club.buscarSesion(c[0]).buscarRegistro(club.buscarSocio(c[1]));
			if (r == null) {
				throw new IllegalArgumentException(c[1] + " no esta convocado a " + c[0]);
			}
			r.restaurar(c[2].isEmpty() ? null : Asistencia.valueOf(c[2]), c[3].isEmpty() ? null : Integer.parseInt(c[3]), c[4]);
		});

		ArchivoTexto.procesar(new File(carpeta, INGRESOS), c -> {
			ArchivoTexto.exigirCampos(c, 4);
			RegistroIngreso r = new RegistroIngreso(club.buscarSocio(c[0]), club.buscarInstalacion(c[1]), LocalDateTime.parse(c[2]));
			if (!c[3].isEmpty()) {
				r.registrarSalida(LocalDateTime.parse(c[3]));
			}
			club.agregarIngreso(r);
		});
	}

	public void salvar(Club club, File carpeta) {
		List<String> instalaciones = new ArrayList<>();
		List<String> reservas = new ArrayList<>();
		List<String> sesiones = new ArrayList<>();
		List<String> registros = new ArrayList<>();
		List<String> ingresos = new ArrayList<>();

		for (Instalacion i : club.getInstalaciones()) {
			if (i instanceof InstalacionIndividual) {
				instalaciones.add(ArchivoTexto.unir("INDIVIDUAL", i.getCodigo(), i.getNombre(), i.getDeporte().getNombre(),
						i.getCapacidad(), i.estaActiva(), ((InstalacionIndividual) i).estaTechada()));
			} else {
				instalaciones.add(ArchivoTexto.unir("CONJUNTO", i.getCodigo(), i.getNombre(), i.getDeporte().getNombre(),
						i.getCapacidad(), i.estaActiva()));
			}
		}
		for (Reserva r : club.getReservas()) {
			reservas.add(ArchivoTexto.unir(r.getId(), r.getSocio().getLogin(), r.getInstalacion().getCodigo(), r.getInicio(),
					r.getNumeroJugadores(), r.getModalidad(), r.estaCancelada()));
		}
		for (SesionEntrenamiento s : club.getSesiones()) {
			List<String> convocados = new ArrayList<>();
			for (Socio socio : s.getConvocados()) {
				convocados.add(socio.getLogin());
			}
			sesiones.add(ArchivoTexto.unir(s.getId(), s.getEntrenador().getLogin(), s.getInstalacion().getCodigo(),
					s.getInicio(), s.getDuracionMinutos(), s.estaCancelada(), ArchivoTexto.unirLista(convocados)));
			for (RegistroEntrenamiento r : s.getRegistros()) {
				if (r.getAsistencia() != null) {
					registros.add(ArchivoTexto.unir(s.getId(), r.getSocio().getLogin(), r.getAsistencia(),
							r.getNota() == null ? "" : r.getNota(), r.getComentario() == null ? "" : r.getComentario()));
				}
			}
		}
		for (RegistroIngreso r : club.getIngresos()) {
			ingresos.add(ArchivoTexto.unir(r.getSocio().getLogin(), r.getInstalacion().getCodigo(), r.getEntrada(),
					r.getSalida() == null ? "" : r.getSalida()));
		}

		ArchivoTexto.escribir(new File(carpeta, INSTALACIONES), "INDIVIDUAL;codigo;nombre;deporte;capacidad;activa;techada | CONJUNTO;codigo;nombre;deporte;capacidad;activa", instalaciones);
		ArchivoTexto.escribir(new File(carpeta, RESERVAS), "id;socio;instalacion;inicio;numeroJugadores;modalidad;cancelada", reservas);
		ArchivoTexto.escribir(new File(carpeta, SESIONES), "id;entrenador;instalacion;inicio;duracionMinutos;cancelada;convocados", sesiones);
		ArchivoTexto.escribir(new File(carpeta, REGISTROS), "idSesion;socio;asistencia;nota;comentario", registros);
		ArchivoTexto.escribir(new File(carpeta, INGRESOS), "socio;instalacion;entrada;salida", ingresos);
	}
}
