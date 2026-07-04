package certificados;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Equivalente ao interface.py
 *
 * tkinter          ->  Swing (ja vem incluido no JDK, como o tkinter no Python)
 * filedialog       ->  JFileChooser
 * ttk.Progressbar  ->  JProgressBar
 * tk.Text          ->  JTextArea
 *
 * Diferenca importante: em Java o processamento roda em um SwingWorker
 * (uma thread separada) para a janela nao travar durante o processo.
 */
public class InterfaceGrafica {

    private static String caminhoPdf = "";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(InterfaceGrafica::criarJanela);
    }

    private static void criarJanela() {
        JFrame janela = new JFrame("Sistema de Certificados");
        janela.setSize(500, 500);
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setLocationRelativeTo(null); // centraliza na tela

        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Sistema de Certificados");
        titulo.setFont(new Font("Arial", Font.PLAIN, 15));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton botaoSelecionar = new JButton("Selecionar PDF");
        botaoSelecionar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel labelArquivo = new JLabel("Arquivo selecionado:");
        labelArquivo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel arquivoMostrado = new JLabel(" ");
        arquivoMostrado.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea saida = new JTextArea(8, 50);
        saida.setEditable(false);
        JScrollPane scroll = new JScrollPane(saida);

        JProgressBar barra = new JProgressBar(0, 100);
        barra.setPreferredSize(new Dimension(350, 20));
        barra.setMaximumSize(new Dimension(350, 20));

        JButton botaoProcessar = new JButton("PROCESSAR");
        botaoProcessar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Equivalente ao selecionar_pdf()
        botaoSelecionar.addActionListener(e -> {
            JFileChooser seletor = new JFileChooser();
            seletor.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos PDF", "pdf"));

            if (seletor.showOpenDialog(janela) == JFileChooser.APPROVE_OPTION) {
                File arquivo = seletor.getSelectedFile();
                caminhoPdf = arquivo.getAbsolutePath();
                arquivoMostrado.setText(arquivo.getName()); // equivalente ao os.path.basename()
            }
        });

        // Equivalente ao processar()
        botaoProcessar.addActionListener(e -> {
            botaoProcessar.setEnabled(false);
            botaoSelecionar.setEnabled(false);
            saida.setText("");
            saida.append("Iniciando processamento...\n");

            // SwingWorker = processamento em segundo plano, sem travar a janela
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Main.processarCertificados(
                            caminhoPdf,
                            texto -> SwingUtilities.invokeLater(() -> {
                                saida.append(texto + "\n");
                                saida.setCaretPosition(saida.getDocument().getLength()); // equivalente ao saida.see(END)
                            }),
                            valor -> SwingUtilities.invokeLater(() -> barra.setValue(valor))
                    );
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); // relanca excecoes ocorridas no processamento
                    } catch (Exception ex) {
                        saida.append("Erro: " + ex.getMessage() + "\n");
                    }
                    botaoProcessar.setEnabled(true);
                    botaoSelecionar.setEnabled(true);
                }
            };

            worker.execute();
        });

        painel.add(titulo);
        painel.add(Box.createVerticalStrut(20));
        painel.add(botaoSelecionar);
        painel.add(Box.createVerticalStrut(10));
        painel.add(labelArquivo);
        painel.add(Box.createVerticalStrut(10));
        painel.add(arquivoMostrado);
        painel.add(Box.createVerticalStrut(10));
        painel.add(scroll);
        painel.add(Box.createVerticalStrut(10));
        painel.add(barra);
        painel.add(Box.createVerticalStrut(10));
        painel.add(botaoProcessar);

        janela.add(painel);
        janela.setVisible(true); // equivalente ao janela.mainloop()
    }
}
