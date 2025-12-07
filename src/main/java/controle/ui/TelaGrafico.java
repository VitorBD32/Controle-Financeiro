package controle.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import controle.dao.TransacaoDAO;
import controle.dao.TransacaoDAOImpl;
import controle.dao.CategoriaDAO;
import controle.dao.CategoriaDAOImpl;
import controle.model.Transacao;
import controle.model.Categoria;

/**
 * TelaGrafico - Dashboard de Analytics Premium
 * Design moderno inspirado em Material Design / Bootstrap
 * - Gráfico de Barras com gradientes e animações
 * - Gráfico de Linha suave com área preenchida
 * - Gráfico de Pizza/Donut para distribuição
 * - Cards informativos com KPIs
 */
public class TelaGrafico extends JFrame {

    // Paleta de cores moderna (Material Design)
    private static final Color PRIMARY = new Color(63, 81, 181);      // Indigo
    private static final Color PRIMARY_DARK = new Color(48, 63, 159);
    private static final Color ACCENT = new Color(0, 188, 212);       // Cyan
    private static final Color SUCCESS = new Color(76, 175, 80);      // Green
    private static final Color WARNING = new Color(255, 152, 0);      // Orange
    private static final Color DANGER = new Color(244, 67, 54);       // Red
    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private static final Color GRID_COLOR = new Color(233, 236, 239);
    
    // Gradientes para barras
    private static final Color[] CHART_COLORS = {
        new Color(102, 126, 234),   // Purple-Blue
        new Color(118, 75, 162),    // Purple
        new Color(237, 100, 166),   // Pink
        new Color(248, 150, 30),    // Orange
        new Color(67, 203, 172),    // Teal
        new Color(32, 201, 151),    // Green
        new Color(255, 107, 107),   // Red
        new Color(78, 205, 196)     // Cyan
    };

    private final TransacaoDAO tdao = new TransacaoDAOImpl();
    private final CategoriaDAO cdao = new CategoriaDAOImpl();
    
    // Dados
    private Map<String, BigDecimal> categorySums = new LinkedHashMap<>();
    private Map<String, BigDecimal> monthlySums = new LinkedHashMap<>();
    private BigDecimal totalReceitas = BigDecimal.ZERO;
    private BigDecimal totalDespesas = BigDecimal.ZERO;
    private int totalTransacoes = 0;
    
    // Referências dos gráficos
    private ModernBarChart barChartRef;
    private ModernLineChart lineChartRef;
    private ModernDonutChart donutChartRef;
    private ModernAreaChart areaChartRef;

    public TelaGrafico() {
        super("Dashboard Analytics - Controle Financeiro");
        initComponents();
        loadData();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBackground(BACKGROUND);
        getContentPane().setBackground(BACKGROUND);
        
        // Layout principal
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Cards KPI no topo
        JPanel kpiPanel = createKPIPanel();
        
        // Área de gráficos
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        chartsPanel.setOpaque(false);
        
        ModernBarChart barChart = new ModernBarChart("Despesas por Categoria");
        ModernLineChart lineChart = new ModernLineChart("Evolução Mensal");
        
        // Armazenar referências
        this.barChartRef = barChart;
        this.lineChartRef = lineChart;
        
        chartsPanel.add(createCardWrapper(barChart, "📊 Top Categorias"));
        chartsPanel.add(createCardWrapper(lineChart, "📈 Tendência Mensal"));
        
        // Painel central com KPIs + Gráficos
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(kpiPanel, BorderLayout.NORTH);
        centerPanel.add(chartsPanel, BorderLayout.CENTER);
        
        // Gráfico de pizza na parte inferior
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        bottomPanel.setOpaque(false);
        
        ModernDonutChart donutChart = new ModernDonutChart("Distribuição");
        ModernAreaChart areaChart = new ModernAreaChart("Fluxo de Caixa");
        
        // Armazenar referências
        this.donutChartRef = donutChart;
        this.areaChartRef = areaChart;
        
        bottomPanel.add(createCardWrapper(donutChart, "🍩 Distribuição por Categoria"));
        bottomPanel.add(createCardWrapper(areaChart, "💰 Fluxo Acumulado"));
        
        // Combinar tudo
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setOpaque(false);
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Scroll para telas menores
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BACKGROUND);
        
        add(scrollPane);
        
