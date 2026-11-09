package persistencia;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import comercio.ArticuloDeportivo;
import comercio.DetalleVenta;
import comercio.Producto;
import comercio.ProductoCafeteria;
import comercio.Venta;
import comercio.VentaCafeteria;
import comercio.VentaTienda;
import global.Club;
import personas.Persona;
import personas.Socio;

/**
 * productos.txt:          TIENDA;nombre;precio;activo;categoria | CAFETERIA;nombre;precio;activo;bebidaCaliente;alergenos
 * existencias.txt:        articulo;ubicacion;cantidad
 * ventas.txt:             id;TIENDA;fecha;comprador;puntosRedimidos;codigo;porcentajeDescuento | id;CAFETERIA;fecha;comprador;puntosRedimidos;porcentajePropina
 * detalles_venta.txt:     idVenta;producto;cantidad;precioUnitario
 * bebidas_calientes.txt:  loginSocio;fecha
 * Los puntos de fidelidad no se guardan: se calculan con las ventas.
 */
public class PersistenciaComercio {

	public static final String PRODUCTOS = "productos.txt";
	public static final String EXISTENCIAS = "existencias.txt";
	public static final String VENTAS = "ventas.txt";
	public static final String DETALLES = "detalles_venta.txt";
	public static final String BEBIDAS = "bebidas_calientes.txt";

	public void cargar(Club club, File carpeta) {
		ArchivoTexto.procesar(new File(carpeta, PRODUCTOS), c -> {
			ArchivoTexto.exigirCampos(c, 5);
			Producto producto;
			if (c[0].equals("TIENDA")) {
				producto = new ArticuloDeportivo(c[1], c[4], Long.parseLong(c[2]));
			} else if (c[0].equals("CAFETERIA")) {
				ArchivoTexto.exigirCampos(c, 6);
				producto = new ProductoCafeteria(c[1], Long.parseLong(c[2]), ArchivoTexto.leerBooleano(c[4]),
						ArchivoTexto.separarLista(c[5]));
			} else {
				throw new IllegalArgumentException("tipo de producto desconocido: " + c[0]);
			}
			if (!ArchivoTexto.leerBooleano(c[3])) {
				producto.desactivar();
			}
			club.agregarProducto(producto);
		});

		ArchivoTexto.procesar(new File(carpeta, EXISTENCIAS), c -> {
			ArchivoTexto.exigirCampos(c, 3);
			club.buscarArticulo(c[0]).restaurarExistencia(c[1], Integer.parseInt(c[2]));
		});

		ArchivoTexto.procesar(new File(carpeta, VENTAS), c -> {
			ArchivoTexto.exigirCampos(c, 6);
			Persona comprador = club.buscarPersona(c[3]);
			LocalDate fecha = LocalDate.parse(c[2]);
			long puntos = Long.parseLong(c[4]);
			Venta venta;
			if (c[1].equals("TIENDA")) {
				ArchivoTexto.exigirCampos(c, 7);
				venta = new VentaTienda(c[0], fecha, comprador, c[5], Integer.parseInt(c[6]), puntos);
			} else if (c[1].equals("CAFETERIA")) {
				venta = new VentaCafeteria(c[0], fecha, comprador, puntos, Integer.parseInt(c[5]));
			} else {
				throw new IllegalArgumentException("tipo de venta desconocido: " + c[1]);
			}
			club.getCuentaFidelidad(comprador).registrarVenta(venta);
		});

		ArchivoTexto.procesar(new File(carpeta, DETALLES), c -> {
			ArchivoTexto.exigirCampos(c, 4);
			club.buscarVenta(c[0]).agregarDetalleGuardado(club.buscarProducto(c[1]), Integer.parseInt(c[2]),
					Long.parseLong(c[3]));
		});

		ArchivoTexto.procesar(new File(carpeta, BEBIDAS), c -> {
			ArchivoTexto.exigirCampos(c, 2);
			club.buscarSocio(c[0]).restaurarBebidaCaliente(LocalDate.parse(c[1]));
		});
	}

	public void salvar(Club club, File carpeta) {
		List<String> productos = new ArrayList<>();
		List<String> existencias = new ArrayList<>();
		List<String> ventas = new ArrayList<>();
		List<String> detalles = new ArrayList<>();
		List<String> bebidas = new ArrayList<>();

		for (Producto p : club.getProductos()) {
			if (p instanceof ArticuloDeportivo) {
				ArticuloDeportivo a = (ArticuloDeportivo) p;
				productos.add(ArchivoTexto.unir("TIENDA", a.getNombre(), a.getPrecio(), a.estaActivo(), a.getCategoria()));
				for (Map.Entry<String, Integer> e : a.getExistencias().entrySet()) {
					existencias.add(ArchivoTexto.unir(a.getNombre(), e.getKey(), e.getValue()));
				}
			} else {
				ProductoCafeteria pc = (ProductoCafeteria) p;
				productos.add(ArchivoTexto.unir("CAFETERIA", pc.getNombre(), pc.getPrecio(), pc.estaActivo(),
						pc.esBebidaCaliente(), ArchivoTexto.unirLista(pc.getAlergenos())));
			}
		}
		for (Venta v : club.getVentas()) {
			if (v instanceof VentaTienda) {
				VentaTienda vt = (VentaTienda) v;
				ventas.add(ArchivoTexto.unir(vt.getId(), "TIENDA", vt.getFecha(), vt.getComprador().getLogin(),
						vt.getPuntosRedimidos(), vt.getCodigoDescuento(), vt.getPorcentajeDescuento()));
			} else {
				VentaCafeteria vc = (VentaCafeteria) v;
				ventas.add(ArchivoTexto.unir(vc.getId(), "CAFETERIA", vc.getFecha(), vc.getComprador().getLogin(),
						vc.getPuntosRedimidos(), vc.getPorcentajePropina()));
			}
			for (DetalleVenta d : v.getDetalles()) {
				detalles.add(ArchivoTexto.unir(v.getId(), d.getProducto().getNombre(), d.getCantidad(), d.getPrecioUnitario()));
			}
		}
		for (Persona p : club.getPersonas()) {
			if (p instanceof Socio && ((Socio) p).getFechaBebidaCaliente() != null) {
				bebidas.add(ArchivoTexto.unir(p.getLogin(), ((Socio) p).getFechaBebidaCaliente()));
			}
		}

		ArchivoTexto.escribir(new File(carpeta, PRODUCTOS), "TIENDA;nombre;precio;activo;categoria | CAFETERIA;nombre;precio;activo;bebidaCaliente;alergenos", productos);
		ArchivoTexto.escribir(new File(carpeta, EXISTENCIAS), "articulo;ubicacion;cantidad", existencias);
		ArchivoTexto.escribir(new File(carpeta, VENTAS), "id;TIENDA;fecha;comprador;puntosRedimidos;codigo;porcentajeDescuento | id;CAFETERIA;fecha;comprador;puntosRedimidos;porcentajePropina", ventas);
		ArchivoTexto.escribir(new File(carpeta, DETALLES), "idVenta;producto;cantidad;precioUnitario", detalles);
		ArchivoTexto.escribir(new File(carpeta, BEBIDAS), "loginSocio;fecha", bebidas);
	}
}
