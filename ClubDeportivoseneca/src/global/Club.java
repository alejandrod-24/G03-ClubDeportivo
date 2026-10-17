package global;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import administracion.EstadoSolicitud;
import administracion.PagoMensualidad;
import administracion.SolicitudCambioTurno;
import administracion.SugerenciaProducto;
import administracion.TipoPeriodo;
import administracion.Turno;
import comercio.ArticuloDeportivo;
import comercio.CuentaFidelidad;
import comercio.DetalleVenta;
import comercio.Producto;
import comercio.ProductoCafeteria;
import comercio.Venta;
import comercio.VentaCafeteria;
import comercio.VentaTienda;
import competencias.Competencia;
import competencias.Fase;
import deportes.Categoria;
import deportes.Deporte;
import deportes.DeporteConjunto;
import deportes.Equipo;
import deportes.InscripcionEquipo;
import partidos.EstadisticaPartido;
import partidos.EstadoPartido;
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
	private final List<Producto> productos;
	private final Map<String, CuentaFidelidad> cuentas;
	private final List<Turno> turnos;
	private final List<SolicitudCambioTurno> solicitudes;
	private final List<SugerenciaProducto> sugerencias;
	private final List<PagoMensualidad> pagos;
	private Persona usuarioActual;

	//Constructor
	public Club() {
		this.personas = new LinkedHashMap<>();
		this.deportes = new ArrayList<>();
		this.competencias = new ArrayList<>();
		this.productos = new ArrayList<>();
		this.cuentas = new LinkedHashMap<>();
		this.turnos = new ArrayList<>();
		this.solicitudes = new ArrayList<>();
		this.sugerencias = new ArrayList<>();
		this.pagos = new ArrayList<>();
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

	public Empleado buscarEmpleado(String login) {
		Persona persona = buscarPersona(login);
		if (!(persona instanceof Empleado)) {
			throw new IllegalArgumentException(login + " no es un empleado");
		}
		return (Empleado) persona;
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
		//Integracion: agregar los partidos de practica de los deportes individuales
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

	public void agregarProducto(Producto producto) {
		for (Producto p : productos) {
			if (p.getNombre().equalsIgnoreCase(producto.getNombre())) {
				throw new IllegalArgumentException("El producto " + producto.getNombre() + " ya existe en el catalogo");
			}
		}
		productos.add(producto);
	}

	public List<Producto> getProductos() {
		return Collections.unmodifiableList(productos);
	}

	public Producto buscarProducto(String nombre) {
		for (Producto p : productos) {
			if (p.getNombre().equalsIgnoreCase(nombre)) {
				return p;
			}
		}
		throw new IllegalArgumentException("El producto " + nombre + " no existe");
	}

	public ArticuloDeportivo buscarArticulo(String nombre) {
		Producto producto = buscarProducto(nombre);
		if (!(producto instanceof ArticuloDeportivo)) {
			throw new IllegalArgumentException(nombre + " no es un articulo de la tienda");
		}
		return (ArticuloDeportivo) producto;
	}

	public ProductoCafeteria buscarProductoCafeteria(String nombre) {
		Producto producto = buscarProducto(nombre);
		if (!(producto instanceof ProductoCafeteria)) {
			throw new IllegalArgumentException(nombre + " no es un producto de la cafeteria");
		}
		return (ProductoCafeteria) producto;
	}

	private boolean existeProducto(String nombre) {
		for (Producto p : productos) {
			if (p.getNombre().equalsIgnoreCase(nombre)) {
				return true;
			}
		}
		return false;
	}

	public CuentaFidelidad getCuentaFidelidad(Persona persona) {
		return cuentas.computeIfAbsent(persona.getLogin(), login -> new CuentaFidelidad(persona));
	}

	//Todas las ventas en orden de creacion
	public List<Venta> getVentas() {
		List<Venta> ventas = new ArrayList<>();
		for (CuentaFidelidad cuenta : cuentas.values()) {
			ventas.addAll(cuenta.getVentas());
		}
		ventas.sort((a, b) -> consecutivo(a.getId()) - consecutivo(b.getId()));
		return ventas;
	}

	public Venta buscarVenta(String id) {
		for (Venta v : getVentas()) {
			if (v.getId().equalsIgnoreCase(id)) {
				return v;
			}
		}
		throw new IllegalArgumentException("La venta " + id + " no existe");
	}

	public void agregarTurno(Turno turno) {
		turnos.add(turno);
	}

	public List<Turno> getTurnos() {
		return Collections.unmodifiableList(turnos);
	}

	public Turno buscarTurno(String id) {
		for (Turno t : turnos) {
			if (t.getId().equalsIgnoreCase(id)) {
				return t;
			}
		}
		throw new IllegalArgumentException("El turno " + id + " no existe");
	}

	public void agregarSolicitud(SolicitudCambioTurno solicitud) {
		solicitudes.add(solicitud);
	}

	public List<SolicitudCambioTurno> getSolicitudes() {
		return Collections.unmodifiableList(solicitudes);
	}

	public SolicitudCambioTurno buscarSolicitud(String id) {
		for (SolicitudCambioTurno s : solicitudes) {
			if (s.getId().equalsIgnoreCase(id)) {
				return s;
			}
		}
		throw new IllegalArgumentException("La solicitud " + id + " no existe");
	}

	public void agregarSugerencia(SugerenciaProducto sugerencia) {
		sugerencias.add(sugerencia);
	}

	public List<SugerenciaProducto> getSugerencias() {
		return Collections.unmodifiableList(sugerencias);
	}

	public SugerenciaProducto buscarSugerencia(String id) {
		for (SugerenciaProducto s : sugerencias) {
			if (s.getId().equalsIgnoreCase(id)) {
				return s;
			}
		}
		throw new IllegalArgumentException("La sugerencia " + id + " no existe");
	}

	public void agregarPago(PagoMensualidad pago) {
		pagos.add(pago);
	}

	public List<PagoMensualidad> getPagos() {
		return Collections.unmodifiableList(pagos);
	}

	//Numero de un id como "V12"
	private static int consecutivo(String id) {
		return Integer.parseInt(id.replaceAll("\\D", ""));
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
			//Integracion etapa 3 (RF-ADM-02): retirarlo de sus academias y cancelar sus reservas y partidos de practica futuros
		}
		//Integracion etapa 3 (RF-ADM-03): rechazar la baja de un entrenador con sesiones de entrenamiento futuras
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
		//Integracion etapa 3 (RF-SOC-01): mostrar academias y nivel de cada una
		YearMonth mes = YearMonth.from(Reloj.hoy());
		sb.append("\nPuntos de fidelidad: ").append(getCuentaFidelidad(socio).getSaldo());
		sb.append("\nMensualidad de ").append(mes).append(": ").append(tieneMensualidadPaga(socio, mes) ? "pagada" : "pendiente");
		if (socio.tieneBebidaCaliente()) {
			sb.append("\nTiene una bebida caliente");
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

	//Para los empleados (RF-EMP-05)
	public String consultarCodigoDescuento() {
		return exigirRol(Empleado.class).getCodigoDescuento();
	}

	//Integracion etapa 3: aqui van las academias, partidos de practica y sesiones de entrenamiento
	//(RF-SOC-04 a 13, RF-ENT-09 a 16). Las usan el perfil, la baja de personas y los reportes RF-ADM-17, 19 y 21.

	// ============================================================
	// Comercio (RF-GEN-06, RF-SOC-14 a 17, RF-EMP-04 y mensualidades)
	// ============================================================

	public List<ArticuloDeportivo> consultarCatalogoTienda() {
		exigirSesion();
		List<ArticuloDeportivo> catalogo = new ArrayList<>();
		for (Producto p : productos) {
			if (p instanceof ArticuloDeportivo && p.estaActivo()) {
				catalogo.add((ArticuloDeportivo) p);
			}
		}
		return catalogo;
	}

	public List<ProductoCafeteria> consultarCatalogoCafeteria() {
		exigirSesion();
		List<ProductoCafeteria> catalogo = new ArrayList<>();
		for (Producto p : productos) {
			if (p instanceof ProductoCafeteria && p.estaActivo()) {
				catalogo.add((ProductoCafeteria) p);
			}
		}
		return catalogo;
	}

	public Producto consultarProducto(String nombre) {
		exigirSesion();
		return buscarProducto(nombre);
	}

	//Compran los socios y los empleados
	private Persona exigirComprador() {
		Persona actual = exigirSesion();
		if (!(actual instanceof Socio) && !(actual instanceof Empleado)) {
			throw new IllegalStateException("Acceso denegado: " + actual.getLogin() + " (" + actual.getRol() + ") no puede comprar");
		}
		return actual;
	}

	//Socio: puede usar el codigo de un empleado (8%) o redimir puntos, pero no ambos.
	//Empleado: su descuento (15%) se aplica solo, salvo que redima puntos.
	public VentaTienda comprarEnTienda(Map<String, Integer> articulos, String codigoDescuento, long puntos) {
		Persona comprador = exigirComprador();
		int porcentaje = 0;
		String codigo = "";
		if (comprador instanceof Empleado) {
			if (codigoDescuento != null) {
				throw new IllegalArgumentException("El descuento de empleado se aplica solo y no es acumulable con otro codigo");
			}
			if (puntos == 0) {
				porcentaje = VentaTienda.DESCUENTO_EMPLEADO;
				codigo = ((Empleado) comprador).getCodigoDescuento();
			}
		} else if (codigoDescuento != null) {
			if (puntos > 0) {
				throw new IllegalArgumentException("El codigo de descuento no es acumulable con la redencion de puntos");
			}
			buscarEmpleadoPorCodigo(codigoDescuento);
			porcentaje = VentaTienda.DESCUENTO_CODIGO;
			codigo = codigoDescuento;
		}
		VentaTienda venta = new VentaTienda(generarIdVenta(), Reloj.hoy(), comprador, codigo, porcentaje, puntos);
		for (Map.Entry<String, Integer> item : articulos.entrySet()) {
			ArticuloDeportivo articulo = buscarArticulo(item.getKey());
			exigirDisponible(articulo);
			venta.agregarDetalle(articulo, item.getValue());
			if (!articulo.hayExistencia(item.getValue())) {
				throw new IllegalStateException("No hay suficientes existencias de " + articulo.getNombre()
						+ " en la tienda (quedan " + articulo.getExistencia(ArticuloDeportivo.TIENDA) + ")");
			}
		}
		validarCompra(comprador, venta);
		for (DetalleVenta detalle : venta.getDetalles()) {
			((ArticuloDeportivo) detalle.getProducto()).descontar(detalle.getCantidad());
		}
		getCuentaFidelidad(comprador).registrarVenta(venta);
		return venta;
	}

	//Alergenos que se le informan al comprador antes de la compra
	public List<String> consultarAlergenos(Map<String, Integer> productosCafeteria) {
		exigirComprador();
		List<String> avisos = new ArrayList<>();
		for (String nombre : productosCafeteria.keySet()) {
			ProductoCafeteria producto = buscarProductoCafeteria(nombre);
			if (!producto.getAlergenos().isEmpty()) {
				avisos.add(producto.getNombre() + ": " + String.join(", ", producto.getAlergenos()));
			}
		}
		return avisos;
	}

	//aceptaAlergenos: el comprador confirma que conoce los alergenos de lo que compra.
	//Si un socio compra una bebida caliente queda marcado hasta que la termine.
	public VentaCafeteria comprarEnCafeteria(Map<String, Integer> productosCafeteria, long puntos, int porcentajePropina,
			boolean aceptaAlergenos) {
		Persona comprador = exigirComprador();
		VentaCafeteria venta = new VentaCafeteria(generarIdVenta(), Reloj.hoy(), comprador, puntos, porcentajePropina);
		for (Map.Entry<String, Integer> item : productosCafeteria.entrySet()) {
			ProductoCafeteria producto = buscarProductoCafeteria(item.getKey());
			exigirDisponible(producto);
			venta.agregarDetalle(producto, item.getValue());
		}
		List<String> alergenos = consultarAlergenos(productosCafeteria);
		if (!aceptaAlergenos && !alergenos.isEmpty()) {
			throw new IllegalStateException("Debe confirmar que conoce los alergenos: " + alergenos);
		}
		validarCompra(comprador, venta);
		if (venta.tieneBebidaCaliente() && comprador instanceof Socio) {
			//Integracion etapa 3: rechazar si el socio esta en una cancha de padel o mesa de tenis de mesa techada
			((Socio) comprador).tomarBebidaCaliente();
		}
		getCuentaFidelidad(comprador).registrarVenta(venta);
		return venta;
	}

	private String generarIdVenta() {
		return "V" + (getVentas().size() + 1);
	}

	private static void exigirDisponible(Producto producto) {
		if (!producto.estaActivo()) {
			throw new IllegalStateException(producto.getNombre() + " no esta disponible");
		}
	}

	private void validarCompra(Persona comprador, Venta venta) {
		if (venta.getDetalles().isEmpty()) {
			throw new IllegalArgumentException("La compra debe tener al menos un producto");
		}
		if (venta.getPuntosRedimidos() > venta.getSubtotal()) {
			throw new IllegalArgumentException("Los puntos a redimir (" + venta.getPuntosRedimidos()
					+ ") superan el subtotal ($" + venta.getSubtotal() + ")");
		}
		long saldo = getCuentaFidelidad(comprador).getSaldo();
		if (venta.getPuntosRedimidos() > saldo) {
			throw new IllegalArgumentException("Solo tiene " + saldo + " puntos para redimir");
		}
	}

	private Empleado buscarEmpleadoPorCodigo(String codigo) {
		for (Persona p : personas.values()) {
			if (p instanceof Empleado && p.estaActivo() && ((Empleado) p).getCodigoDescuento().equalsIgnoreCase(codigo)) {
				return (Empleado) p;
			}
		}
		throw new IllegalArgumentException("El codigo de descuento " + codigo + " no existe");
	}

	public String consultarPuntos() {
		CuentaFidelidad cuenta = getCuentaFidelidad(exigirComprador());
		StringBuilder sb = new StringBuilder("Saldo: " + cuenta.getSaldo() + " puntos (1 punto = 1 peso)");
		for (String movimiento : cuenta.getMovimientos()) {
			sb.append("\n  ").append(movimiento);
		}
		return sb.toString();
	}

	public void declararFinBebidaCaliente() {
		exigirRol(Socio.class).terminarBebidaCaliente();
	}

	//Se paga la mensualidad del mes en curso
	public PagoMensualidad pagarMensualidad() {
		Socio socio = exigirRol(Socio.class);
		YearMonth mes = YearMonth.from(Reloj.hoy());
		if (tieneMensualidadPaga(socio, mes)) {
			throw new IllegalStateException("La mensualidad de " + mes + " ya esta pagada");
		}
		PagoMensualidad pago = new PagoMensualidad("M" + (pagos.size() + 1), socio, mes, Reloj.hoy(), PagoMensualidad.VALOR);
		pagos.add(pago);
		return pago;
	}

	private boolean tieneMensualidadPaga(Socio socio, YearMonth mes) {
		for (PagoMensualidad p : pagos) {
			if (p.getSocio() == socio && p.getPeriodo().equals(mes)) {
				return true;
			}
		}
		return false;
	}

	// ============================================================
	// Empleado: turnos y sugerencias (RF-EMP-01 a 03, 06 y 07)
	// ============================================================

	public List<Turno> consultarTurnos() {
		return turnosDe(exigirRol(Empleado.class));
	}

	private List<Turno> turnosDe(Empleado empleado) {
		List<Turno> propios = new ArrayList<>();
		for (Turno t : turnos) {
			if (t.getEmpleado() == empleado) {
				propios.add(t);
			}
		}
		propios.sort((a, b) -> a.getDia() != b.getDia() ? a.getDia().compareTo(b.getDia())
				: a.getHoraInicio().compareTo(b.getHoraInicio()));
		return propios;
	}

	public SolicitudCambioTurno solicitarCambioTurno(String idTurno, LocalTime nuevaHoraInicio, LocalTime nuevaHoraFin) {
		Empleado empleado = exigirRol(Empleado.class);
		Turno turno = buscarTurno(idTurno);
		if (turno.getEmpleado() != empleado) {
			throw new IllegalStateException("El turno " + idTurno + " no es de " + empleado.getLogin());
		}
		Turno.validarHorario(turno.getDia(), nuevaHoraInicio, nuevaHoraFin);
		validarSinCruceDeTurnos(empleado, turno.getDia(), nuevaHoraInicio, nuevaHoraFin, turno);
		for (SolicitudCambioTurno s : solicitudes) {
			if (s.getTurno() == turno && s.getEstado() == EstadoSolicitud.PENDIENTE) {
				throw new IllegalStateException("Ya hay una solicitud pendiente (" + s.getId() + ") para el turno " + idTurno);
			}
		}
		SolicitudCambioTurno solicitud = new SolicitudCambioTurno("C" + (solicitudes.size() + 1), turno, nuevaHoraInicio, nuevaHoraFin);
		solicitudes.add(solicitud);
		return solicitud;
	}

	public List<SolicitudCambioTurno> consultarSolicitudes() {
		Empleado empleado = exigirRol(Empleado.class);
		List<SolicitudCambioTurno> propias = new ArrayList<>();
		for (SolicitudCambioTurno s : solicitudes) {
			if (s.getEmpleado() == empleado) {
				propias.add(s);
			}
		}
		return propias;
	}

	public SugerenciaProducto sugerirProducto(String nombre, String categoria, long precioSugerido, List<String> alergenos) {
		Empleado empleado = exigirRol(Empleado.class);
		if (nombre != null && existeProducto(nombre)) {
			throw new IllegalArgumentException(nombre + " ya esta en el catalogo");
		}
		for (SugerenciaProducto s : sugerencias) {
			if (s.getNombre().equalsIgnoreCase(nombre) && s.getEstado() == EstadoSolicitud.PENDIENTE) {
				throw new IllegalStateException("Ya hay una sugerencia pendiente (" + s.getId() + ") para " + nombre);
			}
		}
		SugerenciaProducto sugerencia = new SugerenciaProducto("SG" + (sugerencias.size() + 1), empleado, nombre,
				categoria, precioSugerido, alergenos);
		sugerencias.add(sugerencia);
		return sugerencia;
	}

	public List<SugerenciaProducto> consultarSugerencias() {
		Empleado empleado = exigirRol(Empleado.class);
		List<SugerenciaProducto> propias = new ArrayList<>();
		for (SugerenciaProducto s : sugerencias) {
			if (s.getEmpleado() == empleado) {
				propias.add(s);
			}
		}
		return propias;
	}

	// ============================================================
	// Administrador: catalogo, inventario, sugerencias y turnos (RF-ADM-08 a 14)
	// ============================================================

	public ArticuloDeportivo crearArticulo(String nombre, String categoria, long precio) {
		exigirRol(Administrador.class);
		ArticuloDeportivo articulo = new ArticuloDeportivo(nombre, categoria, precio);
		agregarProducto(articulo);
		return articulo;
	}

	public ProductoCafeteria crearProductoCafeteria(String nombre, long precio, boolean bebidaCaliente, List<String> alergenos) {
		exigirRol(Administrador.class);
		ProductoCafeteria producto = new ProductoCafeteria(nombre, precio, bebidaCaliente, alergenos);
		agregarProducto(producto);
		return producto;
	}

	public void cambiarPrecioProducto(String nombre, long nuevoPrecio) {
		exigirRol(Administrador.class);
		buscarProducto(nombre).cambiarPrecio(nuevoPrecio);
	}

	public void desactivarProducto(String nombre) {
		exigirRol(Administrador.class);
		buscarProducto(nombre).desactivar();
	}

	public void reabastecerTienda(String articulo, int cantidad) {
		exigirRol(Administrador.class);
		buscarArticulo(articulo).reabastecer(cantidad);
	}

	public void moverArticulo(String articulo, String origen, String destino, int cantidad) {
		exigirRol(Administrador.class);
		buscarArticulo(articulo).mover(origen, destino, cantidad);
	}

	//Existencias por ubicacion de los articulos activos, con alerta si estan bajas
	public List<String> consultarInventario() {
		exigirRol(Administrador.class);
		Map<String, List<String>> porUbicacion = new TreeMap<>();
		for (Producto p : productos) {
			if (p instanceof ArticuloDeportivo && p.estaActivo()) {
				for (Map.Entry<String, Integer> e : ((ArticuloDeportivo) p).getExistencias().entrySet()) {
					String linea = p.getNombre() + ": " + e.getValue()
							+ (e.getValue() < ArticuloDeportivo.EXISTENCIA_MINIMA ? "  <- EXISTENCIAS BAJAS" : "");
					porUbicacion.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).add(linea);
				}
			}
		}
		List<String> lineas = new ArrayList<>();
		for (Map.Entry<String, List<String>> e : porUbicacion.entrySet()) {
			lineas.add(e.getKey() + ":");
			for (String linea : e.getValue()) {
				lineas.add("  " + linea);
			}
		}
		//Integracion etapa 3: agregar el estado del inventario de instalaciones
		return lineas;
	}

	//Al aprobarla, el producto se crea con crearArticulo o crearProductoCafeteria
	public void aprobarSugerencia(String id) {
		exigirRol(Administrador.class);
		SugerenciaProducto sugerencia = buscarSugerencia(id);
		if (existeProducto(sugerencia.getNombre())) {
			throw new IllegalStateException(sugerencia.getNombre() + " ya existe en el catalogo");
		}
		sugerencia.aprobar();
	}

	public void rechazarSugerencia(String id) {
		exigirRol(Administrador.class);
		buscarSugerencia(id).rechazar();
	}

	public Turno asignarTurno(String login, DayOfWeek dia, LocalTime horaInicio, LocalTime horaFin) {
		exigirRol(Administrador.class);
		Empleado empleado = buscarEmpleado(login);
		if (!empleado.estaActivo()) {
			throw new IllegalStateException(login + " esta dado de baja");
		}
		Turno.validarHorario(dia, horaInicio, horaFin);
		validarSinCruceDeTurnos(empleado, dia, horaInicio, horaFin, null);
		Turno turno = new Turno("T" + (turnos.size() + 1), empleado, dia, horaInicio, horaFin);
		turnos.add(turno);
		return turno;
	}

	private void validarSinCruceDeTurnos(Empleado empleado, DayOfWeek dia, LocalTime inicio, LocalTime fin, Turno excluido) {
		for (Turno t : turnos) {
			if (t != excluido && t.getEmpleado() == empleado && t.seCruzaCon(dia, inicio, fin)) {
				throw new IllegalArgumentException("Se cruza con el turno " + t.getId() + " de " + empleado.getLogin());
			}
		}
	}

	//Se aprueba solo si se mantienen 2 entrenadores y 1 fisioterapeuta en todo el horario de atencion
	public void aprobarSolicitudCambioTurno(String id) {
		exigirRol(Administrador.class);
		SolicitudCambioTurno solicitud = buscarSolicitud(id);
		validarSinCruceDeTurnos(solicitud.getEmpleado(), solicitud.getTurno().getDia(), solicitud.getNuevaHoraInicio(),
				solicitud.getNuevaHoraFin(), solicitud.getTurno());
		solicitud.aprobar(turnos);
	}

	public void rechazarSolicitudCambioTurno(String id) {
		exigirRol(Administrador.class);
		buscarSolicitud(id).rechazar();
	}

	// ============================================================
	// Administrador: historiales y reportes (RF-ADM-16 a 21)
	// ============================================================

	//Filtros en null = sin filtro. login: partidos en los que jugo esa persona.
	//Integracion etapa 3: incluir al entrenador que juega un partido de practica en el filtro por login
	public List<Partido> historialPartidos(String deporte, LocalDate desde, LocalDate hasta, String login) {
		exigirRol(Administrador.class);
		validarFiltros(deporte, desde, hasta, login);
		List<Partido> resultado = new ArrayList<>();
		for (Partido p : getTodosLosPartidos()) {
			boolean jugo = login == null || p.estaAlineado(login);
			if ((deporte == null || p.getDeporte().getNombre().equalsIgnoreCase(deporte)) && enPeriodo(p.getFecha(), desde, hasta) && jugo) {
				resultado.add(p);
			}
		}
		resultado.sort((a, b) -> a.getFecha().compareTo(b.getFecha()));
		return resultado;
	}

	//Integracion etapa 3 (RF-ADM-17): historial de sesiones de entrenamiento con asistencia y evaluaciones, con filtros

	private void validarFiltros(String deporte, LocalDate desde, LocalDate hasta, String login) {
		if (deporte != null) {
			buscarDeporte(deporte);
		}
		if (login != null) {
			buscarPersona(login);
		}
		if (desde != null && hasta != null && desde.isAfter(hasta)) {
			throw new IllegalArgumentException("Periodo invalido: " + desde + " es despues de " + hasta);
		}
	}

	//Ventas por rubro (tienda, cafeteria, mensualidades), agrupadas por dia, semana o mes
	public String reporteVentas(LocalDate desde, LocalDate hasta, TipoPeriodo tipo) {
		exigirRol(Administrador.class);
		validarPeriodo(desde, hasta);
		if (tipo == null) {
			throw new IllegalArgumentException("Debe indicar si el reporte es diario, semanal o mensual");
		}
		Map<LocalDate, List<Venta>> ventasPorPeriodo = new TreeMap<>();
		Map<LocalDate, List<PagoMensualidad>> pagosPorPeriodo = new TreeMap<>();
		List<Venta> ventasDelPeriodo = new ArrayList<>();
		List<PagoMensualidad> pagosDelPeriodo = new ArrayList<>();
		for (Venta v : getVentas()) {
			if (enPeriodo(v.getFecha(), desde, hasta)) {
				ventasPorPeriodo.computeIfAbsent(tipo.inicioDe(v.getFecha()), k -> new ArrayList<>()).add(v);
				pagosPorPeriodo.computeIfAbsent(tipo.inicioDe(v.getFecha()), k -> new ArrayList<>());
				ventasDelPeriodo.add(v);
			}
		}
		for (PagoMensualidad p : pagos) {
			if (enPeriodo(p.getFecha(), desde, hasta)) {
				pagosPorPeriodo.computeIfAbsent(tipo.inicioDe(p.getFecha()), k -> new ArrayList<>()).add(p);
				ventasPorPeriodo.computeIfAbsent(tipo.inicioDe(p.getFecha()), k -> new ArrayList<>());
				pagosDelPeriodo.add(p);
			}
		}
		StringBuilder sb = new StringBuilder("Reporte de ventas " + tipo + " del " + desde + " al " + hasta + "\n");
		if (ventasPorPeriodo.isEmpty()) {
			sb.append("(sin ventas en el periodo)");
			return sb.toString();
		}
		for (LocalDate inicio : ventasPorPeriodo.keySet()) {
			sb.append(tipo.describir(inicio)).append(":\n");
			sb.append(resumirRubros(ventasPorPeriodo.get(inicio), pagosPorPeriodo.get(inicio)));
		}
		sb.append("Total del periodo:\n").append(resumirRubros(ventasDelPeriodo, pagosDelPeriodo));
		return sb.toString().stripTrailing();
	}

	private static String resumirRubros(List<Venta> ventas, List<PagoMensualidad> pagosMensualidad) {
		StringBuilder sb = new StringBuilder();
		for (String rubro : List.of("Tienda", "Cafeteria")) {
			int cantidad = 0;
			long subtotal = 0;
			long descuentos = 0;
			long redimidos = 0;
			long base = 0;
			long iva = 0;
			long consumo = 0;
			long propinas = 0;
			long total = 0;
			long generados = 0;
			for (Venta v : ventas) {
				if (v.getRubro().equals(rubro)) {
					cantidad++;
					subtotal += v.getSubtotal();
					descuentos += v.getDescuento();
					redimidos += v.getPuntosRedimidos();
					base += v.getBase();
					if (v instanceof VentaTienda) {
						iva += v.getImpuesto();
					} else {
						consumo += v.getImpuesto();
					}
					propinas += v.getPropina();
					total += v.getTotal();
					generados += v.getPuntosGenerados();
				}
			}
			if (cantidad > 0) {
				sb.append("  ").append(rubro).append(": ").append(cantidad).append(" ventas | subtotal $").append(subtotal)
						.append(" | descuentos $").append(descuentos).append(" | base $").append(base)
						.append(" | IVA $").append(iva).append(" | impuesto al consumo $").append(consumo)
						.append(" | propinas $").append(propinas).append(" | total $").append(total)
						.append(" | puntos generados ").append(generados).append(" | puntos redimidos ").append(redimidos).append("\n");
			}
		}
		if (!pagosMensualidad.isEmpty()) {
			long total = 0;
			for (PagoMensualidad p : pagosMensualidad) {
				total += p.getMonto();
			}
			sb.append("  Mensualidades: ").append(pagosMensualidad.size()).append(" pagos | total $").append(total).append("\n");
		}
		return sb.toString();
	}

	//Integracion etapa 3 (RF-ADM-19): reporte de asistencias (convocatorias, asistencias, faltas, tardanzas
	//y porcentaje de cada jugador en las sesiones del periodo), con la misma validacion de periodo

	//Victorias, empates y derrotas de cada equipo del club, y el marcador de cada partido jugado.
	//Integracion etapa 3: los partidos de practica tambien apareceran en la lista de partidos
	public String reporteResultados(LocalDate desde, LocalDate hasta) {
		exigirRol(Administrador.class);
		validarPeriodo(desde, hasta);
		Map<String, int[]> balance = new TreeMap<>();
		StringBuilder partidos = new StringBuilder();
		for (Partido p : historialPartidos(null, desde, hasta, null)) {
			if (p.getEstado() == EstadoPartido.JUGADO) {
				partidos.append("  ").append(p).append("\n");
				if (p instanceof PartidoInterno) {
					PartidoInterno interno = (PartidoInterno) p;
					sumarResultado(balance, interno.getLocal(), p.getMarcadorLocal(), p.getMarcadorVisitante());
					sumarResultado(balance, interno.getVisitante(), p.getMarcadorVisitante(), p.getMarcadorLocal());
				} else if (p instanceof PartidoCompetencia) {
					sumarResultado(balance, ((PartidoCompetencia) p).getEquipo(), p.getMarcadorLocal(), p.getMarcadorVisitante());
				}
			}
		}
		StringBuilder sb = new StringBuilder("Reporte de resultados del " + desde + " al " + hasta + "\n");
		if (partidos.length() == 0) {
			sb.append("(sin partidos jugados en el periodo)");
			return sb.toString();
		}
		sb.append("Equipos (internos y de competencia):\n");
		for (Map.Entry<String, int[]> e : balance.entrySet()) {
			sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()[0]).append(" victorias, ")
					.append(e.getValue()[1]).append(" empates, ").append(e.getValue()[2]).append(" derrotas\n");
		}
		sb.append("Partidos:\n").append(partidos);
		return sb.toString().stripTrailing();
	}

	private static void sumarResultado(Map<String, int[]> balance, Equipo equipo, int aFavor, int enContra) {
		int[] resultado = balance.computeIfAbsent(equipo.getDeporte().getNombre() + " - " + equipo.getNombre(), k -> new int[3]);
		if (aFavor > enContra) {
			resultado[0]++;
		} else if (aFavor == enContra) {
			resultado[1]++;
		} else {
			resultado[2]++;
		}
	}

	//Integracion etapa 3 (RF-ADM-21): reporte de evolucion de rankings (puntaje inicial y final del periodo,
	//variacion e historial de cada socio de cada academia), con la misma validacion de periodo

	private static void validarPeriodo(LocalDate desde, LocalDate hasta) {
		if (desde == null || hasta == null || desde.isAfter(hasta)) {
			throw new IllegalArgumentException("Periodo invalido: debe tener inicio y fin, y el inicio no puede ser posterior al fin");
		}
	}

	//null en un extremo = sin limite
	private static boolean enPeriodo(LocalDate fecha, LocalDate desde, LocalDate hasta) {
		return (desde == null || !fecha.isBefore(desde)) && (hasta == null || !fecha.isAfter(hasta));
	}
}
