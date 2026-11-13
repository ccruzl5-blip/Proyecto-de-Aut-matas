import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lee un archivo de texto con la definicion de una MT y construye un objeto
 * {@link MaquinaTuring}. Valida la sintaxis y la coherencia de la definicion.
 *
 * Formato del archivo (una instruccion por linea):
 *   Estados( q0, q1, qa, qr )
 *   Inicial( q0 )
 *   Ha( qa )
 *   He( qr )
 *   Alfabeto( a, b, X, _ )
 *   MT (q0, a) = (q1, X, D)
 *
 * Las lineas vacias y las que empiezan con # o // se ignoran (comentarios).
 */
public class LectorDefinicion {

    // Expresiones regulares: cada una reconoce un tipo de linea.
    private static final Pattern P_ESTADOS = Pattern.compile("^Estados\\s*\\((.*)\\)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_INICIAL = Pattern.compile("^Inicial\\s*\\((.*)\\)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_HA = Pattern.compile("^Ha\\s*\\((.*)\\)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_HE = Pattern.compile("^He\\s*\\((.*)\\)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_ALFABETO = Pattern.compile("^Alfabeto\\s*\\((.*)\\)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_MT = Pattern.compile(
            "^MT\\s*\\(\\s*([^,\\s]+)\\s*,\\s*(\\S)\\s*\\)\\s*=\\s*\\(\\s*([^,\\s]+)\\s*,\\s*(\\S)\\s*,\\s*([DIN])\\s*\\)$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Carga la maquina desde el archivo indicado.
     *
     * @throws IOException si no se puede leer el archivo
     * @throws IllegalArgumentException si la definicion tiene errores (el mensaje indica la linea)
     */
    public static MaquinaTuring cargar(String ruta) throws IOException {
        List<String> lineas = Files.readAllLines(Paths.get(ruta), StandardCharsets.UTF_8);
        MaquinaTuring mt = new MaquinaTuring();
        mt.alfabeto.add(MaquinaTuring.BLANCO); // el blanco siempre pertenece a la cinta

        // Las transiciones se guardan aparte porque pueden aparecer antes que
        // Estados/Alfabeto; se validan al final, cuando todo ya esta cargado.
        List<String[]> pendientes = new ArrayList<>();
        List<Integer> numerosLinea = new ArrayList<>();

        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i).trim();
            int nro = i + 1;
            if (linea.isEmpty() || linea.startsWith("#") || linea.startsWith("//")) {
                continue;
            }

            Matcher m;
            if ((m = P_ESTADOS.matcher(linea)).matches()) {
                for (String e : separar(m.group(1))) {
                    mt.estados.add(e);
                }
            } else if ((m = P_INICIAL.matcher(linea)).matches()) {
                List<String> v = separar(m.group(1));
                if (v.size() != 1) {
                    throw error(nro, "Inicial debe tener exactamente un estado.");
                }
                mt.inicial = v.get(0);
            } else if ((m = P_HA.matcher(linea)).matches()) {
                mt.aceptacion.addAll(separar(m.group(1)));
            } else if ((m = P_HE.matcher(linea)).matches()) {
                mt.rechazo.addAll(separar(m.group(1)));
            } else if ((m = P_ALFABETO.matcher(linea)).matches()) {
                for (String s : separar(m.group(1))) {
                    if (s.length() != 1) {
                        throw error(nro, "Cada simbolo del alfabeto debe ser un solo caracter: '" + s + "'.");
                    }
                    mt.alfabeto.add(s.charAt(0));
                }
            } else if ((m = P_MT.matcher(linea)).matches()) {
                pendientes.add(new String[] {
                    m.group(1), m.group(2), m.group(3), m.group(4), m.group(5).toUpperCase()
                });
                numerosLinea.add(nro);
            } else {
                throw error(nro, "Linea no reconocida: " + linea);
            }
        }

        // ---- Validaciones globales ----
        if (mt.estados.isEmpty()) {
            throw new IllegalArgumentException("Falta la linea Estados(...).");
        }
        if (mt.inicial == null) {
            throw new IllegalArgumentException("Falta la linea Inicial(...).");
        }
        if (!mt.estados.contains(mt.inicial)) {
            throw new IllegalArgumentException("El estado inicial '" + mt.inicial + "' no esta en Estados.");
        }
        if (mt.aceptacion.isEmpty()) {
            throw new IllegalArgumentException("Falta la linea Ha(...) (estado de aceptacion).");
        }
        for (String e : mt.aceptacion) {
            if (!mt.estados.contains(e)) {
                throw new IllegalArgumentException("El estado de aceptacion '" + e + "' no esta en Estados.");
            }
        }
        for (String e : mt.rechazo) {
            if (!mt.estados.contains(e)) {
                throw new IllegalArgumentException("El estado de rechazo '" + e + "' no esta en Estados.");
            }
            if (mt.aceptacion.contains(e)) {
                throw new IllegalArgumentException("El estado '" + e + "' no puede ser de aceptacion y de rechazo a la vez.");
            }
        }

        // ---- Validar y registrar cada transicion ----
        for (int k = 0; k < pendientes.size(); k++) {
            String[] t = pendientes.get(k);
            int nro = numerosLinea.get(k);
            String origen = t[0];
            char leido = t[1].charAt(0);
            String destino = t[2];
            char escrito = t[3].charAt(0);
            char mov = t[4].charAt(0);

            if (!mt.estados.contains(origen)) {
                throw error(nro, "Estado origen '" + origen + "' no definido en Estados.");
            }
            if (!mt.estados.contains(destino)) {
                throw error(nro, "Estado destino '" + destino + "' no definido en Estados.");
            }
            if (!mt.alfabeto.contains(leido)) {
                throw error(nro, "Simbolo leido '" + leido + "' no esta en el Alfabeto.");
            }
            if (!mt.alfabeto.contains(escrito)) {
                throw error(nro, "Simbolo escrito '" + escrito + "' no esta en el Alfabeto.");
            }
            if (mt.existeTransicion(origen, leido)) {
                throw error(nro, "Transicion repetida para (" + origen + ", " + leido + "): la MT debe ser determinista.");
            }
            mt.agregarTransicion(origen, leido, new Transicion(destino, escrito, mov));
        }
        return mt;
    }

    /** Divide "a, b, c" en la lista ["a","b","c"] quitando espacios. */
    private static List<String> separar(String texto) {
        List<String> res = new ArrayList<>();
        for (String parte : texto.split(",")) {
            String s = parte.trim();
            if (!s.isEmpty()) {
                res.add(s);
            }
        }
        return res;
    }

    private static IllegalArgumentException error(int linea, String mensaje) {
        return new IllegalArgumentException("Linea " + linea + ": " + mensaje);
    }
}
