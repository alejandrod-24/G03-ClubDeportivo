package persistencia;

import java.io.File;

import global.Club;
import global.DatosIniciales;

/**
 * Punto de entrada de la persistencia. Carga los grupos en orden
 * (cada grupo referencia a los anteriores por clave):
 * 1. Personas  2. Deportes  3. Partidos y competencias  4. Comercio  5. Administracion
 * Integracion etapa 3: las instalaciones se cargan entre deportes y partidos; las academias, partidos de practica y sesiones de entrenamiento se agregan a sus grupos.
 */
public class Persistencia {

	public static final String CARPETA_POR_DEFECTO = "datos";

	//Atributos
	private final File carpeta;
	private final PersistenciaPersonas personas = new PersistenciaPersonas();
	private final PersistenciaDeportes deportes = new PersistenciaDeportes();
	private final PersistenciaPartidos partidos = new PersistenciaPartidos();
	private final PersistenciaComercio comercio = new PersistenciaComercio();
	private final PersistenciaAdministracion administracion = new PersistenciaAdministracion();

	//Constructor
	public Persistencia(String carpeta) {
		this.carpeta = new File(carpeta);
	}

	public Persistencia() {
		this(CARPETA_POR_DEFECTO);
	}

	//Metodos
	public boolean existenDatos() {
		return new File(carpeta, PersistenciaPersonas.ARCHIVO).exists();
	}

	//Rechaza si la carpeta/archivo no existe o esta corrupto
	public Club cargar() {
		if (!carpeta.isDirectory()) {
			throw new IllegalStateException("No existe la carpeta de datos: " + carpeta.getPath());
		}
		Club club = new Club();
		personas.cargar(club, carpeta);
		deportes.cargar(club, carpeta);
		partidos.cargar(club, carpeta);
		comercio.cargar(club, carpeta);
		administracion.cargar(club, carpeta);
		return club;
	}

	public void salvar(Club club) {
		if (!carpeta.exists() && !carpeta.mkdirs()) {
			throw new IllegalStateException("No se pudo crear la carpeta " + carpeta.getPath());
		}
		personas.salvar(club, carpeta);
		deportes.salvar(club, carpeta);
		partidos.salvar(club, carpeta);
		comercio.salvar(club, carpeta);
		administracion.salvar(club, carpeta);
	}

	//Si ya hay datos guardados los carga; si no, crea el club con los datos iniciales
	public Club cargarOCrear() {
		return existenDatos() ? cargar() : DatosIniciales.crearClub();
	}
}
