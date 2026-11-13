import java.io.IOException;
import java.util.Scanner;

/**
 * Programa principal: simulador de Maquina de Turing por consola.
 *
 * Flujo:
 *   1. Nos pide la ruta del archivo con la definicion de la MT y la carga.
 *   2. Nos solicita una cadena para la cinta (valida los simbolos). Termina con ENTER.
 *   3. Simula y muestra ACEPTACION o NO ACEPTACION.
 *   4. Permite: nueva cinta, nueva maquina o salir.
 */
public class Main {

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        System.out.println("=== SIMULADOR DE MAQUINA DE TURING ===");

        MaquinaTuring mt = null;
        boolean salir = false;

        while (!salir) {
            // ---- Paso 1: cargar la maquina si hace falta ----
            if (mt == null) {
                mt = pedirMaquina(teclado);
                if (mt == null) {
                    break; // el usuario escribio "salir"
                }
                mostrarResumen(mt);
            }

            // ---- Paso 2: leeremos la cadena de la cinta ----
            String cadena = pedirCadena(teclado, mt);

            // ---- Paso 3: Procederemos a simular ----
            System.out.println("\n--- Simulacion ---");
            boolean acepta = mt.simular(cadena);
            System.out.println("\n*** RESULTADO: " + (acepta ? "ACEPTACION" : "NO ACEPTACION") + " ***");

            // ---- Paso 4: Nos muestra el menu final ----
            System.out.println("\nQue desea hacer?");
            System.out.println("  1) Ingresar una nueva cinta (misma maquina)");
            System.out.println("  2) Cargar una nueva definicion de maquina");
            System.out.println("  3) Salir");
            String op = leer(teclado, "Opcion: ");
            if (op.equals("2")) {
                mt = null;
            } else if (op.equals("3")) {
                salir = true;
            }
            // Acá cualquier otra cosa o "1": se repite con la misma maquina
        }
        System.out.println("Fin del programa.");
        teclado.close();
    }

    /** Nos pide rutas hasta que se cargue una maquina valida. Devuelve null si escribe la última opción que es, "salir". */
    private static MaquinaTuring pedirMaquina(Scanner teclado) {
        while (true) {
            String ruta = leer(teclado, "\nUbicacion del archivo de la MT (o 'salir'): ");
            if (ruta.equalsIgnoreCase("salir")) {
                return null;
            }
            // Quita comillas por si el usuario copio la ruta con "".
            ruta = ruta.replace("\"", "");
            try {
                return LectorDefinicion.cargar(ruta);
            } catch (IOException e) {
                System.out.println("ERROR: no se pudo leer el archivo (" + e.getMessage() + ").");
            } catch (IllegalArgumentException e) {
                System.out.println("ERROR en la definicion: " + e.getMessage());
            }
        }
    }

    /** Pide la cadena hasta que solo contenga simbolos validos. ENTER termina el ingreso. */
    private static String pedirCadena(Scanner teclado, MaquinaTuring mt) {
        while (true) {
            String cadena = leer(teclado, "\nIngrese la cadena para la cinta (ENTER = terminar; vacio = cadena vacia): ");
            String error = mt.validarEntrada(cadena);
            if (error == null) {
                return cadena;
            }
            System.out.println("ERROR: " + error + " Intente de nuevo.");
        }
    }

    private static void mostrarResumen(MaquinaTuring mt) {
        System.out.println("\nMaquina cargada correctamente:");
        System.out.println("  Estados  : " + mt.estados);
        System.out.println("  Inicial  : " + mt.inicial);
        System.out.println("  Ha       : " + mt.aceptacion);
        System.out.println("  He       : " + mt.rechazo);
        System.out.println("  Alfabeto : " + mt.alfabeto + "  ('" + MaquinaTuring.BLANCO + "' = blanco)");
        System.out.println("  Transiciones: " + mt.transiciones.size());
    }

    /** Muestra un mensaje y lee una linea completa (sin espacios sobrantes a los lados). */
    private static String leer(Scanner teclado, String mensaje) {
        System.out.print(mensaje);
        if (!teclado.hasNextLine()) {
            System.out.println();
            System.exit(0); // fin de la entrada (Ctrl+D / Ctrl+Z)
        }
        return teclado.nextLine().trim();
    }
}
