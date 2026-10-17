package comercio;

public class DetalleVenta {

	//Atributos
	private final Producto producto;
	private final int cantidad;
	private final long precioUnitario;

	DetalleVenta(Producto producto, int cantidad, long precioUnitario) {
		this.producto = producto;
		this.cantidad = cantidad;
		this.precioUnitario = precioUnitario;
	}

	//Metodos
	public Producto getProducto() {
		return producto;
	}

	public int getCantidad() {
		return cantidad;
	}

	public long getPrecioUnitario() {
		return precioUnitario;
	}

	public long getValor() {
		return cantidad * precioUnitario;
	}

	@Override
	public String toString() {
		return cantidad + " x " + producto.getNombre() + " a $" + precioUnitario + " = $" + getValor();
	}
}
