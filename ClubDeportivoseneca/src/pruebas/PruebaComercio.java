package pruebas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import comercio.ArticuloDeportivo;
import comercio.Venta;
import comercio.VentaCafeteria;
import comercio.VentaTienda;
import global.Club;
import global.Reloj;

/**
 * 8. Compras en tienda y cafeteria de socios y empleados: impuestos, codigo de descuento, alergenos,
 *    propina, puntos de fidelidad, bebida caliente y pago de la mensualidad.
 * RF-SOC-14 a 17, RF-EMP-04, 05 (HU de mensualidad)
 */
public class PruebaComercio {

	private static Club club;

	public static void main(String[] args) {
		ejecutar();
		Prueba.resumen();
	}

	public static void ejecutar() {
		Prueba.titulo("PRUEBA 8: Tienda, cafeteria, puntos y mensualidad");
		club = Prueba.clubDePrueba();
		club.iniciarSesion("admin", "admin123");
		club.desactivarProducto("Agua");

		Prueba.seccion("Comprar en la tienda (RF-SOC-14)");
		club.iniciarSesion("ana", "1234");
		Prueba.debeFuncionar("ana compra 2 balones y 1 tubo de pelotas (IVA 19%, sin descuento)", () -> {
			VentaTienda v = club.comprarEnTienda(Prueba.items("Balon de futbol", 2, "Tubo de pelotas", 1), null, 0);
			Prueba.info(v.generarDesglose());
			Prueba.verificar("total $220150 y 4403 puntos generados", v.getTotal() == 220150 && v.getPuntosGenerados() == 4403);
		});
		Prueba.debeFuncionar("ana usa el codigo de carlos (8%) en un uniforme", () -> {
			VentaTienda v = club.comprarEnTienda(Prueba.items("Uniforme Seneca", 1), "DESC-CARLOS", 0);
			Prueba.info(v.generarDesglose());
			Prueba.verificar("descuento $9600 y total $131376", v.getDescuento() == 9600 && v.getTotal() == 131376);
		});
		Prueba.debeRechazar("codigo inexistente", () -> club.comprarEnTienda(Prueba.items("Balon de futbol", 1), "DESC-NADIE", 0));
		Prueba.debeRechazar("codigo y puntos a la vez (no acumulable)",
				() -> club.comprarEnTienda(Prueba.items("Balon de futbol", 1), "DESC-CARLOS", 1000));
		Prueba.debeRechazar("cantidad cero", () -> club.comprarEnTienda(Prueba.items("Balon de futbol", 0), null, 0));
		Prueba.debeRechazar("5 tubos de pelotas (en la tienda queda 1)",
				() -> club.comprarEnTienda(Prueba.items("Tubo de pelotas", 5), null, 0));
		Prueba.debeRechazar("articulo inexistente", () -> club.comprarEnTienda(Prueba.items("Patines", 1), null, 0));
		Prueba.debeRechazar("un producto de la cafeteria en la tienda",
				() -> club.comprarEnTienda(Prueba.items("Cafe", 1), null, 0));
		Prueba.debeRechazar("redimir mas puntos de los que tiene (10000)",
				() -> club.comprarEnTienda(Prueba.items("Balon de futbol", 1), null, 10000));
		Prueba.debeRechazar("compra vacia", () -> club.comprarEnTienda(Prueba.items(), null, 0));
		Prueba.debeFuncionar("ana redime 5000 puntos en un tubo de pelotas", () -> {
			VentaTienda v = club.comprarEnTienda(Prueba.items("Tubo de pelotas", 1), null, 5000);
			Prueba.info(v.generarDesglose());
		});
		Prueba.verificar("la tienda queda sin tubos de pelotas",
				club.buscarArticulo("Tubo de pelotas").getExistencia(ArticuloDeportivo.TIENDA) == 0);

		Prueba.seccion("Compras de empleados (RF-EMP-04, 05)");
		club.iniciarSesion("pedro", "pedro123");
		Prueba.info("Codigo de descuento de pedro: " + club.consultarCodigoDescuento());
		Prueba.debeFuncionar("pedro compra una raqueta: su 15% se aplica solo", () -> {
			VentaTienda v = club.comprarEnTienda(Prueba.items("Raqueta de tenis", 1), null, 0);
			Prueba.info(v.generarDesglose());
			Prueba.verificar("descuento $52500 y total $354025", v.getDescuento() == 52500 && v.getTotal() == 354025);
		});
		Prueba.debeRechazar("pedro intenta usar otro codigo", () -> club.comprarEnTienda(Prueba.items("Balon de futbol", 1),
				"DESC-LAURA", 0));
		Prueba.debeFuncionar("si pedro redime puntos no tiene el 15%", () -> {
			VentaTienda v = club.comprarEnTienda(Prueba.items("Balon de futbol", 1), null, 5000);
			Prueba.verificar("descuento $0, base $75000", v.getDescuento() == 0 && v.getBase() == 75000);
		});
		club.iniciarSesion("admin", "admin123");
		Prueba.debeRechazar("el administrador no compra", () -> club.comprarEnTienda(Prueba.items("Balon de futbol", 1), null, 0));

		Prueba.seccion("Comprar en la cafeteria (RF-SOC-15)");
		club.iniciarSesion("luis", "1234");
		Prueba.info("Alergenos de galletas de mani y jugo natural:");
		Prueba.lista(club.consultarAlergenos(Prueba.items("Galletas de mani", 1, "Jugo natural", 1)));
		Prueba.debeRechazar("comprar sin confirmar los alergenos", () -> club.comprarEnCafeteria(
				Prueba.items("Galletas de mani", 1, "Jugo natural", 1), 0, VentaCafeteria.PROPINA_SUGERIDA, false));
		Prueba.debeFuncionar("comprar confirmando los alergenos, con la propina sugerida (10%)", () -> {
			VentaCafeteria v = club.comprarEnCafeteria(Prueba.items("Galletas de mani", 1, "Jugo natural", 1), 0,
					VentaCafeteria.PROPINA_SUGERIDA, true);
			Prueba.info(v.generarDesglose());
			Prueba.verificar("impuesto al consumo $880, propina $1100, total $12980",
					v.getImpuesto() == 880 && v.getPropina() == 1100 && v.getTotal() == 12980);
		});
		Prueba.debeFuncionar("luis cambia la propina a 0%", () -> Prueba.info(
				club.comprarEnCafeteria(Prueba.items("Jugo natural", 1), 0, 0, false).toString()));
		Prueba.debeRechazar("propina negativa", () -> club.comprarEnCafeteria(Prueba.items("Jugo natural", 1), 0, -5, false));
		Prueba.debeRechazar("producto desactivado (Agua)", () -> club.comprarEnCafeteria(Prueba.items("Agua", 1), 0, 10, false));
		Prueba.debeRechazar("producto inexistente", () -> club.comprarEnCafeteria(Prueba.items("Pizza", 1), 0, 10, false));
		club.iniciarSesion("ana", "1234");
		Prueba.debeRechazar("redimir mas puntos que el subtotal (6000 en un cafe de $5000)",
				() -> club.comprarEnCafeteria(Prueba.items("Cafe", 1), 6000, 10, false));

		Prueba.seccion("Bebida caliente (RF-SOC-15, 17)");
		Prueba.debeFuncionar("ana compra un cafe", () -> club.comprarEnCafeteria(Prueba.items("Cafe", 1), 0, 10, false));
		Prueba.verificar("queda marcada con bebida caliente", club.buscarSocio("ana").tieneBebidaCaliente());
		Prueba.debeFuncionar("ana declara que termino su bebida", () -> club.declararFinBebidaCaliente());
		Prueba.debeRechazar("declararlo otra vez sin tener bebida caliente", () -> club.declararFinBebidaCaliente());
		Prueba.debeFuncionar("ana compra un chocolate caliente (tiene lacteos)",
				() -> club.comprarEnCafeteria(Prueba.items("Chocolate caliente", 1), 0, 10, true));
		Prueba.info(club.consultarPerfil());
		Reloj.avanzarDias(1);
		Prueba.verificar("al dia siguiente la marca vence", !club.buscarSocio("ana").tieneBebidaCaliente());

		Prueba.seccion("Consultar puntos e historial de compras (RF-SOC-16)");
		club.iniciarSesion("ana", "1234");
		Prueba.info(club.consultarPuntos());
		club.iniciarSesion("pedro", "pedro123");
		Prueba.info(club.consultarPuntos());

		Prueba.seccion("Pagar mensualidad");
		club.iniciarSesion("ana", "1234");
		Prueba.debeFuncionar("ana paga la mensualidad de octubre", () -> Prueba.info(club.pagarMensualidad().toString()));
		Prueba.debeRechazar("pagarla otra vez", () -> club.pagarMensualidad());
		Reloj.fijar(LocalDate.of(2026, 11, 2));
		Prueba.debeFuncionar("en noviembre ya puede pagar la siguiente", () -> Prueba.info(club.pagarMensualidad().toString()));
		club.iniciarSesion("carlos", "carlos123");
		Prueba.debeRechazar("un entrenador no paga mensualidad", () -> club.pagarMensualidad());

		Prueba.seccion("Las ventas, existencias, puntos y la bebida caliente sobreviven a guardar y cargar");
		club.iniciarSesion("ana", "1234");
		club.comprarEnCafeteria(Prueba.items("Cafe", 1), 0, 10, false);
		Club recargado = Prueba.guardarYRecargar(club, "_prueba_comercio");
		recargado.iniciarSesion("ana", "1234");
		Prueba.verificar("puntos e historial de ana identicos tras recargar", club.consultarPuntos().equals(recargado.consultarPuntos()));
		Prueba.verificar("perfil de ana identico (mensualidad y bebida caliente)", club.consultarPerfil().equals(recargado.consultarPerfil()));
		Prueba.verificar("existencias de la raqueta identicas", recargado.buscarArticulo("Raqueta de tenis").getExistencias()
				.equals(club.buscarArticulo("Raqueta de tenis").getExistencias()));
		Prueba.verificar("el agua sigue desactivada", !recargado.buscarProducto("Agua").estaActivo());
		Prueba.verificar("se conservan las " + club.getVentas().size() + " ventas", recargado.getVentas().size() == club.getVentas().size());
		Prueba.verificar("cada venta conserva su total", totales(club).equals(totales(recargado)));
	}

	private static List<Long> totales(Club c) {
		List<Long> totales = new ArrayList<>();
		for (Venta v : c.getVentas()) {
			totales.add(v.getTotal());
		}
		return totales;
	}
}
