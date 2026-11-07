package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import model.Jogo;

/**
 * Janela de cartas do jogador da vez:
 * <ul>
 * <li>o conselheiro secreto dele, com o quanto já atingiu da condição; se ele
 * ainda não escolheu, as 2 cartas recebidas e um botão para escolher
 * (pág. 6);</li>
 * <li>as cartas de conselheiro aberto ainda disponíveis;</li>
 * <li>as cartas abertas que ele já conquistou.</li>
 * </ul>
 * {@link #escolherConselheirosSecretos(Window, Jogo)} abre a escolha para
 * cada jogador, um de cada vez, no início da partida.
 */
class JanelaCartas extends JDialog {
	private static final long serialVersionUID = 1L;

	private final Jogo jogo;
	private final PainelLateral lateral;
	/** Jogador mostrado; {@code null} = o jogador da vez. */
	private final String corFixa;
	private final JPanel conteudo = new JPanel();

	JanelaCartas(Window dono, Jogo jogo, PainelLateral lateral) {
		this(dono, jogo, lateral, null, ModalityType.MODELESS);
	}

	private JanelaCartas(Window dono, Jogo jogo, PainelLateral lateral, String corFixa, ModalityType modo) {
		super(dono, "Cartas de conselheiro", modo);
		this.jogo = jogo;
		this.lateral = lateral;
		this.corFixa = corFixa;
		conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
		conteudo.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
		setLayout(new BorderLayout());
		add(new JScrollPane(conteudo), BorderLayout.CENTER);
		setSize(820, corFixa == null ? 720 : 340);
		setLocationRelativeTo(dono);
		atualizar();
	}

	/**
	 * Início da partida: cada jogador, na ordem de jogo, escolhe o seu
	 * conselheiro secreto numa janela modal (os outros não devem olhar).
	 */
	static void escolherConselheirosSecretos(Window dono, Jogo jogo) {
		for (String cor : jogo.getCores()) {
			if (jogo.getConselheiroSecreto(cor) == null) {
				new JanelaCartas(dono, jogo, null, cor, ModalityType.APPLICATION_MODAL).setVisible(true);
			}
		}
	}

	private String cor() {
		return corFixa != null ? corFixa : jogo.getJogadorDaVez();
	}

	void atualizar() {
		conteudo.removeAll();
		String cor = cor();
		setTitle("Cartas de conselheiro — " + cor);

		String secreto = jogo.getConselheiroSecreto(cor);
		if (secreto == null) {
			adicionarTitulo(cor + ": escolha o seu conselheiro secreto (os outros jogadores não devem olhar)");
			JPanel linha = linhaDeCartas();
			for (String tipo : jogo.getCartasSecretasParaEscolher(cor)) {
				JPanel coluna = new JPanel(new BorderLayout(0, 4));
				coluna.add(cartaSecreta(tipo, null), BorderLayout.CENTER);
				JButton escolher = new JButton("Ficar com esta");
				escolher.addActionListener(new TratadorEscolha(cor, tipo));
				coluna.add(escolher, BorderLayout.SOUTH);
				linha.add(coluna);
			}
			conteudo.add(linha);
		} else if (corFixa == null) {
			adicionarTitulo("Conselheiro secreto de " + cor + " (só ele deve olhar)");
			int progresso = jogo.getProgressoConselheiroSecreto(cor);
			int votos = jogo.getVotosDoConselheiroSecreto(cor);
			JPanel linha = linhaDeCartas();
			linha.add(cartaSecreta(secreto,
					"Você tem " + progresso + " — hoje vale " + votos + (votos == 1 ? " voto" : " votos")));
			conteudo.add(linha);
		}

		if (corFixa == null) {
			adicionarTitulo("Cartas de conselheiro aberto disponíveis (o primeiro a cumprir ganha 1 voto)");
			List<String> abertas = jogo.getCartasAbertasDisponiveis();
			JPanel linha = linhaDeCartas();
			for (String tipo : abertas) {
				linha.add(cartaAberta(tipo, null));
			}
			if (abertas.isEmpty()) {
				linha.add(new JLabel("Todas as cartas abertas já foram conquistadas."));
			}
			conteudo.add(linha);

			List<String> minhas = jogo.getCartasAbertasConquistadas(cor);
			adicionarTitulo("Cartas abertas conquistadas por " + cor + ": " + minhas.size());
			JPanel linhaMinhas = linhaDeCartas();
			for (String tipo : minhas) {
				linhaMinhas.add(cartaAberta(tipo, "Conquistada: +1 voto"));
			}
			conteudo.add(linhaMinhas);
		}
		limitarAlturas();
		conteudo.revalidate();
		conteudo.repaint();
	}

	private ComponenteCarta cartaSecreta(String tipo, String rodape) {
		return ComponenteCarta.secreta(jogo.getNomeCartaSecreta(tipo), jogo.getDescricaoCartaSecreta(tipo),
				jogo.getLimiaresCartaSecreta(tipo), rodape);
	}

	private ComponenteCarta cartaAberta(String tipo, String rodape) {
		return ComponenteCarta.aberta(jogo.getQuantidadeCartaAberta(tipo), jogo.getDescricaoCartaAberta(tipo), rodape);
	}

	private JPanel linhaDeCartas() {
		JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
		linha.setAlignmentX(LEFT_ALIGNMENT);
		return linha;
	}

	private void adicionarTitulo(String texto) {
		JLabel rotulo = new JLabel(texto);
		rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 14f));
		rotulo.setBorder(BorderFactory.createEmptyBorder(10, 4, 2, 4));
		rotulo.setAlignmentX(LEFT_ALIGNMENT);
		rotulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
		conteudo.add(rotulo);
	}

	/** Botão "Ficar com esta": escolhe o conselheiro secreto. */
	private class TratadorEscolha implements ActionListener {
		private final String cor;
		private final String tipo;

		TratadorEscolha(String cor, String tipo) {
			this.cor = cor;
			this.tipo = tipo;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			jogo.escolherConselheiroSecreto(cor, tipo);
			if (corFixa != null) {
				dispose(); // escolha do início da partida: passa para o próximo jogador
			} else if (lateral != null) {
				lateral.atualizar();
			} else {
				atualizar();
			}
		}
	}

	/** No BoxLayout, cada linha fica com a altura que precisa, sem esticar. */
	private void limitarAlturas() {
		for (java.awt.Component c : conteudo.getComponents()) {
			c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
		}
	}
}
