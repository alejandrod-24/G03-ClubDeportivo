package pruebas;

/**
 * Ejecuta todos los programas de prueba disponibles y muestra el total.
 * Integracion: agregar las pruebas 4, 5, 6, 7 y 12 (academias, reservas, partidos de practica,
 * desempeno y calendario, y entrenamiento).
 */
public class EjecutarPruebas {

	public static void main(String[] args) {
		PruebaAutenticacion.ejecutar();
		PruebaConsultasGenerales.ejecutar();
		PruebaInscripcionEquipos.ejecutar();
		PruebaComercio.ejecutar();
		PruebaTurnosYSugerenciasEmpleado.ejecutar();
		PruebaPartidoInterno.ejecutar();
		PruebaCompetencia.ejecutar();
		PruebaGestionUsuarios.ejecutar();
		PruebaGestionDeportiva.ejecutar();
		PruebaGestionInventario.ejecutar();
		PruebaTurnosAdministrador.ejecutar();
		PruebaHistorialesYReportes.ejecutar();
		Prueba.resumen();
		if (Prueba.getFallos() > 0) {
			System.exit(1);
		}
	}
}
