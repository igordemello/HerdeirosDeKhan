package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Jogo;

/**
 * Área lateral da janela do jogo: mostra o jogador da vez e o tabuleiro dele.
 *
 * <ul>
 * <li>o fundo da área é pintado com a cor do jogador da vez (Figura 4 do
 * enunciado);</li>
 * <li>o tabuleiro do jogador da vez é desenhado com {@code drawImage()}, e as
 * fichas de ativação colocadas aparecem sobre as colunas;</li>
 * <li>um JComboBox e um botão permitem escolher a coluna a ativar (também
 * é possível clicar na coluna do tabuleiro);</li>
 * <li>um JComboBox permite ao testador definir o número de movimentos.</li>
 * </ul>
 *
 * Quando o jogador ativa uma coluna ou muda os movimentos, o painel avisa a
 * janela com {@code firePropertyChange("movimentos", ...)}, para que o
 * tabuleiro principal saiba quantos movimentos o peão tem. Quando a vez
 * mudar, quem passou a vez deve chamar {@link #atualizar()}.
 *
 * Os eventos são tratados por classes internas (sem expressões lambda),
 * como pede o enunciado.
 */
public class PainelLateral extends JPanel {
	private static final long serialVersionUID = 1L;

	/** Largura sugerida do painel, em pixels. */
	public static final int LARGURA = 420;
	/** Maior número de movimentos oferecido no JComboBox de teste. */
	private static final int MAX_MOVIMENTOS_TESTE = 12;
	private static final int MARGEM = 16;
	/** Altura da parte desenhada (cabeçalho, tabuleiro e resumo); os controles ficam abaixo. */
	private static final int ALTURA_DESENHO = 475;

	/** Cor de fundo de cada jogador (chave = nome da cor no Model). */
	private static final Map<String, Color> CORES = new HashMap<String, Color>();
	static {
		CORES.put("AZUL", new Color(36, 92, 196));
		CORES.put("VERMELHO", new Color(196, 38, 38));
		CORES.put("VERDE", new Color(34, 140, 64));
		CORES.put("AMARELO", new Color(236, 196, 40));
		CORES.put("ROXO", new Color(118, 58, 168));
	}

	/** Nome das colunas para exibição (chave = nome no Model). */
	private static final Map<String, String> NOMES_COLUNAS = new HashMap<String, String>();
	static {
		NOMES_COLUNAS.put("AMARELA", "Amarela");
		NOMES_COLUNAS.put("VERDE", "Verde");
		NOMES_COLUNAS.put("VERMELHA", "Vermelha");
		NOMES_COLUNAS.put("BRANCA", "Branca");
	}

	/** Abreviação das melhorias desenhadas sobre os espaços das colunas. */
	private static final Map<String, String> NOMES_MELHORIAS = new HashMap<String, String>();
	static {
		NOMES_MELHORIAS.put("MOEDA_VIRTUAL", "moeda\nvirtual");
		NOMES_MELHORIAS.put("TESOURO_VIRTUAL_BOI", "boi\nvirtual");
		NOMES_MELHORIAS.put("TESOURO_VIRTUAL_ESPECIARIA", "especiaria\nvirtual");
		NOMES_MELHORIAS.put("TESOURO_VIRTUAL_FERRO", "ferro\nvirtual");
		NOMES_MELHORIAS.put("TESOURO_VIRTUAL_PELE", "pele\nvirtual");
		NOMES_MELHORIAS.put("TESOURO_VIRTUAL_SEDA", "seda\nvirtual");
		NOMES_MELHORIAS.put("ESPADA_VIRTUAL", "espada\nvirtual");
		NOMES_MELHORIAS.put("YURT_VIRTUAL", "yurt\nvirtual");
		NOMES_MELHORIAS.put("PUNHO", "+1 punho");
		NOMES_MELHORIAS.put("DOIS_CAVALOS", "+2\ncavalos");
	}

	private final Jogo jogo;
	private final List<String> colunas;

	private final JComboBox<String> comboColunas = new JComboBox<String>();
	private final JButton botaoAtivar = new JButton("Ativar coluna");
	private final JComboBox<Integer> comboMovimentos = new JComboBox<Integer>();
	private final JLabel rotuloColuna = new JLabel("Coluna:");
	private final JLabel rotuloMovimentos = new JLabel("Movimentos (teste):");

