import javax.swing.*;
import java.awt.*;

public class InterfazAgro {

    // los datos se conservan al entrar y salir de pantallas
    private static final AnalizadorAgro analizador = new AnalizadorAgro();

    // ---------- Métodos auxiliares ----------

    // con qué año trabajar
    private static void aplicarAño(JComboBox<String> combo) {
        analizador.seleccionarAño(Integer.parseInt((String) combo.getSelectedItem()));
    }

    // Convierte el texto del precio; 
    private static double leerPrecio(String texto) {
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            return Double.NaN;
        }
    }

    // "60mm" -> 60.0
    private static double leerLluvia(String texto) {
        return Double.parseDouble(texto.replace("mm", "").trim());
    }

    private static JScrollPane crearVisor(String texto) {
        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(480, 300));
        return scroll;
    }

    private static void mostrarTexto(Component padre, String titulo, String texto) {
        JOptionPane.showMessageDialog(padre, crearVisor(texto), titulo, JOptionPane.PLAIN_MESSAGE);
    }

    private static void limpiarResultados(JLabel... campos) {
        for (JLabel campo : campos) {
            campo.setText("[       ]");
        }
    }

    // ---------- Apartado 1 ----------

    public static void entrar(JPanel panel){
        panel.removeAll();
        panel.setBackground(new Color(26, 117, 51));
        //titulo1
        JLabel titulo = new JLabel("ANALIZADOR DE PRONÓSTICOS CLIMÁTICOS");
        titulo.setBounds(100, 0, 550, 40);
        titulo.setFont(new Font("Times New Roman", Font.BOLD, 23));
        titulo.setForeground(Color.black);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titulo);
        //y
        JLabel y = new JLabel("&");
        y.setBounds(370, 35, 30, 30);
        y.setFont(new Font("Times New Roman", Font.BOLD, 28));
        y.setForeground(Color.black);
        y.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(y);
        //titulo2
        JLabel titulo2 = new JLabel("PRECIOS DE COSECHA");
        titulo2.setBounds(240, 60, 300, 40);
        titulo2.setFont(new Font("Times New Roman", Font.BOLD, 23));
        titulo2.setForeground(Color.black);
        titulo2.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titulo2);
        //boton entrar
        JButton btnentrar = new JButton("INICIAR");
        btnentrar.setBounds(290,500,200,50);
        btnentrar.setBackground(new Color(113, 176, 126));
        btnentrar.setFocusPainted(false);
        panel.add(btnentrar);
        btnentrar.addActionListener(e ->{
            panelAnalisis(panel);
        });

        ImageIcon icono = new ImageIcon("logoagro.png");
        Image imagen = icono.getImage().getScaledInstance(750, 700, Image.SCALE_SMOOTH);
        ImageIcon imgfinal = new ImageIcon(imagen);
        JLabel logo = new JLabel(imgfinal);
        logo.setBounds(190, 130,400, 400);
        panel.add(logo);

        panel.repaint();
        panel.revalidate();
    }

    // ---------- Apartado 2: Centro de análisis ----------

    public static void panelAnalisis(JPanel panel){
        panel.removeAll();
        panel.setBackground(new Color(216, 235, 204));
        //titulo
        JLabel titulo = new JLabel("CENTRO DE ANALISIS");
        titulo.setFont(new Font("Times New Roman", Font.BOLD, 18));
        titulo.setForeground(Color.black);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setBounds(300, 1, 200, 30);
        panel.add(titulo);
        // Apartado 1
        JPanel panelanalisis = new JPanel();
        panelanalisis.setLayout(null);
        panelanalisis.setBounds(5, 35, 775, 160);
        panelanalisis.setBackground(new Color(226, 250, 220));

        panelanalisis.setBorder(
        BorderFactory.createTitledBorder(BorderFactory.createLineBorder
            ( new Color(26, 117, 51),3),
            "CONFIGURACION Y REGISTRO"
        ));
        //producto: cultivo (los nombres vienen de la lógica)
        JLabel producto = new JLabel("Cultivo: ");
        producto.setBounds(25, 70, 50, 20);
        producto.setForeground(Color.black);
        panel.add(producto);
        JComboBox<String> campocultivo = new JComboBox<>(AnalizadorAgro.getCultivos());
        campocultivo.setBounds(70,70,100,20);
        campocultivo.setBackground(new Color(216, 235, 204));
        panel.add(campocultivo);
        //lluvia
        JLabel lluvia = new JLabel("Lluvia: ");
        lluvia.setBounds(190, 70, 50, 20);
        lluvia.setForeground(Color.black);
        panel.add(lluvia);
        String[] lluvias = {
            "60mm","70mm","80mm","90mm","100mm","110mm",
        };
        JComboBox<String> campolluvia =new JComboBox<>(lluvias);
        campolluvia.setBounds(230, 70,100,20);
        campolluvia.setBackground(new Color(216, 235, 204));
        panel.add(campolluvia);
        //año: 2026 : 2028
        JLabel año = new JLabel("Año: ");
        año.setBounds(350, 70, 50, 20);
        año.setForeground(Color.black);
        panel.add(año);
        String[] años = {
            "2026","2027","2028","2029"
        };
        JComboBox<String> campoaños =new JComboBox<>(años);
        campoaños.setBounds(380,70,100,20);
        campoaños.setBackground(new Color(216, 235, 204));
        panel.add(campoaños);
        //mes : 12 meses
        JLabel mes = new JLabel("Mes: ");
        mes.setBounds(500, 70, 50, 20);
        mes.setForeground(Color.black);
        panel.add(mes);

        JComboBox<String> campomeses = new JComboBox<>(AnalizadorAgro.getMeses());
        campomeses.setBounds(530,70,100,20);
        campomeses.setBackground(new Color(216, 235, 204));
        panel.add(campomeses);
        //precio
        JLabel Precio = new JLabel("Precio:");
        Precio.setBounds(25, 110, 80, 20);
        Precio.setForeground(Color.black);
        panel.add(Precio);

        JTextField campoPrecio = new JTextField();
        campoPrecio.setBounds(70, 110, 90, 20);
        campoPrecio.setBackground(new Color(216, 235, 204));
        panel.add(campoPrecio);

        // ----- Apartado 2: etiquetas de resultados -----
        JLabel campoRproducto = new JLabel("[      ]");
        JLabel campoRestado = new JLabel("[       ]");
        JLabel campoRmes = new JLabel("[       ]");
        JLabel campoRvariacion = new JLabel("[       ]");
        JLabel campoRlluvia = new JLabel("[       ]");
        JLabel campoRmedia = new JLabel("[       ]");
        JLabel campoRtendencia = new JLabel("[       ]");

        //Botones
        //boton analizar
        JButton btnanalizar = new JButton("ANALIZAR");
        btnanalizar.setBounds(310, 145, 100,35);
        btnanalizar.setBackground(new Color(145, 207, 145));
        panel.add(btnanalizar);
        btnanalizar.addActionListener(a ->{
            aplicarAño(campoaños);
            ResultadoAnalisis res = analizador.analizarMes(
                campomeses.getSelectedIndex(), (String) campocultivo.getSelectedItem());

            if (res == null) {
                JOptionPane.showMessageDialog(panel, analizador.getMensaje(),
                    "No se pudo analizar", JOptionPane.WARNING_MESSAGE);
                return;
            }
            campoRproducto.setText(res.cultivo);
            campoRmes.setText(res.mes);
            campoRlluvia.setText(res.lluvia);
            campoRtendencia.setText(res.tendencia);
            campoRestado.setText(res.estado);
            campoRvariacion.setText(res.variacion);
            campoRmedia.setText(res.media);
        });
        // boton registrar datos
        JButton btnregistrar = new JButton("REGISTRAR DATOS");
        btnregistrar.setBounds(430, 145, 150, 35);
        panel.add(btnregistrar);
        btnregistrar.setBackground(new Color(145, 207, 145));
        btnregistrar.addActionListener(h ->{
            aplicarAño(campoaños);
            String nombreCultivo = (String) campocultivo.getSelectedItem();
            int numMes = campomeses.getSelectedIndex();
            double precioIngresado = leerPrecio(campoPrecio.getText());
            double mm = leerLluvia((String) campolluvia.getSelectedItem());

            if (Double.isNaN(precioIngresado)) {
                JOptionPane.showMessageDialog(panel, "El precio debe ser un número (por ejemplo 1250).",
                    "Precio no valido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok;
            if (analizador.existeRegistro(numMes, nombreCultivo)) {
                int resp = JOptionPane.showConfirmDialog(panel,
                    nombreCultivo + " ya tiene datos en ese mes.\n¿Deseas actualizarlos?",
                    "Registro existente", JOptionPane.YES_NO_OPTION);
                if (resp != JOptionPane.YES_OPTION) {
                    return;
                }
                ok = analizador.actualizarDatos(numMes, nombreCultivo, precioIngresado, mm);
            } else {
                ok = analizador.registrarDatos(numMes, nombreCultivo, precioIngresado, mm);
            }

            JOptionPane.showMessageDialog(panel, analizador.getMensaje(),
                ok ? "Listo" : "Error",
                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            if (ok) {
                campoPrecio.setText("");
            }
        });
        // boton de nuevo analisis
        JButton btnnewanalisis = new JButton("NUEVO ANALISIS");
        btnnewanalisis.setBounds(600, 145, 150, 35);
        panel.add(btnnewanalisis);
        btnnewanalisis.setBackground(new Color(145, 207, 145));
        btnnewanalisis.addActionListener(na ->{
            analizador.nuevoAnalisis();
            campoPrecio.setText("");
            limpiarResultados(campoRproducto, campoRestado, campoRmes, campoRvariacion,
                campoRlluvia, campoRmedia, campoRtendencia);
        });
        panel.add(panelanalisis);

        // Apartado 2
        JPanel panelResultados = new JPanel();
        panelResultados.setLayout(null);
        panelResultados.setBounds(5, 195, 775, 150);
        panelResultados.setBackground(new Color(226, 250, 220));

        panelResultados.setBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder
                (new Color(26, 117, 51),3),
                "RESULTADOS DEL ANÁLISIS"
            )
        );
        //label producto resultado
        JLabel Rproducto = new JLabel("Cultivo: ");
        Rproducto.setBounds(25, 220, 50, 20);
        Rproducto.setForeground(Color.black);
        panel.add(Rproducto);

        campoRproducto.setBounds(70,220,100,20);
        panel.add(campoRproducto);
        //label estado
        JLabel estado = new JLabel("Estado: ");
        estado.setBounds(25, 250, 70, 20);
        estado.setForeground(Color.black);
        panel.add(estado);

        campoRestado.setBounds(70,250,100,20);
        panel.add(campoRestado);
        //mes
        JLabel Rmes = new JLabel("Mes:");
        Rmes.setBounds(170,220,100,20);
        panel.add(Rmes);

        campoRmes.setBounds(220,220,100,20);
        panel.add(campoRmes);
        //label variacion %
        JLabel variacion = new JLabel("Variacion: ");
        variacion.setBounds(170, 250, 70, 20);
        variacion.setForeground(Color.black);
        panel.add(variacion);

        campoRvariacion.setBounds(240,250,100,20);
        panel.add(campoRvariacion);
        //lluvia
        JLabel Rlluvia = new JLabel("Lluvia:");
        Rlluvia.setBounds(300,220,100,20);
        panel.add(Rlluvia);

        campoRlluvia.setBounds(360,220,100,20);
        panel.add(campoRlluvia);
        //label media
        JLabel media = new JLabel("Media: ");
        media.setBounds(300, 250, 70, 20);
        media.setForeground(Color.black);
        panel.add(media);

        campoRmedia.setBounds(360,250,100,20);
        panel.add(campoRmedia);
        //label tendencia
        JLabel tendencia = new JLabel("Tendencia: ");
        tendencia.setBounds(430, 220, 70, 20);
        tendencia.setForeground(Color.black);
        panel.add(tendencia);

        campoRtendencia.setBounds(500,220,180,20);
        panel.add(campoRtendencia);

        panel.add(panelResultados);
        // apartado 3
        JPanel panelAcciones = new JPanel();
        panelAcciones.setLayout(null);
        panelAcciones.setBounds(5, 345, 775, 310);
        panelAcciones.setBackground(new Color(226, 250, 220));

        panelAcciones.setBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
            new Color(26, 117, 51),3),
            "ACCIONES Y CONSULTAS"
        ));
        //9 botones apartado 3
        //1 boton historial (vectores de precios y lluvia)
        JButton btnhistorial = new JButton("HISTORIAL");
        btnhistorial.setBounds(25, 390, 180, 50);
        panel.add(btnhistorial);
        btnhistorial.setBackground(new Color(145, 207, 145));
        btnhistorial.addActionListener(h ->{
            aplicarAño(campoaños);
            mostrarTexto(panel, "Historial",
                analizador.historial((String) campocultivo.getSelectedItem()));
        });
        //2 boton estados: pila de escenarios con opcion de deshacer (backtracking)
        JButton btnestados = new JButton("ESTADOS");
        btnestados.setBounds(230, 390, 180, 50);
        panel.add(btnestados);
        btnestados.setBackground(new Color(145, 207, 145));
        btnestados.addActionListener(p ->{
            Object[] opciones = {"Deshacer ultimo", "Cerrar"};
            int op = JOptionPane.showOptionDialog(panel,
                crearVisor(analizador.estadosPila()),
                "Estados (pila de escenarios)",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, opciones, opciones[1]);
            if (op == 0) {
                JOptionPane.showMessageDialog(panel, analizador.deshacerEscenario(),
                    "Backtracking", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        //3 boton comportamiento (matriz de precios y tendencia estacional)
        JButton btncomportamiento = new JButton("COMPORTAMIENTO");
        btncomportamiento.setBounds(430, 390, 180, 50);
        panel.add(btncomportamiento);
        btncomportamiento.setBackground(new Color(145, 207, 145));
        btncomportamiento.addActionListener(p ->{
            aplicarAño(campoaños);
            mostrarTexto(panel, "Comportamiento",
                analizador.comportamiento((String) campocultivo.getSelectedItem()));
        });
        //4  escenario anterior
        JButton btnanterior = new JButton("ESCENARIO ANTERIOR");
        btnanterior.setBounds(25, 460, 180, 50);
        panel.add(btnanterior);
        btnanterior.setBackground(new Color(145, 207, 145));
        btnanterior.addActionListener(p ->{
            aplicarAño(campoaños);
            mostrarTexto(panel, "Escenario anterior",
                analizador.escenarioAnterior((String) campocultivo.getSelectedItem()));
        });
        //5  escenario actual
        JButton btnactual = new JButton("ESCENARIO ACTUAL");
        btnactual.setBounds(230, 460, 180, 50);
        panel.add(btnactual);
        btnactual.setBackground(new Color(145, 207, 145));
        btnactual.addActionListener(p ->{
            aplicarAño(campoaños);
            mostrarTexto(panel, "Escenario actual",
                analizador.escenarioActual((String) campocultivo.getSelectedItem()));
        });
        //6  siguiente escenario
        JButton btnsiguiente = new JButton("ESCENARIO SIGUIENTE");
        btnsiguiente.setBounds(430, 460, 180, 50);
        panel.add(btnsiguiente);
        btnsiguiente.setBackground(new Color(145, 207, 145));
        btnsiguiente.addActionListener(p ->{
            aplicarAño(campoaños);
            mostrarTexto(panel, "Escenario siguiente",
                analizador.escenarioSiguiente((String) campocultivo.getSelectedItem()));
        });
        //7 resultados atipicos
        JButton btnatipico = new JButton("ATIPICOS");
        btnatipico.setBounds(25, 530, 180, 50);
        panel.add(btnatipico);
        btnatipico.setBackground(new Color(145, 207, 145));
        btnatipico.addActionListener(p ->{
            aplicarAño(campoaños);
            mostrarTexto(panel, "Precios atipicos",
                analizador.atipicos((String) campocultivo.getSelectedItem()));
        });
        //8 ciclo anual (lista circular): antes era el boton VACIO
        JButton btnciclo = new JButton("CICLO ANUAL");
        btnciclo.setBounds(230, 530, 180, 50);
        panel.add(btnciclo);
        btnciclo.setBackground(new Color(145, 207, 145));
        btnciclo.addActionListener(p ->{
            aplicarAño(campoaños);
            mostrarTexto(panel, "Ciclo anual",
                analizador.cicloAnual((String) campocultivo.getSelectedItem(), campomeses.getSelectedIndex()));
        });
        //9 boton salir
        JButton btnsalir = new JButton("SALIR");
        btnsalir.setBounds(430, 530, 180, 50);
        panel.add(btnsalir);
        btnsalir.setBackground(new Color(145, 207, 145));
        btnsalir.addActionListener(s ->{
            entrar(panel);
        });
        //logo
        ImageIcon icono = new ImageIcon("logoagro.png");
        Image imagen = icono.getImage().getScaledInstance(350, 450, Image.SCALE_SMOOTH);
        ImageIcon imgfinal = new ImageIcon(imagen);
        JLabel logo = new JLabel(imgfinal);
        logo.setBounds(550, 300,320, 370);
        panel.add(logo);

        panel.add(panelAcciones);
        panel.repaint();
        panel.revalidate();
    }
    public static void main(String[] args) {

        JFrame ventana = new JFrame();

        ventana.setTitle("Ventana AgTech ");
        ventana.setSize(800, 700);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        ventana.add(panel);
        ventana.setLocationRelativeTo(null);

        entrar(panel);

        // inicio de ventana
        ventana.setVisible(true);
    }
} 