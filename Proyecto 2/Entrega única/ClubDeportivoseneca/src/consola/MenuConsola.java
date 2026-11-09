package consola;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import administracion.TipoPeriodo;
import competencias.Fase;
import deportes.Categoria;
import deportes.Nivel;
import entrenamiento.Asistencia;
import global.Club;
import global.Reloj;
import instalaciones.Modalidad;
import partidos.EstadisticaBaloncesto;
import partidos.EstadisticaFutbol;
import partidos.EstadisticaPadel;
import partidos.EstadisticaPartido;
import partidos.EstadisticaTenis;
import partidos.EstadisticaTenisDeMesa;
import partidos.EstadisticaVoleibol;
import persistencia.Persistencia;
import personas.Administrador;
import personas.Entrenador;
import personas.Persona;
import personas.Socio;

/**
 * Aplicacion de consola del Club Deportivo Seneca.
 *
 * Como funciona:
 * 1. Al iniciar carga el estado desde la carpeta datos/ (o crea el club de ejemplo si esta vacia).
 * 2. El usuario inicia sesion y ve el menu de su rol (socio, entrenador, fisioterapeuta o administrador).
 * 3. Cada opcion pide solo los datos necesarios. Si el sistema rechaza la accion se muestra el motivo.
 * 4. Despues de cada accion el estado se guarda automaticamente en datos/.
 *
 * Ejecutar desde la carpeta del proyecto:  java -cp bin consola.MenuConsola
 */
public class MenuConsola {

	private static final Scanner entrada = new Scanner(System.in);
	private static final Persistencia persistencia = new Persistencia();
	private static Club club;

	public static void main(String[] args) {
		System.out.println("==============================================");
		System.out.println("        CLUB DEPORTIVO SENECA - Consola");
		System.out.println("==============================================");
		club = persistencia.cargarOCrear();
		System.out.println(persistencia.existenDatos() ? "Estado cargado desde datos/." : "Club de ejemplo creado (aun no hay datos guardados).");
		guardar();
		System.out.println("Usuarios de ejemplo: admin/admin123, carlos/carlos123 (entrenador), pedro/pedro123 (fisioterapeuta),");
		System.out.println("ana, diego, valentina... (socios, password 1234)");

		boolean seguir = true;
		while (seguir) {
			Persona usuario = club.getUsuarioActual();
			if (usuario == null) {
				seguir = menuInicio();
			} else if (usuario instanceof Socio) {
				menuSocio();
			} else if (usuario instanceof Entrenador) {
				menuEntrenador();
			} else if (usuario instanceof Administrador) {
				menuAdministrador();
			} else {
				menuFisioterapeuta();
			}
		}
		guardar();
		System.out.println("Estado guardado en datos/. Hasta pronto.");
	}

	// ============================================================
	// Menu de inicio
	// ============================================================

	private static boolean menuInicio() {
		titulo("Inicio  (ahora: " + mostrarAhora() + ")");
		System.out.println(" 1. Iniciar sesion");
		System.out.println(" 2. Cambiar la fecha y hora (simulacion)");
		System.out.println(" 0. Guardar y salir");
		switch (leerOpcion()) {
		case 1:
			ejecutar(() -> {
				Persona p = club.iniciarSesion(leerTexto("Login"), leerTexto("Password"));
				System.out.println("Bienvenido(a), " + p.getNombre() + " (" + p.getRol() + ")");
			});
			return true;
		case 2:
			cambiarReloj();
			return true;
		case 0:
			return false;
		default:
			System.out.println("Opcion invalida");
			return true;
		}
	}

	// ============================================================
	// Socio
	// ============================================================

