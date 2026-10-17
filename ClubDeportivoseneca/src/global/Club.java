package global;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import competencias.Competencia;
import competencias.Fase;
import deportes.Categoria;
import deportes.Deporte;
import deportes.DeporteConjunto;
import deportes.Equipo;
import deportes.InscripcionEquipo;
import partidos.EstadisticaPartido;
import partidos.Partido;
import partidos.PartidoCompetencia;
import partidos.PartidoInterno;
import personas.Administrador;
import personas.Empleado;
import personas.Entrenador;
import personas.Fisioterapeuta;
import personas.Persona;
import personas.Socio;

/**
 * Raiz del dominio. Guarda las colecciones principales, maneja la sesion
 * del usuario actual y verifica el rol antes de cada accion (RF-GEN-02).
 * No conoce archivos: la persistencia esta en el paquete persistencia.
 */
public class Club {

	//Atributos
	private final Map<String, Persona> personas;
	private final List<Deporte> deportes;
	private final List<Competencia> competencias;
	private Persona usuarioActual;

	//Constructor
	public Club() {
		this.personas = new LinkedHashMap<>();
		this.deportes = new ArrayList<>();
		this.competencias = new ArrayList<>();
		this.usuarioActual = null;
	}

	// ============================================================
	// Registro y busqueda (sin control de acceso, los usa la carga)
	// ============================================================

	public void registrarPersona(Persona persona) {
		if (personas.containsKey(persona.getLogin())) {
			throw new IllegalArgumentException("El login " + persona.getLogin() + " ya existe");
		}
		if (persona instanceof Administrador) {
			for (Persona p : personas.values()) {
				if (p instanceof Administrador) {
					throw new IllegalStateException("Ya existe un administrador en el club");
				}
			}
		}
		personas.put(persona.getLogin(), persona);
	}

	public Persona buscarPersona(String login) {
		Persona persona = personas.get(login);
		if (persona == null) {
			throw new IllegalArgumentException("El usuario " + login + " no existe");
		}
		return persona;
	}

	public Socio buscarSocio(String login) {
		Persona persona = buscarPersona(login);
		if (!(persona instanceof Socio)) {
			throw new IllegalArgumentException(login + " no es un socio");
		}
		return (Socio) persona;
	}

	public Collection<Persona> getPersonas() {
		return Collections.unmodifiableCollection(personas.values());
	}

	public void agregarDeporte(Deporte deporte) {
		for (Deporte d : deportes) {
			if (d.getNombre().equalsIgnoreCase(deporte.getNombre())) {
				throw new IllegalArgumentException("El deporte " + deporte.getNombre() + " ya existe");
			}
		}
		deportes.add(deporte);
	}

	public Deporte buscarDeporte(String nombre) {
		for (Deporte d : deportes) {
			if (d.getNombre().equalsIgnoreCase(nombre)) {
				return d;
			}
		}
		throw new IllegalArgumentException("El deporte " + nombre + " no existe");
	}

	public DeporteConjunto buscarDeporteConjunto(String nombre) {
		Deporte deporte = buscarDeporte(nombre);
		if (!(deporte instanceof DeporteConjunto)) {
			throw new IllegalArgumentException(nombre + " no es un deporte de conjunto");
		}
		return (DeporteConjunto) deporte;
	}

	public List<Deporte> getDeportes() {
		return Collections.unmodifiableList(deportes);
	}

	public List<DeporteConjunto> getDeportesConjunto() {
		List<DeporteConjunto> resultado = new ArrayList<>();
		for (Deporte d : deportes) {
			if (d instanceof DeporteConjunto) {
				resultado.add((DeporteConjunto) d);
			}
		}
		return resultado;
	}

	public Equipo buscarEquipo(String deporte, String nombre) {
		Equipo equipo = buscarDeporteConjunto(deporte).buscarEquipo(nombre);
		if (equipo == null) {
			throw new IllegalArgumentException("El equipo " + nombre + " no existe en " + deporte);
		}
		return equipo;
	}

