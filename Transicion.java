/**
 * Representa UNA regla de la funcion de transicion de la Maquina de Turing:
 *
 *   MT (estadoOrigen, simboloLeido) = (estadoDestino, simboloEscrito, movimiento)
 *
 * Es una clase de datos simple: solo guarda los valores de la regla.
 */
public class Transicion {

    /** Estado al que pasa la maquina. */
    public final String estadoDestino;

    /** Simbolo que se escribe en la celda actual (reemplaza al leido). */
    public final char simboloEscrito;

    /** Movimiento de la cabeza: 'D' derecha, 'I' izquierda, 'N' no se mueve. */
    public final char movimiento;

    public Transicion(String estadoDestino, char simboloEscrito, char movimiento) {
        this.estadoDestino = estadoDestino;
        this.simboloEscrito = simboloEscrito;
        this.movimiento = movimiento;
    }
}