	private static void menuSocio() {
		titulo("Menu del socio " + club.getUsuarioActual().getLogin() + "  (" + mostrarAhora() + ")");
		seccion("Mi cuenta");
		System.out.println("  1. Ver mi perfil");
		System.out.println("  2. Mi calendario");
		System.out.println("  3. Pagar la mensualidad del mes");
		seccion("Equipos");
		System.out.println("  4. Inscribirme a un equipo");
		System.out.println("  5. Retirarme de un equipo");
		seccion("Academias, instalaciones y practicas");
		System.out.println("  6. Inscribirme a una academia");
		System.out.println("  7. Retirarme de una academia");
		System.out.println("  8. Consultar disponibilidad de instalaciones");
		System.out.println("  9. Reservar una instalacion");
		System.out.println(" 10. Cancelar una reserva");
		System.out.println(" 11. Programar un partido de practica");
		System.out.println(" 12. Cancelar un partido de practica");
		System.out.println(" 13. Registrar ingreso a una instalacion");
		System.out.println(" 14. Registrar salida de la instalacion");
		System.out.println(" 15. Mis partidos de practica y ranking");
		System.out.println(" 16. Mi desempeno en entrenamientos");
		seccion("Tienda y cafeteria");
		System.out.println(" 17. Comprar en la tienda");
		System.out.println(" 18. Comprar en la cafeteria");
		System.out.println(" 19. Mis puntos y compras");
		System.out.println(" 20. Ya termine mi bebida caliente");
		opcionesComunes();
		int opcion = leerOpcion();
		switch (opcion) {
		case 1: ejecutar(() -> System.out.println(club.consultarPerfil())); break;
		case 2: ejecutar(() -> mostrar(club.consultarCalendario())); break;
		case 3: ejecutar(() -> System.out.println("Pagada: " + club.pagarMensualidad())); break;
		case 4: ejecutar(() -> club.inscribirseEnEquipo(leerTexto("Deporte (Futbol, Baloncesto, Voleibol)"), leerTexto("Equipo"),
				leerTexto("Posicion"), leerEntero("Numero de camiseta"))); break;
		case 5: ejecutar(() -> club.retirarseDeEquipo(leerTexto("Deporte"), leerTexto("Equipo"))); break;
		case 6: ejecutar(() -> System.out.println("Inscrito: " + club.inscribirseEnAcademia(leerTexto("Deporte (Tenis, Padel, Tenis de mesa)"),
				leerEnum("Nivel", Nivel.class), leerEntero("ELO inicial (ej. 1500)")))); break;
		case 7: ejecutar(() -> club.retirarseDeAcademia(leerTexto("Deporte"))); break;
		case 8: ejecutar(() -> mostrar(club.consultarDisponibilidad(leerTexto("Deporte"), leerFechaHora()))); break;
		case 9: ejecutar(() -> System.out.println("Reservada: " + club.reservarInstalacion(leerTexto("Deporte"),
				leerTexto("Codigo de la instalacion (ej. TEN-1)"), leerFechaHora(), leerEntero("Numero de jugadores"),
				leerEnum("Modalidad", Modalidad.class)))); break;
		case 10: ejecutar(() -> club.cancelarReserva(leerTexto("Id de la reserva (ej. R1)"))); break;
		case 11: ejecutar(() -> System.out.println("Programado: " + club.programarPartidoPractica(leerTexto("Deporte"),
				leerTexto("Codigo de la instalacion"), leerFechaHora(),
				leerLista("Otros participantes (1 para sencillos, 3 para dobles; puede incluir un entrenador)")))); break;
		case 12: ejecutar(() -> club.cancelarPartidoPractica(leerTexto("Id del partido (ej. P3)"))); break;
		case 13: ejecutar(() -> System.out.println("Ingreso: " + club.registrarIngreso(leerTexto("Codigo de la instalacion")))); break;
		case 14: ejecutar(() -> club.registrarSalida()); break;
		case 15: ejecutar(() -> System.out.println(club.consultarPartidosYRanking())); break;
		case 16: ejecutar(() -> System.out.println(club.consultarDesempeno())); break;
		case 17: comprarEnTienda(true); break;
		case 18: comprarEnCafeteria(); break;
		case 19: ejecutar(() -> System.out.println(club.consultarPuntos())); break;
		case 20: ejecutar(() -> club.declararFinBebidaCaliente()); break;
		default: opcionComun(opcion);
		}
	}

	// ============================================================
	// Empleados: entrenador y fisioterapeuta
	// ============================================================

	private static void menuFisioterapeuta() {
		titulo("Menu del fisioterapeuta " + club.getUsuarioActual().getLogin() + "  (" + mostrarAhora() + ")");
		opcionesEmpleado();
		opcionesComunes();
		int opcion = leerOpcion();
		if (!opcionEmpleado(opcion)) {
			opcionComun(opcion);
		}
	}