        setSize(1400, 900);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel title = new JLabel("📊 Dashboard Financeiro");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);
        
        JLabel subtitle = new JLabel("Análise completa das suas transações");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_SECONDARY);
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(subtitle);
        
        // Botão de atualizar
        JButton btnRefresh = createModernButton("🔄 Atualizar", PRIMARY);
        btnRefresh.addActionListener(e -> loadData());
        
        // Botão exportar
        JButton btnExport = createModernButton("📥 Exportar PDF", SUCCESS);
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(btnRefresh);
        buttonsPanel.add(btnExport);
        
        header.add(titlePanel, BorderLayout.WEST);
        header.add(buttonsPanel, BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        return header;
    }
    
    private JButton createModernButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(color.brighter());
                } else {
                    g2.setColor(color);
                }
                
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(140, 36));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private JPanel createKPIPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 100));
        
        panel.add(createKPICard("💰 Total Receitas", "R$ 0,00", SUCCESS, "totalReceitas"));
        panel.add(createKPICard("💸 Total Despesas", "R$ 0,00", DANGER, "totalDespesas"));
        panel.add(createKPICard("📊 Saldo", "R$ 0,00", PRIMARY, "saldo"));
        panel.add(createKPICard("🔢 Transações", "0", ACCENT, "totalTransacoes"));
        
        return panel;
    }
    
    private JPanel createKPICard(String title, String value, Color accentColor, String id) {
        JPanel card = new JPanel(new BorderLayout(10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Sombra
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fill(new RoundRectangle2D.Float(3, 3, getWidth() - 3, getHeight() - 3, 15, 15));
                
                // Fundo
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 3, getHeight() - 3, 15, 15));
                
                // Barra de acento à esquerda
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, 5, getHeight() - 3, 15, 15));
                g2.fillRect(5, 0, 5, getHeight() - 3);
                
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 15));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(TEXT_SECONDARY);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValue.setForeground(TEXT_PRIMARY);
        lblValue.setName(id);
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        
        // Registrar para atualização
        card.putClientProperty("valueLabel", lblValue);
        card.setName(id);
        
        return card;
    }
    
    private JPanel createCardWrapper(JComponent chart, String title) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Sombra
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fill(new RoundRectangle2D.Float(4, 4, getWidth() - 4, getHeight() - 4, 20, 20));
                
                // Fundo
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 20, 20));
                
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 0));
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        
        return card;
    }

    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<Categoria> categorias = cdao.findAll();
                List<Transacao> transList = tdao.findAll();
                
                totalTransacoes = transList.size();
                totalReceitas = BigDecimal.ZERO;
                totalDespesas = BigDecimal.ZERO;

                // Soma por categoria
                Map<String, BigDecimal> sums = new LinkedHashMap<>();
                for (Transacao t : transList) {
                    try {
                        Categoria cat = cdao.findById(t.getIdCategoria());
                        String nome = cat != null ? cat.getNome() : "Outros";
                        sums.put(nome, sums.getOrDefault(nome, BigDecimal.ZERO).add(t.getValor().abs()));
                        
                        // Separar receitas/despesas
                        if (t.getValor().compareTo(BigDecimal.ZERO) > 0) {
                            totalReceitas = totalReceitas.add(t.getValor());
                        } else {
                            totalDespesas = totalDespesas.add(t.getValor().abs());
                        }
                    } catch (Exception ex) {
                        // ignore
                    }
                }

                // Somas mensais (últimos 6 meses)
                Map<String, BigDecimal> monthly = new LinkedHashMap<>();
                LocalDate now = LocalDate.now();
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM/yy", new Locale("pt", "BR"));
                for (int i = 5; i >= 0; i--) {
                    LocalDate start = now.minusMonths(i).withDayOfMonth(1);
                    String key = start.format(fmt);
                    monthly.put(key, BigDecimal.ZERO);
                }
                
                DateTimeFormatter fmtMatch = DateTimeFormatter.ofPattern("MMM/yy", new Locale("pt", "BR"));
                for (Transacao t : transList) {
                    if (t.getData() != null) {
                        String key = t.getData().format(fmtMatch);
                        if (monthly.containsKey(key)) {
                            monthly.put(key, monthly.get(key).add(t.getValor().abs()));
                        }
                    }
                }

                categorySums = sums;
                monthlySums = monthly;
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    updateCharts();
                    updateKPIs();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void updateCharts() {
        if (barChartRef != null) {
            barChartRef.setData(categorySums);
            barChartRef.repaint();
        }
        if (lineChartRef != null) {
            lineChartRef.setData(monthlySums);
            lineChartRef.repaint();
        }
        if (donutChartRef != null) {
            donutChartRef.setData(categorySums);
            donutChartRef.repaint();
        }
        if (areaChartRef != null) {
            areaChartRef.setData(monthlySums);
            areaChartRef.repaint();
        }
    }
    
    private void updateKPIs() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        
        // Encontrar e atualizar os labels KPI
        updateKPIValue("totalReceitas", nf.format(totalReceitas));
        updateKPIValue("totalDespesas", nf.format(totalDespesas));
        updateKPIValue("saldo", nf.format(totalReceitas.subtract(totalDespesas)));
        updateKPIValue("totalTransacoes", String.valueOf(totalTransacoes));
    }
    
    private void updateKPIValue(String id, String value) {
        Container content = getContentPane();
        updateKPIRecursive(content, id, value);
    }
    
    private void updateKPIRecursive(Container container, String id, String value) {
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel && id.equals(c.getName())) {
                ((JLabel) c).setText(value);
                return;
            }
            if (c instanceof JPanel) {
                JLabel lbl = (JLabel) ((JPanel) c).getClientProperty("valueLabel");
                if (lbl != null && id.equals(lbl.getName())) {
                    lbl.setText(value);
                    return;
                }
            }
            if (c instanceof Container) {
                updateKPIRecursive((Container) c, id, value);
            }
        }
    }

    // ==================== GRÁFICO DE BARRAS MODERNO ====================
    private class ModernBarChart extends JPanel {
        private Map<String, BigDecimal> data = new LinkedHashMap<>();
        @SuppressWarnings("unused")
        private final String title;
        private int hoveredBar = -1;
        private float[] animationProgress;
        private javax.swing.Timer animationTimer;
        
        public ModernBarChart(String title) {
            this.title = title;
            setOpaque(false);
            setPreferredSize(new Dimension(400, 250));
            
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int newHovered = getBarAt(e.getX(), e.getY());
                    if (newHovered != hoveredBar) {
                        hoveredBar = newHovered;
                        repaint();
                    }
                }
            });
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hoveredBar = -1;
                    repaint();
                }
            });
        }
        
        public void setData(Map<String, BigDecimal> data) {
            this.data = data;
            this.animationProgress = new float[data.size()];
            startAnimation();
        }
        
        private void startAnimation() {
            if (animationTimer != null) animationTimer.stop();
            
            animationTimer = new javax.swing.Timer(16, e -> {
                boolean allDone = true;
                for (int i = 0; i < animationProgress.length; i++) {
                    if (animationProgress[i] < 1.0f) {
                        animationProgress[i] = Math.min(1.0f, animationProgress[i] + 0.05f + (i * 0.01f));
                        allDone = false;
                    }
                }
                repaint();
                if (allDone) ((javax.swing.Timer) e.getSource()).stop();
            });
            animationTimer.start();
        }
        
        private int getBarAt(int mx, int my) {
            if (data.isEmpty()) return -1;
            
            int w = getWidth() - 80;
            int h = getHeight() - 80;
            int x = 60;
            int y = 30;
            
            List<Map.Entry<String, BigDecimal>> entries = new ArrayList<>(data.entrySet());
            entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            int max = Math.min(entries.size(), 6);
            int barWidth = (w - 20) / Math.max(max, 1);
            int gap = 15;
            
            for (int i = 0; i < max; i++) {
                int bx = x + i * barWidth + gap / 2;
                if (mx >= bx && mx <= bx + barWidth - gap && my >= y && my <= y + h) {
                    return i;
                }
            }
            return -1;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            
            int w = getWidth() - 80;
            int h = getHeight() - 80;
            int x = 60;
            int y = 30;
            
            // Grade de fundo
            g2.setColor(GRID_COLOR);
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{5, 5}, 0));
            for (int i = 0; i <= 5; i++) {
                int ly = y + (h * i / 5);
                g2.drawLine(x, ly, x + w, ly);
            }
            
            if (data.isEmpty()) {
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2.drawString("Sem dados disponíveis", w / 2 - 50, h / 2);
                g2.dispose();
                return;
            }
            
            // Ordenar e pegar top 6
            List<Map.Entry<String, BigDecimal>> entries = new ArrayList<>(data.entrySet());
            entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            int max = Math.min(entries.size(), 6);
            
            BigDecimal maxVal = entries.isEmpty() ? BigDecimal.ONE : entries.get(0).getValue();
            if (maxVal.compareTo(BigDecimal.ZERO) == 0) maxVal = BigDecimal.ONE;
            
            int barWidth = (w - 20) / Math.max(max, 1);
            int gap = 15;
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            
            // Eixo Y labels
            g2.setColor(TEXT_SECONDARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int i = 0; i <= 5; i++) {
                BigDecimal val = maxVal.multiply(BigDecimal.valueOf(5 - i)).divide(BigDecimal.valueOf(5), RoundingMode.HALF_UP);
                String label = formatShortCurrency(val);
                int ly = y + (h * i / 5);
                g2.drawString(label, 5, ly + 4);
            }
            
            // Barras
            for (int i = 0; i < max; i++) {
                BigDecimal val = entries.get(i).getValue();
                float progress = (animationProgress != null && i < animationProgress.length) ? animationProgress[i] : 1.0f;
                
                int barH = val.multiply(BigDecimal.valueOf(h))
                    .divide(maxVal, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(progress))
                    .intValue();
                
                int bx = x + i * barWidth + gap / 2;
                int by = y + h - barH;
                int bw = barWidth - gap;
                
                // Gradiente
                Color color = CHART_COLORS[i % CHART_COLORS.length];
                GradientPaint gradient = new GradientPaint(bx, by, color, bx, by + barH, color.darker());
                g2.setPaint(gradient);
                
                // Barra com bordas arredondadas
                g2.fill(new RoundRectangle2D.Float(bx, by, bw, barH, 8, 8));
                
                // Highlight se hover
                if (i == hoveredBar) {
                    g2.setColor(new Color(255, 255, 255, 60));
                    g2.fill(new RoundRectangle2D.Float(bx, by, bw, barH, 8, 8));
                    
                    // Tooltip
                    String tooltip = entries.get(i).getKey() + ": " + nf.format(val);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    FontMetrics fm = g2.getFontMetrics();
                    int tw = fm.stringWidth(tooltip) + 16;
                    int th = 24;
                    int tx = bx + bw / 2 - tw / 2;
                    int ty = by - 30;
                    
                    g2.setColor(new Color(50, 50, 50, 230));
                    g2.fill(new RoundRectangle2D.Float(tx, ty, tw, th, 6, 6));
                    g2.setColor(Color.WHITE);
                    g2.drawString(tooltip, tx + 8, ty + 16);
                }
                
                // Label da categoria
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String label = entries.get(i).getKey();
                if (label.length() > 10) label = label.substring(0, 8) + "...";
                FontMetrics fm = g2.getFontMetrics();
                int lx = bx + (bw - fm.stringWidth(label)) / 2;
                g2.drawString(label, lx, y + h + 15);
            }
            
            g2.dispose();
        }
    }

    // ==================== GRÁFICO DE LINHA MODERNO ====================
    private class ModernLineChart extends JPanel {
        private Map<String, BigDecimal> data = new LinkedHashMap<>();
        @SuppressWarnings("unused")
        private final String title;
        private int hoveredPoint = -1;
        private float animationProgress = 0;
        private javax.swing.Timer animationTimer;
        
        public ModernLineChart(String title) {
            this.title = title;
            setOpaque(false);
            setPreferredSize(new Dimension(400, 250));
            
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int newHovered = getPointAt(e.getX(), e.getY());
                    if (newHovered != hoveredPoint) {
                        hoveredPoint = newHovered;
                        repaint();
                    }
                }
            });
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hoveredPoint = -1;
                    repaint();
                }
            });
        }
        
        public void setData(Map<String, BigDecimal> data) {
            this.data = data;
            this.animationProgress = 0;
            startAnimation();
        }
        
        private void startAnimation() {
            if (animationTimer != null) animationTimer.stop();
            
            animationTimer = new javax.swing.Timer(16, e -> {
                animationProgress = Math.min(1.0f, animationProgress + 0.03f);
                repaint();
                if (animationProgress >= 1.0f) ((javax.swing.Timer) e.getSource()).stop();
            });
            animationTimer.start();
        }
        
        private int getPointAt(int mx, int my) {
            if (data.isEmpty()) return -1;
            
            int w = getWidth() - 80;
            int h = getHeight() - 60;
            int x = 60;
            int y = 30;
            
            List<Map.Entry<String, BigDecimal>> entries = new ArrayList<>(data.entrySet());
            int n = entries.size();
            if (n == 0) return -1;
            
            int step = n > 1 ? w / (n - 1) : w;
            
            for (int i = 0; i < n; i++) {
                int px = x + i * step;
                if (Math.abs(mx - px) < 15) {
                    return i;
                }
            }
            return -1;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            
            int w = getWidth() - 80;
            int h = getHeight() - 60;
            int x = 60;
            int y = 30;
            
            // Grade
            g2.setColor(GRID_COLOR);
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{5, 5}, 0));
            for (int i = 0; i <= 5; i++) {
                int ly = y + (h * i / 5);
                g2.drawLine(x, ly, x + w, ly);
            }
            
            if (data.isEmpty()) {
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2.drawString("Sem dados disponíveis", w / 2 - 50, h / 2);
                g2.dispose();
                return;
            }
            
            List<Map.Entry<String, BigDecimal>> entries = new ArrayList<>(data.entrySet());
            int n = entries.size();
            
            BigDecimal maxVal = BigDecimal.ZERO;
            for (var e : entries) maxVal = maxVal.max(e.getValue());
            if (maxVal.compareTo(BigDecimal.ZERO) == 0) maxVal = BigDecimal.ONE;
            
            int step = n > 1 ? w / (n - 1) : w;
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            
            // Eixo Y labels
            g2.setColor(TEXT_SECONDARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int i = 0; i <= 5; i++) {
                BigDecimal val = maxVal.multiply(BigDecimal.valueOf(5 - i)).divide(BigDecimal.valueOf(5), RoundingMode.HALF_UP);
                String label = formatShortCurrency(val);
                int ly = y + (h * i / 5);
                g2.drawString(label, 5, ly + 4);
            }
            
            // Calcular pontos
            int[] px = new int[n];
            int[] py = new int[n];
            for (int i = 0; i < n; i++) {
                px[i] = x + i * step;
                int baseY = y + h - entries.get(i).getValue().multiply(BigDecimal.valueOf(h)).divide(maxVal, RoundingMode.HALF_UP).intValue();
                py[i] = (int) (y + h - (y + h - baseY) * animationProgress);
            }
            
            // Área preenchida com gradiente
            Path2D area = new Path2D.Float();
            area.moveTo(px[0], y + h);
            for (int i = 0; i < n; i++) {
                area.lineTo(px[i], py[i]);
            }
            area.lineTo(px[n - 1], y + h);
            area.closePath();
            
            GradientPaint areaGradient = new GradientPaint(0, y, new Color(102, 126, 234, 100), 0, y + h, new Color(102, 126, 234, 20));
            g2.setPaint(areaGradient);
            g2.fill(area);
            
            // Linha principal
            g2.setColor(new Color(102, 126, 234));
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 1; i < n; i++) {
                g2.drawLine(px[i - 1], py[i - 1], px[i], py[i]);
            }
            
            // Pontos
            for (int i = 0; i < n; i++) {
                // Ponto externo
                g2.setColor(new Color(102, 126, 234));
                g2.fillOval(px[i] - 6, py[i] - 6, 12, 12);
                // Ponto interno
                g2.setColor(Color.WHITE);
                g2.fillOval(px[i] - 4, py[i] - 4, 8, 8);
                
                // Highlight se hover
                if (i == hoveredPoint) {
                    g2.setColor(new Color(102, 126, 234, 50));
                    g2.fillOval(px[i] - 12, py[i] - 12, 24, 24);
                    
                    // Tooltip
                    String tooltip = entries.get(i).getKey() + ": " + nf.format(entries.get(i).getValue());
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    FontMetrics fm = g2.getFontMetrics();
                    int tw = fm.stringWidth(tooltip) + 16;
                    int th = 24;
                    int tx = px[i] - tw / 2;
                    int ty = py[i] - 35;
                    
                    g2.setColor(new Color(50, 50, 50, 230));
                    g2.fill(new RoundRectangle2D.Float(tx, ty, tw, th, 6, 6));
                    g2.setColor(Color.WHITE);
                    g2.drawString(tooltip, tx + 8, ty + 16);
                }
                
                // Label do mês
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String label = entries.get(i).getKey();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, px[i] - fm.stringWidth(label) / 2, y + h + 18);
            }
            
            g2.dispose();
        }
    }

    // ==================== GRÁFICO DONUT MODERNO ====================
    private class ModernDonutChart extends JPanel {
        private Map<String, BigDecimal> data = new LinkedHashMap<>();
        @SuppressWarnings("unused")
        private final String title;
        private int hoveredSlice = -1;
        private float animationProgress = 0;
        private javax.swing.Timer animationTimer;
        
        public ModernDonutChart(String title) {
            this.title = title;
            setOpaque(false);
            setPreferredSize(new Dimension(400, 200));
            
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int newHovered = getSliceAt(e.getX(), e.getY());
                    if (newHovered != hoveredSlice) {
                        hoveredSlice = newHovered;
                        repaint();
                    }
                }
            });
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hoveredSlice = -1;
                    repaint();
                }
            });
        }
        
        public void setData(Map<String, BigDecimal> data) {
            this.data = data;
            this.animationProgress = 0;
            startAnimation();
        }
        
        private void startAnimation() {
            if (animationTimer != null) animationTimer.stop();
            
            animationTimer = new javax.swing.Timer(16, e -> {
                animationProgress = Math.min(1.0f, animationProgress + 0.025f);
                repaint();
                if (animationProgress >= 1.0f) ((javax.swing.Timer) e.getSource()).stop();
            });
            animationTimer.start();
        }
        
        @SuppressWarnings("unused")
        private int getSliceAt(int mx, int my) {
            // Implementação simplificada
            return -1;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth();
            int h = getHeight();
            int size = Math.min(w - 150, h - 40);
            int x = 20;
            int y = (h - size) / 2;
            
            if (data.isEmpty()) {
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2.drawString("Sem dados", w / 2 - 30, h / 2);
                g2.dispose();
                return;
            }
            
            // Calcular total
            BigDecimal total = BigDecimal.ZERO;
            for (var v : data.values()) total = total.add(v);
            if (total.compareTo(BigDecimal.ZERO) == 0) total = BigDecimal.ONE;
            
            // Ordenar
            List<Map.Entry<String, BigDecimal>> entries = new ArrayList<>(data.entrySet());
            entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            int max = Math.min(entries.size(), 6);
            
            // Desenhar arcos
            double startAngle = 90;
            int donutThickness = size / 4;
            
            for (int i = 0; i < max; i++) {
                double percent = entries.get(i).getValue().doubleValue() / total.doubleValue();
                double arcAngle = 360 * percent * animationProgress;
                
                Color color = CHART_COLORS[i % CHART_COLORS.length];
                g2.setColor(color);
                
                Arc2D arc = new Arc2D.Double(x, y, size, size, startAngle, -arcAngle, Arc2D.PIE);
                g2.fill(arc);
                
                startAngle -= arcAngle;
            }
            
            // Centro branco (donut)
            int innerSize = size - donutThickness * 2;
            int innerX = x + donutThickness;
            int innerY = y + donutThickness;
            g2.setColor(CARD_BG);
            g2.fillOval(innerX, innerY, innerSize, innerSize);
            
            // Texto central
            g2.setColor(TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            String centerText = "Total";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(centerText, x + size / 2 - fm.stringWidth(centerText) / 2, y + size / 2 - 5);
            
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            String totalText = formatShortCurrency(total);
            fm = g2.getFontMetrics();
            g2.drawString(totalText, x + size / 2 - fm.stringWidth(totalText) / 2, y + size / 2 + 15);
            
            // Legenda
            int legendX = x + size + 20;
            int legendY = y + 20;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            
            for (int i = 0; i < max; i++) {
                Color color = CHART_COLORS[i % CHART_COLORS.length];
                g2.setColor(color);
                g2.fillRoundRect(legendX, legendY + i * 25, 12, 12, 3, 3);
                
                g2.setColor(TEXT_PRIMARY);
                String name = entries.get(i).getKey();
                if (name.length() > 12) name = name.substring(0, 10) + "...";
                double pct = entries.get(i).getValue().doubleValue() / total.doubleValue() * 100;
                g2.drawString(String.format("%s (%.1f%%)", name, pct), legendX + 18, legendY + i * 25 + 10);
            }
            
            g2.dispose();
        }
    }

    // ==================== GRÁFICO DE ÁREA MODERNO ====================
    private class ModernAreaChart extends JPanel {
        private Map<String, BigDecimal> data = new LinkedHashMap<>();
        @SuppressWarnings("unused")
        private final String title;
        private float animationProgress = 0;
        private javax.swing.Timer animationTimer;
        
        public ModernAreaChart(String title) {
            this.title = title;
            setOpaque(false);
            setPreferredSize(new Dimension(400, 200));
        }
        
        public void setData(Map<String, BigDecimal> data) {
            // Converter para acumulado
            Map<String, BigDecimal> accumulated = new LinkedHashMap<>();
            BigDecimal sum = BigDecimal.ZERO;
            for (var e : data.entrySet()) {
                sum = sum.add(e.getValue());
                accumulated.put(e.getKey(), sum);
            }
            this.data = accumulated;
            this.animationProgress = 0;
            startAnimation();
        }
        
        private void startAnimation() {
            if (animationTimer != null) animationTimer.stop();
            
            animationTimer = new javax.swing.Timer(16, e -> {
                animationProgress = Math.min(1.0f, animationProgress + 0.03f);
                repaint();
                if (animationProgress >= 1.0f) ((javax.swing.Timer) e.getSource()).stop();
            });
            animationTimer.start();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth() - 80;
            int h = getHeight() - 50;
            int x = 60;
            int y = 20;
            
            // Grade
            g2.setColor(GRID_COLOR);
            g2.setStroke(new BasicStroke(1));
            for (int i = 0; i <= 4; i++) {
                int ly = y + (h * i / 4);
                g2.drawLine(x, ly, x + w, ly);
            }
            
            if (data.isEmpty()) {
                g2.setColor(TEXT_SECONDARY);
                g2.drawString("Sem dados", w / 2, h / 2);
                g2.dispose();
                return;
            }
            
            List<Map.Entry<String, BigDecimal>> entries = new ArrayList<>(data.entrySet());
            int n = entries.size();
            
            BigDecimal maxVal = BigDecimal.ZERO;
            for (var e : entries) maxVal = maxVal.max(e.getValue());
            if (maxVal.compareTo(BigDecimal.ZERO) == 0) maxVal = BigDecimal.ONE;
            
            int step = n > 1 ? w / (n - 1) : w;
            
            // Calcular pontos
            int[] px = new int[n];
            int[] py = new int[n];
            for (int i = 0; i < n; i++) {
                px[i] = x + i * step;
                int baseY = y + h - entries.get(i).getValue().multiply(BigDecimal.valueOf(h)).divide(maxVal, RoundingMode.HALF_UP).intValue();
                py[i] = (int) (y + h - (y + h - baseY) * animationProgress);
            }
            
            // Área com gradiente verde
            Path2D area = new Path2D.Float();
            area.moveTo(px[0], y + h);
            for (int i = 0; i < n; i++) {
                area.lineTo(px[i], py[i]);
            }
            area.lineTo(px[n - 1], y + h);
            area.closePath();
            
            GradientPaint gradient = new GradientPaint(0, y, new Color(76, 175, 80, 150), 0, y + h, new Color(76, 175, 80, 30));
            g2.setPaint(gradient);
            g2.fill(area);
            
            // Linha
            g2.setColor(SUCCESS);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 1; i < n; i++) {
                g2.drawLine(px[i - 1], py[i - 1], px[i], py[i]);
            }
            
            // Pontos e labels
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            for (int i = 0; i < n; i++) {
                g2.setColor(SUCCESS);
                g2.fillOval(px[i] - 4, py[i] - 4, 8, 8);
                g2.setColor(Color.WHITE);
                g2.fillOval(px[i] - 2, py[i] - 2, 4, 4);
                
                g2.setColor(TEXT_SECONDARY);
                String label = entries.get(i).getKey();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, px[i] - fm.stringWidth(label) / 2, y + h + 15);
            }
            
            // Eixo Y
            g2.setColor(TEXT_SECONDARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            for (int i = 0; i <= 4; i++) {
                BigDecimal val = maxVal.multiply(BigDecimal.valueOf(4 - i)).divide(BigDecimal.valueOf(4), RoundingMode.HALF_UP);
                g2.drawString(formatShortCurrency(val), 5, y + (h * i / 4) + 4);
            }
            
            g2.dispose();
        }
    }
    
    // Utilitário para formatar valores curtos
    private static String formatShortCurrency(BigDecimal val) {
        if (val.compareTo(BigDecimal.valueOf(1000000)) >= 0) {
            return String.format("R$%.1fM", val.doubleValue() / 1000000);
        } else if (val.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            return String.format("R$%.1fK", val.doubleValue() / 1000);
        } else {
            return String.format("R$%.0f", val.doubleValue());
        }
    }
}