	public void agregarCompetencia(Competencia competencia) {
		for (Competencia c : competencias) {
			if (c.getNombre().equalsIgnoreCase(competencia.getNombre()) && c.getTemporada() == competencia.getTemporada()) {
				throw new IllegalArgumentException("Ya existe la competencia " + competencia.getNombre()
						+ " temporada " + competencia.getTemporada());
			}
		}
		competencias.add(competencia);
	}

	public Competencia buscarCompetencia(String nombre, int temporada) {
		for (Competencia c : competencias) {
			if (c.getNombre().equalsIgnoreCase(nombre) && c.getTemporada() == temporada) {
				return c;
			}
		}
		throw new IllegalArgumentException("La competencia " + nombre + " " + temporada + " no existe");
	}

	public List<Competencia> getCompetencias() {
		return Collections.unmodifiableList(competencias);
	}

	public List<Partido> getTodosLosPartidos() {
		List<Partido> todos = new ArrayList<>();
		for (DeporteConjunto d : getDeportesConjunto()) {
			todos.addAll(d.getPartidos());
		}
		for (Competencia c : competencias) {
			todos.addAll(c.getPartidos());
		}
		return todos;
	}

	public Partido buscarPartido(String id) {
		for (Partido p : getTodosLosPartidos()) {
			if (p.getId().equalsIgnoreCase(id)) {
				return p;
			}
		}
		throw new IllegalArgumentException("El partido " + id + " no existe (no esta asignado)");
	}

	//Los partidos nunca se borran, asi que el consecutivo no se repite
	private String generarIdPartido() {
		return "P" + (getTodosLosPartidos().size() + 1);
	}

	// ============================================================
	// Sesion y control de acceso (RF-GEN-01, 02, 03)
	// ============================================================

	public Persona iniciarSesion(String login, String password) {
		Persona persona = personas.get(login);
		if (persona == null) {
			throw new IllegalArgumentException("El usuario " + login + " no existe");
		}
		if (!persona.esPasswordCorrecta(password)) {
			throw new IllegalArgumentException("Password incorrecto para " + login);
		}
		if (!persona.estaActivo()) {
			throw new IllegalStateException("El usuario " + login + " esta dado de baja");
		}
		usuarioActual = persona;
		return persona;
	}

	public void cerrarSesion() {
		usuarioActual = null;
	}

	public Persona getUsuarioActual() {
		return usuarioActual;
	}

	public void cambiarPassword(String passwordActual, String nuevaPassword) {
		exigirSesion().cambiarPassword(passwordActual, nuevaPassword);
	}

	private Persona exigirSesion() {
		if (usuarioActual == null) {
			throw new IllegalStateException("Debe iniciar sesion");
		}
		return usuarioActual;
	}

	private <T extends Persona> T exigirRol(Class<T> rol) {
		Persona actual = exigirSesion();
		if (!rol.isInstance(actual)) {
			throw new IllegalStateException("Acceso denegado: " + actual.getLogin() + " (" + actual.getRol()
					+ ") no puede realizar esta accion, requiere rol " + rol.getSimpleName());
		}
		return rol.cast(actual);
	}

	// ============================================================
	// Consultas generales (RF-GEN-05, 07) - cualquier usuario autenticado
	// ============================================================

	//deporte o categoria en null = todos
	public List<Equipo> consultarEquipos(String deporte, Categoria categoria) {
		exigirSesion();
		List<DeporteConjunto> fuente = deporte == null ? getDeportesConjunto() : List.of(buscarDeporteConjunto(deporte));
		List<Equipo> resultado = new ArrayList<>();
		for (DeporteConjunto d : fuente) {
			for (Equipo e : d.getEquipos()) {
				if (categoria == null || e.getCategoria() == categoria) {
					resultado.add(e);
				}
			}
		}
		return resultado;
	}

	//Calendario y resultados: partidos internos y de competencia del equipo, por fecha
	public List<Partido> consultarCalendarioEquipo(String deporte, String nombreEquipo) {
		exigirSesion();
		Equipo equipo = buscarEquipo(deporte, nombreEquipo);
		List<Partido> calendario = new ArrayList<>(equipo.getDeporte().getPartidosDe(equipo));
		for (Competencia c : competencias) {
			calendario.addAll(c.getPartidosDe(equipo));
		}
		calendario.sort((a, b) -> a.getFecha().compareTo(b.getFecha()));
		return calendario;
	}

