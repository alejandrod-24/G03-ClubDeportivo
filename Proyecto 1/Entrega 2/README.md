# Proyecto 1 — Entrega 2: Club Deportivo Séneca

Diseño e implementación del sistema de analítica deportiva del Club Deportivo Séneca (DPOO, grupo G03).

## Contenido

| Carpeta | Qué tiene |
|---|---|
| `ClubDeportivoseneca/` | Proyecto Eclipse (Java 21). |
| `ClubDeportivoseneca/src/` | Código fuente, organizado en paquetes por área (`personas`, `deportes`, `partidos`, `competencias`, `instalaciones`, `entrenamiento`, `comercio`, `administracion`, `persistencia`, `global`, `consola`, `pruebas`). |
| `ClubDeportivoseneca/datos/` | Datos persistentes en archivos de texto (uno por tipo de objeto). La aplicación los lee y escribe aquí. |
| `ClubDeportivoseneca/diagramas/` | Diagramas de clases: fuente PlantUML (`.puml`) e imagen (`.png`). Hay un diagrama de alto nivel (`CDS_UML_simplificado`) y el diseño completo partido en 7 áreas (`CDS_UML_completo_*`). |
| `Documento de diseño/` | Documento de diseño, con las justificaciones y los diagramas de secuencia. |

## Cómo ejecutar

### Desde Eclipse
1. `File → Import → General → Existing Projects into Workspace` y elegir la carpeta `ClubDeportivoseneca`.
2. **Consola de la aplicación:** clic derecho en `src/consola/MenuConsola.java` → `Run As → Java Application`.
3. **Programas de prueba:** clic derecho en `src/pruebas/EjecutarPruebas.java` → `Run As → Java Application`. Cada programa de `pruebas/` (por ejemplo, `PruebaReservas`) también se puede correr por separado.

El directorio de trabajo tiene que ser `ClubDeportivoseneca/` (es el valor por defecto de Eclipse), porque la aplicación busca la carpeta `datos/` ahí.

### Desde la terminal
Desde la carpeta `ClubDeportivoseneca/`:

```bash
javac -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp bin consola.MenuConsola
java -cp bin pruebas.EjecutarPruebas
```

## Usuarios de ejemplo

| Rol | Login | Password |
|---|---|---|
| Administrador | `admin` | `admin123` |
| Entrenadores | `carlos`, `laura`, `jaime` | `carlos123`, `laura123`, `jaime123` |
| Fisioterapeuta | `pedro` | `pedro123` |
| Socios | `ana`, `luis`, `diego`, `valentina`, … | `1234` |

## Notas
- La consola guarda el estado en `datos/` después de cada acción. Si `datos/` está vacía, se crea el club de ejemplo (`global.DatosIniciales`).
- Los programas de prueba **no modifican** `datos/`: parten de un club de ejemplo en memoria y fijan la fecha en 2026-10-17 para que la salida sea siempre la misma.
- La consola tiene una opción para simular el paso del tiempo (cambiar la fecha y hora), necesaria para registrar resultados de partidos o asistencias de sesiones ya programadas.
