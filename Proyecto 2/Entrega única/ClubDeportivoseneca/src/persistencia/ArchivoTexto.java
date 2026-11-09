package persistencia;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Utilidades para leer y escribir archivos de texto: una linea por objeto,
 * campos separados por ';' y listas dentro de un campo separadas por ','.
 * Las lineas vacias o que empiezan por '#' se ignoran.
 */
public class ArchivoTexto {

	public static final String SEPARADOR = ";";
	public static final String SEPARADOR_LISTA = ",";

	//Recorre las lineas del archivo. Si una linea no se puede interpretar, el archivo esta corrupto.
	public static void procesar(File archivo, Consumer<String[]> accion) {
		if (!archivo.exists()) {
			throw new IllegalStateException("Archivo inexistente: " + archivo.getPath());
		}
		List<String> lineas;
		try {
			lineas = Files.readAllLines(archivo.toPath(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException("No se pudo leer " + archivo.getPath(), e);
		}
		for (int i = 0; i < lineas.size(); i++) {
			String linea = lineas.get(i).trim();
			if (linea.isEmpty() || linea.startsWith("#")) {
				continue;
			}
			try {
				accion.accept(linea.split(SEPARADOR, -1));
			} catch (RuntimeException e) {
				throw new IllegalStateException("Archivo corrupto: " + archivo.getName() + " linea " + (i + 1)
						+ " (" + e.getMessage() + ")", e);
			}
		}
	}

	public static void escribir(File archivo, String encabezado, List<String> lineas) {
		List<String> contenido = new ArrayList<>();
		contenido.add("# " + encabezado);
		contenido.addAll(lineas);
		try {
			Files.write(archivo.toPath(), contenido, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException("No se pudo escribir " + archivo.getPath(), e);
		}
	}

	public static String unir(Object... campos) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < campos.length; i++) {
			if (i > 0) {
				sb.append(SEPARADOR);
			}
			sb.append(campos[i]);
		}
		return sb.toString();
	}

	public static String unirLista(List<String> valores) {
		return String.join(SEPARADOR_LISTA, valores);
	}

	public static List<String> separarLista(String campo) {
		List<String> valores = new ArrayList<>();
		if (campo.isBlank()) {
			return valores;
		}
		for (String valor : campo.split(SEPARADOR_LISTA)) {
			valores.add(valor.trim());
		}
		return valores;
	}

	public static void exigirCampos(String[] campos, int cantidad) {
		if (campos.length < cantidad) {
			throw new IllegalArgumentException("se esperaban " + cantidad + " campos y hay " + campos.length);
		}
	}

	public static boolean leerBooleano(String campo) {
		if (!campo.equals("true") && !campo.equals("false")) {
			throw new IllegalArgumentException("valor booleano invalido: " + campo);
		}
		return Boolean.parseBoolean(campo);
	}
}
