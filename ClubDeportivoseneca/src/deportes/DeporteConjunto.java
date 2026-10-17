package deportes;

import java.util.ArrayList;
import java.util.List;

public class DeporteConjunto extends Deporte {
	
	//Atributos
	private final ArrayList<Equipo> equipos;
	
	//Constructor
	public DeporteConjunto(String nombre) {
		super(nombre);
		this.equipos = new ArrayList<>();
	}
	
	//Metodos
	
	private Equipo crearEquipo() {
		//TODO agregar metodo para crear un nuevo equipo: Usar un for para recorrer los nombres (tirar Exception) y crear equipo
	}
}
