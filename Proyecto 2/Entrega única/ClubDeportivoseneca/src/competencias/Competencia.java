package competencias;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import deportes.Categoria;
import deportes.DeporteConjunto;
import deportes.Equipo;
import global.Reloj;
import partidos.EstadisticaPartido;
import partidos.EstadoPartido;
import partidos.Partido;
import partidos.PartidoCompetencia;
import personas.Socio;

public class Competencia {

	public static final String CERROS = "Torneo de los Cerros";
	public static final String ASCUN = "ASCUN";

	//Atributos
	private final String nombre;
	private final String entidadOrganizadora;
	private final int temporada;
	private final DeporteConjunto deporte;
	private final Categoria categoria;
	private final FormatoCompetencia formato;
	private final List<String> instituciones;
	private final int nominaMinima;
	//Cada cuantas amarillas acumuladas en la competencia se suspende (0 = no aplica)
	private final int amarillasParaSuspension;
	private boolean finalizada;
	private final List<InscripcionCompetencia> inscripciones;
	private final List<PartidoCompetencia> partidos;

	//Constructor
	public Competencia(String nombre, String entidadOrganizadora, int temporada, DeporteConjunto deporte,
			Categoria categoria, FormatoCompetencia formato, List<String> instituciones,
			int nominaMinima, int amarillasParaSuspension) {
		validarTexto(nombre, "El nombre");
		validarTexto(entidadOrganizadora, "La entidad organizadora");
		if (temporada <= 0) {
			throw new IllegalArgumentException("La temporada no es valida");
		}
		if (deporte == null || categoria == null || formato == null) {
			throw new IllegalArgumentException("Deporte, categoria y formato son obligatorios");
		}
		if (instituciones == null || instituciones.isEmpty()) {
			throw new IllegalArgumentException("Debe haber al menos una institucion participante");
		}
		List<String> sinRepetir = new ArrayList<>();
		for (String institucion : instituciones) {
			validarTexto(institucion, "La institucion");
			for (String otra : sinRepetir) {
				if (otra.equalsIgnoreCase(institucion)) {
					throw new IllegalArgumentException("La institucion " + institucion + " esta repetida");
				}
			}
			sinRepetir.add(institucion);
		}
		this.nombre = nombre;
		this.entidadOrganizadora = entidadOrganizadora;
		this.temporada = temporada;
		this.deporte = deporte;
		this.categoria = categoria;
		this.formato = formato;
		this.instituciones = sinRepetir;
		this.nominaMinima = Math.max(nominaMinima, deporte.getMinimoJugadores());
		this.amarillasParaSuspension = amarillasParaSuspension;
		this.finalizada = false;
		this.inscripciones = new ArrayList<>();
		this.partidos = new ArrayList<>();
	}

	//Configuraciones previstas de los dos circuitos
	public static Competencia torneoDeLosCerros(String nombre, int temporada, DeporteConjunto deporte,
			Categoria categoria, List<String> instituciones) {
		return new Competencia(nombre, CERROS, temporada, deporte, categoria, FormatoCompetencia.LIGA,
				instituciones, deporte.getMinimoJugadores(), 3);
	}

	public static Competencia ascun(String nombre, int temporada, DeporteConjunto deporte,
			Categoria categoria, List<String> instituciones) {
		return new Competencia(nombre, ASCUN, temporada, deporte, categoria, FormatoCompetencia.GRUPOS_Y_ELIMINACION,
				instituciones, deporte.getMinimoJugadores() + 1, 2);
	}

	private static void validarTexto(String texto, String campo) {
		if (texto == null || texto.isBlank()) {
			throw new IllegalArgumentException(campo + " no puede estar vacio");
		}
		if (texto.contains(";") || texto.contains(",")) {
			throw new IllegalArgumentException(campo + " no puede contener ';' ni ','");
		}
	}

	//Getters
	public String getNombre() {
		return nombre;
	}

	public String getEntidadOrganizadora() {
		return entidadOrganizadora;
	}

	public int getTemporada() {
		return temporada;
	}

