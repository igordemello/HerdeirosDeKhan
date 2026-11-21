package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import model.Jogo;

/**
 * Janela modal da habilidade do Jochi, no início da partida: mostra as 2
 * melhorias tiradas ao acaso e o jogador clica na que vai para a coluna
 * branca. A outra volta para a pilha.
 */
final class EscolhaDeMelhoriaInicial extends JDialog {
	private static final long serialVersionUID = 1L;

	private final Jogo jogo;

	private EscolhaDeMelhoriaInicial(Component origem, Jogo jogo, String cor) {
		super(SwingUtilities.getWindowAncestor(origem), "Jochi: melhoria inicial", ModalityType.APPLICATION_MODAL);
		this.jogo = jogo;
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // a escolha é obrigatória
		JLabel texto = new JLabel("<html>" + cor + " (Jochi): clique na melhoria que vai para a sua coluna branca."
				+ "<br>A outra volta para a pilha.</html>");
		texto.setBorder(BorderFactory.createEmptyBorder(10, 12, 4, 12));
		JPanel pecas = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
		List<String> ids = jogo.getMelhoriasIniciaisParaEscolher();
		for (int i = 0; i < ids.size(); i++) {
			BlocoImagem bloco = new BlocoImagem(ImagensTabuleiroJogador.getMelhoria(ids.get(i)),
					Textos.melhoria(ids.get(i)), ids.get(i), 150, 80);
			bloco.setOuvinte(new TratadorEscolha(i));
			pecas.add(bloco);
		}
		setLayout(new BorderLayout());
		add(texto, BorderLayout.NORTH);
		add(pecas, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(origem);
	}

	/** Abre a janela se algum jogador (o Jochi) ainda tem a escolha pendente. */
	static void mostrarSePendente(Component origem, Jogo jogo) {
		String cor = jogo.getCorComMelhoriaInicialParaEscolher();
		if (cor != null) {
			new EscolhaDeMelhoriaInicial(origem, jogo, cor).setVisible(true);
		}
	}

	private class TratadorEscolha implements ActionListener {
		private final int indice;

		TratadorEscolha(int indice) {
			this.indice = indice;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			jogo.escolherMelhoriaInicial(indice);
			dispose();
		}
	}
}
