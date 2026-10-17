package persistencia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import deportes.Categoria;
import deportes.Deporte;
import deportes.DeporteConjunto;
import deportes.DeporteIndividual;
import deportes.Equipo;
import deportes.InscripcionEquipo;
import global.Club;

/**
 * deportes.txt:              CONJUNTO;nombre;minimoJugadores;permiteEmpate;posiciones | INDIVIDUAL;nombre
 * equipos.txt:               deporte;nombre;categoria;cupoMaximo;activo
 * inscripciones_equipo.txt:  deporte;equipo;loginSocio;posicion;camiseta
 */
public class PersistenciaDeportes {

	public static final String DEPORTES = "deportes.txt";
	public static final String EQUIPOS = "equipos.txt";
	public static final String INSCRIPCIONES = "inscripciones_equipo.txt";

	public void cargar(Club club, File carpeta) {
		ArchivoTexto.procesar(new File(carpeta, DEPORTES), campos -> {
			ArchivoTexto.exigirCampos(campos, 2);
			if (campos[0].equals("CONJUNTO")) {
				ArchivoTexto.exigirCampos(campos, 5);
				club.agregarDeporte(new DeporteConjunto(campos[1], Integer.parseInt(campos[2]),
						ArchivoTexto.leerBooleano(campos[3]), ArchivoTexto.separarLista(campos[4])));
			} else if (campos[0].equals("INDIVIDUAL")) {
				club.agregarDeporte(new DeporteIndividual(campos[1]));
			} else {
				throw new IllegalArgumentException("tipo de deporte desconocido: " + campos[0]);
			}
		});

		ArchivoTexto.procesar(new File(carpeta, EQUIPOS), campos -> {
			ArchivoTexto.exigirCampos(campos, 5);
			Equipo equipo = club.buscarDeporteConjunto(campos[0])
					.crearEquipo(campos[1], Categoria.valueOf(campos[2]), Integer.parseInt(campos[3]));
			if (!ArchivoTexto.leerBooleano(campos[4])) {
				equipo.darDeBaja();
			}
		});

		ArchivoTexto.procesar(new File(carpeta, INSCRIPCIONES), campos -> {
			ArchivoTexto.exigirCampos(campos, 5);
			club.buscarEquipo(campos[0], campos[1])
					.agregarInscripcionGuardada(club.buscarSocio(campos[2]), campos[3], Integer.parseInt(campos[4]));
		});
	}

	public void salvar(Club club, File carpeta) {
		List<String> deportes = new ArrayList<>();
		List<String> equipos = new ArrayList<>();
		List<String> inscripciones = new ArrayList<>();
		for (Deporte d : club.getDeportes()) {
			if (d instanceof DeporteConjunto) {
				DeporteConjunto dc = (DeporteConjunto) d;
				deportes.add(ArchivoTexto.unir("CONJUNTO", dc.getNombre(), dc.getMinimoJugadores(),
						dc.permiteEmpate(), ArchivoTexto.unirLista(dc.getPosiciones())));
				for (Equipo e : dc.getEquipos()) {
					equipos.add(ArchivoTexto.unir(dc.getNombre(), e.getNombre(), e.getCategoria(),
							e.getCupoMaximo(), e.estaActivo()));
					for (InscripcionEquipo i : e.getInscripciones()) {
						inscripciones.add(ArchivoTexto.unir(dc.getNombre(), e.getNombre(), i.getSocio().getLogin(),
								i.getPosicion(), i.getNumeroCamiseta()));
					}
				}
			} else {
				deportes.add(ArchivoTexto.unir("INDIVIDUAL", d.getNombre()));
			}
		}
		ArchivoTexto.escribir(new File(carpeta, DEPORTES), "CONJUNTO;nombre;minimoJugadores;permiteEmpate;posiciones | INDIVIDUAL;nombre", deportes);
		ArchivoTexto.escribir(new File(carpeta, EQUIPOS), "deporte;nombre;categoria;cupoMaximo;activo", equipos);
		ArchivoTexto.escribir(new File(carpeta, INSCRIPCIONES), "deporte;equipo;loginSocio;posicion;camiseta", inscripciones);
	}
}
