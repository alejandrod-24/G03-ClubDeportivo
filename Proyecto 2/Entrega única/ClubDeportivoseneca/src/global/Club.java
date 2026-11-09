package global;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import deportes.Academia;
import deportes.DeporteConjunto;
import deportes.DeporteIndividual;
import deportes.Equipo;
import deportes.InscripcionAcademia;
import deportes.InscripcionEquipo;
import deportes.Nivel;
import deportes.RegistroRanking;
import entrenamiento.Asistencia;
import entrenamiento.RegistroEntrenamiento;
import entrenamiento.SesionEntrenamiento;
import instalaciones.Actividad;
import instalaciones.Instalacion;
import instalaciones.InstalacionConjunto;
import instalaciones.InstalacionIndividual;
import instalaciones.Modalidad;
import instalaciones.RegistroIngreso;
import instalaciones.Reserva;
import partidos.EstadisticaPartido;
import partidos.EstadoPartido;
import partidos.Partido;
import partidos.PartidoCompetencia;
import partidos.PartidoInterno;
import partidos.PartidoPractica;
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
	private final List<Instalacion> instalaciones;
	private final List<Reserva> reservas;
	private final List<SesionEntrenamiento> sesiones;
	private final List<RegistroIngreso> ingresos;
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
		this.instalaciones = new ArrayList<>();
		this.reservas = new ArrayList<>();
		this.sesiones = new ArrayList<>();
		this.ingresos = new ArrayList<>();
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
		for (Deporte d : deportes) {
			if (d instanceof DeporteIndividual) {
				todos.addAll(((DeporteIndividual) d).getPartidos());
			}
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

	//Socio: lo retira de sus equipos y academias, cancela sus reservas y practicas futuras e impide
	//iniciar sesion. Entrenador: se rechaza si tiene sesiones futuras. El historial se conserva.
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
			for (DeporteIndividual d : getDeportesIndividuales()) {
				if (d.getAcademia().estaInscrito(socio)) {
					d.getAcademia().retirar(socio);
				}
			}
			cancelarActividadesFuturas(socio, null);
		}
		if (persona instanceof Entrenador) {
			for (SesionEntrenamiento s : sesiones) {
				if (s.getEntrenador() == persona && s.estaVigente() && !s.haEmpezado()) {
					throw new IllegalStateException(login + " tiene sesiones de entrenamiento futuras (" + s.getId() + ")");
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
		sb.append("\nAcademias:");
		boolean tieneAcademias = false;
		for (DeporteIndividual d : getDeportesIndividuales()) {
			InscripcionAcademia i = d.getAcademia().buscarInscripcionActiva(socio);
			if (i != null) {
				tieneAcademias = true;
				sb.append("\n  ").append(d.getNombre()).append(": nivel ").append(i.getNivel())
						.append(" | ELO ").append(i.getRanking().getPuntaje())
						.append(" | posicion ").append(d.getAcademia().getPosicion(socio));
			}
		}
		if (!tieneAcademias) {
			sb.append(" (ninguna)");
		}
		RegistroIngreso dentro = ingresoActualDe(socio);
		if (dentro != null) {
			sb.append("\nEsta dentro de: ").append(dentro.getInstalacion().getNombre());
		}
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

	// ============================================================
	// Etapa 3: busquedas de instalaciones, academias, reservas, practicas y sesiones
	// ============================================================

	public List<DeporteIndividual> getDeportesIndividuales() {
		List<DeporteIndividual> resultado = new ArrayList<>();
		for (Deporte d : deportes) {
			if (d instanceof DeporteIndividual) {
				resultado.add((DeporteIndividual) d);
			}
		}
		return resultado;
	}

	public DeporteIndividual buscarDeporteIndividual(String nombre) {
		Deporte deporte = buscarDeporte(nombre);
		if (!(deporte instanceof DeporteIndividual)) {
			throw new IllegalArgumentException(nombre + " no es un deporte individual (no tiene academia)");
		}
		return (DeporteIndividual) deporte;
	}

	public void agregarInstalacion(Instalacion instalacion) {
		for (Instalacion i : instalaciones) {
			if (i.getCodigo().equalsIgnoreCase(instalacion.getCodigo())) {
				throw new IllegalArgumentException("Ya existe una instalacion con codigo " + instalacion.getCodigo());
			}
		}
		instalaciones.add(instalacion);
	}

	public List<Instalacion> getInstalaciones() {
		return Collections.unmodifiableList(instalaciones);
	}

	public Instalacion buscarInstalacion(String codigo) {
		for (Instalacion i : instalaciones) {
			if (i.getCodigo().equalsIgnoreCase(codigo)) {
				return i;
			}
		}
		throw new IllegalArgumentException("La instalacion " + codigo + " no existe");
	}

	public InstalacionIndividual buscarInstalacionIndividual(String codigo) {
		Instalacion instalacion = buscarInstalacion(codigo);
		if (!(instalacion instanceof InstalacionIndividual)) {
			throw new IllegalArgumentException(codigo + " no es una instalacion de academia");
		}
		return (InstalacionIndividual) instalacion;
	}

	public void agregarReserva(Reserva reserva) {
		reservas.add(reserva);
	}

	public List<Reserva> getReservas() {
		return Collections.unmodifiableList(reservas);
	}

	public Reserva buscarReserva(String id) {
		for (Reserva r : reservas) {
			if (r.getId().equalsIgnoreCase(id)) {
				return r;
			}
		}
		throw new IllegalArgumentException("La reserva " + id + " no existe");
	}

	public void agregarSesion(SesionEntrenamiento sesion) {
		sesiones.add(sesion);
	}

	public List<SesionEntrenamiento> getSesiones() {
		return Collections.unmodifiableList(sesiones);
	}

	public SesionEntrenamiento buscarSesion(String id) {
		for (SesionEntrenamiento s : sesiones) {
			if (s.getId().equalsIgnoreCase(id)) {
				return s;
			}
		}
		throw new IllegalArgumentException("La sesion " + id + " no existe");
	}

	public void agregarIngreso(RegistroIngreso ingreso) {
		ingresos.add(ingreso);
	}

	public List<RegistroIngreso> getIngresos() {
		return Collections.unmodifiableList(ingresos);
	}

	public RegistroIngreso ingresoActualDe(Socio socio) {
		for (RegistroIngreso r : ingresos) {
			if (r.getSocio() == socio && r.estaAdentro()) {
				return r;
			}
		}
		return null;
	}

	public PartidoPractica buscarPartidoPractica(String id) {
		Partido partido = buscarPartido(id);
		if (!(partido instanceof PartidoPractica)) {
			throw new IllegalArgumentException(id + " no es un partido de practica");
		}
		return (PartidoPractica) partido;
	}

	// ---- Cruces de horario: reservas, partidos de practica y sesiones son "actividades"

	public List<Actividad> getActividades() {
		List<Actividad> actividades = new ArrayList<>(reservas);
		for (DeporteIndividual d : getDeportesIndividuales()) {
			actividades.addAll(d.getPartidos());
		}
		actividades.addAll(sesiones);
		return actividades;
	}

	private Actividad buscarOcupacion(Instalacion instalacion, LocalDateTime inicio, LocalDateTime fin, Actividad excluida) {
		for (Actividad a : getActividades()) {
			if (a != excluida && a.estaVigente() && a.getInstalacion() == instalacion && a.seCruzaCon(inicio, fin)) {
				return a;
			}
		}
		return null;
	}

	private Actividad buscarCompromiso(Persona persona, LocalDateTime inicio, LocalDateTime fin, Actividad excluida) {
		for (Actividad a : getActividades()) {
			if (a != excluida && a.estaVigente() && a.involucra(persona) && a.seCruzaCon(inicio, fin)) {
				return a;
			}
		}
		return null;
	}

	//Cuantas instalaciones distintas de la disciplina estan en uso en ese horario
	private int instalacionesEnUso(DeporteIndividual deporte, LocalDateTime inicio, LocalDateTime fin, Actividad excluida) {
		List<Instalacion> enUso = new ArrayList<>();
		for (Actividad a : getActividades()) {
			if (a != excluida && a.estaVigente() && a.getInstalacion().getDeporte() == deporte && a.seCruzaCon(inicio, fin)
					&& !enUso.contains(a.getInstalacion())) {
				enUso.add(a.getInstalacion());
			}
		}
		return enUso.size();
	}

	private void validarInstalacionDisponible(Instalacion instalacion, LocalDateTime inicio, LocalDateTime fin, Actividad excluida) {
		if (!instalacion.estaActiva()) {
			throw new IllegalStateException("La instalacion " + instalacion.getCodigo() + " esta desactivada");
		}
		Actividad ocupacion = buscarOcupacion(instalacion, inicio, fin, excluida);
		if (ocupacion != null) {
			throw new IllegalStateException(instalacion.getNombre() + " esta ocupada en ese horario (" + describir(ocupacion) + ")");
		}
		if (instalacion.getDeporte() instanceof DeporteIndividual) {
			DeporteIndividual d = (DeporteIndividual) instalacion.getDeporte();
			if (instalacionesEnUso(d, inicio, fin, excluida) >= d.getMaximoSimultaneas()) {
				throw new IllegalStateException("Cupo agotado: ya hay " + d.getMaximoSimultaneas() + " instalaciones de "
						+ d.getNombre() + " en uso en ese horario (maximo simultaneo)");
			}
		}
	}

	private void validarSinCompromiso(Persona persona, LocalDateTime inicio, LocalDateTime fin, Actividad excluida) {
		Actividad compromiso = buscarCompromiso(persona, inicio, fin, excluida);
		if (compromiso != null) {
			throw new IllegalStateException(persona.getLogin() + " ya tiene otro compromiso en ese horario ("
					+ describir(compromiso) + ")");
		}
	}

	private static String describir(Actividad a) {
		if (a instanceof Reserva) {
			return "reserva " + ((Reserva) a).getId();
		}
		if (a instanceof PartidoPractica) {
			return "partido de practica " + ((PartidoPractica) a).getId();
		}
		return "sesion de entrenamiento " + ((SesionEntrenamiento) a).getId();
	}

	private static void validarFuturo(LocalDateTime inicio) {
		if (!Reloj.ahora().isBefore(inicio)) {
			throw new IllegalArgumentException("El horario " + inicio.toLocalDate() + " " + inicio.toLocalTime()
					+ " ya paso o ya comenzo");
		}
	}

	//Conjunto: estar en un equipo del deporte. Individual: estar inscrito en la academia.
	private boolean estaInscritoEnDeporte(Socio socio, Deporte deporte) {
		if (deporte instanceof DeporteConjunto) {
			return ((DeporteConjunto) deporte).getEquipoDe(socio) != null;
		}
		return ((DeporteIndividual) deporte).getAcademia().estaInscrito(socio);
	}

	//Retiro de academia (un deporte) o baja del socio (deporte = null): cancela reservas y practicas futuras
	private void cancelarActividadesFuturas(Socio socio, DeporteIndividual deporte) {
		for (Reserva r : reservas) {
			if (r.getSocio() == socio && r.estaVigente() && Reloj.ahora().isBefore(r.getInicio())
					&& (deporte == null || r.getInstalacion().getDeporte() == deporte)) {
				r.cancelarPorSistema();
			}
		}
		for (DeporteIndividual d : getDeportesIndividuales()) {
			if (deporte == null || d == deporte) {
				for (PartidoPractica p : d.getPartidos()) {
					if (p.involucra(socio)) {
						p.cancelarPorSistema();
					}
				}
			}
		}
	}

	// ============================================================
	// Administrador: instalaciones (RF-ADM-06)
	// ============================================================

	public InstalacionIndividual crearInstalacionIndividual(String codigo, String nombre, String deporte, int capacidad,
			boolean techada) {
		exigirRol(Administrador.class);
		InstalacionIndividual i = new InstalacionIndividual(codigo, nombre, buscarDeporteIndividual(deporte), capacidad, techada);
		agregarInstalacion(i);
		return i;
	}

	public InstalacionConjunto crearInstalacionConjunto(String codigo, String nombre, String deporte, int capacidad) {
		exigirRol(Administrador.class);
		InstalacionConjunto i = new InstalacionConjunto(codigo, nombre, buscarDeporteConjunto(deporte), capacidad);
		agregarInstalacion(i);
		return i;
	}

	public void editarInstalacion(String codigo, String nuevoNombre, int nuevaCapacidad) {
		exigirRol(Administrador.class);
		buscarInstalacion(codigo).editar(nuevoNombre, nuevaCapacidad);
	}

	public void desactivarInstalacion(String codigo) {
		exigirRol(Administrador.class);
		Instalacion instalacion = buscarInstalacion(codigo);
		for (Actividad a : getActividades()) {
			if (a.getInstalacion() == instalacion && a.estaVigente() && Reloj.ahora().isBefore(a.getFin())) {
				throw new IllegalStateException(instalacion.getNombre() + " tiene actividades futuras (" + describir(a) + ")");
			}
		}
		instalacion.desactivar();
	}

	// ============================================================
	// Socio: academias (RF-SOC-04, 05)
	// ============================================================

	public InscripcionAcademia inscribirseEnAcademia(String deporte, Nivel nivel, int eloInicial) {
		Socio socio = exigirRol(Socio.class);
		return buscarDeporteIndividual(deporte).getAcademia().inscribir(socio, nivel, eloInicial);
	}

	//El ranking historico se conserva; se cancelan sus reservas y partidos de practica futuros del deporte
	public void retirarseDeAcademia(String deporte) {
		Socio socio = exigirRol(Socio.class);
		DeporteIndividual d = buscarDeporteIndividual(deporte);
		d.getAcademia().retirar(socio);
		cancelarActividadesFuturas(socio, d);
	}

	// ============================================================
	// Socio: instalaciones y reservas (RF-SOC-06 a 09)
	// ============================================================

	public List<String> consultarDisponibilidad(String deporte, LocalDateTime bloque) {
		exigirSesion();
		DeporteIndividual d = buscarDeporteIndividual(deporte);
		Horario.validarBloque(bloque);
		LocalDateTime fin = Horario.finDeBloque(bloque);
		int cupo = d.getMaximoSimultaneas() - instalacionesEnUso(d, bloque, fin, null);
		List<String> lineas = new ArrayList<>();
		lineas.add("Cupo restante de " + d.getNombre() + " el " + bloque.toLocalDate() + " a las " + bloque.toLocalTime()
				+ ": " + cupo + " de " + d.getMaximoSimultaneas() + " instalaciones simultaneas");
		for (Instalacion i : instalaciones) {
			if (i.getDeporte() == d && i.estaActiva() && cupo > 0 && buscarOcupacion(i, bloque, fin, null) == null) {
				lineas.add("Libre: " + i);
			}
		}
		if (lineas.size() == 1) {
			lineas.add("(no hay instalaciones libres en ese bloque)");
		}
		return lineas;
	}

	public Reserva reservarInstalacion(String deporte, String codigoInstalacion, LocalDateTime bloque, int numeroJugadores,
			Modalidad modalidad) {
		Socio socio = exigirRol(Socio.class);
		DeporteIndividual d = buscarDeporteIndividual(deporte);
		if (!d.getAcademia().estaInscrito(socio)) {
			throw new IllegalStateException(socio.getLogin() + " no esta inscrito en la academia de " + d.getNombre());
		}
		InstalacionIndividual instalacion = buscarInstalacionIndividual(codigoInstalacion);
		if (instalacion.getDeporte() != d) {
			throw new IllegalArgumentException(instalacion.getNombre() + " no es de " + d.getNombre());
		}
		Horario.validarBloque(bloque);
		validarFuturo(bloque);
		if (modalidad == null || !d.permiteModalidad(modalidad)) {
			throw new IllegalArgumentException(d.getNombre() + " no se juega en modalidad " + modalidad
					+ " (modalidades: " + d.getModalidades() + ")");
		}
		if (numeroJugadores > instalacion.getCapacidad() || modalidad.getJugadores() > instalacion.getCapacidad()) {
			throw new IllegalArgumentException("Mas jugadores que la capacidad de " + instalacion.getNombre()
					+ " (" + instalacion.getCapacidad() + ")");
		}
		LocalDateTime fin = Horario.finDeBloque(bloque);
		validarInstalacionDisponible(instalacion, bloque, fin, null);
		validarSinCompromiso(socio, bloque, fin, null);
		Reserva reserva = new Reserva("R" + (reservas.size() + 1), socio, instalacion, bloque, numeroJugadores, modalidad);
		reservas.add(reserva);
		return reserva;
	}

	public void cancelarReserva(String id) {
		Socio socio = exigirRol(Socio.class);
		buscarReserva(id).cancelar(socio);
	}

	//Solo entra si tiene una reserva, partido o convocatoria en curso en esa instalacion
	public RegistroIngreso registrarIngreso(String codigoInstalacion) {
		Socio socio = exigirRol(Socio.class);
		Instalacion instalacion = buscarInstalacion(codigoInstalacion);
		RegistroIngreso actual = ingresoActualDe(socio);
		if (actual != null) {
			throw new IllegalStateException(socio.getLogin() + " ya esta en " + actual.getInstalacion().getNombre());
		}
		boolean autorizado = false;
		for (Actividad a : getActividades()) {
			if (a.estaVigente() && a.getInstalacion() == instalacion && a.involucra(socio) && a.estaEnCursoEn(Reloj.ahora())) {
				autorizado = true;
			}
		}
		if (!autorizado) {
			throw new IllegalStateException(socio.getLogin() + " no tiene reserva, partido ni convocatoria en "
					+ instalacion.getNombre() + " en este momento");
		}
		if (socio.tieneBebidaCaliente() && instalacion instanceof InstalacionIndividual
				&& ((InstalacionIndividual) instalacion).restringeBebidaCaliente()) {
			throw new IllegalStateException("No puede entrar a " + instalacion.getNombre() + " con una bebida caliente");
		}
		RegistroIngreso ingreso = new RegistroIngreso(socio, instalacion, Reloj.ahora());
		ingresos.add(ingreso);
		return ingreso;
	}

	public void registrarSalida() {
		Socio socio = exigirRol(Socio.class);
		RegistroIngreso actual = ingresoActualDe(socio);
		if (actual == null) {
			throw new IllegalStateException(socio.getLogin() + " no esta en ninguna instalacion");
		}
		actual.registrarSalida(Reloj.ahora());
	}

	// ============================================================
	// Socio: partidos de practica (RF-SOC-08, 10, 11)
	// ============================================================

	//El socio que programa es el primer participante. Con 2 participantes es sencillos y con 4 dobles
	//(lado A = los dos primeros). Un entrenador puede jugar y en ese caso no se exige nivel compatible.
	public PartidoPractica programarPartidoPractica(String deporte, String codigoInstalacion, LocalDateTime bloque,
			List<String> otrosParticipantes) {
		Socio socio = exigirRol(Socio.class);
		DeporteIndividual d = buscarDeporteIndividual(deporte);
		InstalacionIndividual instalacion = buscarInstalacionIndividual(codigoInstalacion);
		if (instalacion.getDeporte() != d) {
			throw new IllegalArgumentException(instalacion.getNombre() + " no es de " + d.getNombre());
		}
		Horario.validarBloque(bloque);
		validarFuturo(bloque);
		List<Persona> participantes = new ArrayList<>();
		participantes.add(socio);
		for (String login : otrosParticipantes) {
			Persona p = buscarPersona(login);
			if (!(p instanceof Socio) && !(p instanceof Entrenador)) {
				throw new IllegalArgumentException(login + " no puede jugar partidos de practica");
			}
			if (participantes.contains(p)) {
				throw new IllegalArgumentException(login + " esta repetido en el partido");
			}
			participantes.add(p);
		}
		Modalidad modalidad = Modalidad.deJugadores(participantes.size());
		if (!d.permiteModalidad(modalidad)) {
			throw new IllegalArgumentException(d.getNombre() + " no se juega en modalidad " + modalidad);
		}
		if (participantes.size() > instalacion.getCapacidad()) {
			throw new IllegalArgumentException("Los jugadores (" + participantes.size() + ") superan la capacidad de "
					+ instalacion.getNombre() + " (" + instalacion.getCapacidad() + ")");
		}
		boolean juegaEntrenador = false;
		List<InscripcionAcademia> inscritos = new ArrayList<>();
		for (Persona p : participantes) {
			if (!p.estaActivo()) {
				throw new IllegalStateException(p.getLogin() + " esta dado de baja");
			}
			if (p instanceof Entrenador) {
				juegaEntrenador = true;
			} else {
				InscripcionAcademia i = d.getAcademia().buscarInscripcionActiva((Socio) p);
				if (i == null) {
					throw new IllegalStateException(p.getLogin() + " no esta inscrito en la academia de " + d.getNombre());
				}
				inscritos.add(i);
			}
		}
		if (!juegaEntrenador) {
			validarNivelesCompatibles(inscritos);
		}
		LocalDateTime fin = Horario.finDeBloque(bloque);
		for (Persona p : participantes) {
			validarSinCompromiso(p, bloque, fin, null);
		}
		validarInstalacionDisponible(instalacion, bloque, fin, null);
		PartidoPractica partido = new PartidoPractica(generarIdPartido(), d, instalacion, bloque, socio, participantes);
		d.agregarPartido(partido);
		return partido;
	}

	private static void validarNivelesCompatibles(List<InscripcionAcademia> inscritos) {
		for (InscripcionAcademia a : inscritos) {
			for (InscripcionAcademia b : inscritos) {
				if (!a.getNivel().esCompatibleCon(b.getNivel())) {
					throw new IllegalArgumentException("Niveles no compatibles: " + a.getSocio().getLogin() + " (" + a.getNivel()
							+ ") y " + b.getSocio().getLogin() + " (" + b.getNivel() + ")");
				}
			}
		}
	}

	public void cancelarPartidoPractica(String id) {
		Socio socio = exigirRol(Socio.class);
		buscarPartidoPractica(id).cancelar(socio);
	}

	public String consultarPartidosYRanking() {
		Socio socio = exigirRol(Socio.class);
		StringBuilder sb = new StringBuilder("Academias:");
		boolean tieneAcademias = false;
		for (DeporteIndividual d : getDeportesIndividuales()) {
			InscripcionAcademia i = d.getAcademia().buscarInscripcion(socio);
			if (i != null) {
				tieneAcademias = true;
				sb.append("\n  ").append(d.getNombre()).append(": nivel ").append(i.getNivel())
						.append(" | ELO ").append(i.getRanking().getPuntaje());
				if (i.estaActiva()) {
					sb.append(" | posicion ").append(d.getAcademia().getPosicion(socio)).append(" de ")
							.append(d.getAcademia().getRankingActual().size());
				} else {
					sb.append(" | RETIRADO (ranking historico)");
				}
			}
		}
		if (!tieneAcademias) {
			sb.append(" (ninguna)");
		}
		sb.append("\nPartidos de practica:");
		boolean tienePartidos = false;
		for (DeporteIndividual d : getDeportesIndividuales()) {
			for (PartidoPractica p : d.getPartidos()) {
				if (p.involucra(socio)) {
					tienePartidos = true;
					sb.append("\n  ").append(p);
					if (p.getEstado() == EstadoPartido.JUGADO) {
						sb.append(p.gano(socio) ? " (GANO)" : " (PERDIO)");
						EstadisticaPartido e = p.getEstadistica(socio.getLogin());
						if (e != null) {
							sb.append("\n      ").append(e);
						}
					}
				}
			}
		}
		if (!tienePartidos) {
			sb.append(" (ninguno)");
		}
		return sb.toString();
	}

	// ============================================================
	// Socio: desempeno en entrenamiento y calendario (RF-SOC-12, 13)
	// ============================================================

	public String consultarDesempeno() {
		Socio socio = exigirRol(Socio.class);
		StringBuilder sb = new StringBuilder("Sesiones de entrenamiento terminadas:");
		boolean hay = false;
		for (SesionEntrenamiento s : sesiones) {
			RegistroEntrenamiento r = s.buscarRegistro(socio);
			if (r != null && s.estaVigente() && s.haTerminado()) {
				hay = true;
				sb.append("\n  [").append(s.getId()).append("] ").append(s.getInicio().toLocalDate()).append(" ")
						.append(s.getInstalacion().getNombre()).append(" (").append(s.getEntrenador().getLogin()).append("): ")
						.append(r.getAsistencia() == null ? "asistencia sin registrar" : r.getAsistencia())
						.append(r.getNota() == null ? "" : " | nota " + r.getNota())
						.append(r.getComentario() == null || r.getComentario().isEmpty() ? "" : " | \"" + r.getComentario() + "\"");
			}
		}
		if (!hay) {
			sb.append(" (ninguna)");
		}
		sb.append("\nResumen: ").append(describirAsistencia(contarAsistencia(socio, null, null)));
		return sb.toString();
	}

	//Sesiones, reservas, partidos de practica, internos y de competencia futuros, en orden
	public List<String> consultarCalendario() {
		Socio socio = exigirRol(Socio.class);
		Map<String, LocalDateTime> eventos = new LinkedHashMap<>();
		LocalDateTime ahora = Reloj.ahora();
		for (Actividad a : getActividades()) {
			if (a.estaVigente() && a.involucra(socio) && a.getFin().isAfter(ahora)
					&& !(a instanceof PartidoPractica && ((PartidoPractica) a).getEstado() != EstadoPartido.PROGRAMADO)) {
				eventos.put(a.getInicio().toLocalDate() + " " + a.getInicio().toLocalTime() + "  " + describirParaCalendario(a), a.getInicio());
			}
		}
		for (Partido p : getTodosLosPartidos()) {
			if (p.getEstado() == EstadoPartido.PROGRAMADO && !p.getFecha().isBefore(ahora.toLocalDate())
					&& juegaPartidoDeEquipo(socio, p)) {
				eventos.put(p.getFecha() + "        " + p, p.getFecha().atStartOfDay());
			}
		}
		List<String> calendario = new ArrayList<>(eventos.keySet());
		calendario.sort((a, b) -> eventos.get(a).compareTo(eventos.get(b)));
		return calendario;
	}

	private static String describirParaCalendario(Actividad a) {
		if (a instanceof Reserva) {
			return "Reserva " + a;
		}
		if (a instanceof PartidoPractica) {
			return "Partido de practica " + a;
		}
		return "Entrenamiento " + a;
	}

	private boolean juegaPartidoDeEquipo(Socio socio, Partido partido) {
		if (partido instanceof PartidoInterno) {
			PartidoInterno p = (PartidoInterno) partido;
			return p.getLocal().estaInscrito(socio) || p.getVisitante().estaInscrito(socio);
		}
		if (partido instanceof PartidoCompetencia) {
			PartidoCompetencia p = (PartidoCompetencia) partido;
			return p.getCompetencia().buscarInscripcion(p.getEquipo()) != null
					&& p.getCompetencia().buscarInscripcion(p.getEquipo()).estaEnNomina(socio);
		}
		return false;
	}

	// ============================================================
	// Entrenador: academias y partidos de practica (RF-ENT-09, 10)
	// ============================================================

	public void asignarNivel(String deporte, String login, Nivel nuevoNivel) {
		exigirRol(Entrenador.class);
		DeporteIndividual d = buscarDeporteIndividual(deporte);
		Socio socio = buscarSocio(login);
		InscripcionAcademia inscripcion = d.getAcademia().buscarInscripcionActiva(socio);
		if (inscripcion == null) {
			throw new IllegalStateException(login + " no esta inscrito en la academia de " + d.getNombre());
		}
		if (nuevoNivel == null) {
			throw new IllegalArgumentException("Debe indicar el nivel");
		}
		//No puede dejar inhabilitado un partido de practica ya programado
		for (PartidoPractica p : d.getPartidos()) {
			if (p.esFuturoProgramado() && p.involucra(socio)) {
				boolean juegaEntrenador = false;
				for (Persona otro : p.getParticipantes()) {
					juegaEntrenador = juegaEntrenador || otro instanceof Entrenador;
				}
				for (Socio otro : p.getAlineados()) {
					InscripcionAcademia i = d.getAcademia().buscarInscripcionActiva(otro);
					if (!juegaEntrenador && otro != socio && i != null && !nuevoNivel.esCompatibleCon(i.getNivel())) {
						throw new IllegalStateException("El cambio a " + nuevoNivel + " inhabilita el partido programado "
								+ p.getId() + " con " + otro.getLogin() + " (" + i.getNivel() + ")");
					}
				}
			}
		}
		inscripcion.cambiarNivel(nuevoNivel);
	}

	//Marcador en sets (lado A - lado B). Actualiza el ELO de los socios que jugaron.
	public void registrarResultadoPractica(String idPartido, int setsA, int setsB, List<EstadisticaPartido> estadisticas) {
		exigirRol(Entrenador.class);
		PartidoPractica partido = buscarPartidoPractica(idPartido);
		partido.registrarResultado(setsA, setsB, estadisticas);
		partido.getDeporte().getAcademia().actualizarRanking(partido);
	}

	// ============================================================
	// Entrenador: sesiones de entrenamiento (RF-ENT-11 a 14, 16)
	// ============================================================

	public SesionEntrenamiento planificarSesion(String codigoInstalacion, LocalDateTime inicio, int duracionMinutos,
			List<String> convocados) {
		Entrenador entrenador = exigirRol(Entrenador.class);
		Instalacion instalacion = buscarInstalacion(codigoInstalacion);
		List<Socio> socios = buscarSocios(convocados);
		validarSesion(entrenador, instalacion, inicio, duracionMinutos, socios, null);
		SesionEntrenamiento sesion = new SesionEntrenamiento("S" + (sesiones.size() + 1), entrenador, instalacion, inicio,
				duracionMinutos, socios);
		sesiones.add(sesion);
		return sesion;
	}

	public void modificarSesion(String id, String codigoInstalacion, LocalDateTime inicio, int duracionMinutos,
			List<String> convocados) {
		Entrenador entrenador = exigirRol(Entrenador.class);
		SesionEntrenamiento sesion = buscarSesionPropia(entrenador, id);
		Instalacion instalacion = buscarInstalacion(codigoInstalacion);
		List<Socio> socios = buscarSocios(convocados);
		if (sesion.haEmpezado()) {
			throw new IllegalStateException("La sesion " + id + " ya empezo o termino");
		}
		validarSesion(entrenador, instalacion, inicio, duracionMinutos, socios, sesion);
		sesion.modificar(instalacion, inicio, duracionMinutos, socios);
	}

	public void cancelarSesion(String id) {
		Entrenador entrenador = exigirRol(Entrenador.class);
		buscarSesionPropia(entrenador, id).cancelar();
	}

	public void registrarAsistencia(String idSesion, String login, Asistencia asistencia) {
		Entrenador entrenador = exigirRol(Entrenador.class);
		buscarSesionPropia(entrenador, idSesion).registrarAsistencia(buscarSocio(login), asistencia);
	}

	public void registrarEvaluacion(String idSesion, String login, int nota, String comentario) {
		Entrenador entrenador = exigirRol(Entrenador.class);
		buscarSesionPropia(entrenador, idSesion).evaluar(buscarSocio(login), nota, comentario);
	}

	private SesionEntrenamiento buscarSesionPropia(Entrenador entrenador, String id) {
		SesionEntrenamiento sesion = buscarSesion(id);
		if (sesion.getEntrenador() != entrenador) {
			throw new IllegalStateException("La sesion " + id + " es de " + sesion.getEntrenador().getLogin());
		}
		return sesion;
	}

	private void validarSesion(Entrenador entrenador, Instalacion instalacion, LocalDateTime inicio, int duracionMinutos,
			List<Socio> convocados, SesionEntrenamiento excluida) {
		if (duracionMinutos <= 0) {
			throw new IllegalArgumentException("La duracion debe ser mayor a cero");
		}
		if (inicio == null) {
			throw new IllegalArgumentException("Debe indicar fecha y hora");
		}
		validarFuturo(inicio);
		LocalDateTime fin = inicio.plusMinutes(duracionMinutos);
		Horario.validarIntervalo(inicio, fin);
		if (convocados.isEmpty()) {
			throw new IllegalArgumentException("La sesion debe tener al menos un convocado");
		}
		validarInstalacionDisponible(instalacion, inicio, fin, excluida);
		Actividad otra = buscarCompromiso(entrenador, inicio, fin, excluida);
		if (otra != null) {
			throw new IllegalStateException("Se cruza con otra actividad del entrenador (" + describir(otra) + ")");
		}
		List<Socio> vistos = new ArrayList<>();
		for (Socio s : convocados) {
			if (vistos.contains(s)) {
				throw new IllegalArgumentException(s.getLogin() + " esta repetido en la convocatoria");
			}
			vistos.add(s);
			if (!s.estaActivo() || !estaInscritoEnDeporte(s, instalacion.getDeporte())) {
				throw new IllegalArgumentException(s.getLogin() + " no esta inscrito en " + instalacion.getDeporte().getNombre());
			}
			validarSinCompromiso(s, inicio, fin, excluida);
		}
	}

	//Sesiones propias, partidos de practica en los que juega y partidos del club programados
	public List<String> consultarAgenda() {
		Entrenador entrenador = exigirRol(Entrenador.class);
		Map<String, LocalDateTime> eventos = new LinkedHashMap<>();
		LocalDateTime ahora = Reloj.ahora();
		for (Actividad a : getActividades()) {
			if (a.estaVigente() && a.involucra(entrenador) && a.getFin().isAfter(ahora)) {
				eventos.put(a.getInicio().toLocalDate() + " " + a.getInicio().toLocalTime() + "  " + describirParaCalendario(a),
						a.getInicio());
			}
		}
		for (Partido p : getTodosLosPartidos()) {
			if (!(p instanceof PartidoPractica) && p.getEstado() == EstadoPartido.PROGRAMADO
					&& !p.getFecha().isBefore(ahora.toLocalDate())) {
				eventos.put(p.getFecha() + "        " + p, p.getFecha().atStartOfDay());
			}
		}
		List<String> agenda = new ArrayList<>(eventos.keySet());
		agenda.sort((a, b) -> eventos.get(a).compareTo(eventos.get(b)));
		return agenda;
	}

	// ============================================================
	// Analiticas de entrenamiento (RF-ENT-15)
	// ============================================================

	//{convocatorias, asistencias, tardanzas, faltas, suma de notas, cantidad de notas} en sesiones terminadas del periodo
	private int[] contarAsistencia(Socio socio, LocalDate desde, LocalDate hasta) {
		int[] c = new int[6];
		for (SesionEntrenamiento s : sesiones) {
			RegistroEntrenamiento r = s.buscarRegistro(socio);
			if (r != null && s.estaVigente() && enPeriodo(s.getInicio().toLocalDate(), desde, hasta)) {
				c[0]++;
				if (r.getAsistencia() == Asistencia.ASISTIO) {
					c[1]++;
				} else if (r.getAsistencia() == Asistencia.LLEGO_TARDE) {
					c[2]++;
				} else if (r.getAsistencia() == Asistencia.FALTO) {
					c[3]++;
				}
				if (r.getNota() != null) {
					c[4] += r.getNota();
					c[5]++;
				}
			}
		}
		return c;
	}

	private static String describirAsistencia(int[] c) {
		int registradas = c[1] + c[2] + c[3];
		String porcentaje = registradas == 0 ? "-" : (100 * (c[1] + c[2]) / registradas) + "%";
		String nota = c[5] == 0 ? "-" : String.format("%.1f", (double) c[4] / c[5]);
		return c[0] + " convocatorias | " + c[1] + " asistencias | " + c[2] + " tardanzas | " + c[3] + " faltas | asistencia "
				+ porcentaje + " | nota promedio " + nota;
	}

	public String consultarAnaliticasJugador(String login, LocalDate desde, LocalDate hasta) {
		exigirRol(Entrenador.class);
		validarPeriodo(desde, hasta);
		Socio socio = buscarSocio(login);
		StringBuilder sb = new StringBuilder("Analiticas de " + socio.getNombre() + " del " + desde + " al " + hasta);
		sb.append("\n  Entrenamiento: ").append(describirAsistencia(contarAsistencia(socio, desde, hasta)));
		for (DeporteIndividual d : getDeportesIndividuales()) {
			InscripcionAcademia i = d.getAcademia().buscarInscripcion(socio);
			if (i != null) {
				int jugados = 0;
				int ganados = 0;
				for (PartidoPractica p : d.getPartidos()) {
					if (p.involucra(socio) && p.getEstado() == EstadoPartido.JUGADO && enPeriodo(p.getFecha(), desde, hasta)) {
						jugados++;
						ganados += p.gano(socio) ? 1 : 0;
					}
				}
				sb.append("\n  ").append(d.getNombre()).append(": nivel ").append(i.getNivel()).append(" | ELO ")
						.append(i.getRanking().getPuntaje()).append(" | practicas jugadas ").append(jugados)
						.append(" (ganadas ").append(ganados).append(")");
			}
		}
		for (DeporteConjunto d : getDeportesConjunto()) {
			Equipo e = d.getEquipoDe(socio);
			if (e != null) {
				int partidos = 0;
				int anotaciones = 0;
				for (Partido p : getTodosLosPartidos()) {
					EstadisticaPartido est = p.getEstadistica(socio.getLogin());
					if (p.getDeporte() == d && est != null && enPeriodo(p.getFecha(), desde, hasta)) {
						partidos++;
						anotaciones += est.getAnotaciones();
					}
				}
				sb.append("\n  ").append(d.getNombre()).append(" (").append(e.getNombre()).append("): ").append(partidos)
						.append(" partidos con estadisticas, ").append(anotaciones).append(" anotaciones");
			}
		}
		return sb.toString();
	}

	//Grupo = un equipo (deporte de conjunto) o una academia (deporte individual, equipo = null)
	public String consultarAnaliticasGrupo(String deporte, String nombreEquipo, LocalDate desde, LocalDate hasta) {
		exigirRol(Entrenador.class);
		validarPeriodo(desde, hasta);
		Deporte d = buscarDeporte(deporte);
		List<Socio> miembros;
		String nombreGrupo;
		if (d instanceof DeporteConjunto) {
			if (nombreEquipo == null) {
				throw new IllegalArgumentException("Para un deporte de conjunto debe indicar el equipo");
			}
			Equipo equipo = buscarEquipo(deporte, nombreEquipo);
			miembros = equipo.getJugadores();
			nombreGrupo = equipo.getNombre();
		} else {
			Academia academia = ((DeporteIndividual) d).getAcademia();
			miembros = new ArrayList<>();
			for (InscripcionAcademia i : academia.getRankingActual()) {
				miembros.add(i.getSocio());
			}
			nombreGrupo = academia.getNombre();
		}
		StringBuilder sb = new StringBuilder("Analiticas de " + nombreGrupo + " del " + desde + " al " + hasta);
		int[] total = new int[6];
		for (Socio s : miembros) {
			int[] c = contarAsistencia(s, desde, hasta);
			for (int k = 0; k < total.length; k++) {
				total[k] += c[k];
			}
			sb.append("\n  ").append(s.getLogin()).append(": ").append(describirAsistencia(c));
		}
		sb.append("\n  TOTAL: ").append(describirAsistencia(total));
		return sb.toString();
	}

	// ============================================================
	// Administrador: historiales y reportes de la etapa 3 (RF-ADM-15, 17, 19, 21)
	// ============================================================

	//Incluye las canceladas. Filtros en null = sin filtro. login = socio que reservo.
	public List<Reserva> historialReservas(String deporte, LocalDate desde, LocalDate hasta, String codigoInstalacion,
			String login) {
		exigirRol(Administrador.class);
		validarFiltros(deporte, desde, hasta, login);
		if (codigoInstalacion != null) {
			buscarInstalacion(codigoInstalacion);
		}
		List<Reserva> resultado = new ArrayList<>();
		for (Reserva r : reservas) {
			if ((deporte == null || r.getInstalacion().getDeporte().getNombre().equalsIgnoreCase(deporte))
					&& enPeriodo(r.getInicio().toLocalDate(), desde, hasta)
					&& (codigoInstalacion == null || r.getInstalacion().getCodigo().equalsIgnoreCase(codigoInstalacion))
					&& (login == null || r.getSocio().getLogin().equals(login))) {
				resultado.add(r);
			}
		}
		resultado.sort((a, b) -> a.getInicio().compareTo(b.getInicio()));
		return resultado;
	}

	//Con asistencia y evaluaciones. login = entrenador responsable o convocado.
	public List<String> historialSesiones(String deporte, LocalDate desde, LocalDate hasta, String codigoInstalacion,
			String login) {
		exigirRol(Administrador.class);
		validarFiltros(deporte, desde, hasta, login);
		if (codigoInstalacion != null) {
			buscarInstalacion(codigoInstalacion);
		}
		List<SesionEntrenamiento> filtradas = new ArrayList<>();
		for (SesionEntrenamiento s : sesiones) {
			if ((deporte == null || s.getInstalacion().getDeporte().getNombre().equalsIgnoreCase(deporte))
					&& enPeriodo(s.getInicio().toLocalDate(), desde, hasta)
					&& (codigoInstalacion == null || s.getInstalacion().getCodigo().equalsIgnoreCase(codigoInstalacion))
					&& (login == null || s.involucra(buscarPersona(login)))) {
				filtradas.add(s);
			}
		}
		filtradas.sort((a, b) -> a.getInicio().compareTo(b.getInicio()));
		List<String> resultado = new ArrayList<>();
		for (SesionEntrenamiento s : filtradas) {
			resultado.add(s.generarDetalle());
		}
		return resultado;
	}

	public String reporteAsistencias(LocalDate desde, LocalDate hasta) {
		exigirRol(Administrador.class);
		validarPeriodo(desde, hasta);
		List<Socio> convocados = new ArrayList<>();
		for (SesionEntrenamiento s : sesiones) {
			if (s.estaVigente() && enPeriodo(s.getInicio().toLocalDate(), desde, hasta)) {
				for (Socio socio : s.getConvocados()) {
					if (!convocados.contains(socio)) {
						convocados.add(socio);
					}
				}
			}
		}
		StringBuilder sb = new StringBuilder("Reporte de asistencias del " + desde + " al " + hasta);
		if (convocados.isEmpty()) {
			return sb.append("\n(sin sesiones de entrenamiento en el periodo)").toString();
		}
		int[] total = new int[6];
		for (Socio socio : convocados) {
			int[] c = contarAsistencia(socio, desde, hasta);
			for (int k = 0; k < total.length; k++) {
				total[k] += c[k];
			}
			sb.append("\n  ").append(socio.getLogin()).append(": ").append(describirAsistencia(c));
		}
		sb.append("\n  TOTAL: ").append(describirAsistencia(total));
		return sb.toString();
	}

	public String reporteRankings(LocalDate desde, LocalDate hasta) {
		exigirRol(Administrador.class);
		validarPeriodo(desde, hasta);
		StringBuilder sb = new StringBuilder("Reporte de evolucion de rankings del " + desde + " al " + hasta);
		for (DeporteIndividual d : getDeportesIndividuales()) {
			sb.append("\n").append(d.getAcademia().getNombre()).append(":");
			boolean hay = false;
			for (InscripcionAcademia i : d.getAcademia().getInscripciones()) {
				Integer fin = i.getRanking().getPuntajeEn(hasta);
				if (fin == null) {
					continue;
				}
				List<RegistroRanking> enPeriodo = new ArrayList<>();
				for (RegistroRanking r : i.getRanking().getHistorial()) {
					if (enPeriodo(r.getFecha(), desde, hasta)) {
						enPeriodo.add(r);
					}
				}
				Integer inicio = i.getRanking().getPuntajeEn(desde.minusDays(1));
				if (inicio == null) {
					inicio = enPeriodo.get(0).getPuntaje();
				}
				hay = true;
				int variacion = fin - inicio;
				sb.append("\n  ").append(i.getSocio().getLogin()).append(": inicial ").append(inicio).append(" | final ")
						.append(fin).append(" | variacion ").append(variacion >= 0 ? "+" : "").append(variacion);
				for (RegistroRanking r : enPeriodo) {
					sb.append("\n      ").append(r);
				}
			}
			if (!hay) {
				sb.append("\n  (sin inscritos en el periodo)");
			}
		}
		return sb.toString();
	}


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
			RegistroIngreso dentro = ingresoActualDe((Socio) comprador);
			if (dentro != null && dentro.getInstalacion() instanceof InstalacionIndividual
					&& ((InstalacionIndividual) dentro.getInstalacion()).restringeBebidaCaliente()) {
				throw new IllegalStateException("No se despachan bebidas calientes dentro de " + dentro.getInstalacion().getNombre());
			}
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
		lineas.add("Instalaciones:");
		for (Instalacion i : instalaciones) {
			lineas.add("  " + i);
		}
		return lineas;
	}

	//Al aprobarla el producto entra al catalogo: categorias "Snack..." o "Bebida..." van a la cafeteria
	//(bebida caliente si la categoria dice "caliente"); las demas a la tienda, sin existencias
	public Producto aprobarSugerencia(String id) {
		exigirRol(Administrador.class);
		SugerenciaProducto sugerencia = buscarSugerencia(id);
		if (existeProducto(sugerencia.getNombre())) {
			throw new IllegalStateException(sugerencia.getNombre() + " ya existe en el catalogo");
		}
		String categoria = sugerencia.getCategoria().toLowerCase();
		Producto producto;
		if (categoria.startsWith("snack") || categoria.startsWith("bebida")) {
			producto = new ProductoCafeteria(sugerencia.getNombre(), sugerencia.getPrecioSugerido(),
					categoria.contains("caliente"), sugerencia.getAlergenos());
		} else {
			producto = new ArticuloDeportivo(sugerencia.getNombre(), sugerencia.getCategoria(), sugerencia.getPrecioSugerido());
		}
		sugerencia.aprobar();
		agregarProducto(producto);
		return producto;
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

	//Filtros en null = sin filtro. login: partidos en los que jugo esa persona (incluye al entrenador que juega una practica).
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

	//Victorias, empates y derrotas de cada equipo del club, y el marcador de cada partido jugado.
	//Los partidos de practica aparecen en la lista de partidos.
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
