import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Definicion formal de una Maquina de Turing (MT) y su simulador.
 *
 * Una MT se define como (Q, Sigma/Gamma, q0, Ha, He, delta):
 *   Q      -> conjunto de estados            ({@link #estados})
 *   Gamma  -> alfabeto/simbolos de la cinta  ({@link #alfabeto})
 *   q0     -> estado inicial                 ({@link #inicial})
 *   Ha     -> estados de aceptacion          ({@link #aceptacion})
 *   He     -> estados de rechazo             ({@link #rechazo})
 *   delta  -> funcion de transicion          ({@link #transiciones})
 *
 * La cinta es "infinita" a ambos lados: se simula con un StringBuilder que
 * crece con simbolos BLANCO cada vez que la cabeza sale de los extremos.
 */
public class MaquinaTuring {

    /** Simbolo blanco: es lo que hay en las celdas de la cinta sin escribir. */
    public static final char BLANCO = '_';

    /** Maximo de pasos antes de asumir que la maquina cicla para siempre. */
    public static final int LIMITE_PASOS = 10000;

    public final Set<String> estados = new LinkedHashSet<>();
    public final Set<String> aceptacion = new LinkedHashSet<>();
    public final Set<String> rechazo = new LinkedHashSet<>();
    public final Set<Character> alfabeto = new LinkedHashSet<>();
    public String inicial;

    /** Clave = "estado|simbolo". Valor = la transicion correspondiente. */
    public final Map<String, Transicion> transiciones = new HashMap<>();

    /** Construye la clave del mapa de transiciones. */
    private static String clave(String estado, char simbolo) {
        return estado + "|" + simbolo;
    }

    /** Agrega una transicion (el lector ya verifico que sea valida). */
    public void agregarTransicion(String estado, char leido, Transicion t) {
        transiciones.put(clave(estado, leido), t);
    }

    /** Indica si ya existe una transicion para (estado, simbolo). */
    public boolean existeTransicion(String estado, char leido) {
        return transiciones.containsKey(clave(estado, leido));
    }

    /**
     * Revisa que la cadena del usuario solo use simbolos del alfabeto y que
     * no use el blanco. Devuelve null si es valida o un mensaje de error.
     */
    public String validarEntrada(String cadena) {
        for (int i = 0; i < cadena.length(); i++) {
            char c = cadena.charAt(i);
            if (c == BLANCO) {
                return "El simbolo blanco '" + BLANCO + "' no se puede ingresar (posicion " + (i + 1) + ").";
            }
            if (!alfabeto.contains(c)) {
                return "El simbolo '" + c + "' (posicion " + (i + 1) + ") no pertenece al alfabeto " + alfabeto + ".";
            }
        }
        return null;
    }

    /**
     * Simula la MT sobre la cadena dada, imprimiendo cada configuracion.
     *
     * @return true si termina en ACEPTACION, false si es NO ACEPTACION.
     */
    public boolean simular(String entrada) {
        // La cinta empieza con la cadena; si es vacia, con un solo blanco.
        StringBuilder cinta = new StringBuilder(entrada.isEmpty() ? String.valueOf(BLANCO) : entrada);
        int cabeza = 0;
        String estado = inicial;
        int paso = 0;
        String motivo;

        imprimirConfiguracion(paso, estado, cinta, cabeza);

        while (true) {
            // 1) Estados de parada.
            if (aceptacion.contains(estado)) {
                System.out.println("\n>>> La maquina llego al estado de aceptacion '" + estado + "'.");
                return true;
            }
            if (rechazo.contains(estado)) {
                motivo = "La maquina llego al estado de rechazo '" + estado + "'.";
                break;
            }
            // 2) Proteccion contra ciclos infinitos.
            if (paso >= LIMITE_PASOS) {
                motivo = "Se alcanzo el limite de " + LIMITE_PASOS + " pasos (posible ciclo infinito).";
                break;
            }

            // 3) Buscar la transicion para (estado, simbolo bajo la cabeza).
            char leido = cinta.charAt(cabeza);
            Transicion t = transiciones.get(clave(estado, leido));
            if (t == null) {
                motivo = "No existe transicion para (" + estado + ", " + leido + "): la maquina se detiene.";
                break;
            }

            // 4) Aplicar: escribir, mover y cambiar de estado.
            cinta.setCharAt(cabeza, t.simboloEscrito);
            if (t.movimiento == 'D') {
                cabeza++;
            } else if (t.movimiento == 'I') {
                cabeza--;
            }
            estado = t.estadoDestino;
            paso++;

            // 5) Extender la cinta "infinita" si la cabeza salio del limite.
            if (cabeza < 0) {
                cinta.insert(0, BLANCO);
                cabeza = 0;
            } else if (cabeza >= cinta.length()) {
                cinta.append(BLANCO);
            }

            imprimirConfiguracion(paso, estado, cinta, cabeza);
        }

        System.out.println("\n>>> " + motivo);
        return false;
    }

    /** Imprime una linea con el paso, el estado y la cinta (cabeza entre corchetes). */
    private void imprimirConfiguracion(int paso, String estado, StringBuilder cinta, int cabeza) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cinta.length(); i++) {
            if (i == cabeza) {
                sb.append('[').append(cinta.charAt(i)).append(']');
            } else {
                sb.append(' ').append(cinta.charAt(i)).append(' ');
            }
        }
        System.out.printf("Paso %-4d Estado: %-6s Cinta: %s%n", paso, estado, sb);
    }
}
