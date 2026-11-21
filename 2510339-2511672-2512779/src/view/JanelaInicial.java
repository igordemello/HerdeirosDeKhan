package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Primeira janela (Figura 1 do enunciado): começar uma nova partida ou
 * continuar uma partida salva. O fundo é a imagem do tabuleiro, desenhada com
 * drawImage(). Quem trata os botões é o Controller.
 */
public class JanelaInicial extends JFrame {
	private static final long serialVersionUID = 1L;

	private final JButton botaoNovoJogo = new JButton("Novo jogo");
	private final JButton botaoContinuar = new JButton("Continuar partida");
	private final Fundo fundo = new Fundo();

	public JanelaInicial() {
		super("Herdeiros do Khan - Início");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 24));
		botoes.setOpaque(false);
		botoes.add(botaoNovoJogo);
		botoes.add(botaoContinuar);

		fundo.setLayout(new BorderLayout());
		fundo.add(botoes, BorderLayout.SOUTH);
		setContentPane(fundo);
		setSize(640, 640);
		setLocationRelativeTo(null);
	}

	public JButton getBotaoNovoJogo() {
		return botaoNovoJogo;
	}

	public JButton getBotaoContinuar() {
		return botaoContinuar;
	}

	/** Aviso em cima dos botões (ex.: o arquivo escolhido não é uma partida válida). */
	public void mostrarAviso(String texto) {
		fundo.aviso = texto;
		fundo.repaint();
	}

	/** Imagem do tabuleiro com o nome do jogo por cima (e o aviso, se houver). */
	private static class Fundo extends JPanel {
		private static final long serialVersionUID = 1L;
		private final Image imagem = ImagensTabuleiroJogador.carregarArquivo("imagens/", "tabuleiro");
		private String aviso = null;

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.drawImage(imagem, 0, 0, getWidth(), getHeight(), this);

			g2.setColor(new Color(0, 0, 0, 150));
			g2.fillRect(0, 40, getWidth(), 80);
			g2.setColor(Color.WHITE);
			g2.setFont(new Font(Font.SERIF, Font.BOLD | Font.ITALIC, 44));
			FontMetrics fm = g2.getFontMetrics();
			String titulo = "Herdeiros do Khan";
			g2.drawString(titulo, (getWidth() - fm.stringWidth(titulo)) / 2, 40 + (80 + fm.getAscent()) / 2 - 6);

			if (aviso != null) {
				int y = getHeight() - 120;
				g2.setColor(new Color(0, 0, 0, 170));
				g2.fillRect(0, y, getWidth(), 34);
				g2.setColor(new Color(255, 200, 120));
				int tamanho = 13;
				do { // diminui a letra até o aviso caber na largura da janela
					g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, tamanho--));
					fm = g2.getFontMetrics();
				} while (fm.stringWidth(aviso) > getWidth() - 16 && tamanho > 8);
				g2.drawString(aviso, Math.max(8, (getWidth() - fm.stringWidth(aviso)) / 2), y + (34 + fm.getAscent()) / 2 - 2);
			}
		}
	}
}