	/** Evita que as mudanças feitas pelo próprio painel nos JComboBox disparem eventos. */
	private boolean atualizandoComponentes = false;

	/** Onde o tabuleiro foi desenhado na última pintura (para tratar cliques). */
	private Rectangle areaTabuleiro = new Rectangle();

	public PainelLateral(Jogo jogo) {
		if (jogo == null) {
			throw new IllegalArgumentException("Jogo nulo");
		}
		this.jogo = jogo;
		this.colunas = jogo.getColunas();

		setPreferredSize(new Dimension(LARGURA, 700));
		setLayout(new BorderLayout());
		// os controles ficam logo abaixo da área desenhada em paintComponent
		add(Box.createVerticalStrut(ALTURA_DESENHO), BorderLayout.NORTH);
		JPanel envoltorio = new JPanel(new BorderLayout());
		envoltorio.setOpaque(false);
		envoltorio.add(criarPainelDeControles(), BorderLayout.NORTH);
		add(envoltorio, BorderLayout.CENTER);

		for (int i = 0; i <= MAX_MOVIMENTOS_TESTE; i++) {
			comboMovimentos.addItem(i);
		}
		comboColunas.setRenderer(new RenderizadorColuna());

		botaoAtivar.addActionListener(new TratadorAtivarColuna());
		comboMovimentos.addActionListener(new TratadorMovimentos());
		addMouseListener(new TratadorCliqueNoTabuleiro());

		atualizar();
	}

	// --------------------------------------------------------------- estado

	/**
	 * Lê de novo o estado do jogo e redesenha o painel. Deve ser chamado
	 * sempre que a vez mudar ou o tabuleiro do jogador for alterado fora
	 * deste painel.
	 */
	public void atualizar() {
		atualizandoComponentes = true;
		try {
			DefaultComboBoxModel<String> modelo = new DefaultComboBoxModel<String>();
			for (String coluna : colunas) {
				if (jogo.podeAtivarColuna(coluna)) {
					modelo.addElement(coluna);
				}
			}
			comboColunas.setModel(modelo);
			boolean podeAtivar = modelo.getSize() > 0;
			comboColunas.setEnabled(podeAtivar);
			botaoAtivar.setEnabled(podeAtivar);
			comboMovimentos.setSelectedItem(jogo.getMovimentosDaVez());

			Color texto = corDoTexto(corDoJogador());
			rotuloColuna.setForeground(texto);
			rotuloMovimentos.setForeground(texto);
		} finally {
			atualizandoComponentes = false;
		}
		repaint();
	}

	private boolean ativarColuna(String coluna) {
		int antes = jogo.getMovimentosDaVez();
		if (coluna == null || !jogo.ativarColuna(coluna)) {
			return false;
		}
		atualizar();
		firePropertyChange("movimentos", antes, jogo.getMovimentosDaVez());
		return true;
	}

	// ------------------------------------------------------------- desenho

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		String cor = jogo.getJogadorDaVez();
		Color fundo = corDoJogador();
		Color texto = corDoTexto(fundo);

		// fundo com a cor do jogador da vez
		g2.setColor(fundo);
		g2.fillRect(0, 0, getWidth(), getHeight());