	private static void menuEntrenador() {
		titulo("Menu del entrenador " + club.getUsuarioActual().getLogin() + "  (" + mostrarAhora() + ")");
		opcionesEmpleado();
		seccion("Partidos internos");
		System.out.println(" 31. Programar partido interno");
		System.out.println(" 32. Registrar alineacion y marcador de un partido interno");
		System.out.println(" 33. Registrar estadisticas y tarjetas de un jugador (cualquier partido)");
		System.out.println(" 34. Declarar partido no jugado");
		System.out.println(" 35. Consultar sanciones de un equipo");
		seccion("Competencias");
		System.out.println(" 36. Inscribir equipo en competencia");
		System.out.println(" 37. Definir nomina");
		System.out.println(" 38. Programar partido de competencia");
		System.out.println(" 39. Registrar partido de competencia");
		seccion("Academias");
		System.out.println(" 40. Asignar nivel a un socio");
		System.out.println(" 41. Registrar resultado de un partido de practica");
		seccion("Entrenamientos");
		System.out.println(" 42. Planificar sesion");
		System.out.println(" 43. Modificar sesion");
		System.out.println(" 44. Cancelar sesion");
		System.out.println(" 45. Registrar asistencia");
		System.out.println(" 46. Registrar evaluacion");
		System.out.println(" 47. Analiticas de un jugador");
		System.out.println(" 48. Analiticas de un equipo o academia");
		System.out.println(" 49. Mi agenda");
		opcionesComunes();
		int opcion = leerOpcion();
		if (opcionEmpleado(opcion)) {
			return;
		}
		switch (opcion) {
		case 31: ejecutar(() -> System.out.println("Programado: " + club.programarPartidoInterno(leerTexto("Deporte"),
				leerTexto("Equipo local"), leerTexto("Equipo visitante"), leerFecha("Fecha")))); break;
		case 32: ejecutar(() -> club.registrarResultadoInterno(leerTexto("Id del partido (ej. P1)"), leerLista("Alineacion local"),
				leerLista("Alineacion visitante"), leerEntero("Marcador local"), leerEntero("Marcador visitante"))); break;
		case 33: ejecutar(() -> {
			String id = leerTexto("Id del partido");
			String deporte = club.buscarPartido(id).getDeporte().getNombre();
			club.registrarEstadistica(id, leerEstadistica(deporte, leerTexto("Login del jugador")));
		}); break;
		case 34: ejecutar(() -> club.declararNoJugado(leerTexto("Id del partido"))); break;
		case 35: ejecutar(() -> mostrar(club.consultarSanciones(leerTexto("Deporte"), leerTexto("Equipo")))); break;
		case 36: ejecutar(() -> club.inscribirEquipoEnCompetencia(leerTexto("Competencia"), leerEntero("Temporada"),
				leerTexto("Deporte del equipo"), leerTexto("Equipo"))); break;
		case 37: ejecutar(() -> club.definirNomina(leerTexto("Competencia"), leerEntero("Temporada"), leerTexto("Equipo"),
				leerLista("Logins de la nomina"))); break;
		case 38: ejecutar(() -> System.out.println("Programado: " + club.programarPartidoCompetencia(leerTexto("Competencia"),
				leerEntero("Temporada"), leerTexto("Equipo"), leerTexto("Institucion rival"), leerTexto("Escenario"),
				leerFecha("Fecha"), leerEnum("Fase", Fase.class)))); break;
		case 39: ejecutar(() -> {
			club.registrarPartidoCompetencia(leerTexto("Id del partido"), leerLista("Alineacion"), leerEntero("Goles/puntos del club"),
					leerEntero("Goles/puntos del rival"), new ArrayList<>());
			System.out.println("Registrado. Las estadisticas y tarjetas se agregan con la opcion 33.");
		}); break;
		case 40: ejecutar(() -> club.asignarNivel(leerTexto("Deporte"), leerTexto("Login del socio"), leerEnum("Nuevo nivel", Nivel.class))); break;
		case 41: ejecutar(() -> {
			club.registrarResultadoPractica(leerTexto("Id del partido"), leerEntero("Sets del lado A"), leerEntero("Sets del lado B"),
					new ArrayList<>());
			System.out.println("Registrado y ELO actualizado. Las estadisticas de cada jugador se agregan con la opcion 33.");
		}); break;
		case 42: ejecutar(() -> System.out.println("Planificada: " + club.planificarSesion(leerTexto("Codigo de la instalacion"),
				leerFechaHora(), leerEntero("Duracion en minutos"), leerLista("Convocados")))); break;
		case 43: ejecutar(() -> club.modificarSesion(leerTexto("Id de la sesion (ej. S1)"), leerTexto("Codigo de la instalacion"),
				leerFechaHora(), leerEntero("Duracion en minutos"), leerLista("Convocados"))); break;
		case 44: ejecutar(() -> club.cancelarSesion(leerTexto("Id de la sesion"))); break;
		case 45: ejecutar(() -> club.registrarAsistencia(leerTexto("Id de la sesion"), leerTexto("Login del convocado"),
				leerEnum("Asistencia", Asistencia.class))); break;
		case 46: ejecutar(() -> club.registrarEvaluacion(leerTexto("Id de la sesion"), leerTexto("Login del convocado"),
				leerEntero("Nota (1 a 5)"), leerTextoOpcional("Comentario"))); break;
		case 47: ejecutar(() -> System.out.println(club.consultarAnaliticasJugador(leerTexto("Login del jugador"),
				leerFecha("Desde"), leerFecha("Hasta")))); break;
		case 48: ejecutar(() -> System.out.println(club.consultarAnaliticasGrupo(leerTexto("Deporte"),
				leerTextoOpcional("Equipo (vacio para una academia)"), leerFecha("Desde"), leerFecha("Hasta")))); break;
		case 49: ejecutar(() -> mostrar(club.consultarAgenda())); break;
		default: opcionComun(opcion);
		}
	}

