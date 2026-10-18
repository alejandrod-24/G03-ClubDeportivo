package persistencia;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import deportes.Categoria;
import deportes.Deporte;
import deportes.DeporteConjunto;
import deportes.DeporteIndividual;
import deportes.Equipo;
import deportes.InscripcionAcademia;
import deportes.InscripcionEquipo;
import deportes.Nivel;
import deportes.RegistroRanking;
import global.Club;
import instalaciones.Modalidad;

/**
 * deportes.txt:              CONJUNTO;nombre;minimoJugadores;permiteEmpate;posiciones | INDIVIDUAL;nombre;maximoSimultaneas;modalidades
 * equipos.txt:               deporte;nombre;categoria;cupoMaximo;activo
 * inscripciones_equipo.txt:  deporte;equipo;loginSocio;posicion;camiseta
 * inscripciones_academia.txt: deporte;loginSocio;nivel;activa
 * ranking.txt:               deporte;loginSocio;fecha;puntaje;motivo  (historial ELO, en orden)
 */
public class PersistenciaDeportes {

	public static final String DEPORTES = "deportes.txt";
	public static final String EQUIPOS = "equipos.txt";
	public static final String INSCRIPCIONES = "inscripciones_equipo.txt";
	public static final String ACADEMIAS = "inscripciones_academia.txt";
	public static final String RANKING = "ranking.txt";

	public void cargar(Club club, File carpeta) {
		ArchivoTexto.procesar(new File(carpeta, DEPORTES), campos -> {
			ArchivoTexto.exigirCampos(campos, 2);
			if (campos[0].equals("CONJUNTO")) {
				ArchivoTexto.exigirCampos(campos, 5);
				club.agregarDeporte(new DeporteConjunto(campos[1], Integer.parseInt(campos[2]),
						ArchivoTexto.leerBooleano(campos[3]), ArchivoTexto.separarLista(campos[4])));
			} else if (campos[0].equals("INDIVIDUAL")) {
				ArchivoTexto.exigirCampos(campos, 4);
				List<Modalidad> modalidades = new ArrayList<>();
				for (String m : ArchivoTexto.separarLista(campos[3])) {
					modalidades.add(Modalidad.valueOf(m));
				}
				club.agregarDeporte(new DeporteIndividual(campos[1], Integer.parseInt(campos[2]), modalidades));
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

		ArchivoTexto.procesar(new File(carpeta, ACADEMIAS), campos -> {
			ArchivoTexto.exigirCampos(campos, 4);
			club.buscarDeporteIndividual(campos[0]).getAcademia().agregarInscripcionGuardada(club.buscarSocio(campos[1]),
					Nivel.valueOf(campos[2]), ArchivoTexto.leerBooleano(campos[3]));
		});

		ArchivoTexto.procesar(new File(carpeta, RANKING), campos -> {
			ArchivoTexto.exigirCampos(campos, 5);
			InscripcionAcademia i = club.buscarDeporteIndividual(campos[0]).getAcademia()
					.buscarInscripcion(club.buscarSocio(campos[1]));
			if (i == null) {
				throw new IllegalArgumentException(campos[1] + " no tiene inscripcion en " + campos[0]);
			}
			i.getRanking().registrar(LocalDate.parse(campos[2]), Integer.parseInt(campos[3]), campos[4]);
		});
	}

	public void salvar(Club club, File carpeta) {
		List<String> deportes = new ArrayList<>();
		List<String> equipos = new ArrayList<>();
		List<String> inscripciones = new ArrayList<>();
		List<String> academias = new ArrayList<>();
		List<String> ranking = new ArrayList<>();
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
				DeporteIndividual di = (DeporteIndividual) d;
				List<String> modalidades = new ArrayList<>();
				for (Modalidad m : di.getModalidades()) {
					modalidades.add(m.name());
				}
				deportes.add(ArchivoTexto.unir("INDIVIDUAL", di.getNombre(), di.getMaximoSimultaneas(),
						ArchivoTexto.unirLista(modalidades)));
				for (InscripcionAcademia i : di.getAcademia().getInscripciones()) {
					academias.add(ArchivoTexto.unir(di.getNombre(), i.getSocio().getLogin(), i.getNivel(), i.estaActiva()));
					for (RegistroRanking r : i.getRanking().getHistorial()) {
						ranking.add(ArchivoTexto.unir(di.getNombre(), i.getSocio().getLogin(), r.getFecha(), r.getPuntaje(),
								r.getMotivo()));
					}
				}
			}
		}
		ArchivoTexto.escribir(new File(carpeta, DEPORTES), "CONJUNTO;nombre;minimoJugadores;permiteEmpate;posiciones | INDIVIDUAL;nombre;maximoSimultaneas;modalidades", deportes);
		ArchivoTexto.escribir(new File(carpeta, EQUIPOS), "deporte;nombre;categoria;cupoMaximo;activo", equipos);
		ArchivoTexto.escribir(new File(carpeta, INSCRIPCIONES), "deporte;equipo;loginSocio;posicion;camiseta", inscripciones);
		ArchivoTexto.escribir(new File(carpeta, ACADEMIAS), "deporte;loginSocio;nivel;activa", academias);
		ArchivoTexto.escribir(new File(carpeta, RANKING), "deporte;loginSocio;fecha;puntaje;motivo", ranking);
	}
}
