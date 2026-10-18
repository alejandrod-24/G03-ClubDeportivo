package pruebas;

import java.util.List;

import comercio.ArticuloDeportivo;
import global.Club;

/**
 * 15. Gestion del catalogo, reabastecimiento, movimiento entre ubicaciones, consulta de inventario
 *     y resolucion de sugerencias.
 * RF-ADM-08 a 12
 */
public class PruebaGestionInventario {

	private static Club club;

	public static void main(String[] args) {
		ejecutar();
		Prueba.resumen();
	}

	public static void ejecutar() {
		Prueba.titulo("PRUEBA 15: Catalogo e inventario (administrador)");
		club = Prueba.clubDePrueba();
		club.iniciarSesion("admin", "admin123");

		Prueba.seccion("Gestionar catalogo (RF-ADM-08)");
		Prueba.debeFuncionar("crear el articulo 'Rodilleras' (Proteccion, $45000)",
				() -> Prueba.info("Creado: " + club.crearArticulo("Rodilleras", "Proteccion", 45000)));
		Prueba.debeFuncionar("crear la bebida caliente 'Te caliente' ($4000)",
				() -> Prueba.info("Creado: " + club.crearProductoCafeteria("Te caliente", 4000, true, List.of())));
		Prueba.debeRechazar("nombre que ya existe (Cafe)", () -> club.crearArticulo("Cafe", "Bebidas", 5000));
		Prueba.debeRechazar("precio cero", () -> club.crearArticulo("Gorra", "Accesorios", 0));
		Prueba.debeFuncionar("cambiar el precio del cafe a $5500", () -> club.cambiarPrecioProducto("Cafe", 5500));
		Prueba.verificar("el cafe cuesta $5500", club.buscarProducto("Cafe").getPrecio() == 5500);
		Prueba.debeRechazar("precio negativo", () -> club.cambiarPrecioProducto("Cafe", -1));
		Prueba.debeFuncionar("desactivar el sandwich", () -> club.desactivarProducto("Sandwich"));
		Prueba.debeRechazar("desactivarlo otra vez", () -> club.desactivarProducto("Sandwich"));
		Prueba.info("Catalogo de la cafeteria:");
		Prueba.lista(club.consultarCatalogoCafeteria());
		club.iniciarSesion("carlos", "carlos123");
		Prueba.debeRechazar("un entrenador no gestiona el catalogo", () -> club.crearArticulo("Gorra", "Accesorios", 30000));
		club.iniciarSesion("admin", "admin123");

		Prueba.seccion("Reabastecer la tienda (RF-ADM-09)");
		Prueba.debeFuncionar("10 rodilleras nuevas", () -> club.reabastecerTienda("Rodilleras", 10));
		Prueba.verificar("quedan 10 rodilleras en la tienda",
				club.buscarArticulo("Rodilleras").getExistencia(ArticuloDeportivo.TIENDA) == 10);
		Prueba.debeRechazar("cantidad cero", () -> club.reabastecerTienda("Rodilleras", 0));
		Prueba.debeRechazar("articulo inexistente", () -> club.reabastecerTienda("Patines", 5));
		Prueba.debeRechazar("un producto de la cafeteria", () -> club.reabastecerTienda("Cafe", 5));
		club.desactivarProducto("Malla de voleibol");
		Prueba.debeRechazar("articulo desactivado (Malla de voleibol)", () -> club.reabastecerTienda("Malla de voleibol", 5));

		Prueba.seccion("Mover articulos entre ubicaciones (RF-ADM-10)");
		Prueba.info("Inventario antes:");
		Prueba.lista(club.consultarInventario());
		Prueba.debeFuncionar("mover 10 tubos de pelotas de la Bodega a la Tienda",
				() -> club.moverArticulo("Tubo de pelotas", "Bodega", "Tienda", 10));
		Prueba.debeFuncionar("mover 3 uniformes a una ubicacion nueva (Bodega Norte)",
				() -> club.moverArticulo("Uniforme Seneca", "Tienda", "Bodega Norte", 3));
		Prueba.debeRechazar("mover mas raquetas de las que hay en la bodega",
				() -> club.moverArticulo("Raqueta de tenis", "Bodega", "Tienda", 50));
		Prueba.debeRechazar("cantidad negativa", () -> club.moverArticulo("Raqueta de tenis", "Bodega", "Tienda", -2));
		Prueba.debeRechazar("mismo origen y destino", () -> club.moverArticulo("Raqueta de tenis", "Tienda", "Tienda", 1));

		Prueba.seccion("Consultar inventario (RF-ADM-11)");
		Prueba.info("Inventario despues (articulos activos):");
		Prueba.lista(club.consultarInventario());

		Prueba.seccion("Resolver sugerencias (RF-ADM-12)");
		club.iniciarSesion("pedro", "pedro123");
		String gel = club.sugerirProducto("Gel de recuperacion", "Recuperacion", 45000, List.of()).getId();
		String barra = club.sugerirProducto("Barra de proteina", "Snacks", 6000, List.of("Mani")).getId();
		String toalla = club.sugerirProducto("Toalla deportiva", "Accesorios", 35000, List.of()).getId();
		club.iniciarSesion("admin", "admin123");
		club.crearArticulo("Toalla deportiva", "Accesorios", 35000);
		Prueba.debeFuncionar("aprobar el gel de recuperacion: entra a la tienda sin existencias",
				() -> Prueba.info("Agregado al catalogo: " + club.aprobarSugerencia(gel)));
		Prueba.verificar("el gel aprobado ya esta en el catalogo de la tienda",
				club.consultarCatalogoTienda().contains(club.buscarProducto("Gel de recuperacion")));
		Prueba.debeRechazar("crear de nuevo el gel: ya esta en el catalogo",
				() -> club.crearArticulo("Gel de recuperacion", "Recuperacion", 45000));
		Prueba.debeFuncionar("rechazar la barra de proteina", () -> club.rechazarSugerencia(barra));
		Prueba.debeRechazar("aprobar la toalla: ya existe en el catalogo", () -> club.aprobarSugerencia(toalla));
		Prueba.debeRechazar("rechazar el gel, que ya fue aprobado", () -> club.rechazarSugerencia(gel));
		Prueba.debeRechazar("sugerencia inexistente", () -> club.aprobarSugerencia("SG99"));
		Prueba.lista(club.getSugerencias());

		Prueba.seccion("El catalogo y el inventario sobreviven a guardar y cargar");
		Club recargado = Prueba.guardarYRecargar(club, "_prueba_inventario");
		recargado.iniciarSesion("admin", "admin123");
		Prueba.verificar("inventario identico tras recargar", club.consultarInventario().equals(recargado.consultarInventario()));
		Prueba.verificar("se conservan los " + club.getProductos().size() + " productos",
				recargado.getProductos().size() == club.getProductos().size());
		Prueba.verificar("se conservan las sugerencias y su estado",
				club.getSugerencias().toString().equals(recargado.getSugerencias().toString()));
	}
}
