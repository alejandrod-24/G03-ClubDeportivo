package personas;

import java.time.LocalDate;
import java.time.Period;

import deportes.Categoria;
import global.Reloj;

public class Socio extends Persona {

	//Atributos
	private final LocalDate fechaNacimiento;


	//Constructor
	public Socio(String nombre, String login, String password, LocalDate fechaNacimiento) {
		super(nombre, login, password);
		validarFechaNacimiento(fechaNacimiento);
		this.fechaNacimiento = fechaNacimiento;
	}

	//Metodos
	public LocalDate getFechaNacimiento() {
		return fechaNacimiento;
	}

	public int getEdad() {
		return Period.between(fechaNacimiento, Reloj.hoy()).getYears();
	}

	public Categoria getCategoria() {
		int anios = getEdad();
		if (anios < 13) {
			return Categoria.SUB13;
		}
		if (anios < 15) {
			return Categoria.SUB15;
		}
		if (anios < 17) {
			return Categoria.SUB17;
		}
		return Categoria.MAYORES;
	}

	@Override
	public Rol getRol() {
		return Rol.SOCIO;
	}

	//Validar fecha de nacimiento
	private void validarFechaNacimiento(LocalDate fechaNacimiento) {
		if (fechaNacimiento == null) {
			throw new IllegalArgumentException("La fecha de nacimiento no puede estar vacia");
		}
		if (fechaNacimiento.isAfter(Reloj.hoy())) {
			throw new IllegalArgumentException("La fecha no puede ser en el futuro");
		}
	}

}
