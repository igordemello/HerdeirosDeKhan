package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import model.Jogo;

/**
 * Janela para testar a área lateral sozinha, antes de ela ser integrada à
 * janela principal do jogo.
 *
 * O centro da janela é só um espaço reservado para o tabuleiro principal;
 * o botão "Passar vez" mostra como a janela principal deve avisar o painel
 * lateral quando a vez mudar.
 */
public class JanelaTesteLateral extends JFrame {
	private static final long serialVersionUID = 1L;

	private final Jogo jogo;
	private final PainelLateral painelLateral;
	private final AreaTabuleiroProvisoria areaTabuleiro = new AreaTabuleiroProvisoria();

	public JanelaTesteLateral(Jogo jogo) {
		super("Herdeiros do Khan — teste da área lateral");
		this.jogo = jogo;
		this.painelLateral = new PainelLateral(jogo);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		add(areaTabuleiro, BorderLayout.CENTER);
		add(painelLateral, BorderLayout.EAST);

		JButton passarVez = new JButton("Passar vez");
		passarVez.addActionListener(new TratadorPassarVez());
		JPanel rodape = new JPanel();
		rodape.add(passarVez);
		add(rodape, BorderLayout.SOUTH);

		painelLateral.addPropertyChangeListener("movimentos", new OuvinteMovimentos());

		setSize(1280, 820); // dentro do limite de 1400 x 1050 do enunciado
		setLocationRelativeTo(null);
	}

	/** Passa a vez no Model e atualiza o painel lateral. */
	private class TratadorPassarVez implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			jogo.passarVez();
			painelLateral.atualizar();
			areaTabuleiro.repaint();
		}
	}

	/** Exemplo de como o tabuleiro principal fica sabendo dos movimentos. */
	private class OuvinteMovimentos implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			areaTabuleiro.repaint();
		}
	}

	/** Espaço reservado para o tabuleiro principal (parte de outro integrante). */
	private class AreaTabuleiroProvisoria extends JPanel {
		private static final long serialVersionUID = 1L;

		AreaTabuleiroProvisoria() {
			setPreferredSize(new Dimension(840, 780));
			setBackground(new Color(222, 232, 242));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setColor(new Color(90, 110, 130));
			g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
			String t1 = "Tabuleiro principal";
			String t2 = jogo.getJogadorDaVez() + " pode andar " + jogo.getMovimentosDaVez() + " parada(s)";
			FontMetrics fm = g2.getFontMetrics();
			g2.drawString(t1, (getWidth() - fm.stringWidth(t1)) / 2, getHeight() / 2 - 10);
			g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
			fm = g2.getFontMetrics();
			g2.drawString(t2, (getWidth() - fm.stringWidth(t2)) / 2, getHeight() / 2 + 20);
		}
	}

	/** Abre a janela com uma partida de 5 jogadores. */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Jogo jogo = new Jogo(Arrays.asList("VERMELHO", "AZUL", "AMARELO", "VERDE", "ROXO"));
				new JanelaTesteLateral(jogo).setVisible(true);
			}
		});
	}
}