	private static void opcionesEmpleado() {
		seccion("Empleado");
		System.out.println(" 21. Mis turnos");
		System.out.println(" 22. Solicitar cambio de turno");
		System.out.println(" 23. Mis solicitudes de cambio de turno");
		System.out.println(" 24. Mi codigo de descuento");
		System.out.println(" 25. Sugerir un articulo o snack");
		System.out.println(" 26. Mis sugerencias");
		System.out.println(" 27. Comprar en la tienda (15% de descuento automatico)");
		System.out.println(" 28. Comprar en la cafeteria");
		System.out.println(" 29. Mis puntos y compras");
	}

	//true si la opcion era de empleado
	private static boolean opcionEmpleado(int opcion) {
		switch (opcion) {
		case 21: ejecutar(() -> mostrar(club.consultarTurnos())); return true;
		case 22: ejecutar(() -> System.out.println("Solicitud creada: " + club.solicitarCambioTurno(leerTexto("Id del turno (ej. T1)"),
				leerHora("Nueva hora de inicio"), leerHora("Nueva hora de fin")))); return true;
		case 23: ejecutar(() -> mostrar(club.consultarSolicitudes())); return true;
		case 24: ejecutar(() -> System.out.println("Su codigo: " + club.consultarCodigoDescuento())); return true;
		case 25: ejecutar(() -> System.out.println("Sugerencia creada: " + club.sugerirProducto(leerTexto("Nombre"),
				leerTexto("Categoria (las que empiezan por Snack o Bebida van a la cafeteria)"), leerEntero("Precio sugerido"),
				leerLista("Alergenos (vacio si no tiene)")))); return true;
		case 26: ejecutar(() -> mostrar(club.consultarSugerencias())); return true;
		case 27: comprarEnTienda(false); return true;
		case 28: comprarEnCafeteria(); return true;
		case 29: ejecutar(() -> System.out.println(club.consultarPuntos())); return true;
		default: return false;
		}
	}

	// ============================================================
	// Administrador
	// ============================================================

