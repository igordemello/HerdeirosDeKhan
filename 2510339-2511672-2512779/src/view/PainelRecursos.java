package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import model.Jogo;

/**
 * Parte de baixo da área lateral: votos, peças de tributo, tesouros e peças
 * de bônus do jogador da vez, desenhados com {@code drawImage()}, e os
 * botões que abrem as janelas de melhorias, de entregas e de cartas.
 * Clicar numa peça de bônus usa a peça.
 */
class PainelRecursos extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final String[] TRIBUTOS = { "ESPADA", "YURT", "MOEDA" };
	private static final Color FUNDO = new Color(255, 255, 255, 232);
	private static final Color TINTA = new Color(34, 30, 40);
	private static final Color AVISO = new Color(170, 72, 0);

	private final Jogo jogo;
	private final PainelLateral lateral;
	private final Desenho desenho = new Desenho();
	private final JButton botaoMelhorias = new JButton("Melhorias");
	private final JButton botaoEntregas = new JButton("Entregar tesouros");
	private final JButton botaoCartas = new JButton("Cartas");

	PainelRecursos(Jogo jogo, PainelLateral lateral) {
		this.jogo = jogo;
		this.lateral = lateral;
		setOpaque(false);
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(4, 16, 12, 16));

		JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
		botoes.setOpaque(false);
		botoes.add(botaoMelhorias);
		botoes.add(botaoEntregas);
		botoes.add(botaoCartas);
		botaoMelhorias.addActionListener(new TratadorAbrir(TratadorAbrir.MELHORIAS));
		botaoEntregas.addActionListener(new TratadorAbrir(TratadorAbrir.ENTREGAS));
		botaoCartas.addActionListener(new TratadorAbrir(TratadorAbrir.CARTAS));

		add(desenho, BorderLayout.CENTER);
		add(botoes, BorderLayout.SOUTH);
	}

	void atualizar() {
		botaoEntregas.setEnabled(!jogo.getConselheirosAlcancaveis().isEmpty());
		desenho.repaint();
	}

	/** Área desenhada: votos, tributos, tesouros, bônus e avisos. */
	private class Desenho extends JComponent {
		private static final long serialVersionUID = 1L;
		/** Onde cada peça de bônus foi desenhada (para tratar cliques). */
		private final List<Rectangle> areasBonus = new ArrayList<Rectangle>();
		private final List<String> tiposBonus = new ArrayList<String>();

		Desenho() {
			addMouseListener(new TratadorCliqueBonus());
			addMouseMotionListener(new TratadorCursor());
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			String cor = jogo.getJogadorDaVez();
			int w = getWidth();

			g2.setColor(FUNDO);
			g2.fill(new RoundRectangle2D.Double(0, 0, w, getHeight(), 14, 14));

			// votos numa linha só (antes eram duas e cortava o fim do painel)
			int y = 8;
			g2.setColor(TINTA);
			g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
			FontMetrics fm = g2.getFontMetrics();
			g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
			FontMetrics fmGrande = g2.getFontMetrics();
			int base = y + fmGrande.getAscent();
			g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
			String rotuloVotos = "Votos no Kurultai:";
			g2.drawString(rotuloVotos, 12, base);
			g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
			g2.drawString(String.valueOf(jogo.getVotos(cor)), 18 + fm.stringWidth(rotuloVotos), base);
			g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
			fm = g2.getFontMetrics();
			String yurts = "Yurts na reserva: " + jogo.getYurtsNaReserva(cor);
			g2.drawString(yurts, w - 12 - fm.stringWidth(yurts), base);
			y = base + 8;

			// tributos
			y = rotulo(g2, "Peças de tributo", y);
			int x = 12;
			for (String tipo : TRIBUTOS) {
				int reais = jogo.getTributo(cor, tipo);
				int virtuais = jogo.getTributoDisponivelDaVez(tipo) - reais;
				String texto = "×" + reais + (virtuais > 0 ? " +" + virtuais + " virt." : "");
				x = peca(g2, ImagensTabuleiroJogador.getTributo(tipo), Textos.tributo(tipo), texto, x, y, 34) + 14;
			}
			y += 42;

			// tesouros
			y = rotulo(g2, "Tesouros", y);
			x = 12;
			for (String tipo : jogo.getTiposDeTesouro()) {
				x = peca(g2, ImagensTabuleiroJogador.getTesouro(tipo), Textos.tesouro(tipo),
						"×" + jogo.getTesouro(cor, tipo), x, y, 30) + 8;
			}
			y += 38;

			// bônus
			y = rotulo(g2, "Peças de bônus (clique para usar)", y);
			areasBonus.clear();
			tiposBonus.clear();
			List<String> bonus = jogo.getBonusDisponiveis(cor);
			x = 12;
			for (String tipo : bonus) {
				Image img = ImagensTabuleiroJogador.getBonus(tipo);
				Rectangle r = new Rectangle(x, y, 46, 46);
				if (img != null) {
					g2.drawImage(img, r.x, r.y, r.width, r.height, this);
				} else {
					g2.setColor(new Color(220, 200, 150));
					g2.fill(r);
					ImagensTabuleiroJogador.desenharTextoCentralizado(g2, Textos.bonus(tipo), r.x, r.y, r.width,
							r.height, TINTA);
				}
				areasBonus.add(r);
				tiposBonus.add(tipo);
				x += 52;
			}
			if (bonus.isEmpty()) {
				g2.setColor(Color.GRAY);
				g2.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
				g2.drawString("nenhuma (a 1ª entrega a um conselheiro dá uma)", 12, y + 18);
			}
			y += 54;

			// avisos
			g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
			fm = g2.getFontMetrics();
			g2.setColor(AVISO);
			for (String aviso : avisos(cor)) {
				g2.drawString(aviso, 12, y + fm.getAscent());
				y += fm.getHeight();
			}
			g2.dispose();
		}

		private List<String> avisos(String cor) {
			List<String> avisos = new ArrayList<String>();
			if (!jogo.todosEscolheramConselheiroSecreto()) {
				avisos.add("Falta escolher conselheiros secretos (botão Cartas).");
			}
			if (jogo.getMelhoriasGratisPendentes() > 0) {
				avisos.add("Linha completa: escolha a melhoria grátis.");
			}
			if (!jogo.getConselheirosAlcancaveis().isEmpty()) {
				avisos.add("Você está num conselheiro: pode entregar tesouros.");
			}
			if (!jogo.getTesourosDoBonusParaEscolher().isEmpty()) {
				avisos.add("Escolha o tesouro do bônus.");
			}
			return avisos;
		}

		private int rotulo(Graphics2D g2, String texto, int y) {
			g2.setColor(TINTA);
			g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
			FontMetrics fm = g2.getFontMetrics();
			g2.drawString(texto, 12, y + fm.getAscent());
			return y + fm.getHeight() + 2;
		}

		/** Desenha uma peça com a quantidade ao lado; devolve o x final. */
		private int peca(Graphics2D g2, Image img, String nome, String texto, int x, int y, int lado) {
			if (img != null) {
				g2.drawImage(img, x, y, lado, lado, this);
			} else {
				g2.setColor(Color.LIGHT_GRAY);
				g2.fillOval(x, y, lado, lado);
				ImagensTabuleiroJogador.desenharTextoCentralizado(g2, nome, x, y, lado, lado, TINTA);
			}
			g2.setColor(TINTA);
			g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
			FontMetrics fm = g2.getFontMetrics();
			g2.drawString(texto, x + lado + 4, y + (lado + fm.getAscent()) / 2 - 2);
			return x + lado + 4 + fm.stringWidth(texto);
		}

		private String bonusEm(int px, int py) {
			for (int i = 0; i < areasBonus.size(); i++) {
				if (areasBonus.get(i).contains(px, py)) {
					return tiposBonus.get(i);
				}
			}
			return null;
		}

		/** Clique numa peça de bônus: pergunta e usa. */
		private class TratadorCliqueBonus extends MouseAdapter {
			@Override
			public void mouseClicked(MouseEvent e) {
				String tipo = bonusEm(e.getX(), e.getY());
				if (tipo == null) {
					return;
				}
				int resposta = JOptionPane.showConfirmDialog(PainelRecursos.this,
						"Usar a peça de bônus \"" + jogo.getDescricaoBonus(tipo) + "\"?\nEla só pode ser usada uma vez.",
						"Peça de bônus", JOptionPane.YES_NO_OPTION);
				// usarBonus avisa os observadores, então a tela se atualiza sozinha
				if (resposta == JOptionPane.YES_OPTION && jogo.usarBonus(tipo)
						&& !jogo.getTesourosDoBonusParaEscolher().isEmpty()) {
					EscolhaDeTesouro.mostrar(PainelRecursos.this, jogo);
				}
			}
		}

		/** Mãozinha sobre as peças de bônus. */
		private class TratadorCursor extends MouseMotionAdapter {
			@Override
			public void mouseMoved(MouseEvent e) {
				String tipo = bonusEm(e.getX(), e.getY());
				setCursor(tipo == null ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				setToolTipText(tipo == null ? null : jogo.getDescricaoBonus(tipo));
			}
		}
	}

	/** Botões que abrem as janelas. */
	private class TratadorAbrir implements ActionListener {
		static final int MELHORIAS = 0;
		static final int ENTREGAS = 1;
		static final int CARTAS = 2;
		private final int qual;

		TratadorAbrir(int qual) {
			this.qual = qual;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (qual == MELHORIAS) {
				lateral.abrirMelhorias();
			} else if (qual == ENTREGAS) {
				lateral.abrirEntregas();
			} else {
				lateral.abrirCartas();
			}
		}
	}
}