		// cabeçalho
		int y = MARGEM;
		g2.setColor(texto);
		g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
		FontMetrics fm = g2.getFontMetrics();
		y += fm.getAscent();
		g2.drawString("Jogador da vez", MARGEM, y);
		g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
		fm = g2.getFontMetrics();
		y += fm.getAscent() + 2;
		g2.drawString(cor, MARGEM, y);
		g2.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 16));
		fm = g2.getFontMetrics();
		y += fm.getHeight() + 2;
		g2.drawString("Herdeiro: " + jogo.getHerdeiro(cor), MARGEM, y);
		y += 14;

		// tabuleiro do jogador
		int largura = LARGURA - 2 * MARGEM; // tamanho fixo: os controles ficam logo abaixo
		int altura = (int) Math.round(largura / ImagensTabuleiroJogador.PROPORCAO);
		areaTabuleiro = new Rectangle(MARGEM, y, largura, altura);
		Image imagem = ImagensTabuleiroJogador.getImagem(jogo.getHerdeiro(cor), descricoesBase(cor));
		g2.drawImage(imagem, areaTabuleiro.x, areaTabuleiro.y, areaTabuleiro.width, areaTabuleiro.height, this);
		desenharMelhorias(g2, cor);
		desenharFichas(g2, cor, fundo);
		y += altura + 18;

		// resumo do turno
		y = desenharResumoDoTurno(g2, cor, texto, y);
		desenharCavalosPorColuna(g2, cor, texto, y + 8);
		g2.dispose();
	}

	/** Texto das ações iniciais de cada coluna, usado na imagem provisória. */
	private String[] descricoesBase(String cor) {
		String[] textos = new String[colunas.size()];
		for (int i = 0; i < colunas.size(); i++) {
			String c = colunas.get(i);
			StringBuilder sb = new StringBuilder();
			sb.append(jogo.getCavalosDaColuna(cor, c) - cavalosDeMelhorias(cor, c)).append(" cavalo(s)");
			int punhos = jogo.getPunhosDaColuna(cor, c) - punhosDeMelhorias(cor, c);
			if (punhos > 0) {
				sb.append('\n').append(punhos).append(punhos == 1 ? " punho" : " punhos");
			}
			if (jogo.getMoedasVirtuaisDaColuna(cor, c) > 0) {
				sb.append("\nmoeda virt.");
			}
			if (jogo.colunaUsaKhan(cor, c)) {
				sb.append("\nKhan");
			}
			if ("BRANCA".equals(c)) {
				sb.append("\nrecupera");
			}
			textos[i] = sb.toString();
		}
		return textos;
	}

	private int cavalosDeMelhorias(String cor, String coluna) {
		int n = 0;
		for (String m : jogo.getMelhoriasDaColuna(cor, coluna)) {
			if ("DOIS_CAVALOS".equals(m)) {
				n += 2;
			}
		}
		return n;
	}

	private int punhosDeMelhorias(String cor, String coluna) {
		int n = 0;
		for (String m : jogo.getMelhoriasDaColuna(cor, coluna)) {
			if ("PUNHO".equals(m)) {
				n++;
			}
		}
		return n;
	}

	/**
	 * Desenha as peças de melhoria de cada coluna sobre os espaços do
	 * tabuleiro, de baixo para cima, com a imagem da peça (pelo id dado pelo
	 * Model, ex.: "CAV-V"). Sem a imagem, desenha um cartão com o nome.
	 */
	private void desenharMelhorias(Graphics2D g2, String cor) {
		for (int i = 0; i < colunas.size(); i++) {
			List<String> melhorias = jogo.getMelhoriasDaColuna(cor, colunas.get(i));
			List<String> ids = jogo.getIdsDasMelhoriasDaColuna(cor, colunas.get(i));
			for (int j = 0; j < melhorias.size() && j < ImagensTabuleiroJogador.Y_ESPACO.length; j++) {
				Rectangle r = celula(i, ImagensTabuleiroJogador.Y_ESPACO[j]);
				Image peca = ImagensTabuleiroJogador.getMelhoria(ids.get(j));
				if (peca != null) {
					g2.drawImage(peca, r.x, r.y, r.width, r.height, this);
					continue;
				}
				g2.setColor(new Color(255, 248, 225));
				g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 8, 8));
				g2.setColor(new Color(120, 90, 40));
				g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 8, 8));
				String nome = NOMES_MELHORIAS.get(melhorias.get(j));
				ImagensTabuleiroJogador.desenharTextoCentralizado(g2, nome == null ? melhorias.get(j) : nome,
						r.x, r.y, r.width, r.height, new Color(70, 50, 20));
			}
		}
	}

	/** Desenha as fichas de ativação e destaca a coluna ativada neste turno. */
	private void desenharFichas(Graphics2D g2, String cor, Color corJogador) {
		String ativada = jogo.getColunaAtivadaNaVez();
		for (int i = 0; i < colunas.size(); i++) {
			String coluna = colunas.get(i);
			Rectangle base = celula(i, ImagensTabuleiroJogador.Y_BASE);

			if (coluna.equals(ativada)) {
				Rectangle col = new Rectangle(base.x - 3,
						areaTabuleiro.y + (int) (ImagensTabuleiroJogador.Y_ESPACO[3] * areaTabuleiro.height) - 3,
						base.width + 6, base.y + base.height - areaTabuleiro.y
								- (int) (ImagensTabuleiroJogador.Y_ESPACO[3] * areaTabuleiro.height) + 6);
				g2.setStroke(new BasicStroke(3.5f));
				g2.setColor(Color.WHITE);
				g2.draw(new RoundRectangle2D.Double(col.x, col.y, col.width, col.height, 12, 12));
				g2.setColor(Color.BLACK);
				g2.setStroke(new BasicStroke(1.2f));
				g2.draw(new RoundRectangle2D.Double(col.x - 2.5, col.y - 2.5, col.width + 5, col.height + 5, 14, 14));
			}

			if (jogo.colunaOcupada(cor, coluna)) {
				double d = Math.min(base.width, base.height) * 0.85;
				double cx = base.getCenterX() - d / 2;
				double cy = base.getCenterY() - d / 2;
				Ellipse2D circulo = new Ellipse2D.Double(cx, cy, d, d);
				g2.setColor(new Color(0, 0, 0, 90));
				g2.fill(new Ellipse2D.Double(cx + 2, cy + 3, d, d));

				Image ficha = ImagensTabuleiroJogador.getFicha(cor);
				if (ficha != null) {
					// a imagem da ficha é quadrada com fundo branco: recorta o círculo
					java.awt.Shape recorteAnterior = g2.getClip();
					g2.clip(circulo);
					double margem = d * 0.04; // descarta a borda branca da foto
					g2.drawImage(ficha, (int) Math.round(cx - margem), (int) Math.round(cy - margem),
							(int) Math.round(d + 2 * margem), (int) Math.round(d + 2 * margem), this);
					g2.setClip(recorteAnterior);
					g2.setStroke(new BasicStroke(1.5f));
					g2.setColor(new Color(60, 60, 60));
					g2.draw(circulo);
				} else {
					g2.setColor(corJogador);
					g2.fill(circulo);
					g2.setStroke(new BasicStroke(2.5f));
					g2.setColor(Color.WHITE);
					g2.draw(circulo);
				}
			}
		}
	}

	private int desenharResumoDoTurno(Graphics2D g2, String cor, Color texto, int y) {
		String ativada = jogo.getColunaAtivadaNaVez();
		g2.setColor(texto);
		g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
		FontMetrics fm = g2.getFontMetrics();
		y += fm.getAscent();
		g2.drawString(ativada == null ? "Escolha uma coluna para ativar"
				: "Coluna ativada: " + NOMES_COLUNAS.get(ativada), MARGEM, y);

		// caixa com o número de movimentos (o equivalente aos dados)
		y += 12;
		int lado = 76;
		g2.setColor(Color.WHITE);
		g2.fill(new RoundRectangle2D.Double(MARGEM, y, lado, lado, 16, 16));
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(2f));
		g2.draw(new RoundRectangle2D.Double(MARGEM, y, lado, lado, 16, 16));
		g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 44));
		fm = g2.getFontMetrics();
		String mov = String.valueOf(jogo.getMovimentosDaVez());
		g2.drawString(mov, MARGEM + (lado - fm.stringWidth(mov)) / 2,
				y + (lado - fm.getHeight()) / 2 + fm.getAscent());

		g2.setColor(texto);
		g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		g2.drawString("movimentos", MARGEM + lado + 14, y + 32);
		g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
		g2.drawString("Fichas livres: " + jogo.getFichasAtivacaoDisponiveis(cor) + " de 4", MARGEM + lado + 14,
				y + 56);
		return y + lado;
	}

	private void desenharCavalosPorColuna(Graphics2D g2, String cor, Color texto, int y) {
		g2.setColor(texto);
		g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
		FontMetrics fm = g2.getFontMetrics();
		y += fm.getAscent() + 6;
		StringBuilder sb = new StringBuilder("Cavalos: ");
		for (int i = 0; i < colunas.size(); i++) {
			String c = colunas.get(i);
			if (i > 0) {
				sb.append("  ·  ");
			}
			sb.append(NOMES_COLUNAS.get(c)).append(' ').append(jogo.getCavalosDaColuna(cor, c));
		}
		g2.drawString(sb.toString(), MARGEM, y);
	}

	/** Retângulo de uma célula da coluna i, na linha cujo topo é yFrac. */
	private Rectangle celula(int coluna, double yFrac) {
		int x = areaTabuleiro.x + (int) Math.round(ImagensTabuleiroJogador.X_COLUNA[coluna] * areaTabuleiro.width);
		int y = areaTabuleiro.y + (int) Math.round(yFrac * areaTabuleiro.height);
		int w = (int) Math.round(ImagensTabuleiroJogador.LARGURA_COLUNA * areaTabuleiro.width);
		int h = (int) Math.round(ImagensTabuleiroJogador.ALTURA_LINHA * areaTabuleiro.height);
		return new Rectangle(x, y, w, h);
	}

	/** Coluna do tabuleiro que contém o ponto, ou -1. */
	private int colunaNoPonto(int px, int py) {
		if (!areaTabuleiro.contains(px, py)) {
			return -1;
		}
		for (int i = 0; i < colunas.size(); i++) {
			Rectangle base = celula(i, ImagensTabuleiroJogador.Y_BASE);
			int topo = areaTabuleiro.y + (int) (ImagensTabuleiroJogador.Y_ESPACO[3] * areaTabuleiro.height);
			if (px >= base.x && px <= base.x + base.width && py >= topo && py <= base.y + base.height) {
				return i;
			}
		}
		return -1;
	}

	private Color corDoJogador() {
		Color c = CORES.get(jogo.getJogadorDaVez());
		return c == null ? Color.GRAY : c;
	}

	/** Preto sobre fundos claros (amarelo), branco sobre os escuros. */
	private static Color corDoTexto(Color fundo) {
		double luminancia = 0.299 * fundo.getRed() + 0.587 * fundo.getGreen() + 0.114 * fundo.getBlue();
		return luminancia > 160 ? Color.BLACK : Color.WHITE;
	}

	// ----------------------------------------------------------- controles

	private JPanel criarPainelDeControles() {
		JPanel controles = new JPanel(new GridLayout(2, 1, 0, 6));
		controles.setOpaque(false);
		controles.setBorder(BorderFactory.createEmptyBorder(0, MARGEM - 6, MARGEM, MARGEM));

		JPanel linhaColuna = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		linhaColuna.setOpaque(false);
		linhaColuna.add(rotuloColuna);
		comboColunas.setPreferredSize(new Dimension(150, 28));
		linhaColuna.add(comboColunas);
		linhaColuna.add(botaoAtivar);

		JPanel linhaMovimentos = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		linhaMovimentos.setOpaque(false);
		linhaMovimentos.add(rotuloMovimentos);
		comboMovimentos.setPreferredSize(new Dimension(70, 28));
		linhaMovimentos.add(comboMovimentos);

		controles.add(linhaColuna);
		controles.add(linhaMovimentos);
		return controles;
	}

	// ------------------------------------------------- tratadores de eventos

	/** Botão "Ativar coluna": ativa a coluna escolhida no JComboBox. */
	private class TratadorAtivarColuna implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			ativarColuna((String) comboColunas.getSelectedItem());
		}
	}

	/** JComboBox de teste: define à mão o número de movimentos do turno. */
	private class TratadorMovimentos implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (atualizandoComponentes) {
				return;
			}
			Integer valor = (Integer) comboMovimentos.getSelectedItem();
			if (valor == null) {
				return;
			}
			int antes = jogo.getMovimentosDaVez();
			jogo.definirMovimentosDaVez(valor);
			repaint();
			firePropertyChange("movimentos", antes, valor.intValue());
		}
	}

	/** Clique numa coluna do tabuleiro desenhado: ativa a coluna, se puder. */
	private class TratadorCliqueNoTabuleiro extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			int i = colunaNoPonto(e.getX(), e.getY());
			if (i >= 0 && jogo.podeAtivarColuna(colunas.get(i))) {
				ativarColuna(colunas.get(i));
			}
		}
	}

	/** Mostra os nomes das colunas com a inicial maiúscula e o número de cavalos. */
	private class RenderizadorColuna extends javax.swing.DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> lista, Object valor,
				int indice, boolean selecionado, boolean comFoco) {
			String texto = "";
			if (valor != null) {
				String coluna = (String) valor;
				texto = NOMES_COLUNAS.get(coluna) + " ("
						+ jogo.getCavalosDaColuna(jogo.getJogadorDaVez(), coluna) + " cav.)";
			}
			return super.getListCellRendererComponent(lista, texto, indice, selecionado, comFoco);
		}
	}
}
