package pruebas;

/**
 * Ejecuta los 17 programas de prueba en orden y muestra el total.
 */
public class EjecutarPruebas {

	public static void main(String[] args) {
		PruebaAutenticacion.ejecutar();
		PruebaConsultasGenerales.ejecutar();
		PruebaInscripcionEquipos.ejecutar();
		PruebaInscripcionAcademia.ejecutar();
		PruebaReservas.ejecutar();
		PruebaPartidoPractica.ejecutar();
		PruebaDesempenoYCalendario.ejecutar();
		PruebaComercio.ejecutar();
		PruebaTurnosYSugerenciasEmpleado.ejecutar();
		PruebaPartidoInterno.ejecutar();
		PruebaCompetencia.ejecutar();
		PruebaEntrenamiento.ejecutar();
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
