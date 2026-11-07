package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import model.Jogo;

/**
 * Janela de entregas de tesouros aos conselheiros (págs. 12-13). Mostra os
 * tesouros do jogador da vez e, para cada conselheiro em que ele fez parada
 * neste turno, o pedido, os votos e a peça de bônus que ainda está lá.
 */
class JanelaEntregas extends JDialog {
	private static final long serialVersionUID = 1L;

	private final Jogo jogo;
	private final PainelLateral lateral;
	private final JPanel conteudo = new JPanel();
	private final JLabel mensagem = new JLabel(" ");

	JanelaEntregas(Window dono, Jogo jogo, PainelLateral lateral) {
		super(dono, "Entregar tesouros", ModalityType.MODELESS);
		this.jogo = jogo;
		this.lateral = lateral;
		conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
		conteudo.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
		mensagem.setBorder(BorderFactory.createEmptyBorder(4, 14, 8, 14));
		mensagem.setForeground(new Color(150, 60, 0));
		setLayout(new BorderLayout());
		add(new JScrollPane(conteudo), BorderLayout.CENTER);
		add(mensagem, BorderLayout.SOUTH);
		setSize(760, 520);
		setLocationRelativeTo(dono);
		atualizar();
	}

	void atualizar() {
		conteudo.removeAll();
		String cor = jogo.getJogadorDaVez();
		setTitle("Entregar tesouros — vez de " + cor);

		titulo("Seus tesouros");
		JPanel meus = linha();
		List<String> virtuais = jogo.getTesourosVirtuaisDaVez();
		for (String tipo : jogo.getTiposDeTesouro()) {
			int n = jogo.getTesouro(cor, tipo);
			int v = 0;
			for (String x : virtuais) {
				if (x.equals(tipo)) {
					v++;
				}
			}
			meus.add(new BlocoImagem(ImagensTabuleiroJogador.getTesouro(tipo),
					Textos.tesouro(tipo) + " ×" + n + (v > 0 ? " (+" + v + " virt.)" : ""), Textos.tesouro(tipo), 96, 52));
		}
		conteudo.add(meus);

		List<String> alcancaveis = jogo.getConselheirosAlcancaveis();
		titulo("Conselheiros em que você fez parada neste turno");
		if (alcancaveis.isEmpty()) {
			JLabel nenhum = new JLabel("Nenhum. Para entregar, termine o movimento ou passe por uma parada de conselheiro.");
			nenhum.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
			nenhum.setAlignmentX(LEFT_ALIGNMENT);
			conteudo.add(nenhum);
		}
		for (String id : alcancaveis) {
			conteudo.add(linhaDoConselheiro(id));
		}
		limitarAlturas();
		conteudo.revalidate();
		conteudo.repaint();
	}

	private JPanel linhaDoConselheiro(String id) {
		JPanel linha = linha();
		linha.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
				BorderFactory.createEmptyBorder(6, 0, 6, 0)));

		JLabel nome = new JLabel("<html><b>" + Textos.conselheiro(id) + "</b><br>" + jogo.getVotosPorEntrega(id)
				+ (jogo.getVotosPorEntrega(id) == 1 ? " voto" : " votos") + " por entrega</html>");
		nome.setPreferredSize(new Dimension(140, 52));
		linha.add(nome);

		List<String> pedido = jogo.getPedidoDoConselheiro(id);
		List<JComboBox<String>> escolhas = new ArrayList<JComboBox<String>>();
		List<String> sugestao = jogo.sugerirEntrega(id);
		for (int i = 0; i < pedido.size(); i++) {
			String tipo = pedido.get(i);
			if (Jogo.QUALQUER_TESOURO.equals(tipo)) {
				JComboBox<String> combo = new JComboBox<String>(jogo.getTiposDeTesouro().toArray(new String[0]));
				if (i < sugestao.size()) {
					combo.setSelectedItem(sugestao.get(i));
				}
				escolhas.add(combo);
				linha.add(combo);
			} else {
				linha.add(new BlocoImagem(ImagensTabuleiroJogador.getTesouro(tipo), Textos.tesouro(tipo),
						Textos.tesouro(tipo), 56, 44));
			}
		}

		String bonus = jogo.getBonusDoConselheiro(id);
		if (bonus != null) {
			JLabel mais = new JLabel("  1ª entrega leva:");
			linha.add(mais);
			linha.add(new BlocoImagem(ImagensTabuleiroJogador.getBonus(bonus), Textos.bonus(bonus), Textos.bonus(bonus),
					56, 44));
		}

		JButton entregar = new JButton("Entregar");
		entregar.addActionListener(new TratadorEntrega(id, pedido, escolhas));
		entregar.setEnabled(!escolhas.isEmpty() || jogo.podeEntregar(id, pedido));
		linha.add(entregar);
		return linha;
	}

	private JPanel linha() {
		JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
		linha.setAlignmentX(LEFT_ALIGNMENT);
		return linha;
	}

	private void titulo(String texto) {
		JLabel rotulo = new JLabel(texto);
		rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 14f));
		rotulo.setBorder(BorderFactory.createEmptyBorder(10, 4, 2, 4));
		rotulo.setAlignmentX(LEFT_ALIGNMENT);
		rotulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
		conteudo.add(rotulo);
	}

	/** Botão "Entregar" de um conselheiro. */
	private class TratadorEntrega implements ActionListener {
		private final String id;
		private final List<String> pedido;
		private final List<JComboBox<String>> escolhas;

		TratadorEntrega(String id, List<String> pedido, List<JComboBox<String>> escolhas) {
			this.id = id;
			this.pedido = pedido;
			this.escolhas = escolhas;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<String> tesouros = new ArrayList<String>();
			int c = 0;
			for (String tipo : pedido) {
				tesouros.add(Jogo.QUALQUER_TESOURO.equals(tipo) ? (String) escolhas.get(c++).getSelectedItem() : tipo);
			}
			if (jogo.entregar(id, tesouros)) {
				mensagem.setText("Entrega feita a " + Textos.conselheiro(id) + ".");
			} else {
				mensagem.setText("Você não tem esses tesouros.");
			}
			lateral.atualizar();
		}
	}

	/** No BoxLayout, cada linha fica com a altura que precisa, sem esticar. */
	private void limitarAlturas() {
		for (java.awt.Component c : conteudo.getComponents()) {
			c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
		}
	}
}
