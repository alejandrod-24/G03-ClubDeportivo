package comercio;

import java.time.LocalDate;

import personas.Persona;

public class VentaTienda extends Venta {

	public static final int IVA = 19;
	public static final int DESCUENTO_EMPLEADO = 15;
	public static final int DESCUENTO_CODIGO = 8;

	
	private final String codigoDescuento;
	private final int porcentajeDescuento;

	//Constructor
	public VentaTienda(String id, LocalDate fecha, Persona comprador, String codigoDescuento, int porcentajeDescuento,
			long puntosRedimidos) {
		super(id, fecha, comprador, puntosRedimidos);
		if (porcentajeDescuento > 0 && puntosRedimidos > 0) {
			throw new IllegalArgumentException("El descuento no es acumulable con la redencion de puntos");
		}
		this.codigoDescuento = codigoDescuento;
		this.porcentajeDescuento = porcentajeDescuento;
	}

	//Metodos
	public String getCodigoDescuento() {
		return codigoDescuento;
	}

	public int getPorcentajeDescuento() {
		return porcentajeDescuento;
	}

	@Override
	public String getRubro() {
		return "Tienda";
	}

	@Override
	public String getNombreImpuesto() {
		return "IVA " + IVA + "%";
	}

	@Override
	public long getDescuento() {
		return porcentaje(getSubtotal(), porcentajeDescuento);
	}

	@Override
	public long getImpuesto() {
		return porcentaje(getBase(), IVA);
	}
}
