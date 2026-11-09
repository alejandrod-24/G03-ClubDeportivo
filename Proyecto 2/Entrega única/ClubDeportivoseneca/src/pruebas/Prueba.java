package pruebas;

import java.io.File;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import global.Club;
import global.DatosIniciales;
import global.Reloj;
import persistencia.Persistencia;

/**
 * Utilidades para los programas de prueba de consola.
 * Cada paso imprime [OK] si se comporta como se espera y [FALLO] si no,
 * asi la salida muestra a la vez el estado de la aplicacion y si la logica es correcta.
 */
public class Prueba {

	//Fecha fija para que las pruebas den siempre el mismo resultado
	public static final LocalDate FECHA_INICIAL = LocalDate.of(2026, 10, 17);

	private static int exitos = 0;
	private static int fallos = 0;

	//Club nuevo con los datos iniciales (no toca la carpeta datos/)
	public static Club clubDePrueba() {
		Reloj.fijar(FECHA_INICIAL);
		return DatosIniciales.crearClub();
	}

	public static void titulo(String texto) {
		System.out.println();
		System.out.println("==================================================================");
		System.out.println(" " + texto);
		System.out.println("==================================================================");
	}

	public static void seccion(String texto) {
		System.out.println();
		System.out.println("--- " + texto);
	}

	public static void info(String texto) {
		for (String linea : texto.split("\n")) {
			System.out.println("    " + linea);
		}
	}

	public static void lista(List<?> elementos) {
		if (elementos.isEmpty()) {
			info("(vacio)");
		}
		for (Object e : elementos) {
			info("- " + e);
		}
	}

	public static void debeFuncionar(String descripcion, Runnable accion) {
		try {
			accion.run();
			exitos++;
			System.out.println("  [OK]    " + descripcion);
		} catch (RuntimeException e) {
			fallos++;
			System.out.println("  [FALLO] " + descripcion + " -> error inesperado: " + e.getMessage());
		}
	}

	public static void debeRechazar(String descripcion, Runnable accion) {
		try {
			accion.run();
			fallos++;
			System.out.println("  [FALLO] " + descripcion + " -> se esperaba un rechazo y se acepto");
		} catch (RuntimeException e) {
			exitos++;
			System.out.println("  [OK]    " + descripcion + " -> rechazado: " + e.getMessage());
		}
	}

	public static void verificar(String descripcion, boolean condicion) {
		if (condicion) {
			exitos++;
			System.out.println("  [OK]    " + descripcion);
		} else {
			fallos++;
			System.out.println("  [FALLO] " + descripcion);
		}
	}

	public static Map<String, Integer> items(Object... pares) {
		Map<String, Integer> items = new LinkedHashMap<>();
		for (int i = 0; i + 1 < pares.length; i += 2) {
			items.put((String) pares[i], (Integer) pares[i + 1]);
		}
		return items;
	}

	//Guarda el club en una carpeta temporal dentro de datos/, lo carga de nuevo y borra la carpeta
	public static Club guardarYRecargar(Club club, String nombreCarpeta) {
		File carpeta = new File(Persistencia.CARPETA_POR_DEFECTO, nombreCarpeta);
		try {
			Persistencia persistencia = new Persistencia(carpeta.getPath());
			persistencia.salvar(club);
			return persistencia.cargar();
		} finally {
			File[] archivos = carpeta.listFiles();
			if (archivos != null) {
				for (File f : archivos) {
					f.delete();
				}
			}
			carpeta.delete();
		}
	}

	public static int getFallos() {
		return fallos;
	}

	public static void resumen() {
		System.out.println();
		System.out.println("Resultado: " + exitos + " verificaciones correctas, " + fallos + " fallidas.");
	}
}
