package competencias;

import java.util.ArrayList;
import java.util.List;

import deportes.Equipo;
import personas.Socio;

public class InscripcionCompetencia {

	//Atributos
	private final Competencia competencia;
	private final Equipo equipo;
	//Registro de nomina: socios habilitados para los partidos oficiales
	private List<Socio> nomina;

	//Constructor (de paquete: se crea con Competencia.inscribirEquipo)
	InscripcionCompetencia(Competencia competencia, Equipo equipo) {
		this.competencia = competencia;
		this.equipo = equipo;
		this.nomina = new ArrayList<>();
	}

	//Metodos
	public Competencia getCompetencia() {
		return competencia;
	}

	public Equipo getEquipo() {
		return equipo;
	}

	public List<Socio> getNomina() {
		return new ArrayList<>(nomina);
	}

	public boolean estaEnNomina(Socio socio) {
		return nomina.contains(socio);
	}

	void setNomina(List<Socio> nomina) {
		this.nomina = new ArrayList<>(nomina);
	}
}
