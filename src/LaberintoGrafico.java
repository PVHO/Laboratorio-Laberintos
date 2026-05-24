/**
 * Nombre del archivo: Laboratorio Laberintos.java
 * Descripción: Implementación de un resolvedor de laberintos mediante el algoritmo de Backtracking.
 * Permite configuraciones dinámicas de tamaño, prioridad de direcciones y modo de juego.
 *
 * @author Pablo Vinicio Hernández ;Jaime Velastegui; Mattias Revelo; Felipe Corral
 * @version 1.2
 * @date 2026-05-24
 * * Historial de cambios:
 * - 1.0: Versión inicial con generación base.
 * - 1.1: Añadido menú de prioridades y lógica de laberintos imposibles.
 * - 1.2: Implementada lógica heurística y persistencia de mapa.
 */

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class LaberintoGrafico extends JPanel {

    // Variables de estadísticas
    private int llamadas = 0;
    private int retrocesos = 0;
    private int profundidadActual = 0;
    private long inicio;
    private long fin;

    // Estructuras para conservar el mismo mapa
    private int[][] laberintoOriginal;
    private int[][] laberinto;

    // Matriz para la prioridad de movimientos del resolvedor
    private int[][] ordenResolver;
    private String prioridadActualTexto;

    // Tamaño de cada celda
    private int TAM;

    // Componentes de interfaz para actualización en tiempo real
    private JLabel lblNodos;
    private JLabel lblProfundidad;
    private JLabel lblRetrocesos;
    private JLabel lblTiempo;

    private JFrame ventanaPrincipal;
    private String tipoLaberintoGlobal;

    // CONSTRUCTOR
    public LaberintoGrafico(int tamaño, String prioridad, String tipoSolucion) {
        this.prioridadActualTexto = prioridad;
        this.tipoLaberintoGlobal = tipoSolucion;
        configurarPrioridad(prioridad);
        generarEstructuraBase(tamaño, tipoSolucion);
        reseteoValoresLaberinto();
    }

    public void setVentanaPrincipal(JFrame ventana) {
        this.ventanaPrincipal = ventana;
    }

    public void setLabels(JLabel lblNodos, JLabel lblProfundidad, JLabel lblRetrocesos, JLabel lblTiempo) {
        this.lblNodos = lblNodos;
        this.lblProfundidad = lblProfundidad;
        this.lblRetrocesos = lblRetrocesos;
        this.lblTiempo = lblTiempo;
    }

    // MÉTODO MAIN
    public static void main(String[] args) {
        int tamaño = 0;
        while (true) {
            String entrada = JOptionPane.showInputDialog(null, "Ingrese tamaño del laberinto:", "Configuración", JOptionPane.QUESTION_MESSAGE);
            if (entrada == null) {
                JOptionPane.showMessageDialog(null, "Debe ingresar un tamaño para continuar.", "Error", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            try {
                tamaño = Integer.parseInt(entrada);
                if (tamaño <= 3) {
                    JOptionPane.showMessageDialog(null, "El tamaño debe ser mayor a 3.", "Error", JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Por favor, ingrese un número entero válido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        String[] opcionesPrioridad = {
                "Arriba, Derecha, Abajo, Izquierda",
                "Derecha, Abajo, Izquierda, Arriba",
                "Abajo, Izquierda, Arriba, Derecha",
                "Izquierda, Arriba, Derecha, Abajo"
        };

        String prioridad = null;
        while (prioridad == null) {
            prioridad = (String) JOptionPane.showInputDialog(null, "Seleccione la prioridad inicial:", "Prioridad de Backtracking", JOptionPane.QUESTION_MESSAGE, null, opcionesPrioridad, opcionesPrioridad[0]);
            if (prioridad == null) {
                JOptionPane.showMessageDialog(null, "Debe seleccionar una prioridad obligatoriamente.", "Selección Requerida", JOptionPane.WARNING_MESSAGE);
            }
        }

        String[] opcionesTipo = {"Con Solución", "Imposible"};
        String tipoSolucion = null;
        while (tipoSolucion == null) {
            tipoSolucion = (String) JOptionPane.showInputDialog(null, "Seleccione el tipo de laberinto:", "Modo de Juego", JOptionPane.QUESTION_MESSAGE, null, opcionesTipo, opcionesTipo[0]);
            if (tipoSolucion == null) {
                JOptionPane.showMessageDialog(null, "Debe seleccionar un modo obligatoriamente.", "Selección Requerida", JOptionPane.WARNING_MESSAGE);
            }
        }

        JFrame ventana = new JFrame("Backtracking - Mismo Laberinto Fijo");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLayout(new BorderLayout());

        LaberintoGrafico panelLaberinto = new LaberintoGrafico(tamaño, prioridad, tipoSolucion);
        panelLaberinto.setVentanaPrincipal(ventana);

        JPanel panelStats = new JPanel(new GridLayout(1, 4, 10, 0));
        panelStats.setBackground(new Color(230, 230, 230));
        panelStats.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel lblNodos = new JLabel("Nodos: 0");
        JLabel lblProfundidad = new JLabel("Profundidad: 0");
        JLabel lblRetrocesos = new JLabel("Retrocesos: 0");
        JLabel lblTiempo = new JLabel("Tiempo: 0.0 ms");

        Font formatoFuente = new Font("Arial", Font.BOLD, 12);
        lblNodos.setFont(formatoFuente);
        lblProfundidad.setFont(formatoFuente);
        lblRetrocesos.setFont(formatoFuente);
        lblTiempo.setFont(formatoFuente);

        panelStats.add(lblNodos);
        panelStats.add(lblProfundidad);
        panelStats.add(lblRetrocesos);
        panelStats.add(lblTiempo);

        panelLaberinto.setLabels(lblNodos, lblProfundidad, lblRetrocesos, lblTiempo);

        ventana.add(panelStats, BorderLayout.NORTH);
        ventana.add(panelLaberinto, BorderLayout.CENTER);
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);

        // Disparar ciclo del hilo ejecutor
        panelLaberinto.iniciarHilosResolucion();
    }

    // DISPARAR EL PROCESO DE BÚSQUEDA
    public void iniciarHilosResolucion() {
        new Thread(() -> {
            reseteoValoresLaberinto();
            inicio = System.nanoTime();
            boolean solucion = resolver(0, 0);
            fin = System.nanoTime();
            actualizarEstadisticas();

            mostrarMenuInteractivoFinal(solucion);
        }).start();
    }

    // RESTAURAR EL MAPA AL ESTADO ORIGINAL SIN ALTERAR SUS MUROS
    public void reseteoValoresLaberinto() {
        llamadas = 0;
        retrocesos = 0;
        profundidadActual = 0;

        int n = laberintoOriginal.length;
        laberinto = new int[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(laberintoOriginal[i], 0, laberinto[i], 0, n);
        }
        repaint();
    }

    // MENU CON BOTONES E INTELIGENCIA HEURÍSTICA
    private void mostrarMenuInteractivoFinal(boolean solucion) {
        double tiempoTotalMs = (fin - inicio) / 1_000_000.0;

        // --- CÁLCULO LOGICO DE LA SUGERENCIA ---
        String sugerencia;
        if (!solucion) {
            sugerencia = "Ninguna (El mapa está sellado herméticamente)";
        } else {
            // Evaluamos geométricamente cuál dirección reduce los callejones según el sesgo
            int caminoEfectivo = llamadas - retrocesos;
            double eficiencia = (double) caminoEfectivo / llamadas;

            if (eficiencia > 0.6 && (prioridadActualTexto.startsWith("Derecha") || prioridadActualTexto.startsWith("Abajo"))) {
                sugerencia = "Mantener '" + prioridadActualTexto + "' (Es óptima para este cuadrante)";
            } else {
                sugerencia = "Cambiar a 'Derecha, Abajo, Izquierda, Arriba' (Alineación natural hacia el vector destino)";
            }
        }

        // Crear cuerpo del panel de JDialog personalizado
        JPanel panelDialogo = new JPanel(new GridLayout(6, 1, 5, 5));
        panelDialogo.add(new JLabel("--- REPORTES DE EJECUCIÓN ---"));
        panelDialogo.add(new JLabel("¿Solución Encontrada?: " + (solucion ? "SÍ" : "NO")));
        panelDialogo.add(new JLabel("Nodos Totales: " + llamadas + " | Retrocesos: " + retrocesos));
        panelDialogo.add(new JLabel(String.format("Tiempo Empleado: %.2f ms", tiempoTotalMs)));
        panelDialogo.add(new JLabel("💡 SUGERENCIA HEURÍSTICA:"));

        JLabel lblSugText = new JLabel(sugerencia);
        lblSugText.setForeground(new Color(0, 128, 64));
        lblSugText.setFont(new Font("Arial", Font.BOLD, 12));
        panelDialogo.add(lblSugText);

        String[] opcionesBotones = {"Probar Otro Orden (Mismo Mapa)", "Salir del Programa"};

        int seleccion = JOptionPane.showOptionDialog(
                this,
                panelDialogo,
                "Análisis de Backtracking Realizado",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                opcionesBotones,
                opcionesBotones[0]
        );

        if (seleccion == JOptionPane.YES_OPTION) {
            // Desplegar menú de cambio dinámico de prioridades
            String[] opcionesPrioridad = {
                    "Arriba, Derecha, Abajo, Izquierda",
                    "Derecha, Abajo, Izquierda, Arriba",
                    "Abajo, Izquierda, Arriba, Derecha",
                    "Izquierda, Arriba, Derecha, Abajo"
            };

            String nuevaPrioridad = (String) JOptionPane.showInputDialog(
                    this,
                    "Seleccione la nueva prioridad estratégica:",
                    "Cambio de Configuración",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opcionesPrioridad,
                    prioridadActualTexto
            );

            if (nuevaPrioridad != null) {
                this.prioridadActualTexto = nuevaPrioridad;
                configurarPrioridad(nuevaPrioridad);
                // Reiniciar ejecución recursiva en bucle sobre el mismo mapa
                iniciarHilosResolucion();
            } else {
                mostrarMenuInteractivoFinal(solucion); // Evitar bypass de cancelación
            }
        } else {
            System.exit(0);
        }
    }

    // CONFIGURAR PRIORIDAD DE MOVIMIENTOS
    private void configurarPrioridad(String opcion) {
        switch (opcion) {
            case "Derecha, Abajo, Izquierda, Arriba":
                ordenResolver = new int[][]{{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
                break;
            case "Abajo, Izquierda, Arriba, Derecha":
                ordenResolver = new int[][]{{1, 0}, {0, -1}, {-1, 0}, {0, 1}};
                break;
            case "Izquierda, Arriba, Derecha, Abajo":
                ordenResolver = new int[][]{{0, -1}, {-1, 0}, {0, 1}, {1, 0}};
                break;
            default: // "Arriba, Derecha, Abajo, Izquierda"
                ordenResolver = new int[][]{{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
                break;
        }
    }

    // ENCARGADO DE TALLAR LA MATRIZ ORIGINAL UNA ÚNICA VEZ
    public void generarEstructuraBase(int tamaño, String tipoSolucion) {
        if (tamaño % 2 == 0) {
            tamaño++;
        }

        laberintoOriginal = new int[tamaño][tamaño];
        TAM = Math.min(600 / tamaño, 80);

        for (int fila = 0; fila < tamaño; fila++) {
            for (int col = 0; col < tamaño; col++) {
                laberintoOriginal[fila][col] = 1;
            }
        }

        carvarLaberintoFijo(0, 0);
        laberintoOriginal[tamaño - 1][tamaño - 1] = 2;

        if (tipoSolucion.equals("Imposible")) {
            if (tamaño - 2 >= 0) {
                laberintoOriginal[tamaño - 1][tamaño - 2] = 1;
                laberintoOriginal[tamaño - 2][tamaño - 1] = 1;
            }
        }
    }

    private void carvarLaberintoFijo(int fila, int col) {
        laberintoOriginal[fila][col] = 0;

        int[][] direcciones = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}};
        ArrayList<int[]> listaDirs = new ArrayList<>();
        for (int[] dir : direcciones) listaDirs.add(dir);
        Collections.shuffle(listaDirs);

        for (int[] dir : listaDirs) {
            int nuevaFila = fila + dir[0];
            int nuevaCol = col + dir[1];

            if (nuevaFila >= 0 && nuevaFila < laberintoOriginal.length && nuevaCol >= 0 && nuevaCol < laberintoOriginal[0].length) {
                if (laberintoOriginal[nuevaFila][nuevaCol] == 1) {
                    laberintoOriginal[fila + dir[0] / 2][col + dir[1] / 2] = 0;
                    carvarLaberintoFijo(nuevaFila, nuevaCol);
                }
            }
        }
    }

    // RECURSIVIDAD COMPLETA
    public boolean resolver(int fila, int col) {
        if (fila < 0 || col < 0 || fila >= laberinto.length || col >= laberinto[0].length) {
            return false;
        }

        if (laberinto[fila][col] == 1 || laberinto[fila][col] == 9 || laberinto[fila][col] == 5) {
            return false;
        }

        llamadas++;
        profundidadActual++;
        actualizarEstadisticas();

        if (laberinto[fila][col] == 2) {
            profundidadActual--;
            return true;
        }

        laberinto[fila][col] = 9;
        repaint();
        dormir();

        for (int[] dir : ordenResolver) {
            if (resolver(fila + dir[0], col + dir[1])) {
                profundidadActual--;
                return true;
            }
        }

        laberinto[fila][col] = 5;
        retrocesos++;
        profundidadActual--;
        actualizarEstadisticas();
        repaint();
        dormir();

        return false;
    }

    private void actualizarEstadisticas() {
        long tiempoActual = System.nanoTime();
        double ms = (tiempoActual - inicio) / 1_000_000.0;

        SwingUtilities.invokeLater(() -> {
            if (lblNodos != null) lblNodos.setText("Nodos Explorados: " + llamadas);
            if (lblProfundidad != null) lblProfundidad.setText("Profundidad Actual: " + profundidadActual);
            if (lblRetrocesos != null) lblRetrocesos.setText("Retrocesos: " + retrocesos);
            if (lblTiempo != null) lblTiempo.setText(String.format("Tiempo: %.1f ms", ms));
        });
    }

    public void dormir() {
        try {
            Thread.sleep(40); // 40ms para acelerar pruebas repetitivas
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(laberintoOriginal[0].length * TAM, laberintoOriginal.length * TAM);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int fila = 0; fila < laberinto.length; fila++) {
            for (int col = 0; col < laberinto[0].length; col++) {
                switch (laberinto[fila][col]) {
                    case 0: g.setColor(Color.WHITE); break;
                    case 1: g.setColor(Color.BLACK); break;
                    case 2: g.setColor(Color.BLUE); break;
                    case 9: g.setColor(Color.GREEN); break;
                    case 5: g.setColor(Color.RED); break;
                }

                g.fillRect(col * TAM, fila * TAM, TAM, TAM);
                g.setColor(Color.GRAY);
                g.drawRect(col * TAM, fila * TAM, TAM, TAM);
            }
        }
    }
}