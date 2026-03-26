package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class TelaPrincipal extends JFrame {

    // Componentes visuais
    private JTable tabela;
    private ModeloTabelaEditavel modeloTabela;
    private JLabel labelVisualizacaoFoto;
    private TableRowSorter<ModeloTabelaEditavel> sorter;
    private JComboBox<String> comboFiltro;

    // Contadores (Stats)
    private JLabel labelTotal;
    private JLabel labelLidos;
    private JLabel labelFalhas;
    private int totalFiles = 0;
    private int lidosComSucesso = 0;
    private int falhas = 0;

    public TelaPrincipal() {
        setTitle("Leitor e Conferente de Patrimônios (Conf. HTR Manual)");
        setSize(1024, 600); // Janela maior para acomodar a visualização
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ==========================================
        // 1. Painel Superior (Filtros e Contadores)
        // ==========================================
        JPanel painelTopo = new JPanel(new BorderLayout());
        JPanel painelFiltros = new JPanel();
        JButton btnCarregarPasta = new JButton("Processar Pasta...");
        painelFiltros.add(btnCarregarPasta);

        // Combo de Filtro
        String[] opcoesFiltro = {"Tudo", "Apenas Lidos", "Com Falhas"};
        comboFiltro = new JComboBox<>(opcoesFiltro);
        painelFiltros.add(new JLabel("Filtrar por:"));
        painelFiltros.add(comboFiltro);
        painelTopo.add(painelFiltros, BorderLayout.NORTH);

        // Painel de Stats (Contadores no rodapé da barra superior)
        JPanel painelStats = new JPanel();
        labelTotal = new JLabel("Total: 0 | ");
        labelLidos = new JLabel("Lidos: 0 | ");
        labelFalhas = new JLabel("Falhas: 0");
        labelFalhas.setForeground(Color.RED); // Destaca as falhas
        painelStats.add(labelTotal);
        painelStats.add(labelLidos);
        painelStats.add(labelFalhas);
        painelTopo.add(painelStats, BorderLayout.SOUTH);

        add(painelTopo, BorderLayout.NORTH);

        // ==========================================
        // 2. Painel Central (Tabela + Visualização)
        // ==========================================
        JPanel painelCentral = new JPanel(new BorderLayout());

        // Criando a Tabela com o Model Editável
        modeloTabela = new ModeloTabelaEditavel();
        tabela = new JTable(modeloTabela);

        // Ativando Sorter para filtros
        sorter = new TableRowSorter<>(modeloTabela);
        tabela.setRowSorter(sorter);

        // Escondendo a coluna que guarda o objeto File original (Col. 3)
        tabela.removeColumn(tabela.getColumnModel().getColumn(3));

        JScrollPane scrollTabela = new JScrollPane(tabela);
        painelCentral.add(scrollTabela, BorderLayout.CENTER);

        // Painel de Visualização da Foto (Sidebar Direita)
        JPanel painelVisualizacao = new JPanel(new BorderLayout());
        painelVisualizacao.setPreferredSize(new Dimension(300, 0)); // Largura fixa de 300px
        labelVisualizacaoFoto = new JLabel("Clique em uma linha para ver a foto", JLabel.CENTER);
        labelVisualizacaoFoto.setBorder(BorderFactory.createTitledBorder("Visualização da Foto"));
        painelVisualizacao.add(labelVisualizacaoFoto, BorderLayout.CENTER);
        painelCentral.add(painelVisualizacao, BorderLayout.EAST);

        add(painelCentral, BorderLayout.CENTER);

        // ==========================================
        // 3. Configuração de Eventos
        // ==========================================

        // Evento 1: Botão Carregar Pasta
        btnCarregarPasta.addActionListener(e -> escolherEProcessarPasta());

        // Evento 2: Filtro Combo Box
        comboFiltro.addActionListener(e -> filtrarTabela());

        // Evento 3: Seleção na Tabela (Mostra a foto na sidebar)
        tabela.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && tabela.getSelectedRow() != -1) {
                // Recupera a linha correta do model (mesmo com filtro ativado)
                int modelRowIdx = tabela.convertRowIndexToModel(tabela.getSelectedRow());
                // Recupera o objeto File da coluna oculta (Col 3)
                File arquivoFoto = (File) modeloTabela.getValueAt(modelRowIdx, 3);
                mostrarVisualizacaoFoto(arquivoFoto);
            }
        });
    }

    // ==========================================
    // Métodos Auxiliares
    // ==========================================

    private void escolherEProcessarPasta() {
        JFileChooser seletor = new JFileChooser();
        seletor.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        seletor.setDialogTitle("Selecione a pasta com as fotos");

        if (seletor.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File pasta = seletor.getSelectedFile();
            File[] fotos = pasta.listFiles((dir, name) -> {
                String lName = name.toLowerCase();
                return lName.endsWith(".jpg") || lName.endsWith(".jpeg") || lName.endsWith(".png");
            });

            if (fotos != null && fotos.length > 0) {
                // Limpa tabela e reseta contadores
                modeloTabela.setRowCount(0);
                totalFiles = fotos.length;
                lidosComSucesso = 0;
                falhas = 0;

                for (File foto : fotos) {
                    System.out.println("Processando: " + foto.getName());
                    String[] dadosIniciais = LeitorMotor.processarImagem(foto);

                    // Verifica se o patrimônio foi encontrado
                    boolean patFound = !dadosIniciais[1].equals("Não encontrado");
                    boolean snFound = !dadosIniciais[2].equals("Não encontrado");

                    // Lógica de contagem
                    if (patFound) {
                        lidosComSucesso++;
                    } else {
                        falhas++;
                        // Dica Visual: Altera o valor para o usuário saber onde clicar
                        dadosIniciais[1] = "[ Falha OCR: Clique e Digite Manuscrito ]";
                    }

                    // Prepara a linha para a tabela (adicionando o objeto File no final)
                    Object[] rowData = {
                            dadosIniciais[0], // Foto
                            dadosIniciais[1], // Patrimônio
                            dadosIniciais[2], // Serial
                            foto              // OBJETO FILE OCULTO
                    };
                    modeloTabela.addRow(rowData);
                }
                atualizarContadores();
                JOptionPane.showMessageDialog(this, "Processamento concluído!");
            }
        }
    }

    private void atualizarContadores() {
        // Recalcula falhas baseadas no estado atual da tabela (usuário pode ter corrigido)
        int falhasAtuais = 0;
        int lidosAtuais = 0;
        int rows = modeloTabela.getRowCount();

        for (int i = 0; i < rows; i++) {
            String pat = (String) modeloTabela.getValueAt(i, 1);
            if (pat.contains("Falha OCR") || pat.equals("Não encontrado")) {
                falhasAtuais++;
            } else {
                lidosAtuais++;
            }
        }

        falhas = falhasAtuais;
        lidosComSucesso = lidosAtuais;

        labelTotal.setText("Total: " + rows + " | ");
        labelLidos.setText("Lidos: " + lidosAtuais + " | ");
        labelFalhas.setText("Falhas: " + falhasAtuais);

        // Muda cor do contador se ainda houver falhas
        labelFalhas.setForeground(falhasAtuais > 0 ? Color.RED : new Color(0, 150, 0));
    }

    private void filtrarTabela() {
        String selecionado = (String) comboFiltro.getSelectedItem();

        // Remove filtros anteriores
        sorter.setRowFilter(null);

        if ("Apenas Lidos".equals(selecionado)) {
            // Filtra rows onde coluna 1 NÃO contém "Falha OCR" e NÃO é "Não encontrado"
            RowFilter<ModeloTabelaEditavel, Integer>rf = new RowFilter<>() {
                public boolean include(Entry<? extends ModeloTabelaEditavel, ? extends Integer> entry) {
                    String pat = (String) entry.getValue(1);
                    return !(pat.contains("Falha OCR") || pat.equals("Não encontrado"));
                }
            };
            sorter.setRowFilter(rf);

        } else if ("Com Falhas".equals(selecionado)) {
            // Filtra rows onde coluna 1 CONTÉM "Falha OCR" ou É "Não encontrado"
            sorter.setRowFilter(RowFilter.regexFilter("(?i)Falha OCR|Não encontrado", 1));
        }
    }

    private void mostrarVisualizacaoFoto(File arquivoFoto) {
        labelVisualizacaoFoto.setText(""); // Limpa o texto
        labelVisualizacaoFoto.setIcon(null);

        try {
            BufferedImage buffer = ImageIO.read(arquivoFoto);
            // Redimensiona a imagem para caber na sidebar mantendo a proporção
            int larg = labelVisualizacaoFoto.getWidth() - 20; // Ajuste para bordas
            int alt = labelVisualizacaoFoto.getHeight() - 20;

            if (buffer.getWidth() > larg || buffer.getHeight() > alt) {
                // Se a foto for maior que o painel, redimensiona
                Image scaled = buffer.getScaledInstance(larg, alt, Image.SCALE_SMOOTH);
                labelVisualizacaoFoto.setIcon(new ImageIcon(scaled));
            } else {
                labelVisualizacaoFoto.setIcon(new ImageIcon(buffer));
            }

        } catch (Exception e) {
            labelVisualizacaoFoto.setText("Erro ao carregar imagem.");
        }
    }

    // ==========================================
    // CLASSE INTERNA: O Modelo Tabela Editável
    // ==========================================
    class ModeloTabelaEditavel extends DefaultTableModel {

        // Define as colunas originais
        public ModeloTabelaEditavel() {
            super(new Object[][]{}, new String[]{"Foto", "Patrimônio", "Número de Série", "FileObj"});
        }

        // Define quais colunas são editáveis
        @Override
        public boolean isCellEditable(int row, int column) {
            // Coluna 0 (Foto) não editável.
            // Coluna 1 (Patrimônio) e 2 (Serial) são editáveis!
            // Coluna 3 (FileObj) é oculta, não editável.
            return (column == 1 || column == 2);
        }

        // Gatilho para atualizar contadores quando o usuário edita manualmente
        @Override
        public void setValueAt(Object aValue, int row, int column) {
            super.setValueAt(aValue, row, column);
            if (column == 1) { // Se o patrimônio foi editado
                atualizarContadores();
            }
        }
    }

    // Ponto de entrada
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TelaPrincipal tela = new TelaPrincipal();
            tela.setVisible(true);
        });
    }
}