	public Competencia consultarCompetencia(String nombre, int temporada) {
		exigirSesion();
		return buscarCompetencia(nombre, temporada);
	}

	// ============================================================
	// Administrador (RF-ADM-01 a 05, 07)
	// ============================================================

	public Socio crearSocio(String nombre, String login, String password, LocalDate fechaNacimiento) {
		exigirRol(Administrador.class);
		Socio socio = new Socio(nombre, login, password, fechaNacimiento);
		registrarPersona(socio);
		return socio;
	}

	public Entrenador crearEntrenador(String nombre, String login, String password) {
		exigirRol(Administrador.class);
		Entrenador entrenador = new Entrenador(nombre, login, password, generarCodigoDescuento(login));
		registrarPersona(entrenador);
		return entrenador;
	}

	public Fisioterapeuta crearFisioterapeuta(String nombre, String login, String password) {
		exigirRol(Administrador.class);
		Fisioterapeuta fisio = new Fisioterapeuta(nombre, login, password, generarCodigoDescuento(login));
		registrarPersona(fisio);
		return fisio;
	}

	private String generarCodigoDescuento(String login) {
		return "DESC-" + (login == null ? "" : login.toUpperCase());
	}

	public void restablecerPassword(String login, String nuevaPassword) {
		exigirRol(Administrador.class);
		buscarPersona(login).restablecerPassword(nuevaPassword);
	}

	//Socio: lo retira de sus equipos e impide iniciar sesion. El historial se conserva.
	//TODO etapa 3: retirar de academias, cancelar reservas/practicas; empleado: rechazar si tiene sesiones futuras
	public void darDeBajaPersona(String login) {
		Administrador admin = exigirRol(Administrador.class);
		Persona persona = buscarPersona(login);
		if (persona == admin) {
			throw new IllegalArgumentException("El administrador no puede darse de baja a si mismo");
		}
		if (!persona.estaActivo()) {
			throw new IllegalStateException(login + " ya esta dado de baja");
		}
		if (persona instanceof Socio) {
			Socio socio = (Socio) persona;
			for (Competencia c : competencias) {
				if (c.estaEnCurso() && c.tieneEnNomina(socio)) {
					throw new IllegalStateException(login + " participa en la competencia en curso " + c.getNombre());
				}
			}
			for (DeporteConjunto d : getDeportesConjunto()) {
				Equipo equipo = d.getEquipoDe(socio);
				if (equipo != null) {
					equipo.retirar(socio);
				}
			}
		}
		persona.darDeBaja();
	}

	public Equipo crearEquipo(String deporte, String nombre, Categoria categoria, int cupoMaximo) {
		exigirRol(Administrador.class);
		return buscarDeporteConjunto(deporte).crearEquipo(nombre, categoria, cupoMaximo);
	}

	public void darDeBajaEquipo(String deporte, String nombre) {
		exigirRol(Administrador.class);
		Equipo equipo = buscarEquipo(deporte, nombre);
		for (Competencia c : competencias) {
			if (c.estaEnCurso() && c.buscarInscripcion(equipo) != null) {
				throw new IllegalStateException(nombre + " esta inscrito en la competencia en curso " + c.getNombre());
			}
		}
		if (equipo.getDeporte().tienePartidosProgramados(equipo)) {
			throw new IllegalStateException(nombre + " tiene partidos internos programados");
		}
		equipo.darDeBaja();
	}

	public Competencia crearCompetenciaCerros(String nombre, int temporada, String deporte,
			Categoria categoria, List<String> instituciones) {
		exigirRol(Administrador.class);
		Competencia c = Competencia.torneoDeLosCerros(nombre, temporada, buscarDeporteConjunto(deporte), categoria, instituciones);
		agregarCompetencia(c);
		return c;
	}

	public Competencia crearCompetenciaAscun(String nombre, int temporada, String deporte,
			Categoria categoria, List<String> instituciones) {
		exigirRol(Administrador.class);
		Competencia c = Competencia.ascun(nombre, temporada, buscarDeporteConjunto(deporte), categoria, instituciones);
		agregarCompetencia(c);
		return c;
	}

