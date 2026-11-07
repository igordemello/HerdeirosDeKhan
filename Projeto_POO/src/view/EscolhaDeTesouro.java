package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import model.Jogo;

/**
 * Janela modal da peça de bônus de 3 tesouros (pág. 17): o jogador clica no
 * tesouro com que quer ficar; os outros são descartados.
 */
final class EscolhaDeTesouro extends JDialog {
	private static final long serialVersionUID = 1L;

	private final Jogo jogo;

	private EscolhaDeTesouro(Component origem, Jogo jogo) {
		super(SwingUtilities.getWindowAncestor(origem), "Escolha 1 tesouro", ModalityType.APPLICATION_MODAL);
		this.jogo = jogo;
		JLabel texto = new JLabel("Clique no tesouro com que quer ficar. Os outros são descartados.");
		texto.setBorder(BorderFactory.createEmptyBorder(10, 12, 4, 12));
		JPanel pecas = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
		for (String tipo : jogo.getTesourosDoBonusParaEscolher()) {
			BlocoImagem bloco = new BlocoImagem(ImagensTabuleiroJogador.getTesouro(tipo), Textos.tesouro(tipo),
					Textos.tesouro(tipo), 90, 90);
			bloco.setOuvinte(new TratadorEscolha(tipo));
			pecas.add(bloco);
		}
		setLayout(new BorderLayout());
		add(texto, BorderLayout.NORTH);
		add(pecas, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(origem);
	}

	static void mostrar(Component origem, Jogo jogo) {
		new EscolhaDeTesouro(origem, jogo).setVisible(true);
	}

	private class TratadorEscolha implements ActionListener {
		private final String tipo;

		TratadorEscolha(String tipo) {
			this.tipo = tipo;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			jogo.escolherTesouroDoBonus(tipo);
			dispose();
		}
	}
}
