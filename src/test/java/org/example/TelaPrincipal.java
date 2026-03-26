package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

public class TelaPrincipal extends JFrame {

    private JTable tabela;
    private DefaultTableModel modeloTabela;

    public TelaPrincipal() {
        // Configurações básicas da Janela
        setTitle("Leitor de Patrimônios");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza na tela
        setLayout(new BorderLayout());

        // 1. Criando a Tabela
        String[] colunas = {"Foto", "Patrimônio", "Número de Série"};
        modeloTabela = new DefaultTableModel(colunas, 0);
        tabela = new JTable(modeloTabela);
        JScrollPane scrollPane = new JScrollPane(tabela); // Adiciona barra de rolagem
        add(scrollPane, BorderLayout.CENTER);

        // 2. Criando o Painel Superior com o Botão
        JPanel painelTopo = new JPanel();
        JButton btnCarregarFotos = new JButton("Carregar Fotos...");
        painelTopo.add(btnCarregarFotos);
        add(painelTopo, BorderLayout.NORTH);

        // 3. Ação do Botão (O que acontece ao clicar)
        btnCarregarFotos.addActionListener(e -> escolherEProcessarFotos());
    }

    private void escolherEProcessarFotos() {
        // Abre o explorador de arquivos do Windows
        JFileChooser seletor = new JFileChooser();
        seletor.setMultiSelectionEnabled(true); // Permite selecionar várias fotos de uma vez!
        seletor.setDialogTitle("Selecione as fotos das etiquetas");

        int resultado = seletor.showOpenDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File[] fotosSelecionadas = seletor.getSelectedFiles();

            // Para cada foto selecionada, chama o motor e adiciona na tabela
            for (File foto : fotosSelecionadas) {
                // Chama a nossa classe de leitura
                String[] dados = LeitorMotor.processarImagem(foto);

                // Adiciona uma nova linha na tabela visual
                modeloTabela.addRow(dados);
            }

            JOptionPane.showMessageDialog(this, "Leitura concluída!");
        }
    }

    // Ponto de partida do aplicativo
    public static void main(String[] args) {
        // Inicia a interface gráfica de forma segura
        SwingUtilities.invokeLater(() -> {
            TelaPrincipal tela = new TelaPrincipal();
            tela.setVisible(true);
        });
    }
}