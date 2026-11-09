package comercio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductoCafeteria extends Producto {

	//Atributos
	private final boolean bebidaCaliente;
	private final List<String> alergenos;

	//Constructor
	public ProductoCafeteria(String nombre, long precio, boolean bebidaCaliente, List<String> alergenos) {
		super(nombre, precio);
		this.alergenos = new ArrayList<>();
		if (alergenos != null) {
			for (String alergeno : alergenos) {
				validarTexto(alergeno, "El alergeno");
				this.alergenos.add(alergeno);
			}
		}
		this.bebidaCaliente = bebidaCaliente;
	}

	//Getters
	public boolean esBebidaCaliente() {
		return bebidaCaliente;
	}

	public List<String> getAlergenos() {
		return Collections.unmodifiableList(alergenos);
	}

	@Override
	public String toString() {
		return nombre + " $" + precio + (bebidaCaliente ? " (bebida caliente)" : "")
				+ (alergenos.isEmpty() ? "" : " | alergenos: " + String.join(", ", alergenos))
				+ (activo ? "" : " [DESACTIVADO]");
	}
}
