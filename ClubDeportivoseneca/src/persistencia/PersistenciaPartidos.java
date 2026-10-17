package persistencia;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import competencias.Competencia;
import competencias.Fase;
import competencias.FormatoCompetencia;
import competencias.InscripcionCompetencia;
import deportes.Categoria;
import deportes.DeporteConjunto;
import global.Club;
import partidos.EstadisticaBaloncesto;
import partidos.EstadisticaFutbol;
import partidos.EstadisticaPartido;
import partidos.EstadisticaVoleibol;
import partidos.EstadoPartido;
import partidos.Partido;
import partidos.PartidoCompetencia;
import partidos.PartidoInterno;
import personas.Socio;

/**
 * Partidos internos y competencias. Referencia equipos y socios por clave.
 * partidos_internos.txt:          id;deporte;local;visitante;fecha;estado;marcadorLocal;marcadorVisitante;alineacionLocal;alineacionVisitante
 * competencias.txt:               nombre;entidad;temporada;deporte;categoria;formato;nominaMinima;amarillasParaSuspension;finalizada;instituciones
 * inscripciones_competencia.txt:  competencia;temporada;equipo;nomina
 * partidos_competencia.txt:       id;competencia;temporada;equipo;rival;escenario;fecha;fase;estado;marcadorClub;marcadorRival;alineacion
 * estadisticas.txt:               idPartido;deporte;login;valores...
 */
public class PersistenciaPartidos {

	public static final String INTERNOS = "partidos_internos.txt";
	public static final String COMPETENCIAS = "competencias.txt";
	public static final String INSCRIPCIONES = "inscripciones_competencia.txt";
	public static final String PARTIDOS_COMPETENCIA = "partidos_competencia.txt";
	public static final String ESTADISTICAS = "estadisticas.txt";

	public void cargar(Club club, File carpeta) {
		ArchivoTexto.procesar(new File(carpeta, INTERNOS), c -> {
			ArchivoTexto.exigirCampos(c, 10);
			DeporteConjunto deporte = club.buscarDeporteConjunto(c[1]);
			PartidoInterno p = new PartidoInterno(c[0], deporte, club.buscarEquipo(c[1], c[2]),
					club.buscarEquipo(c[1], c[3]), LocalDate.parse(c[4]));
			p.restaurarEstado(EstadoPartido.valueOf(c[5]), Integer.parseInt(c[6]), Integer.parseInt(c[7]));
			p.restaurarAlineaciones(socios(club, c[8]), socios(club, c[9]));
			deporte.agregarPartidoGuardado(p);
		});

		ArchivoTexto.procesar(new File(carpeta, COMPETENCIAS), c -> {
			ArchivoTexto.exigirCampos(c, 10);
			Competencia comp = new Competencia(c[0], c[1], Integer.parseInt(c[2]), club.buscarDeporteConjunto(c[3]),
					Categoria.valueOf(c[4]), FormatoCompetencia.valueOf(c[5]), ArchivoTexto.separarLista(c[9]),
					Integer.parseInt(c[6]), Integer.parseInt(c[7]));
			if (ArchivoTexto.leerBooleano(c[8])) {
				comp.finalizar();
			}
			club.agregarCompetencia(comp);
		});

		ArchivoTexto.procesar(new File(carpeta, INSCRIPCIONES), c -> {
			ArchivoTexto.exigirCampos(c, 4);
			Competencia comp = club.buscarCompetencia(c[0], Integer.parseInt(c[1]));
			comp.agregarInscripcionGuardada(club.buscarEquipo(comp.getDeporte().getNombre(), c[2]), socios(club, c[3]));
		});

		ArchivoTexto.procesar(new File(carpeta, PARTIDOS_COMPETENCIA), c -> {
			ArchivoTexto.exigirCampos(c, 12);
			Competencia comp = club.buscarCompetencia(c[1], Integer.parseInt(c[2]));
			PartidoCompetencia p = new PartidoCompetencia(c[0], comp, club.buscarEquipo(comp.getDeporte().getNombre(), c[3]),
					c[4], c[5], LocalDate.parse(c[6]), Fase.valueOf(c[7]));
			p.restaurarEstado(EstadoPartido.valueOf(c[8]), Integer.parseInt(c[9]), Integer.parseInt(c[10]));
			p.restaurarAlineacion(socios(club, c[11]));
			comp.agregarPartidoGuardado(p);
		});

		ArchivoTexto.procesar(new File(carpeta, ESTADISTICAS), c -> {
			ArchivoTexto.exigirCampos(c, 3);
			club.buscarPartido(c[0]).restaurarEstadistica(crearEstadistica(c));
		});
	}

