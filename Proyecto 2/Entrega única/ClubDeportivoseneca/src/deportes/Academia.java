package deportes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import global.Reloj;
import partidos.PartidoPractica;
import personas.Persona;
import personas.Socio;

/** Academia de un deporte individual. Cada socio tiene una inscripcion (activa o historica) con su ranking. */
public class Academia {

	public static final int ELO_MINIMO = 100;
	public static final int ELO_MAXIMO = 3000;
	public static final int FACTOR_K = 32;
	public static final int ELO_ENTRENADOR = 1600;

	//Atributos
	private final DeporteIndividual deporte;
	private final List<InscripcionAcademia> inscripciones;

	//Constructor (de paquete: la crea DeporteIndividual)
	Academia(DeporteIndividual deporte) {
		this.deporte = deporte;
		this.inscripciones = new ArrayList<>();
	}

	//Metodos
	public DeporteIndividual getDeporte() {
		return deporte;
	}

	public String getNombre() {
		return "Academia de " + deporte.getNombre();
	}

	public List<InscripcionAcademia> getInscripciones() {
		return Collections.unmodifiableList(inscripciones);
	}

	//Inscripcion historica o activa del socio (null si nunca se inscribio)
	public InscripcionAcademia buscarInscripcion(Socio socio) {
		for (InscripcionAcademia i : inscripciones) {
			if (i.getSocio() == socio) {
				return i;
			}
		}
		return null;
	}

	public InscripcionAcademia buscarInscripcionActiva(Socio socio) {
		InscripcionAcademia i = buscarInscripcion(socio);
		return i != null && i.estaActiva() ? i : null;
	}

	public boolean estaInscrito(Socio socio) {
		return buscarInscripcionActiva(socio) != null;
	}

	public InscripcionAcademia inscribir(Socio socio, Nivel nivel, int puntajeInicial) {
		if (!socio.estaActivo()) {
			throw new IllegalStateException("El socio esta dado de baja");
		}
		if (nivel == null) {
			throw new IllegalArgumentException("Debe indicar el nivel inicial");
		}
		if (puntajeInicial < ELO_MINIMO || puntajeInicial > ELO_MAXIMO) {
			throw new IllegalArgumentException("El ELO inicial debe estar entre " + ELO_MINIMO + " y " + ELO_MAXIMO);
		}
		InscripcionAcademia existente = buscarInscripcion(socio);
		if (existente != null && existente.estaActiva()) {
			throw new IllegalStateException(socio.getLogin() + " ya esta inscrito en la " + getNombre());
		}
		if (existente != null) {
			existente.reactivar(nivel, puntajeInicial, Reloj.hoy());
			return existente;
		}
		InscripcionAcademia nueva = new InscripcionAcademia(socio, this, nivel);
		nueva.getRanking().registrar(Reloj.hoy(), puntajeInicial, "Inscripcion");
		inscripciones.add(nueva);
		return nueva;
	}

	//Para la persistencia (el historial del ranking se carga aparte)
	public InscripcionAcademia agregarInscripcionGuardada(Socio socio, Nivel nivel, boolean activa) {
		InscripcionAcademia i = new InscripcionAcademia(socio, this, nivel);
		i.restaurarEstado(activa);
		inscripciones.add(i);
		return i;
	}

	public void retirar(Socio socio) {
		InscripcionAcademia i = buscarInscripcionActiva(socio);
		if (i == null) {
			throw new IllegalStateException(socio.getLogin() + " no esta inscrito en la " + getNombre());
		}
		i.retirar();
	}

	//ELO por equipos: cada lado vale el promedio de sus jugadores; todos los socios del lado
	//ganan (o pierden) lo mismo. El entrenador que juega cuenta como ELO_ENTRENADOR y no tiene ranking.
	public void actualizarRanking(PartidoPractica partido) {
		double eloA = promedio(partido.getLadoA());
		double eloB = promedio(partido.getLadoB());
		double esperadoA = 1.0 / (1.0 + Math.pow(10, (eloB - eloA) / 400.0));
		boolean ganoA = partido.getMarcadorLocal() > partido.getMarcadorVisitante();
		int cambioA = (int) Math.round(FACTOR_K * ((ganoA ? 1 : 0) - esperadoA));
		registrarCambio(partido, partido.getLadoA(), cambioA, ganoA);
		registrarCambio(partido, partido.getLadoB(), -cambioA, !ganoA);
	}

	private void registrarCambio(PartidoPractica partido, List<Persona> lado, int cambio, boolean gano) {
		for (Persona p : lado) {
			InscripcionAcademia i = p instanceof Socio ? buscarInscripcionActiva((Socio) p) : null;
			if (i != null) {
				Ranking r = i.getRanking();
				r.registrar(partido.getFecha(), Math.max(ELO_MINIMO, r.getPuntaje() + cambio),
						partido.getId() + " " + (gano ? "victoria" : "derrota") + " (" + (cambio >= 0 ? "+" : "") + cambio + ")");
			}
		}
	}

	private double promedio(List<Persona> lado) {
		double suma = 0;
		for (Persona p : lado) {
			InscripcionAcademia i = p instanceof Socio ? buscarInscripcionActiva((Socio) p) : null;
			suma += i != null ? i.getRanking().getPuntaje() : ELO_ENTRENADOR;
		}
		return suma / lado.size();
	}

	//Inscritos activos ordenados de mayor a menor ELO
	public List<InscripcionAcademia> getRankingActual() {
		List<InscripcionAcademia> activos = new ArrayList<>();
		for (InscripcionAcademia i : inscripciones) {
			if (i.estaActiva()) {
				activos.add(i);
			}
		}
		activos.sort((a, b) -> b.getRanking().getPuntaje() - a.getRanking().getPuntaje());
		return activos;
	}

	public int getPosicion(Socio socio) {
		List<InscripcionAcademia> ranking = getRankingActual();
		for (int i = 0; i < ranking.size(); i++) {
			if (ranking.get(i).getSocio() == socio) {
				return i + 1;
			}
		}
		return 0;
	}
}