	public void finalizarCompetencia(String nombre, int temporada) {
		exigirRol(Administrador.class);
		buscarCompetencia(nombre, temporada).finalizar();
	}

	// ============================================================
	// Socio (RF-SOC-01 a 03)
	// ============================================================

	public String consultarPerfil() {
		Socio socio = exigirRol(Socio.class);
		StringBuilder sb = new StringBuilder();
		sb.append("Nombre: ").append(socio.getNombre()).append(" (").append(socio.getLogin()).append(")\n");
		sb.append("Fecha de nacimiento: ").append(socio.getFechaNacimiento())
				.append(" | Edad: ").append(socio.getEdad())
				.append(" | Categoria: ").append(socio.getCategoria()).append("\n");
		sb.append("Equipos:");
		boolean tieneEquipos = false;
		for (DeporteConjunto d : getDeportesConjunto()) {
			Equipo equipo = d.getEquipoDe(socio);
			if (equipo != null) {
				tieneEquipos = true;
				InscripcionEquipo ins = equipo.buscarInscripcion(socio);
				sb.append("\n  ").append(d.getNombre()).append(": ").append(equipo.getNombre())
						.append(" | posicion ").append(ins.getPosicion())
						.append(" | camiseta #").append(ins.getNumeroCamiseta())
						.append(" | ").append(describirSuspension(socio, equipo));
			}
		}
		if (!tieneEquipos) {
			sb.append(" (ninguno)");
		}
		return sb.toString();
	}

	private String describirSuspension(Socio socio, Equipo equipo) {
		List<String> suspensiones = new ArrayList<>();
		Partido ultimoInterno = Partido.ultimoJugadoAntesDe(equipo.getDeporte().getPartidosDe(equipo), LocalDate.MAX);
		if (Partido.fueExpulsadoEn(ultimoInterno, socio)) {
			suspensiones.add("proximo partido interno");
		}
		for (Competencia c : competencias) {
			if (c.estaEnCurso() && c.buscarInscripcion(equipo) != null && c.estaSuspendido(socio, equipo, LocalDate.MAX)) {
				suspensiones.add("proximo partido de " + c.getNombre());
			}
		}
		return suspensiones.isEmpty() ? "habilitado" : "SUSPENDIDO para " + String.join(" y ", suspensiones);
	}

	public void inscribirseEnEquipo(String deporte, String nombreEquipo, String posicion, int numeroCamiseta) {
		Socio socio = exigirRol(Socio.class);
		buscarEquipo(deporte, nombreEquipo).inscribir(socio, posicion, numeroCamiseta);
	}

	public void retirarseDeEquipo(String deporte, String nombreEquipo) {
		Socio socio = exigirRol(Socio.class);
		Equipo equipo = buscarEquipo(deporte, nombreEquipo);
		if (!equipo.estaInscrito(socio)) {
			throw new IllegalStateException("No esta inscrito en " + nombreEquipo);
		}
		for (Competencia c : competencias) {
			if (c.estaEnCurso() && c.buscarInscripcion(equipo) != null
					&& c.buscarInscripcion(equipo).estaEnNomina(socio)) {
				throw new IllegalStateException("Hace parte de la competencia en curso " + c.getNombre());
			}
		}
		equipo.retirar(socio);
	}

	// ============================================================
	// Entrenador (RF-ENT-01 a 08)
	// ============================================================

	public PartidoInterno programarPartidoInterno(String deporte, String local, String visitante, LocalDate fecha) {
		exigirRol(Entrenador.class);
		DeporteConjunto d = buscarDeporteConjunto(deporte);
		return d.programarPartido(generarIdPartido(), buscarEquipo(deporte, local), buscarEquipo(deporte, visitante), fecha);
	}

	public void registrarResultadoInterno(String idPartido, List<String> alineacionLocal,
			List<String> alineacionVisitante, int marcadorLocal, int marcadorVisitante) {
		exigirRol(Entrenador.class);
		PartidoInterno partido = buscarPartidoInterno(idPartido);
		partido.registrarResultado(buscarSocios(alineacionLocal), buscarSocios(alineacionVisitante),
				marcadorLocal, marcadorVisitante);
	}

