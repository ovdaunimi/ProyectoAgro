public class AnalizadorAgro {
    public static final int Meses = 12;
    public static final int ventana = 3;
    public static final int num_cultivos = 3;
    public static final int Año_base = 2026;
    public static final int Num_años = 3;

    // Un DatosAgro por año (2026, 2027, 2028); "datos" apunta al año seleccionado en la GUI
    private final DatosAgro[] datosPorAño = new DatosAgro[Num_años];
    private DatosAgro datos;
    private int añoActual = Año_base;
    private String mensaje = "";

    private ListaAtipico listaAtipicos = new ListaAtipico();
    private final ListaEscenarios listaEscenarios = new ListaEscenarios();
    private final PilaEscenarios pilaEscenarios = new PilaEscenarios();
    private final CicloAnual cicloAnual = new CicloAnual();
    private String claveEscenarios = "";

    public AnalizadorAgro() {
        for (int i = 0; i < Num_años; i++) {
            datosPorAño[i] = new DatosAgro();
        }
        datos = datosPorAño[0];
        for (int i = 0; i < Meses; i++) {
            cicloAnual.agregarMes(DatosAgro.MESES[i]);
        }
    }
    // ---------- Datos para llenar los combos de la GUI ----------
    public static String[] getCultivos() {
        return DatosAgro.CULTIVOS;
    }
    public static String[] getMeses() {
        return DatosAgro.MESES;
    }
    public String getMensaje() {
        return mensaje;
    }
    public void seleccionarAño(int año) {
        int idx = año - Año_base;
        if (idx >= 0 && idx < Num_años) {
            datos = datosPorAño[idx];
            añoActual = año;
        }
    }
    // ---------- Registro (un solo cultivo por llamada) ----------

    public boolean existeRegistro(int mes, String cultivo) {
        int c = datos.indiceCultivo(cultivo);
        return c >= 0 && mes >= 0 && mes < Meses && datos.registrado[c][mes];
    }
    public boolean registrarDatos(int mes, String cultivo, double precio, double lluvia) {
        String error = datos.registrarDatos(mes, cultivo, precio, lluvia);
        if (error != null) {
            mensaje = error;
            return false;
        }
        mensaje = "Datos de " + cultivo + " en " + DatosAgro.MESES[mes] + " " + añoActual
                + " registrados correctamente.";
        return true;
    }

    public boolean actualizarDatos(int mes, String cultivo, double precio, double lluvia) {
        String error = datos.actualizarDatos(mes, cultivo, precio, lluvia);
        if (error != null) {
            mensaje = error;
            return false;
        }
        mensaje = "Datos de " + cultivo + " en " + DatosAgro.MESES[mes] + " " + añoActual
                + " actualizados correctamente.";
        return true;
    }
    // ---------- media móvil con cola circular ----------

    public ResultadoAnalisis analizarMes(int mes, String cultivo) {
        int c = datos.indiceCultivo(cultivo);
        if (c < 0 || mes < 0 || mes >= Meses) {
            mensaje = "Selecciona un cultivo y un mes validos.";
            return null;
        }
        if (!datos.registrado[c][mes]) {
            mensaje = "No hay datos de " + DatosAgro.CULTIVOS[c] + " en " + DatosAgro.MESES[mes] + " "
                    + añoActual + ".\nREGISTRA DATOS primero.";
            return null;
        }
        // Ventana: hasta 'ventana' meses consecutivos que terminan en el mes analizado
        int inicio = mes;
        int cuenta = 1;
        while (cuenta < ventana && inicio - 1 >= 0 && datos.registrado[c][inicio - 1]) {
            inicio--;
            cuenta++;
        }
        ColaCircular cola = new ColaCircular();
        for (int i = inicio; i <= mes; i++) {
            cola.agregarPrecio(datos.precios[c][i]);
        }
        double precio = datos.precios[c][mes];
        double media = cola.calcularMedia();
        double variacion = (precio - media) / media * 100;

        String estado;
        if (variacion > 10) {
            estado = "Alto";
        } else if (variacion < -10) {
            estado = "Bajo";
        } else {
            estado = "Normal";
        }
        // Tendencia: comparación con el mes anterior (si está registrado)
        String tendencia;
        if (mes > 0 && datos.registrado[c][mes - 1]) {
            double previo = datos.precios[c][mes - 1];
            double cambio = (precio - previo) / previo * 100;
            if (cambio > 2) {
                tendencia = "Alza";
            } else if (cambio < -2) {
                tendencia = "Baja";
            } else {
                tendencia = "Estable";
            }
        } else {
            tendencia = "Sin mes previo";
        }

        // Cada análisis genera los escenarios del cultivo
        generarEscenarios(c);

        ResultadoAnalisis r = new ResultadoAnalisis();
        r.cultivo = DatosAgro.CULTIVOS[c];
        r.mes = DatosAgro.MESES[mes];
        r.lluvia = String.format("%.0f mm", datos.lluvia[c][mes]);
        r.tendencia = tendencia;
        r.estado = estado;
        r.variacion = String.format("%+.1f%%", variacion);
        r.media = String.format("$%.2f", media);
        return r;
    }
    // ---------- HISTORIAL (vectores) ----------

    public String historial(String cultivo) {
        int c = datos.indiceCultivo(cultivo);
        if (c < 0) {
            return "Cultivo no valido.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("HISTORIAL: ").append(DatosAgro.CULTIVOS[c]).append(" - ").append(añoActual).append("\n\n");
        sb.append(String.format("%-12s %-14s %-12s%n", "Mes", "Precio ($/kg)", "Lluvia (mm)"));

        int n = 0;
        for (int i = 0; i < Meses; i++) {
            if (!datos.registrado[c][i]) continue;
            sb.append(String.format("%-12s %-14.2f %-12.1f%n", DatosAgro.MESES[i], datos.precios[c][i], datos.lluvia[c][i]));
            n++;
        }
        if (n == 0) {
            return "Todavía no hay datos registrados de " + DatosAgro.CULTIVOS[c] + " en " + añoActual + ".";
        }
        sb.append("\nTotal de meses registrados: ").append(n);
        return sb.toString();
    }
    // ---------- COMPORTAMIENTO (matriz 2D + tendencia estacional) ----------

    public String comportamiento(String cultivo) {
        int sel = datos.indiceCultivo(cultivo);
        StringBuilder sb = new StringBuilder();
        sb.append("MATRIZ DE PRECIOS ($/kg) - ").append(añoActual).append("\n\n");
        sb.append(String.format("%-12s", "Mes"));
        for (int c = 0; c < num_cultivos; c++) {
            sb.append(String.format("%-12s", DatosAgro.CULTIVOS[c]));
        }
        sb.append("\n");

        for (int i = 0; i < Meses; i++) {
            sb.append(String.format("%-12s", DatosAgro.MESES[i]));
            for (int c = 0; c < num_cultivos; c++) {
                if (datos.registrado[c][i]) {
                    sb.append(String.format("%-12.2f", datos.precios[c][i]));
                } else {
                    sb.append(String.format("%-12s", "-"));
                }
            }
            sb.append("\n");
        }

        if (sel >= 0 && datos.contarMeses(sel) > 0) {
            int mayor = -1;
            int menor = -1;
            for (int i = 0; i < Meses; i++) {
                if (!datos.registrado[sel][i]) continue;
                if (mayor == -1 || datos.precios[sel][i] > datos.precios[sel][mayor]) mayor = i;
                if (menor == -1 || datos.precios[sel][i] < datos.precios[sel][menor]) menor = i;
            }
            sb.append("\nTendencia estacional de ").append(DatosAgro.CULTIVOS[sel]).append(":\n");
            sb.append(String.format("Mes más caro:   %s ($%.2f)%n", DatosAgro.MESES[mayor], datos.precios[sel][mayor]));
            sb.append(String.format("Mes más barato: %s ($%.2f)%n", DatosAgro.MESES[menor], datos.precios[sel][menor]));
        }
        return sb.toString();
    }

    // ---------- ATÍPICOS (lista simple) ----------

    public String atipicos(String cultivo) {
        int c = datos.indiceCultivo(cultivo);
        if (c < 0) {
            return "Cultivo no válido.";
        }
        if (datos.contarMeses(c) < 3) {
            return "Se necesitan al menos 3 meses registrados de " + DatosAgro.CULTIVOS[c]
                    + " para detectar precios atípicos.";
        }
        listaAtipicos = new ListaAtipico();

        for (int i = 0; i < Meses; i++) {
            if (!datos.registrado[c][i]) continue;

            double sumaOtros = 0;
            int cantidadOtros = 0;
            for (int j = 0; j < Meses; j++) {
                if (j != i && datos.registrado[c][j]) {
                    sumaOtros += datos.precios[c][j];
                    cantidadOtros++;
                }
            }
            double promedioOtros = sumaOtros / cantidadOtros;

            if (Math.abs(datos.precios[c][i] - promedioOtros) > promedioOtros * 0.50) {
                listaAtipicos.agregarAtipico(DatosAgro.MESES[i], DatosAgro.CULTIVOS[c], datos.precios[c][i]);
            }
        }
        return "PRECIOS ATÍPICOS - " + añoActual + "\n\n" + listaAtipicos.comoTexto();
    }

    // ---------- ESCENARIOS (lista doble + pila) ----------

    private void generarEscenarios(int c) {
        int n = datos.contarMeses(c);
        double suma = 0;
        for (int i = 0; i < Meses; i++) {
            if (datos.registrado[c][i]) suma += datos.precios[c][i];
        }
        double promedio = suma / n;

        listaEscenarios.limpiar();
        pilaEscenarios.limpiar();

        guardarEscenario("Bajo", DatosAgro.CULTIVOS[c], promedio * 0.90);
        guardarEscenario("Normal", DatosAgro.CULTIVOS[c], promedio);
        guardarEscenario("Alto", DatosAgro.CULTIVOS[c], promedio * 1.10);

        listaEscenarios.seleccionarPorNombre("Normal");
        claveEscenarios = DatosAgro.CULTIVOS[c] + "|" + añoActual;
    }

    private void guardarEscenario(String nombre, String cultivo, double precio) {
        EscenarioPrecio escenario = new EscenarioPrecio(nombre, cultivo, precio);
        listaEscenarios.agregarEscenario(escenario);
        pilaEscenarios.apilar(escenario);
    }

    // Si no hay escenarios del cultivo/año seleccionado, los genera
    private boolean asegurarEscenarios(String cultivo) {
        int c = datos.indiceCultivo(cultivo);
        if (c < 0) {
            mensaje = "Cultivo no valido.";
            return false;
        }
        String clave = DatosAgro.CULTIVOS[c] + "|" + añoActual;
        if (listaEscenarios.cabeza == null || !clave.equals(claveEscenarios)) {
            if (datos.contarMeses(c) == 0) {
                mensaje = "No hay datos de " + DatosAgro.CULTIVOS[c] + " en " + añoActual
                        + " para generar escenarios.\nUsa REGISTRAR DATOS primero.";
                return false;
            }
            generarEscenarios(c);
        }
        return true;
    }

    private String describirActual() {
        return "Escenario " + listaEscenarios.posicionActual() + " de " + listaEscenarios.total()
                + " (" + añoActual + ")\n\n" + listaEscenarios.verActual().toString();
    }

    public String escenarioActual(String cultivo) {
        if (!asegurarEscenarios(cultivo)) return mensaje;
        return describirActual();
    }

    public String escenarioAnterior(String cultivo) {
        if (!asegurarEscenarios(cultivo)) return mensaje;
        if (listaEscenarios.retroceder() == null) {
            return "Ya estás en el primer escenario.\n\n" + describirActual();
        }
        return describirActual();
    }

    public String escenarioSiguiente(String cultivo) {
        if (!asegurarEscenarios(cultivo)) return mensaje;
        if (listaEscenarios.avanzar() == null) {
            return "Ya estás en el último escenario.\n\n" + describirActual();
        }
        return describirActual();
    }

    // Contenido de la pila (el tope es el último escenario generado)
    public String estadosPila() {
        return pilaEscenarios.comoTexto();
    }

    // Backtracking: retira el escenario del tope de la pila y de la lista doble
    public String deshacerEscenario() {
        EscenarioPrecio e = pilaEscenarios.desapilar();
        if (e == null) {
            return "No hay escenarios para deshacer.";
        }
        listaEscenarios.eliminarUltimo();
        return "Se deshizo el escenario: " + e.nombre + " (" + e.cultivo + ").";
    }

    // Borra escenarios y atípicos para empezar un análisis nuevo (los datos registrados se conservan)
    public void nuevoAnalisis() {
        listaEscenarios.limpiar();
        pilaEscenarios.limpiar();
        listaAtipicos = new ListaAtipico();
        claveEscenarios = "";
    }

    // ---------- CICLO ANUAL (lista circular) ----------

    public String cicloAnual(String cultivo, int mesInicio) {
        int c = datos.indiceCultivo(cultivo);
        if (c < 0 || mesInicio < 0 || mesInicio >= Meses) {
            return "Selecciona un cultivo y un mes validos.";
        }
        // El ciclo arranca 2 meses antes del seleccionado, para ver el contexto previo
        int inicioCiclo = (mesInicio - 2 + Meses) % Meses;
        String[] orden = cicloAnual.recorrerDesde(DatosAgro.MESES[inicioCiclo]);

        // Si al retroceder se cruza Enero, el primer mes pertenece al año anterior
        int año = (mesInicio - 2 < 0) ? añoActual - 1 : añoActual;
        int añoInicial = año;

        StringBuilder sb = new StringBuilder();
        sb.append("CICLO ANUAL de ").append(DatosAgro.CULTIVOS[c]).append(" alrededor de ")
                .append(DatosAgro.MESES[mesInicio]).append(" ").append(añoActual).append("\n\n");

        for (int k = 0; k < Meses; k++) {
            String nombreMes = orden[k];
            int i = DatosAgro.indiceMes(nombreMes);

            // Al pasar de Diciembre a Enero se suma un año
            if (k > 0 && i == 0) {
                año++;
            }

            String marca = (i == mesInicio && año == añoActual) ? "  ◄" : "";
            String etiqueta = nombreMes + " " + año;
            int idxAño = año - Año_base;

            if (idxAño < 0 || idxAño >= Num_años) {
                sb.append(String.format("%-17s fuera del rango (%d-%d)%s%n",
                        etiqueta, Año_base, Año_base + Num_años - 1, marca));
            } else if (datosPorAño[idxAño].registrado[c][i]) {
                sb.append(String.format("%-17s $%.2f | %.1f mm%s%n", etiqueta,
                        datosPorAño[idxAño].precios[c][i], datosPorAño[idxAño].lluvia[c][i], marca));
            } else {
                sb.append(String.format("%-17s sin datos%s%n", etiqueta, marca));
            }
        }
        sb.append("\nEl ciclo continúa en ").append(orden[0]).append(" ").append(añoInicial + 1).append(".");
        return sb.toString();
    }
}
// ================= CLASES =================

// Resultado que se muestra en el panel RESULTADOS DEL ANÁLISIS
class ResultadoAnalisis {
    String cultivo;
    String mes;
    String lluvia;
    String tendencia;
    String estado;
    String variacion;
    String media;
}

// Datos agrícolas de un año: matrices [cultivo][mes]
class DatosAgro {
    static final String[] CULTIVOS = {"Tomate", "Maíz", "Zanahoria"};
    static final String[] MESES = {"Enero", "Febrero", "Marzo", "Abril",
            "Mayo", "Junio", "Julio", "Agosto",
            "Septiembre", "Octubre", "Noviembre", "Diciembre"};

    double[][] precios = new double[AnalizadorAgro.num_cultivos][AnalizadorAgro.Meses];
    double[][] lluvia = new double[AnalizadorAgro.num_cultivos][AnalizadorAgro.Meses];
    boolean[][] registrado = new boolean[AnalizadorAgro.num_cultivos][AnalizadorAgro.Meses];

    // Devuelve el índice del cultivo o -1 si no existe (acepta "Maiz" sin tilde)
    public int indiceCultivo(String cultivo) {
        if (cultivo == null) return -1;
        for (int i = 0; i < CULTIVOS.length; i++) {
            if (CULTIVOS[i].equalsIgnoreCase(cultivo)) return i;
        }
        if (cultivo.equalsIgnoreCase("Maiz")) return 1;
        return -1;
    }

    public static int indiceMes(String mes) {
        for (int i = 0; i < MESES.length; i++) {
            if (MESES[i].equals(mes)) return i;
        }
        return -1;
    }

    public int contarMeses(int c) {
        int n = 0;
        for (int i = 0; i < AnalizadorAgro.Meses; i++) {
            if (registrado[c][i]) n++;
        }
        return n;
    }

    // Devuelve un mensaje de error o null si todo es válido
    private String validar(int mes, int c, double precio, double lluviaMm) {
        if (mes < 0 || mes >= AnalizadorAgro.Meses) return "El mes no es válido.";
        if (c < 0) return "El cultivo no es válido.";
        if (!Double.isFinite(precio) || !Double.isFinite(lluviaMm)) return "Los datos deben ser números validos.";
        if (precio <= 0) return "El precio debe ser mayor que 0.";
        if (lluviaMm < 0) return "La lluvia no puede ser negativa.";
        return null;
    }

    // Retorna null si registró; si no, el mensaje de error
    public String registrarDatos(int mes, String cultivo, double precio, double lluviaMm) {
        int c = indiceCultivo(cultivo);
        String error = validar(mes, c, precio, lluviaMm);
        if (error != null) return error;

        if (registrado[c][mes]) {
            return CULTIVOS[c] + " ya tiene datos en " + MESES[mes] + ". Actualiza los datos para modificarlos.";
        }
        precios[c][mes] = precio;
        lluvia[c][mes] = lluviaMm;
        registrado[c][mes] = true;
        return null;
    }

    // Retorna null si actualizó; si no, el mensaje de error
    public String actualizarDatos(int mes, String cultivo, double precio, double lluviaMm) {
        int c = indiceCultivo(cultivo);
        String error = validar(mes, c, precio, lluviaMm);
        if (error != null) return error;

        if (!registrado[c][mes]) {
            return "No puedes actualizar " + MESES[mes] + " de " + CULTIVOS[c] + " porque todavía no tiene datos.";
        }
        precios[c][mes] = precio;
        lluvia[c][mes] = lluviaMm;
        return null;
    }
}

// Cola circular (ventana de media móvil)
class ColaCircular {
    double[] precios = new double[AnalizadorAgro.ventana];
    int frente = 0;
    int fin = -1;
    int tamañoActual = 0;

    public void insertar(double precio) {
        if (tamañoActual == precios.length) return;
        fin = (fin + 1) % precios.length;
        precios[fin] = precio;
        tamañoActual++;
    }

    public double eliminar() {
        if (tamañoActual == 0) return -1;
        double eliminado = precios[frente];
        frente = (frente + 1) % precios.length;
        tamañoActual--;
        return eliminado;
    }

    public double calcularMedia() {
        if (tamañoActual == 0) return 0;
        double suma = 0;
        for (int i = 0; i < tamañoActual; i++) {
            suma += precios[(frente + i) % precios.length];
        }
        return suma / tamañoActual;
    }

    // Si la ventana está llena, saca el precio más viejo antes de insertar
    public void agregarPrecio(double precio) {
        if (tamañoActual == precios.length) {
            eliminar();
        }
        insertar(precio);
    }
}

// Escenario de precio de un solo cultivo
class EscenarioPrecio {
    String nombre;
    String cultivo;
    double precio;

    public EscenarioPrecio(String nombre, String cultivo, double precio) {
        this.nombre = nombre;
        this.cultivo = cultivo;
        this.precio = precio;
    }

    @Override
    public String toString() {
        return "Escenario: " + nombre + "\nCultivo: " + cultivo
                + String.format("%nPrecio estimado: $%.2f/kg", precio);
    }
}

// Lista doblemente enlazada de escenarios, con puntero al escenario actual
class ListaEscenarios {
    NodoDoble cabeza;
    NodoDoble cola;
    NodoDoble actual;

    public void agregarEscenario(EscenarioPrecio escenario) {
        NodoDoble nuevo = new NodoDoble(escenario);
        if (cabeza == null) {
            cabeza = nuevo;
            cola = nuevo;
            actual = nuevo;
        } else {
            cola.siguiente = nuevo;
            nuevo.anterior = cola;
            cola = nuevo;
        }
    }

    public EscenarioPrecio verActual() {
        return actual == null ? null : actual.escenario;
    }

    // Devuelve null si no hay anterior
    public EscenarioPrecio retroceder() {
        if (actual == null || actual.anterior == null) return null;
        actual = actual.anterior;
        return actual.escenario;
    }

    // Devuelve null si no hay siguiente
    public EscenarioPrecio avanzar() {
        if (actual == null || actual.siguiente == null) return null;
        actual = actual.siguiente;
        return actual.escenario;
    }

    public void seleccionarPorNombre(String nombre) {
        NodoDoble n = cabeza;
        while (n != null) {
            if (n.escenario.nombre.equals(nombre)) {
                actual = n;
                return;
            }
            n = n.siguiente;
        }
    }

    public int posicionActual() {
        int pos = 1;
        NodoDoble n = cabeza;
        while (n != null && n != actual) {
            pos++;
            n = n.siguiente;
        }
        return pos;
    }

    public int total() {
        int t = 0;
        NodoDoble n = cabeza;
        while (n != null) {
            t++;
            n = n.siguiente;
        }
        return t;
    }

    public EscenarioPrecio eliminarUltimo() {
        if (cola == null) return null;
        NodoDoble borrado = cola;
        cola = cola.anterior;
        if (cola == null) {
            cabeza = null;
        } else {
            cola.siguiente = null;
        }
        if (actual == borrado) actual = cola;
        return borrado.escenario;
    }

    public void limpiar() {
        cabeza = null;
        cola = null;
        actual = null;
    }
}

// Pila de escenarios (backtracking)
class PilaEscenarios {
    EscenarioPrecio[] pila = new EscenarioPrecio[100];
    int cima = -1;

    public void apilar(EscenarioPrecio escenario) {
        if (cima == pila.length - 1) return;
        pila[++cima] = escenario;
    }

    public EscenarioPrecio desapilar() {
        if (cima == -1) return null;
        EscenarioPrecio eliminado = pila[cima];
        pila[cima] = null;
        cima--;
        return eliminado;
    }

    public EscenarioPrecio consultarCima() {
        return cima == -1 ? null : pila[cima];
    }

    public String comoTexto() {
        if (cima == -1) {
            return "No hay escenarios guardados.\nPulsa ANALIZAR para generarlos.";
        }
        StringBuilder sb = new StringBuilder("PILA DE ESCENARIOS (tope arriba)\n\n");
        for (int i = cima; i >= 0; i--) {
            sb.append(String.format("%-8s %-10s $%.2f/kg%n", pila[i].nombre, pila[i].cultivo, pila[i].precio));
        }
        return sb.toString();
    }

    public void limpiar() {
        for (int i = 0; i <= cima; i++) pila[i] = null;
        cima = -1;
    }
}

class NodoDoble {
    EscenarioPrecio escenario;
    NodoDoble siguiente;
    NodoDoble anterior;

    public NodoDoble(EscenarioPrecio escenario) {
        this.escenario = escenario;
    }
}

class NodoSimple {
    String mes;
    String cultivo;
    double precio;
    NodoSimple siguiente;

    public NodoSimple(String mes, String cultivo, double precio) {
        this.mes = mes;
        this.cultivo = cultivo;
        this.precio = precio;
    }
}

class NodoCircular {
    String mes;
    NodoCircular siguiente;

    public NodoCircular(String mes) {
        this.mes = mes;
    }
}

// Lista circular: ciclo anual
class CicloAnual {
    NodoCircular cabeza;

    public void agregarMes(String mes) {
        NodoCircular nuevo = new NodoCircular(mes);
        if (cabeza == null) {
            cabeza = nuevo;
            nuevo.siguiente = cabeza;
        } else {
            NodoCircular actual = cabeza;
            while (actual.siguiente != cabeza) {
                actual = actual.siguiente;
            }
            actual.siguiente = nuevo;
            nuevo.siguiente = cabeza;
        }
    }

    // Recorre los 12 meses empezando en el mes indicado y dando la vuelta al ciclo
    public String[] recorrerDesde(String mesInicio) {
        String[] resultado = new String[AnalizadorAgro.Meses];
        if (cabeza == null) return resultado;

        NodoCircular actual = cabeza;
        while (!actual.mes.equals(mesInicio)) {
            actual = actual.siguiente;
        }
        for (int i = 0; i < AnalizadorAgro.Meses; i++) {
            resultado[i] = actual.mes;
            actual = actual.siguiente;
        }
        return resultado;
    }
}

// Lista simple: atípicos
class ListaAtipico {
    NodoSimple cabeza;

    public void agregarAtipico(String mes, String cultivo, double precio) {
        NodoSimple nuevo = new NodoSimple(mes, cultivo, precio);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            NodoSimple actual = cabeza;
            while (actual.siguiente != null) {
                actual = actual.siguiente;
            }
            actual.siguiente = nuevo;
        }
    }

    public String comoTexto() {
        if (cabeza == null) {
            return "No hay precios atipicos.";
        }
        StringBuilder sb = new StringBuilder();
        NodoSimple actual = cabeza;
        while (actual != null) {
            sb.append(String.format("%-12s %-10s $%.2f/kg%n", actual.mes, actual.cultivo, actual.precio));
            actual = actual.siguiente;
        }
        return sb.toString();
    }
}