	private static void menuAdministrador() {
		titulo("Menu del administrador  (" + mostrarAhora() + ")");
		seccion("Personas");
		System.out.println("  1. Ver todas las personas");
		System.out.println("  2. Crear socio");
		System.out.println("  3. Crear entrenador");
		System.out.println("  4. Crear fisioterapeuta");
		System.out.println("  5. Dar de baja a una persona");
		System.out.println("  6. Restablecer contrasena");
		seccion("Deportes e instalaciones");
		System.out.println("  7. Crear equipo");
		System.out.println("  8. Dar de baja un equipo");
		System.out.println("  9. Crear instalacion");
		System.out.println(" 10. Editar instalacion");
		System.out.println(" 11. Desactivar instalacion");
		System.out.println(" 12. Crear competencia");
		System.out.println(" 13. Finalizar competencia");
		seccion("Catalogo e inventario");
		System.out.println(" 14. Crear articulo de la tienda");
		System.out.println(" 15. Crear producto de la cafeteria");
		System.out.println(" 16. Cambiar precio de un producto");
		System.out.println(" 17. Desactivar producto");
		System.out.println(" 18. Reabastecer la tienda");
		System.out.println(" 19. Mover articulos entre ubicaciones");
		System.out.println(" 20. Consultar inventario");
		System.out.println(" 21. Ver y resolver sugerencias");
		seccion("Turnos");
		System.out.println(" 22. Ver todos los turnos");
		System.out.println(" 23. Asignar turno");
		System.out.println(" 24. Ver y resolver solicitudes de cambio de turno");
		seccion("Historiales y reportes");
		System.out.println(" 25. Historial de reservas");
		System.out.println(" 26. Historial de partidos");
		System.out.println(" 27. Historial de sesiones de entrenamiento");
		System.out.println(" 28. Reporte de ventas");
		System.out.println(" 29. Reporte de asistencias");
		System.out.println(" 30. Reporte de resultados de partidos");
		System.out.println(" 31. Reporte de evolucion de rankings");
		opcionesComunes();
		int opcion = leerOpcion();
		switch (opcion) {
		case 1: mostrar(new ArrayList<>(club.getPersonas())); break;
		case 2: ejecutar(() -> System.out.println("Creado: " + club.crearSocio(leerTexto("Nombre"), leerTexto("Login"),
				leerTexto("Password"), leerFecha("Fecha de nacimiento")))); break;
		case 3: ejecutar(() -> System.out.println("Creado: " + club.crearEntrenador(leerTexto("Nombre"), leerTexto("Login"),
				leerTexto("Password")))); break;
		case 4: ejecutar(() -> System.out.println("Creado: " + club.crearFisioterapeuta(leerTexto("Nombre"), leerTexto("Login"),
				leerTexto("Password")))); break;
		case 5: ejecutar(() -> club.darDeBajaPersona(leerTexto("Login"))); break;
		case 6: ejecutar(() -> club.restablecerPassword(leerTexto("Login"), leerTexto("Nueva password"))); break;
		case 7: ejecutar(() -> System.out.println("Creado: " + club.crearEquipo(leerTexto("Deporte"), leerTexto("Nombre"),
				leerEnum("Categoria", Categoria.class), leerEntero("Cupo maximo")))); break;
		case 8: ejecutar(() -> club.darDeBajaEquipo(leerTexto("Deporte"), leerTexto("Equipo"))); break;
		case 9: ejecutar(() -> {
			String deporte = leerTexto("Deporte");
			if (club.buscarDeporte(deporte) instanceof deportes.DeporteIndividual) {
				System.out.println("Creada: " + club.crearInstalacionIndividual(leerTexto("Codigo"), leerTexto("Nombre"), deporte,
						leerEntero("Capacidad (2 o 4)"), leerSiNo("Es techada")));
			} else {
				System.out.println("Creada: " + club.crearInstalacionConjunto(leerTexto("Codigo"), leerTexto("Nombre"), deporte,
						leerEntero("Capacidad")));
			}
		}); break;
		case 10: ejecutar(() -> club.editarInstalacion(leerTexto("Codigo"), leerTexto("Nuevo nombre"), leerEntero("Nueva capacidad"))); break;
		case 11: ejecutar(() -> club.desactivarInstalacion(leerTexto("Codigo"))); break;
		case 12: ejecutar(() -> {
			boolean ascun = leerSiNo("Es de ASCUN (si) o del Torneo de los Cerros (no)");
			String nombre = leerTexto("Nombre");
			int temporada = leerEntero("Temporada");
			String deporte = leerTexto("Deporte");
			Categoria categoria = leerEnum("Categoria", Categoria.class);
			List<String> instituciones = leerLista("Instituciones participantes");
			System.out.println("Creada: " + (ascun ? club.crearCompetenciaAscun(nombre, temporada, deporte, categoria, instituciones)
					: club.crearCompetenciaCerros(nombre, temporada, deporte, categoria, instituciones)));
		}); break;
		case 13: ejecutar(() -> club.finalizarCompetencia(leerTexto("Competencia"), leerEntero("Temporada"))); break;
		case 14: ejecutar(() -> System.out.println("Creado: " + club.crearArticulo(leerTexto("Nombre"), leerTexto("Categoria"),
				leerEntero("Precio")))); break;
		case 15: ejecutar(() -> System.out.println("Creado: " + club.crearProductoCafeteria(leerTexto("Nombre"), leerEntero("Precio"),
				leerSiNo("Es bebida caliente"), leerLista("Alergenos (vacio si no tiene)")))); break;
		case 16: ejecutar(() -> club.cambiarPrecioProducto(leerTexto("Producto"), leerEntero("Nuevo precio"))); break;
		case 17: ejecutar(() -> club.desactivarProducto(leerTexto("Producto"))); break;
		case 18: ejecutar(() -> club.reabastecerTienda(leerTexto("Articulo"), leerEntero("Cantidad"))); break;
		case 19: ejecutar(() -> club.moverArticulo(leerTexto("Articulo"), leerTexto("Origen (ej. Bodega)"), leerTexto("Destino (ej. Tienda)"),
				leerEntero("Cantidad"))); break;
		case 20: ejecutar(() -> mostrar(club.consultarInventario())); break;
		case 21: ejecutar(() -> {
			mostrar(club.getSugerencias());
			String id = leerTextoOpcional("Id de la sugerencia a resolver (vacio para volver)");
			if (!id.isEmpty()) {
				if (leerSiNo("Aprobar (si) o rechazar (no)")) {
					System.out.println("Agregado al catalogo: " + club.aprobarSugerencia(id));
				} else {
					club.rechazarSugerencia(id);
				}
			}
		}); break;
		case 22: mostrar(club.getTurnos()); break;
		case 23: ejecutar(() -> System.out.println("Asignado: " + club.asignarTurno(leerTexto("Login del empleado"),
				leerEnum("Dia", DayOfWeek.class), leerHora("Hora de inicio"), leerHora("Hora de fin")))); break;
		case 24: ejecutar(() -> {
			mostrar(club.getSolicitudes());
			String id = leerTextoOpcional("Id de la solicitud a resolver (vacio para volver)");
			if (!id.isEmpty()) {
				if (leerSiNo("Aprobar (si) o rechazar (no)")) {
					club.aprobarSolicitudCambioTurno(id);
				} else {
					club.rechazarSolicitudCambioTurno(id);
				}
			}
		}); break;
		case 25: ejecutar(() -> mostrar(club.historialReservas(filtro("Deporte"), fechaFiltro("Desde"), fechaFiltro("Hasta"),
				filtro("Codigo de instalacion"), filtro("Login del socio")))); break;
		case 26: ejecutar(() -> mostrar(club.historialPartidos(filtro("Deporte"), fechaFiltro("Desde"), fechaFiltro("Hasta"),
				filtro("Login de un jugador")))); break;
		case 27: ejecutar(() -> mostrar(club.historialSesiones(filtro("Deporte"), fechaFiltro("Desde"), fechaFiltro("Hasta"),
				filtro("Codigo de instalacion"), filtro("Login del entrenador o convocado")))); break;
		case 28: ejecutar(() -> System.out.println(club.reporteVentas(leerFecha("Desde"), leerFecha("Hasta"),
				leerEnum("Agrupar por", TipoPeriodo.class)))); break;
		case 29: ejecutar(() -> System.out.println(club.reporteAsistencias(leerFecha("Desde"), leerFecha("Hasta")))); break;
		case 30: ejecutar(() -> System.out.println(club.reporteResultados(leerFecha("Desde"), leerFecha("Hasta")))); break;
		case 31: ejecutar(() -> System.out.println(club.reporteRankings(leerFecha("Desde"), leerFecha("Hasta")))); break;
		default: opcionComun(opcion);
		}
	}