	public DeporteConjunto getDeporte() {
		return deporte;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public FormatoCompetencia getFormato() {
		return formato;
	}

	public List<String> getInstituciones() {
		return Collections.unmodifiableList(instituciones);
	}

	public int getNominaMinima() {
		return nominaMinima;
	}

	public int getAmarillasParaSuspension() {
		return amarillasParaSuspension;
	}

	public boolean estaEnCurso() {
		return !finalizada;
	}

	public List<InscripcionCompetencia> getInscripciones() {
		return Collections.unmodifiableList(inscripciones);
	}

	public List<PartidoCompetencia> getPartidos() {
		return Collections.unmodifiableList(partidos);
	}

	public void finalizar() {
		if (finalizada) {
			throw new IllegalStateException("La competencia ya esta finalizada");
		}
		finalizada = true;
	}

	// -------------------- Inscripcion y nomina --------------------

	public InscripcionCompetencia buscarInscripcion(Equipo equipo) {
		for (InscripcionCompetencia inscripcion : inscripciones) {
			if (inscripcion.getEquipo() == equipo) {
				return inscripcion;
			}
		}
		return null;
	}

	//Un socio "hace parte" de la competencia si esta en alguna nomina
	public boolean tieneEnNomina(Socio socio) {
		for (InscripcionCompetencia inscripcion : inscripciones) {
			if (inscripcion.estaEnNomina(socio)) {
				return true;
			}
		}
		return false;
	}

	public InscripcionCompetencia inscribirEquipo(Equipo equipo) {
		if (finalizada) {
			throw new IllegalStateException("La competencia ya finalizo");
		}
		if (equipo.getDeporte() != deporte) {
			throw new IllegalArgumentException("La competencia es de " + deporte.getNombre()
					+ " y el equipo de " + equipo.getDeporte().getNombre());
		}
		if (equipo.getCategoria() != categoria) {
			throw new IllegalArgumentException("La competencia es para " + categoria
					+ " y el equipo es " + equipo.getCategoria());
		}
		if (!equipo.estaActivo()) {
			throw new IllegalStateException("El equipo esta inactivo");
		}
		if (buscarInscripcion(equipo) != null) {
			throw new IllegalStateException(equipo.getNombre() + " ya esta inscrito en " + nombre);
		}
		if (equipo.getJugadores().size() < nominaMinima) {
			throw new IllegalStateException(equipo.getNombre() + " no alcanza la nomina minima de "
					+ nominaMinima + " jugadores");
		}
		InscripcionCompetencia inscripcion = new InscripcionCompetencia(this, equipo);
		inscripciones.add(inscripcion);
		return inscripcion;
	}

	public void definirNomina(Equipo equipo, List<Socio> nomina) {
		InscripcionCompetencia inscripcion = buscarInscripcion(equipo);
		if (inscripcion == null) {
			throw new IllegalStateException(equipo.getNombre() + " no esta inscrito en " + nombre);
		}
		if (nomina.size() > equipo.getCupoMaximo()) {
			throw new IllegalArgumentException("La nomina (" + nomina.size() + ") supera el cupo del equipo ("
					+ equipo.getCupoMaximo() + ")");
		}
		if (nomina.size() < nominaMinima) {
			throw new IllegalArgumentException("La nomina debe tener al menos " + nominaMinima + " jugadores");
		}
		List<Socio> vistos = new ArrayList<>();
		for (Socio socio : nomina) {
			if (!equipo.estaInscrito(socio)) {
				throw new IllegalArgumentException(socio.getLogin() + " no pertenece a " + equipo.getNombre());
			}
			if (!socio.estaActivo() || socio.getCategoria() != categoria) {
				throw new IllegalArgumentException(socio.getLogin() + " no esta habilitado para esta competencia");
			}
			if (vistos.contains(socio)) {
				throw new IllegalArgumentException(socio.getLogin() + " esta repetido en la nomina");
			}
			vistos.add(socio);
		}
		inscripcion.setNomina(nomina);
	}

	//Para la persistencia
	public void agregarInscripcionGuardada(Equipo equipo, List<Socio> nomina) {
		InscripcionCompetencia inscripcion = new InscripcionCompetencia(this, equipo);
		inscripcion.setNomina(nomina);
		inscripciones.add(inscripcion);
	}

	// -------------------- Partidos --------------------

	public PartidoCompetencia programarPartido(String id, Equipo equipo, String rival, String escenario,
			LocalDate fecha, Fase fase) {
		if (finalizada) {
			throw new IllegalStateException("La competencia ya finalizo");
		}
		InscripcionCompetencia inscripcion = buscarInscripcion(equipo);
		if (inscripcion == null) {
			throw new IllegalStateException(equipo.getNombre() + " no esta inscrito en " + nombre);
		}
		if (inscripcion.getNomina().isEmpty()) {
			throw new IllegalStateException("Primero debe definirse la nomina de " + equipo.getNombre());
		}
		if (!esInstitucionParticipante(rival)) {
			throw new IllegalArgumentException(rival + " no participa en " + nombre);
		}
		if (fase == null || !fase.esValidaPara(formato)) {
			throw new IllegalArgumentException("La fase " + fase + " no corresponde al formato " + formato);
		}
		if (fecha == null || fecha.isBefore(Reloj.hoy())) {
			throw new IllegalArgumentException("La fecha del partido no puede ser pasada");
		}
		PartidoCompetencia partido = new PartidoCompetencia(id, this, equipo, rival, escenario, fecha, fase);
		partidos.add(partido);
		return partido;
	}

	//Para la persistencia
	public void agregarPartidoGuardado(PartidoCompetencia partido) {
		partidos.add(partido);
	}

	private boolean esInstitucionParticipante(String institucion) {
		for (String i : instituciones) {
			if (i.equalsIgnoreCase(institucion)) {
				return true;
			}
		}
		return false;
	}

	public List<PartidoCompetencia> getPartidosDe(Equipo equipo) {
		List<PartidoCompetencia> resultado = new ArrayList<>();
		for (PartidoCompetencia p : partidos) {
			if (p.getEquipo() == equipo) {
				resultado.add(p);
			}
		}
		return resultado;
	}

	public boolean tienePartidosProgramados(Equipo equipo) {
		for (PartidoCompetencia p : getPartidosDe(equipo)) {
			if (p.getEstado() == EstadoPartido.PROGRAMADO) {
				return true;
			}
		}
		return false;
	}

	// -------------------- Sanciones --------------------

	//Suspendido si en el partido anterior del equipo recibio roja o 2 amarillas,
	//o si con ese partido sus amarillas acumuladas llegaron a un multiplo de amarillasParaSuspension
	public boolean estaSuspendido(Socio socio, Equipo equipo, LocalDate fechaPartido) {
		List<PartidoCompetencia> delEquipo = getPartidosDe(equipo);
		Partido anterior = Partido.ultimoJugadoAntesDe(delEquipo, fechaPartido);
		if (anterior == null) {
			return false;
		}
		if (Partido.fueExpulsadoEn(anterior, socio)) {
			return true;
		}
		if (amarillasParaSuspension <= 0) {
			return false;
		}
		int total = 0;
		for (PartidoCompetencia p : delEquipo) {
			if (p.getEstado() == EstadoPartido.JUGADO && !p.getFecha().isAfter(anterior.getFecha())) {
				total += amarillasDe(p, socio);
			}
		}
		int antes = total - amarillasDe(anterior, socio);
		return antes / amarillasParaSuspension < total / amarillasParaSuspension;
	}

	private static int amarillasDe(Partido partido, Socio socio) {
		EstadisticaPartido e = partido.getEstadistica(socio.getLogin());
		return e == null ? 0 : e.getTarjetasAmarillas();
	}

	// -------------------- Consultas (RF-GEN-05) --------------------

	public List<FilaPosicion> getTablaPosiciones() {
		Map<String, FilaPosicion> filas = new LinkedHashMap<>();
		for (InscripcionCompetencia inscripcion : inscripciones) {
			String n = inscripcion.getEquipo().getNombre();
			filas.put(n, new FilaPosicion(n));
		}
		for (String institucion : instituciones) {
			filas.put(institucion, new FilaPosicion(institucion));
		}
		for (PartidoCompetencia p : partidos) {
			if (p.getEstado() == EstadoPartido.JUGADO && !p.getFase().esEliminatoria()) {
				filas.get(p.getEquipo().getNombre()).registrar(p.getMarcadorLocal(), p.getMarcadorVisitante());
				filas.get(buscarNombreInstitucion(p.getInstitucionRival()))
						.registrar(p.getMarcadorVisitante(), p.getMarcadorLocal());
			}
		}
		List<FilaPosicion> tabla = new ArrayList<>(filas.values());
		tabla.sort((a, b) -> a.getPuntos() != b.getPuntos() ? b.getPuntos() - a.getPuntos()
				: b.getDiferencia() - a.getDiferencia());
		return tabla;
	}

	private String buscarNombreInstitucion(String institucion) {
		for (String i : instituciones) {
			if (i.equalsIgnoreCase(institucion)) {
				return i;
			}
		}
		return institucion;
	}

	public List<PartidoCompetencia> getCuadroEliminacion() {
		List<PartidoCompetencia> cuadro = new ArrayList<>();
		for (PartidoCompetencia p : partidos) {
			if (p.getFase().esEliminatoria()) {
				cuadro.add(p);
			}
		}
		cuadro.sort((a, b) -> a.getFase().compareTo(b.getFase()));
		return cuadro;
	}

	//Suma de anotaciones o valoraciones por jugador a lo largo de la competencia
	private Map<String, Integer> acumular(boolean usarValoracion) {
		Map<String, Integer> totales = new LinkedHashMap<>();
		for (PartidoCompetencia p : partidos) {
			for (EstadisticaPartido e : p.getEstadisticas()) {
				int valor = usarValoracion ? e.getValoracion() : e.getAnotaciones();
				totales.merge(e.getLoginJugador(), valor, Integer::sum);
			}
		}
		return totales;
	}

	private static String maximo(Map<String, Integer> totales) {
		String mejor = null;
		for (Map.Entry<String, Integer> entrada : totales.entrySet()) {
			if (mejor == null || entrada.getValue() > totales.get(mejor)) {
				mejor = entrada.getKey();
			}
		}
		return mejor == null ? "(sin datos)" : mejor + " (" + totales.get(mejor) + ")";
	}

	public String getGoleador() {
		return maximo(acumular(false));
	}

	public String getJugadorDestacado() {
		return maximo(acumular(true));
	}

	public List<String> getAmonestaciones() {
		Map<String, int[]> tarjetas = new LinkedHashMap<>();
		for (PartidoCompetencia p : partidos) {
			for (EstadisticaPartido e : p.getEstadisticas()) {
				if (e.getTarjetasAmarillas() > 0 || e.tieneTarjetaRoja()) {
					int[] t = tarjetas.computeIfAbsent(e.getLoginJugador(), k -> new int[2]);
					t[0] += e.getTarjetasAmarillas();
					t[1] += e.tieneTarjetaRoja() ? 1 : 0;
				}
			}
		}
		List<String> lineas = new ArrayList<>();
		for (Map.Entry<String, int[]> entrada : tarjetas.entrySet()) {
			lineas.add(entrada.getKey() + ": " + entrada.getValue()[0] + " amarillas, " + entrada.getValue()[1] + " rojas");
		}
		return lineas;
	}

	public String generarResumen() {
		StringBuilder sb = new StringBuilder();
		sb.append(this).append("\n");
		sb.append("Instituciones: ").append(instituciones).append("\n");
		sb.append("Nomina minima: ").append(nominaMinima).append(" | Suspension cada ")
				.append(amarillasParaSuspension).append(" amarillas acumuladas\n");
		sb.append("Equipos inscritos:\n");
		if (inscripciones.isEmpty()) {
			sb.append("  (ninguno)\n");
		}
		for (InscripcionCompetencia inscripcion : inscripciones) {
			sb.append("  ").append(inscripcion.getEquipo().getNombre()).append(" - nomina: ");
			List<String> logins = new ArrayList<>();
			for (Socio s : inscripcion.getNomina()) {
				logins.add(s.getLogin());
			}
			sb.append(logins.isEmpty() ? "(sin definir)" : logins).append("\n");
		}
		sb.append("Partidos:\n");
		if (partidos.isEmpty()) {
			sb.append("  (ninguno)\n");
		}
		for (PartidoCompetencia p : partidos) {
			sb.append("  ").append(p).append("\n");
		}
		if (formato == FormatoCompetencia.LIGA || !partidos.isEmpty()) {
			sb.append("Tabla de posiciones").append(formato == FormatoCompetencia.LIGA ? "" : " (fase de grupos)").append(":\n");
			for (FilaPosicion fila : getTablaPosiciones()) {
				sb.append("  ").append(fila).append("\n");
			}
		}
		if (formato == FormatoCompetencia.GRUPOS_Y_ELIMINACION) {
			sb.append("Cuadro de eliminacion:\n");
			List<PartidoCompetencia> cuadro = getCuadroEliminacion();
			if (cuadro.isEmpty()) {
				sb.append("  (sin partidos de eliminacion)\n");
			}
			for (PartidoCompetencia p : cuadro) {
				sb.append("  ").append(p.getFase()).append(": ").append(p.getEquipo().getNombre()).append(" vs ")
						.append(p.getInstitucionRival()).append(" -> ").append(p.getResultado())
						.append(" | avanza: ").append(p.getGanador()).append("\n");
			}
		}
		sb.append("Goleador: ").append(getGoleador()).append("\n");
		sb.append("Jugador destacado: ").append(getJugadorDestacado()).append("\n");
		sb.append("Amonestaciones: ").append(getAmonestaciones().isEmpty() ? "(ninguna)" : getAmonestaciones());
		return sb.toString();
	}

	@Override
	public String toString() {
		return nombre + " " + temporada + " (" + entidadOrganizadora + ", " + deporte.getNombre() + " "
				+ categoria + ", " + formato + (finalizada ? ", FINALIZADA" : ", en curso") + ")";
	}
}
