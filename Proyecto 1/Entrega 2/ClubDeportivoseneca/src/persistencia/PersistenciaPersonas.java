package persistencia;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import global.Club;
import personas.Administrador;
import personas.Empleado;
import personas.Entrenador;
import personas.Fisioterapeuta;
import personas.Persona;
import personas.Rol;
import personas.Socio;

/**
 * personas.txt: ROL;nombre;login;password;activo;extra
 * extra = fecha de nacimiento (socio) o codigo de descuento (empleado).
 */
public class PersistenciaPersonas {

	public static final String ARCHIVO = "personas.txt";

	public void cargar(Club club, File carpeta) {
		ArchivoTexto.procesar(new File(carpeta, ARCHIVO), campos -> {
			ArchivoTexto.exigirCampos(campos, 6);
			Rol rol = Rol.valueOf(campos[0]);
			String nombre = campos[1];
			String login = campos[2];
			String password = campos[3];
			boolean activo = ArchivoTexto.leerBooleano(campos[4]);
			String extra = campos[5];
			Persona persona;
			switch (rol) {
			case SOCIO:
				persona = new Socio(nombre, login, password, LocalDate.parse(extra));
				break;
			case ENTRENADOR:
				persona = new Entrenador(nombre, login, password, extra);
				break;
			case FISIOTERAPEUTA:
				persona = new Fisioterapeuta(nombre, login, password, extra);
				break;
			default:
				persona = new Administrador(nombre, login, password);
			}
			if (!activo) {
				persona.darDeBaja();
			}
			club.registrarPersona(persona);
		});
	}

	public void salvar(Club club, File carpeta) {
		List<String> lineas = new ArrayList<>();
		for (Persona p : club.getPersonas()) {
			String extra = "";
			if (p instanceof Socio) {
				extra = ((Socio) p).getFechaNacimiento().toString();
			} else if (p instanceof Empleado) {
				extra = ((Empleado) p).getCodigoDescuento();
			}
			lineas.add(ArchivoTexto.unir(p.getRol(), p.getNombre(), p.getLogin(), p.getPasswordParaGuardar(),
					p.estaActivo(), extra));
		}
		ArchivoTexto.escribir(new File(carpeta, ARCHIVO), "ROL;nombre;login;password;activo;fechaNacimiento|codigoDescuento", lineas);
	}
}