	public void registrarEstadistica(String idPartido, EstadisticaPartido estadistica) {
		exigirRol(Entrenador.class);
		buscarPartido(idPartido).registrarEstadistica(estadistica);
	}

	public void declararNoJugado(String idPartido) {
		exigirRol(Entrenador.class);
		buscarPartido(idPartido).declararNoJugado();
	}

	//Tarjetas acumuladas y suspensiones vigentes de cada jugador del equipo
	public List<String> consultarSanciones(String deporte, String nombreEquipo) {
		exigirRol(Entrenador.class);
		Equipo equipo = buscarEquipo(deporte, nombreEquipo);
		List<Partido> partidosEquipo = new ArrayList<>(equipo.getDeporte().getPartidosDe(equipo));
		for (Competencia c : competencias) {
			partidosEquipo.addAll(c.getPartidosDe(equipo));
		}
		List<String> lineas = new ArrayList<>();
		for (Socio socio : equipo.getJugadores()) {
			int amarillas = 0;
			int rojas = 0;
			for (Partido p : partidosEquipo) {
				EstadisticaPartido e = p.getEstadistica(socio.getLogin());
				if (e != null) {
					amarillas += e.getTarjetasAmarillas();
					rojas += e.tieneTarjetaRoja() ? 1 : 0;
				}
			}
			String estado = describirSuspension(socio, equipo);
			if (amarillas > 0 || rojas > 0 || !estado.equals("habilitado")) {
				lineas.add(socio.getLogin() + ": " + amarillas + " amarillas, " + rojas + " rojas -> " + estado);
			}
		}
		return lineas;
	}

	public void inscribirEquipoEnCompetencia(String competencia, int temporada, String deporte, String nombreEquipo) {
		exigirRol(Entrenador.class);
		buscarCompetencia(competencia, temporada).inscribirEquipo(buscarEquipo(deporte, nombreEquipo));
	}

	public void definirNomina(String competencia, int temporada, String nombreEquipo, List<String> logins) {
		exigirRol(Entrenador.class);
		Competencia c = buscarCompetencia(competencia, temporada);
		Equipo equipo = buscarEquipo(c.getDeporte().getNombre(), nombreEquipo);
		c.definirNomina(equipo, buscarSocios(logins));
	}

	public PartidoCompetencia programarPartidoCompetencia(String competencia, int temporada, String nombreEquipo,
			String rival, String escenario, LocalDate fecha, Fase fase) {
		exigirRol(Entrenador.class);
		Competencia c = buscarCompetencia(competencia, temporada);
		Equipo equipo = buscarEquipo(c.getDeporte().getNombre(), nombreEquipo);
		return c.programarPartido(generarIdPartido(), equipo, rival, escenario, fecha, fase);
	}

	public void registrarPartidoCompetencia(String idPartido, List<String> alineacion, int marcadorClub,
			int marcadorRival, List<EstadisticaPartido> estadisticas) {
		exigirRol(Entrenador.class);
		Partido partido = buscarPartido(idPartido);
		if (!(partido instanceof PartidoCompetencia)) {
			throw new IllegalArgumentException(idPartido + " no es un partido de competencia");
		}
		((PartidoCompetencia) partido).registrarResultado(buscarSocios(alineacion), marcadorClub, marcadorRival, estadisticas);
	}

	private PartidoInterno buscarPartidoInterno(String id) {
		Partido partido = buscarPartido(id);
		if (!(partido instanceof PartidoInterno)) {
			throw new IllegalArgumentException(id + " no es un partido interno");
		}
		return (PartidoInterno) partido;
	}

	private List<Socio> buscarSocios(List<String> logins) {
		List<Socio> socios = new ArrayList<>();
		for (String login : logins) {
			socios.add(buscarSocio(login));
		}
		return socios;
	}

	//Para los empleados (RF-EMP-05 se completa en la etapa 5)
	public String consultarCodigoDescuento() {
		return exigirRol(Empleado.class).getCodigoDescuento();
	}
}
