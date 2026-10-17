package pruebas;

/**
 * Ejecuta todos los programas de prueba disponibles y muestra el total.
 * (Etapas 1 y 2: pruebas 1, 2, 3, 10, 11, 13 y 14.)
 */
public class EjecutarPruebas {

	public static void main(String[] args) {
		PruebaAutenticacion.ejecutar();
		PruebaConsultasGenerales.ejecutar();
		PruebaInscripcionEquipos.ejecutar();
		PruebaPartidoInterno.ejecutar();
		PruebaCompetencia.ejecutar();
		PruebaGestionUsuarios.ejecutar();
		PruebaGestionDeportiva.ejecutar();
		Prueba.resumen();
		if (Prueba.getFallos() > 0) {
			System.exit(1);
		}
	}
}