	private static EstadisticaPartido crearEstadistica(String[] c) {
		String deporte = c[1];
		String login = c[2];
		if (deporte.equals(EstadisticaFutbol.DEPORTE)) {
			ArchivoTexto.exigirCampos(c, 7);
			return new EstadisticaFutbol(login, Integer.parseInt(c[3]), Integer.parseInt(c[4]),
					Integer.parseInt(c[5]), ArchivoTexto.leerBooleano(c[6]));
		}
		ArchivoTexto.exigirCampos(c, 5);
		if (deporte.equals(EstadisticaBaloncesto.DEPORTE)) {
			return new EstadisticaBaloncesto(login, Integer.parseInt(c[3]), Integer.parseInt(c[4]));
		}
		if (deporte.equals(EstadisticaVoleibol.DEPORTE)) {
			return new EstadisticaVoleibol(login, Integer.parseInt(c[3]), Integer.parseInt(c[4]));
		}
		throw new IllegalArgumentException("estadistica de deporte desconocido: " + deporte);
	}

	private static List<Socio> socios(Club club, String campo) {
		List<Socio> socios = new ArrayList<>();
		for (String login : ArchivoTexto.separarLista(campo)) {
			socios.add(club.buscarSocio(login));
		}
		return socios;
	}

	private static String logins(List<Socio> socios) {
		List<String> logins = new ArrayList<>();
		for (Socio s : socios) {
			logins.add(s.getLogin());
		}
		return ArchivoTexto.unirLista(logins);
	}

	public void salvar(Club club, File carpeta) {
		List<String> internos = new ArrayList<>();
		List<String> competencias = new ArrayList<>();
		List<String> inscripciones = new ArrayList<>();
		List<String> partidosCompetencia = new ArrayList<>();
		List<String> estadisticas = new ArrayList<>();

		for (DeporteConjunto d : club.getDeportesConjunto()) {
			for (PartidoInterno p : d.getPartidos()) {
				internos.add(ArchivoTexto.unir(p.getId(), d.getNombre(), p.getLocal().getNombre(),
						p.getVisitante().getNombre(), p.getFecha(), p.getEstado(), p.getMarcadorLocal(),
						p.getMarcadorVisitante(), logins(p.getAlineacionLocal()), logins(p.getAlineacionVisitante())));
			}
		}
		for (Competencia comp : club.getCompetencias()) {
			competencias.add(ArchivoTexto.unir(comp.getNombre(), comp.getEntidadOrganizadora(), comp.getTemporada(),
					comp.getDeporte().getNombre(), comp.getCategoria(), comp.getFormato(), comp.getNominaMinima(),
					comp.getAmarillasParaSuspension(), !comp.estaEnCurso(), ArchivoTexto.unirLista(comp.getInstituciones())));
			for (InscripcionCompetencia i : comp.getInscripciones()) {
				inscripciones.add(ArchivoTexto.unir(comp.getNombre(), comp.getTemporada(), i.getEquipo().getNombre(),
						logins(i.getNomina())));
			}
			for (PartidoCompetencia p : comp.getPartidos()) {
				partidosCompetencia.add(ArchivoTexto.unir(p.getId(), comp.getNombre(), comp.getTemporada(),
						p.getEquipo().getNombre(), p.getInstitucionRival(), p.getEscenario(), p.getFecha(), p.getFase(),
						p.getEstado(), p.getMarcadorLocal(), p.getMarcadorVisitante(), logins(p.getAlineados())));
			}
		}
		for (Partido p : club.getTodosLosPartidos()) {
			for (EstadisticaPartido e : p.getEstadisticas()) {
				estadisticas.add(p.getId() + ArchivoTexto.SEPARADOR + e.getNombreDeporte() + ArchivoTexto.SEPARADOR
						+ e.getLoginJugador() + ArchivoTexto.SEPARADOR + String.join(ArchivoTexto.SEPARADOR, e.getValores()));
			}
		}

		ArchivoTexto.escribir(new File(carpeta, INTERNOS), "id;deporte;local;visitante;fecha;estado;marcadorLocal;marcadorVisitante;alineacionLocal;alineacionVisitante", internos);
		ArchivoTexto.escribir(new File(carpeta, COMPETENCIAS), "nombre;entidad;temporada;deporte;categoria;formato;nominaMinima;amarillasParaSuspension;finalizada;instituciones", competencias);
		ArchivoTexto.escribir(new File(carpeta, INSCRIPCIONES), "competencia;temporada;equipo;nomina", inscripciones);
		ArchivoTexto.escribir(new File(carpeta, PARTIDOS_COMPETENCIA), "id;competencia;temporada;equipo;rival;escenario;fecha;fase;estado;marcadorClub;marcadorRival;alineacion", partidosCompetencia);
		ArchivoTexto.escribir(new File(carpeta, ESTADISTICAS), "idPartido;deporte;login;valores (Futbol: goles;asistencias;amarillas;roja | Baloncesto: puntos;rebotes | Voleibol: aces;bloqueos)", estadisticas);
	}
}
