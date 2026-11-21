package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Jogo;

/**
 * Segunda janela (Figura 2 do enunciado): escolha dos herdeiros que vão
 * jogar. Cada linha mostra a cartela do herdeiro (com drawImage()) e uma
 * caixa para marcar. A ordem de jogo é a ordem em que as caixas foram
 * marcadas (começa quem tiver mais descendentes, pág. 6).
 */
public class JanelaJogadores extends JFrame {
	private static final long serialVersionUID = 1L;

	private final JButton botaoJogar = new JButton("Jogar");
	private final JLabel aviso = new JLabel(" ");
	/** Caixa de cada cor (chave = nome da cor no Model). */
	private final Map<String, JCheckBox> caixas = new HashMap<String, JCheckBox>();
	/** Cores marcadas, na ordem em que foram marcadas. */
	private final List<String> escolhidas = new ArrayList<String>();

	public JanelaJogadores() {
		super("Herdeiros do Khan - Jogadores");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JLabel instrucao = new JLabel("<html>Marque de 2 a 5 herdeiros <b>na ordem em que vão jogar</b>."
				+ " Começa quem tiver mais descendentes.</html>");
		instrucao.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));

		JPanel linhas = new JPanel(new GridLayout(0, 1, 0, 6));
		linhas.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
		for (String cor : Jogo.getCoresPossiveis()) {
			String herdeiro = Jogo.getHerdeiroDaCor(cor);
			JCheckBox caixa = new JCheckBox();
			caixa.setFont(caixa.getFont().deriveFont(Font.BOLD, 15f));
			caixa.addItemListener(new TratadorMarcacao(cor));
			caixas.put(cor, caixa);

			JPanel linha = new JPanel(new BorderLayout(12, 0));
			linha.add(new Cartela(herdeiro), BorderLayout.WEST);
			linha.add(caixa, BorderLayout.CENTER);
			linhas.add(linha);
		}
		atualizarTextos();

		aviso.setForeground(new Color(170, 30, 30));
		JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
		rodape.add(aviso);
		rodape.add(botaoJogar);

		setLayout(new BorderLayout());
		add(instrucao, BorderLayout.NORTH);
		add(linhas, BorderLayout.CENTER);
		add(rodape, BorderLayout.SOUTH);
		setSize(560, 760);
		setLocationRelativeTo(null);
	}

	public JButton getBotaoJogar() {
		return botaoJogar;
	}

	/** Cores escolhidas, na ordem de jogo. */
	public List<String> getCoresEscolhidas() {
		return new ArrayList<String>(escolhidas);
	}

	public void mostrarAviso(String texto) {
		aviso.setText(texto);
	}

	/** Texto de cada caixa: posição na ordem de jogo (se marcada), cor e herdeiro. */
	private void atualizarTextos() {
		for (String cor : Jogo.getCoresPossiveis()) {
			int posicao = escolhidas.indexOf(cor);
			String nome = cor.charAt(0) + cor.substring(1).toLowerCase() + " - " + Jogo.getHerdeiroDaCor(cor);
			caixas.get(cor).setText(posicao < 0 ? nome : (posicao + 1) + "º  " + nome);
		}
	}

	/** Marca/desmarca uma cor, guardando a ordem das marcações. */
	private class TratadorMarcacao implements ItemListener {
		private final String cor;

		TratadorMarcacao(String cor) {
			this.cor = cor;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				escolhidas.add(cor);
			} else {
				escolhidas.remove(cor);
			}
			atualizarTextos();
			aviso.setText(" ");
		}
	}

	/** Cartela (tabuleiro do jogador) do herdeiro, reduzida. */
	private static class Cartela extends JPanel {
		private static final long serialVersionUID = 1L;
		private final Image imagem;

		Cartela(String herdeiro) {
			imagem = ImagensTabuleiroJogador.carregarArquivo(ImagensTabuleiroJogador.PASTA, herdeiro.toLowerCase());
			setPreferredSize(new Dimension(240, (int) Math.round(240 / ImagensTabuleiroJogador.PROPORCAO)));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(imagem, 0, 0, getWidth(), getHeight(), this);
		}
	}
}