	// ============================================================
	// Opciones comunes a todos los usuarios
	// ============================================================

	private static void opcionesComunes() {
		seccion("General");
		System.out.println(" 90. Consultar equipos");
		System.out.println(" 91. Consultar el calendario de un equipo");
		System.out.println(" 92. Consultar una competencia");
		System.out.println(" 93. Ver catalogos de tienda y cafeteria");
		System.out.println(" 94. Cambiar mi contrasena");
		System.out.println(" 95. Cambiar la fecha y hora (simulacion)");
		System.out.println("  0. Cerrar sesion");
	}

	private static void opcionComun(int opcion) {
		switch (opcion) {
		case 90: ejecutar(() -> mostrar(club.consultarEquipos(filtro("Deporte"), null))); break;
		case 91: ejecutar(() -> mostrar(club.consultarCalendarioEquipo(leerTexto("Deporte"), leerTexto("Equipo")))); break;
		case 92: ejecutar(() -> {
			mostrar(club.getCompetencias());
			System.out.println(club.consultarCompetencia(leerTexto("Competencia"), leerEntero("Temporada")).generarResumen());
		}); break;
		case 93: ejecutar(() -> {
			System.out.println("Tienda:");
			mostrar(club.consultarCatalogoTienda());
			System.out.println("Cafeteria:");
			mostrar(club.consultarCatalogoCafeteria());
		}); break;
		case 94: ejecutar(() -> club.cambiarPassword(leerTexto("Password actual"), leerTexto("Nueva password"))); break;
		case 95: cambiarReloj(); break;
		case 0:
			club.cerrarSesion();
			System.out.println("Sesion cerrada.");
			break;
		default: System.out.println("Opcion invalida");
		}
	}

	// ============================================================
	// Compras (tienen varios pasos)
	// ============================================================

