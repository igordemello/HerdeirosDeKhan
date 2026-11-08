package view;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JFrame;

import model.Jogo;

/**
 * Janela do jogo: o tabuleiro principal no centro e a área lateral (jogador
 * da vez e tabuleiro dele) à direita. O enunciado limita as janelas a
 * 1400 x 1050 pixels.
 *
 * Mostra a partida em andamento ({@link Jogo#getInstancia()}). Os painéis
 * são observadores da partida e se redesenham sozinhos quando ela muda.
 */
public class JanelaPrincipal extends JFrame {
	private static final long serialVersionUID = 1L;

	public static final int LARGURA = 1400;
	public static final int ALTURA = 1000;

	private final PainelTabuleiro painelTabuleiro;
	private final PainelLateral painelLateral;

	public JanelaPrincipal() {
		super("Herdeiros do Khan");
		Jogo jogo = Jogo.getInstancia();
		painelTabuleiro = new PainelTabuleiro(jogo);
		painelLateral = new PainelLateral(jogo);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		add(painelTabuleiro, BorderLayout.CENTER);
		add(painelLateral, BorderLayout.EAST);
		// em notebook a tela é menor que 1000 de altura e o fim da área lateral
		// ficava escondido atrás da barra de tarefas
		Rectangle tela = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		setSize(Math.min(LARGURA, tela.width), Math.min(ALTURA, tela.height));
		setLocationRelativeTo(null);
	}

	public PainelTabuleiro getPainelTabuleiro() {
		return painelTabuleiro;
	}

	public PainelLateral getPainelLateral() {
		return painelLateral;
	}

	public JButton getBotaoPassarVez() {
		return painelLateral.getBotaoPassarVez();
	}

	public JButton getBotaoHabilidade() {
		return painelLateral.getBotaoHabilidade();
	}

	public void mostrarMensagem(String texto) {
		painelLateral.mostrarMensagem(texto);
	}

	/** Escolha dos conselheiros secretos, um jogador de cada vez (antes da 1ª rodada). */
	public void escolherConselheirosSecretos() {
		painelLateral.escolherConselheirosSecretos();
	}
}
