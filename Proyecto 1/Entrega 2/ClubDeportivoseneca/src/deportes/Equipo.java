package deportes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import personas.Socio;

public class Equipo {

	//Atributos
	private final DeporteConjunto deporte;
	private final String nombre;
	private Categoria categoria;
	private final int cupoMaximo;
	private boolean activo;
	private final List<InscripcionEquipo> inscripciones;

	//Constructor (de paquete: solo se crea con DeporteConjunto.crearEquipo)
	Equipo(DeporteConjunto deporte, String nombre, Categoria categoria, int cupoMaximo) {
		this.deporte = deporte;
		validarNombre(nombre);
		this.nombre = nombre;
		validarCategoria(categoria);
		this.categoria = categoria;
		validarCupoMaximo(cupoMaximo);
		this.cupoMaximo = cupoMaximo;
		this.activo = true;
		this.inscripciones = new ArrayList<>();
	}

	//Validador
	private void validarNombre(String nombre) {
		if (nombre == null || nombre.isBlank())
			throw new IllegalArgumentException("El nombre no puede ir vacio");
		if (nombre.contains(";"))
			throw new IllegalArgumentException("El nombre no puede contener ';'");
	}

	private void validarCategoria(Categoria categoria) {
		if (categoria == null)
			throw new IllegalArgumentException("El equipo debe tener una categoria");
	}

	private void validarCupoMaximo(int cupoMaximo) {
		if (cupoMaximo <= 0 )
			throw new IllegalArgumentException("El cupo maximo debe ser mayor a cero");
	}

	//Metodos
	public DeporteConjunto getDeporte() {
		return deporte;
	}

	public String getNombre() {
		return nombre;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public int getCupoMaximo() {
		return cupoMaximo;
	}

	public int getCuposDisponibles() {
		return cupoMaximo - inscripciones.size();
	}

	public boolean estaActivo() {
		return activo;
	}

	public List<InscripcionEquipo> getInscripciones() {
		return Collections.unmodifiableList(inscripciones);
	}

	public List<Socio> getJugadores() {
		List<Socio> jugadores = new ArrayList<>();
		for (InscripcionEquipo inscripcion : inscripciones) {
			jugadores.add(inscripcion.getSocio());
		}
		return jugadores;
	}

	public InscripcionEquipo buscarInscripcion(Socio socio) {
		for (InscripcionEquipo inscripcion : inscripciones) {
			if (inscripcion.getSocio() == socio) {
				return inscripcion;
			}
		}
		return null;
	}

	public boolean estaInscrito(Socio socio) {
		return buscarInscripcion(socio) != null;
	}

	public void inscribir(Socio socio, String posicion, int numeroCamiseta) {
		if (!activo) {
			throw new IllegalStateException("El equipo " + nombre + " esta inactivo");
		}
		if (!socio.estaActivo()) {
			throw new IllegalStateException("El socio esta dado de baja");
		}
		if (socio.getCategoria() != categoria) {
			throw new IllegalArgumentException("La edad del socio (" + socio.getEdad()
					+ " anios, " + socio.getCategoria() + ") no corresponde a la categoria " + categoria);
		}
		if (getCuposDisponibles() <= 0) {
			throw new IllegalStateException("El equipo " + nombre + " tiene el cupo lleno (" + cupoMaximo + ")");
		}
		Equipo actual = deporte.getEquipoDe(socio);
		if (actual != null) {
			throw new IllegalStateException("El socio ya pertenece al equipo " + actual.getNombre()
					+ " de " + deporte.getNombre());
		}
		if (!deporte.tienePosicion(posicion)) {
			throw new IllegalArgumentException("La posicion '" + posicion + "' no existe en "
					+ deporte.getNombre() + ". Posiciones: " + deporte.getPosiciones());
		}
		if (numeroCamiseta < 1 || numeroCamiseta > 99) {
			throw new IllegalArgumentException("El numero de camiseta debe estar entre 1 y 99");
		}
		for (InscripcionEquipo inscripcion : inscripciones) {
			if (inscripcion.getNumeroCamiseta() == numeroCamiseta) {
				throw new IllegalArgumentException("La camiseta " + numeroCamiseta + " ya la usa "
						+ inscripcion.getSocio().getNombre());
			}
		}
		inscripciones.add(new InscripcionEquipo(socio, this, posicion, numeroCamiseta));
	}

	//Usado por la persistencia (los datos ya fueron validados al guardarse)
	public void agregarInscripcionGuardada(Socio socio, String posicion, int numeroCamiseta) {
		inscripciones.add(new InscripcionEquipo(socio, this, posicion, numeroCamiseta));
	}

	//Libera el cupo y la camiseta. Las estadisticas quedan en los partidos.
	public void retirar(Socio socio) {
		InscripcionEquipo inscripcion = buscarInscripcion(socio);
		if (inscripcion == null) {
			throw new IllegalStateException(socio.getNombre() + " no esta inscrito en " + nombre);
		}
		inscripciones.remove(inscripcion);
	}

	public void cambiarCategoria(Categoria nueva) {
		validarCategoria(nueva);
		if (!inscripciones.isEmpty()) {
			throw new IllegalStateException("Solo se puede cambiar la categoria de un equipo sin jugadores");
		}
		this.categoria = nueva;
	}

	//Retira a sus jugadores; el equipo y sus partidos quedan como historial
	public void darDeBaja() {
		if (!activo) {
			throw new IllegalStateException("El equipo " + nombre + " ya esta dado de baja");
		}
		inscripciones.clear();
		activo = false;
	}

	@Override
	public String toString() {
		return nombre + " (" + deporte.getNombre() + ", " + categoria + ") jugadores "
				+ inscripciones.size() + "/" + cupoMaximo + (activo ? "" : " [INACTIVO]");
	}
}