	private static void comprarEnTienda(boolean esSocio) {
		ejecutar(() -> {
			mostrar(club.consultarCatalogoTienda());
			Map<String, Integer> articulos = leerProductos();
			String codigo = esSocio ? leerTextoOpcional("Codigo de descuento de un empleado (vacio si no tiene)") : "";
			int puntos = leerEntero("Puntos a redimir", 0);
			System.out.println(club.comprarEnTienda(articulos, codigo.isEmpty() ? null : codigo, puntos).generarDesglose());
		});
	}

	private static void comprarEnCafeteria() {
		ejecutar(() -> {
			mostrar(club.consultarCatalogoCafeteria());
			Map<String, Integer> productos = leerProductos();
			List<String> alergenos = club.consultarAlergenos(productos);
			boolean acepta = true;
			if (!alergenos.isEmpty()) {
				System.out.println("Atencion, alergenos:");
				mostrar(alergenos);
				acepta = leerSiNo("Confirma que conoce los alergenos");
			}
			int propina = leerEntero("Porcentaje de propina", 10);
			int puntos = leerEntero("Puntos a redimir", 0);
			System.out.println(club.comprarEnCafeteria(productos, puntos, propina, acepta).generarDesglose());
		});
	}

	private static Map<String, Integer> leerProductos() {
		Map<String, Integer> productos = new LinkedHashMap<>();
		System.out.println("Escriba los productos uno por uno (vacio para terminar).");
		while (true) {
			String nombre = leerTextoOpcional("Producto");
			if (nombre.isEmpty()) {
				return productos;
			}
			productos.put(nombre, leerEntero("Cantidad"));
		}
	}

	// ============================================================
	// Lectura de datos (pide lo minimo, con ejemplos del formato)
	// ============================================================

	//Ejecuta la accion; si el sistema la rechaza muestra el motivo. Luego guarda el estado.
	private static void ejecutar(Runnable accion) {
		try {
			accion.run();
			System.out.println("[OK]");
		} catch (RuntimeException e) {
			System.out.println("[RECHAZADO] " + e.getMessage());
		}
		guardar();
	}

	private static void guardar() {
		try {
			persistencia.salvar(club);
		} catch (RuntimeException e) {
			System.out.println("No se pudo guardar el estado: " + e.getMessage());
		}
	}

