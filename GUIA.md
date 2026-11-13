# Guía de compilación y ejecución – Simulador de Máquina de Turing

## Requisitos
- JDK 8 o superior (probado con `javac` / `java`). Verifica con: `java -version` y `javac -version`
- VSCode con la extensión **Extension Pack for Java** (opcional pero recomendada)

## Archivos del proyecto
| Archivo | Descripción |
|---|---|
| `Main.java` | Programa principal (menú, lectura de cinta, resultado) |
| `MaquinaTuring.java` | Definición de la MT y motor de simulación |
| `LectorDefinicion.java` | Lee y valida el archivo de definición |
| `Transicion.java` | Una regla de la función de transición |
| `anbn.txt` | Ejemplo: MT para L = { aⁿbⁿ } |

## Opción A: desde VSCode
1. `Archivo > Abrir carpeta` y elige la carpeta del proyecto.
2. Abre `Main.java`.
3. Pulsa **Run** (▶) sobre `public static void main`.
4. En la terminal integrada escribe la ruta del archivo, por ejemplo `anbn.txt`
   (o la ruta completa, como `C:\proyecto\anbn.txt`).

## Opción B: desde la terminal
```
javac *.java
java Main
```

## Uso del programa
1. Escribe la ubicación del archivo de la MT (ej. `anbn.txt`).
2. Escribe la cadena para la cinta y presiona **ENTER**.
   Si hay símbolos que no están en el alfabeto, el programa lo avisa y vuelve a pedir la cadena.
3. Se muestra la simulación paso a paso (la cabeza va entre `[ ]`) y el resultado:
   **ACEPTACION** o **NO ACEPTACION**.
4. Menú final: nueva cinta, nueva máquina o salir.

## Formato del archivo de la MT
```
Estados( q0, q1, qa, qr )
Inicial( q0 )
Ha( qa )          # estado de aceptación
He( qr )          # estado de rechazo
Alfabeto( a, b, X, _ )
MT (q0, a) = (q1, X, D)    # D = derecha, I = izquierda, N = no se mueve
```
- El símbolo `_` es el **blanco** (se agrega solo al alfabeto, pero puedes escribirlo).
- Si no existe transición para (estado, símbolo), la máquina se detiene y **no acepta**.
- Las líneas que empiezan con `#` o `//` son comentarios.
