package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Jogo;

/*
 * Faixa no pé da área lateral: uma dica do que dá pra clicar no mapa, a
 * mensagem da última ação e os botões "Passar a vez" e o da habilidade do
 * Chagatai. Os cliques nos botões são tratados no Controller.
 *
 * Usamos esse texto em vez de JOptionPane porque o enunciado pede pra
 * evitar caixa de diálogo só pra mostrar mensagem.
 */
class BarraDoTurno extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final Color FUNDO = new Color(250, 248, 244);
	private static final Color TINTA = new Color(34, 30, 40);
	private static final Color AVISO = new Color(170, 40, 0);

	private final Jogo jogo;
	private final JLabel info = new JLabel();
	private final JButton botaoPassarVez = new JButton("Passar a vez");
	private final JButton botaoHabilidade = new JButton("Cavalo->espada");
	private String mensagem = "";

	BarraDoTurno(Jogo jogo) {
		this.jogo = jogo;
		setLayout(new BorderLayout(8, 0));
		setBackground(FUNDO);
		setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 8));
		info.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		info.setForeground(TINTA);
		info.setPreferredSize(new Dimension(250, 32));

		botaoPassarVez.setPreferredSize(new Dimension(112, 30));
		botaoPassarVez.setMargin(new Insets(2, 4, 2, 4));
		botaoHabilidade.setPreferredSize(new Dimension(104, 30));
		botaoHabilidade.setMargin(new Insets(2, 1, 2, 1));
		botaoHabilidade.setFont(botaoHabilidade.getFont().deriveFont(10.5f));
		botaoHabilidade.setToolTipText("Chagatai: 1x por turno troca 1 cavalo por 1 espada");

		JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		botoes.setOpaque(false);
		botoes.add(botaoHabilidade);
		botoes.add(botaoPassarVez);
		add(info, BorderLayout.CENTER);
		add(botoes, BorderLayout.EAST);
		atualizar();
	}

	JButton getBotaoPassarVez() {
		return botaoPassarVez;
	}

	JButton getBotaoHabilidade() {
		return botaoHabilidade;
	}

	void mostrarMensagem(String texto) {
		mensagem = texto == null ? "" : texto;
		atualizar();
	}

	// Só cabem 2 linhas: a dica (ou o aviso do Khan) e a última mensagem.
	// Se cortar, o texto inteiro aparece no tooltip.
	void atualizar() {
		String linha1 = jogo.khanPendente()
				? "<font color='#" + hex(AVISO) + "'><b>Use o Khan: clique num lugar laranja.</b></font>"
				: dica();
		String texto = "<html>" + linha1 + (mensagem.isEmpty() ? "" : "<br><i>" + mensagem + "</i>") + "</html>";
		info.setText(texto);
		info.setToolTipText(texto);

		boolean chagatai = jogo.jogadorDaVezTrocaCavaloPorEspada();
		botaoHabilidade.setVisible(chagatai);
		botaoHabilidade.setEnabled(jogo.podeTrocarCavaloPorEspada());
		info.setPreferredSize(new Dimension(chagatai ? 150 : 250, 32)); // abre espaço pro botão extra
		revalidate();

		botaoPassarVez.setEnabled(!jogo.khanPendente());
		botaoPassarVez.setToolTipText(jogo.khanPendente() ? "Essa coluna obriga a usar o Khan" : null);
	}

	private String dica() {
		if (jogo.getColunaAtivadaNaVez() == null) {
			return "Ative uma coluna para começar o turno.";
		}
		List<String> partes = new ArrayList<String>();
		if (!jogo.getProvinciasAlcancaveis().isEmpty()) {
			partes.add("tributo amarelo: pegar");
		}
		if (!jogo.getCidadesAtacaveis().isEmpty()) {
			partes.add("tesouro vermelho: atacar");
		}
		if (!jogo.getParadasParaYurt().isEmpty()) {
			partes.add("botão direito na parada: yurt");
		}
		if (ogedeiPodeComprarTesouro()) {
			partes.add("botão direito no tesouro: pagar 1 moeda");
		}
		if (partes.isEmpty()) {
			return "Clique numa parada para mover o peão.";
		}
		return "Clique: " + String.join(" / ", partes);
	}

	private boolean ogedeiPodeComprarTesouro() {
		for (String cidade : jogo.getCidades()) {
			for (String t : jogo.getTesourosDaCidade(cidade)) {
				if (jogo.podePegarTesouroComMoeda(cidade, t)) {
					return true;
				}
			}
		}
		return false;
	}

	private static String hex(Color c) {
		return String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
	}
}