	private static int leerOpcion() {
		try {
			return Integer.parseInt(leerTexto("Opcion"));
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private static String leerTexto(String mensaje) {
		String texto = leerTextoOpcional(mensaje);
		while (texto.isEmpty()) {
			texto = leerTextoOpcional(mensaje + " (obligatorio)");
		}
		return texto;
	}

	private static String leerTextoOpcional(String mensaje) {
		System.out.print(mensaje + ": ");
		if (!entrada.hasNextLine()) {
			throw new IllegalStateException("Se acabo la entrada");
		}
		return entrada.nextLine().trim();
	}

	private static int leerEntero(String mensaje) {
		while (true) {
			try {
				return Integer.parseInt(leerTexto(mensaje));
			} catch (NumberFormatException e) {
				System.out.println("Debe ser un numero entero");
			}
		}
	}

	//Si se deja vacio devuelve el valor por defecto
	private static int leerEntero(String mensaje, int porDefecto) {
		while (true) {
			String texto = leerTextoOpcional(mensaje + " (vacio = " + porDefecto + ")");
			if (texto.isEmpty()) {
				return porDefecto;
			}
			try {
				return Integer.parseInt(texto);
			} catch (NumberFormatException e) {
				System.out.println("Debe ser un numero entero");
			}
		}
	}

	private static boolean leerSiNo(String mensaje) {
		String texto = leerTexto(mensaje + " (s/n)").toLowerCase();
		return texto.startsWith("s");
	}

	private static List<String> leerLista(String mensaje) {
		List<String> valores = new ArrayList<>();
		for (String v : leerTextoOpcional(mensaje + " (separados por coma)").split(",")) {
			if (!v.isBlank()) {
				valores.add(v.trim());
			}
		}
		return valores;
	}

	private static <E extends Enum<E>> E leerEnum(String mensaje, Class<E> tipo) {
		E[] valores = tipo.getEnumConstants();
		StringBuilder opciones = new StringBuilder();
		for (int i = 0; i < valores.length; i++) {
			opciones.append(i + 1).append("=").append(valores[i]).append(i + 1 < valores.length ? ", " : "");
		}
		while (true) {
			int n = leerEntero(mensaje + " [" + opciones + "]");
			if (n >= 1 && n <= valores.length) {
				return valores[n - 1];
			}
			System.out.println("Elija un numero de la lista");
		}
	}

	private static LocalDate leerFecha(String mensaje) {
		while (true) {
			String texto = leerTextoOpcional(mensaje + " (AAAA-MM-DD, vacio = hoy " + Reloj.hoy() + ")");
			if (texto.isEmpty()) {
				return Reloj.hoy();
			}
			try {
				return LocalDate.parse(texto);
			} catch (RuntimeException e) {
				System.out.println("Formato de fecha invalido");
			}
		}
	}

	private static LocalTime leerHora(String mensaje) {
		while (true) {
			String texto = leerTexto(mensaje + " (HH:MM o solo la hora, ej. 9)");
			try {
				return texto.contains(":") ? LocalTime.parse(texto.length() == 4 ? "0" + texto : texto) : LocalTime.of(Integer.parseInt(texto), 0);
			} catch (RuntimeException e) {
				System.out.println("Formato de hora invalido");
			}
		}
	}

	private static LocalDateTime leerFechaHora() {
		return leerFecha("Fecha").atTime(leerHora("Hora"));
	}

	//Filtros opcionales de los historiales: vacio = sin filtro (null)
	private static String filtro(String mensaje) {
		String texto = leerTextoOpcional(mensaje + " (vacio = todos)");
		return texto.isEmpty() ? null : texto;
	}

	private static LocalDate fechaFiltro(String mensaje) {
		while (true) {
			String texto = leerTextoOpcional(mensaje + " (AAAA-MM-DD, vacio = sin limite)");
			if (texto.isEmpty()) {
				return null;
			}
			try {
				return LocalDate.parse(texto);
			} catch (RuntimeException e) {
				System.out.println("Formato de fecha invalido");
			}
		}
	}

	private static EstadisticaPartido leerEstadistica(String deporte, String login) {
		switch (deporte) {
		case EstadisticaFutbol.DEPORTE:
			return new EstadisticaFutbol(login, leerEntero("Goles"), leerEntero("Asistencias"), leerEntero("Tarjetas amarillas (0 a 2)"),
					leerSiNo("Tarjeta roja"));
		case EstadisticaBaloncesto.DEPORTE:
			return new EstadisticaBaloncesto(login, leerEntero("Puntos"), leerEntero("Rebotes"));
		case EstadisticaVoleibol.DEPORTE:
			return new EstadisticaVoleibol(login, leerEntero("Aces"), leerEntero("Bloqueos"));
		case EstadisticaTenis.DEPORTE:
			return new EstadisticaTenis(login, leerEntero("Sets ganados"), leerEntero("Games ganados"), leerEntero("Aces"),
					leerEntero("Errores no forzados"));
		case EstadisticaPadel.DEPORTE:
			return new EstadisticaPadel(login, leerEntero("Sets ganados"), leerEntero("Games ganados"), leerEntero("Aces"),
					leerEntero("Errores no forzados"));
		case EstadisticaTenisDeMesa.DEPORTE:
			return new EstadisticaTenisDeMesa(login, leerEntero("Sets ganados"), leerEntero("Puntos totales"));
		default:
			throw new IllegalArgumentException("No hay estadisticas definidas para " + deporte);
		}
	}

	private static void cambiarReloj() {
		System.out.println("Ahora (simulado): " + mostrarAhora());
		System.out.println(" 1. Avanzar dias   2. Avanzar horas   3. Fijar fecha y hora   4. Volver a la hora real");
		switch (leerOpcion()) {
		case 1: Reloj.avanzarDias(leerEntero("Dias")); break;
		case 2: Reloj.avanzarHoras(leerEntero("Horas")); break;
		case 3: Reloj.fijar(leerFechaHora()); break;
		case 4: Reloj.restablecer(); break;
		default: System.out.println("Opcion invalida");
		}
		System.out.println("Ahora (simulado): " + mostrarAhora());
	}

	// ============================================================
	// Presentacion
	// ============================================================

	private static String mostrarAhora() {
		LocalDateTime ahora = Reloj.ahora();
		return ahora.toLocalDate() + " " + ahora.toLocalTime() + " " + ahora.getDayOfWeek();
	}

	private static void titulo(String texto) {
		System.out.println();
		System.out.println("==============================================");
		System.out.println(" " + texto);
		System.out.println("==============================================");
	}

	private static void seccion(String texto) {
		System.out.println(" -- " + texto);
	}

	private static void mostrar(List<?> elementos) {
		if (elementos.isEmpty()) {
			System.out.println("  (vacio)");
		}
		for (Object e : elementos) {
			System.out.println("  - " + e);
		}
	}
}
