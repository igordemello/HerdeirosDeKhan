package controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import model.Jogo;
import view.JanelaPrincipal;

/**
 * Controla o fluxo da jogada:
 * <ol>
 * <li>o jogador da vez ativa uma coluna na área lateral (ou o testador
 * escolhe os movimentos no JComboBox), o que define os movimentos do turno;</li>
 * <li>o jogador clica numa parada do tabuleiro;</li>
 * <li>se o peão puder chegar lá, ele é movido, a vez passa e a tela é
 * redesenhada; se não puder, nada acontece e a vez continua com o mesmo
 * jogador.</li>
 * </ol>
 * Os eventos são tratados por classes internas (sem expressões lambda), como
 * pede o enunciado.
 */
public class ControladorJogo {
	private final Jogo jogo;
	private final JanelaPrincipal janela;

	public ControladorJogo(Jogo jogo, JanelaPrincipal janela) {
		this.jogo = jogo;
		this.janela = janela;
		janela.getPainelTabuleiro().addMouseListener(new TratadorCliqueNoTabuleiro());
	}

	/**
	 * Tenta mover um peão do jogador da vez até a parada clicada. Altani tem
	 * 2 peões: move o primeiro que conseguir chegar.
	 */
	void paradaClicada(String idParada) {
		String cor = jogo.getJogadorDaVez();
		for (int n = 1; n <= jogo.getNumeroDePeoes(cor); n++) {
			if (!jogo.moverPeaoDaVezAte(n, idParada).isEmpty()) {
				jogo.passarVez();
				janela.atualizar();
				return;
			}
		}
	}

	/** Converte o clique no tabuleiro na parada clicada. */
	private class TratadorCliqueNoTabuleiro extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			String idParada = janela.getPainelTabuleiro().paradaEm(e.getX(), e.getY());
			if (idParada != null) {
				paradaClicada(idParada);
			}
		}
	}

	/** Abre o jogo com uma partida de 5 jogadores (as janelas iniciais ficam para depois). */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Jogo jogo = new Jogo(Arrays.asList("VERMELHO", "AZUL", "AMARELO", "VERDE", "ROXO"));
				JanelaPrincipal janela = new JanelaPrincipal(jogo);
				new ControladorJogo(jogo, janela);
				janela.setVisible(true);
			}
		});
	}
}
