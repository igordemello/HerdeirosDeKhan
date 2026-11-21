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
	private final JButton botaoContinuar = new JButton("Continuar");

	public JanelaInicial() {
		super("Herdeiros do Khan - Início");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		botaoContinuar.setEnabled(false);
		botaoContinuar.setToolTipText("Salvar e continuar uma partida fica para a 4ª iteração");

		JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 24));
		botoes.setOpaque(false);
		botoes.add(botaoNovoJogo);
		botoes.add(botaoContinuar);

		Fundo fundo = new Fundo();
		fundo.setLayout(new BorderLayout());
		fundo.add(botoes, BorderLayout.SOUTH);
		setContentPane(fundo);
		setSize(640, 640);
		setLocationRelativeTo(null);
	}

	public JButton getBotaoNovoJogo() {
		return botaoNovoJogo;
	}

	/** Imagem do tabuleiro com o nome do jogo por cima. */
	private static class Fundo extends JPanel {
		private static final long serialVersionUID = 1L;
		private final Image imagem = ImagensTabuleiroJogador.carregarArquivo("imagens/", "tabuleiro");

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
		}
	}
}
