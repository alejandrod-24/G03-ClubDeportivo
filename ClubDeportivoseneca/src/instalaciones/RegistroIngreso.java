package instalaciones;

import java.time.LocalDateTime;

import personas.Socio;

/** Entrada y salida de un socio de una instalacion. Mientras no tenga salida, el socio esta adentro. */
public class RegistroIngreso {

	//Atributos
	private final Socio socio;
	private final Instalacion instalacion;
	private final LocalDateTime entrada;
	private LocalDateTime salida;

	//Constructor
	public RegistroIngreso(Socio socio, Instalacion instalacion, LocalDateTime entrada) {
		this.socio = socio;
		this.instalacion = instalacion;
		this.entrada = entrada;
		this.salida = null;
	}

	//Metodos
	public Socio getSocio() {
		return socio;
	}

	public Instalacion getInstalacion() {
		return instalacion;
	}

	public LocalDateTime getEntrada() {
		return entrada;
	}

	public LocalDateTime getSalida() {
		return salida;
	}

	public boolean estaAdentro() {
		return salida == null;
	}

	public void registrarSalida(LocalDateTime momento) {
		if (salida != null) {
			throw new IllegalStateException("La salida ya fue registrada");
		}
		this.salida = momento;
	}

	@Override
	public String toString() {
		return socio.getLogin() + " en " + instalacion.getNombre() + ": entrada " + entrada
				+ (salida == null ? " (adentro)" : ", salida " + salida);
	}
}
