package comercio;

import java.time.LocalDate;

import personas.Persona;

public class VentaCafeteria extends Venta {

	public static final int IMPUESTO_CONSUMO = 8;
	public static final int PROPINA_SUGERIDA = 10;

	private final int porcentajePropina;

	//Constructor
	public VentaCafeteria(String id, LocalDate fecha, Persona comprador, long puntosRedimidos, int porcentajePropina) {
		super(id, fecha, comprador, puntosRedimidos);
		if (porcentajePropina < 0) {
			throw new IllegalArgumentException("La propina no puede ser negativa");
		}
		this.porcentajePropina = porcentajePropina;
	}

	//Metodos
	public int getPorcentajePropina() {
		return porcentajePropina;
	}

	public boolean tieneBebidaCaliente() {
		for (DetalleVenta detalle : detalles) {
			if (((ProductoCafeteria) detalle.getProducto()).esBebidaCaliente()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getRubro() {
		return "Cafeteria";
	}

	@Override
	public String getNombreImpuesto() {
		return "Impuesto al consumo " + IMPUESTO_CONSUMO + "%";
	}

	@Override
	public long getImpuesto() {
		return porcentaje(getBase(), IMPUESTO_CONSUMO);
	}

	@Override
	public long getPropina() {
		return porcentaje(getBase(), porcentajePropina);
	}
}
