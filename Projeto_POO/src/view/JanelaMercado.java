package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Jogo;
import model.ObservadoIF;
import model.ObservadorIF;

/**
 * Janela da área de melhorias: mostra as peças disponíveis e o topo da
 * pilha, e permite comprar uma melhoria com 2 moedas (pág. 11). Quando o
 * jogador completa uma linha, a mesma janela é usada para escolher a
 * melhoria grátis da coluna branca (pág. 12).
 *
 * É observadora da partida e se atualiza sozinha quando o estado muda.
 */
class JanelaMercado extends JDialog implements ObservadorIF {
	private static final long serialVersionUID = 1L;
	/** Índice que, na API do Model, representa o topo da pilha. */
	private static final int TOPO = -1;
	private static final int NENHUMA = -2;

	private final Jogo jogo;
	private final JPanel pecas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
	private final JLabel titulo = new JLabel();
	private final JLabel situacao = new JLabel();
	private final JButton botaoComprar = new JButton();
	private int selecionada = NENHUMA;

	JanelaMercado(Window dono, Jogo jogo) {
		super(dono, "Melhorias", ModalityType.MODELESS);
		this.jogo = jogo;
		jogo.add(this);

		titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 15f));
		JPanel topo = new JPanel(new BorderLayout(0, 4));
		topo.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
		topo.add(titulo, BorderLayout.NORTH);
		topo.add(situacao, BorderLayout.SOUTH);

		botaoComprar.addActionListener(new TratadorComprar());
		JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		rodape.add(botaoComprar);

		setLayout(new BorderLayout());
		add(topo, BorderLayout.NORTH);
		add(pecas, BorderLayout.CENTER);
		add(rodape, BorderLayout.SOUTH);
		setSize(980, 260);
		setLocationRelativeTo(dono);
		atualizar();
	}

	@Override
	public void notify(ObservadoIF o) {
		atualizar();
	}

	@Override
	public void dispose() {
		jogo.remove(this);
		super.dispose();
	}

	private boolean modoGratis() {
		return jogo.getMelhoriasGratisPendentes() > 0;
	}

	/** Lê de novo o mercado e o jogador da vez. */
	void atualizar() {
		List<String> ids = jogo.getMelhoriasDisponiveis();
		if (selecionada >= ids.size() || (selecionada == TOPO && jogo.getTamanhoPilhaMelhorias() == 0)) {
			selecionada = NENHUMA;
		}
		String cor = jogo.getJogadorDaVez();
		if (modoGratis()) {
			titulo.setText(cor + " completou uma linha: escolha uma melhoria grátis para a coluna branca");
			situacao.setText("A melhoria grátis pode ser de qualquer cor.");
			botaoComprar.setText("Pegar grátis");
			botaoComprar.setEnabled(selecionada != NENHUMA);
		} else {
			titulo.setText("Melhorias disponíveis — vez de " + cor);
			situacao.setText("Moedas disponíveis: " + jogo.getTributoDisponivelDaVez("MOEDA") + " (cada melhoria custa "
					+ jogo.getPrecoMelhoria() + "). A peça vai para a coluna da sua cor e só vale a partir do próximo turno.");
			botaoComprar.setText("Comprar");
			botaoComprar.setEnabled(selecionada != NENHUMA && jogo.podeComprarMelhoria(selecionada));
		}

		pecas.removeAll();
		for (int i = 0; i < ids.size(); i++) {
			BlocoImagem bloco = new BlocoImagem(ImagensTabuleiroJogador.getMelhoria(ids.get(i)),
					Textos.melhoria(ids.get(i)), ids.get(i), 150, 80);
			bloco.setSelecionado(selecionada == i);
			bloco.setOuvinte(new TratadorSelecao(i));
			pecas.add(bloco);
		}
		if (jogo.getTamanhoPilhaMelhorias() > 0) {
			BlocoImagem topoPilha = new BlocoImagem(null, "Topo da pilha (aleatória)",
					"?\n" + jogo.getTamanhoPilhaMelhorias() + " na pilha", 150, 80);
			topoPilha.setSelecionado(selecionada == TOPO);
			topoPilha.setOuvinte(new TratadorSelecao(TOPO));
			pecas.add(topoPilha);
		}
		pecas.revalidate();
		pecas.repaint();
		situacao.setForeground(modoGratis() ? new Color(160, 80, 0) : Color.DARK_GRAY);
	}

	/** Clique numa peça: passa a ser a selecionada. */
	private class TratadorSelecao implements ActionListener {
		private final int indice;

		TratadorSelecao(int indice) {
			this.indice = indice;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			selecionada = indice;
			atualizar();
		}
	}

	/** Botão "Comprar" / "Pegar grátis". */
	private class TratadorComprar implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			boolean ok = modoGratis() ? jogo.escolherMelhoriaGratis(selecionada) : jogo.comprarMelhoria(selecionada);
			if (ok) {
				selecionada = NENHUMA;
				atualizar(); // o Model já avisou; aqui é só para tirar a seleção
			} else if (selecionada == TOPO) {
				situacao.setText("A peça do topo não cabia no seu tabuleiro e voltou para o fundo da pilha.");
			}
		}
	}
}
