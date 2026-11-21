package view;

import java.awt.BorderLayout;
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

/**
 * Resultado da partida (págs. 14-16): os votos de cada jogador (Kurultai,
 * influência nas regiões e conselheiro secreto), os tesouros não entregues
 * (1º desempate) e o vencedor. É modal: o Controller espera ela ser fechada.
 */
public class JanelaFimDeJogo extends JDialog {
	private static final long serialVersionUID = 1L;

	public JanelaFimDeJogo(Window dono, Jogo jogo) {
		super(dono, "Fim de jogo", ModalityType.APPLICATION_MODAL);

		StringBuilder tabela = new StringBuilder("<html><table cellpadding='4'><tr><th align='left'>Jogador</th>"
				+ "<th>Kurultai</th><th>Influência</th><th>Conselheiro<br>secreto</th><th>Total</th>"
				+ "<th>Tesouros</th></tr>");
		for (String cor : jogo.getCores()) {
			tabela.append("<tr><td>").append(cor).append(" (").append(jogo.getHerdeiro(cor)).append(")</td>")
					.append(celula(jogo.getVotos(cor))).append(celula(jogo.getVotosDeInfluencia(cor)))
					.append(celula(jogo.getVotosDoConselheiroSecreto(cor)))
					.append("<td align='center'><b>").append(jogo.getVotosFinais(cor)).append("</b></td>")
					.append(celula(jogo.getTotalTesouros(cor))).append("</tr>");
		}
		tabela.append("</table></html>");
		JLabel votos = new JLabel(tabela.toString());
		votos.setFont(votos.getFont().deriveFont(15f));
		votos.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

		List<String> vencedores = jogo.getVencedores();
		JLabel resultado = new JLabel((vencedores.size() == 1 ? "Vencedor: " : "Vitória compartilhada: ")
				+ String.join(", ", vencedores));
		resultado.setFont(resultado.getFont().deriveFont(Font.BOLD, 22f));
		resultado.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));

		JButton botaoFechar = new JButton("Voltar ao início");
		botaoFechar.addActionListener(new TratadorFechar());
		JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		rodape.add(botaoFechar);

		setLayout(new BorderLayout());
		add(resultado, BorderLayout.NORTH);
		add(votos, BorderLayout.CENTER);
		add(rodape, BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(dono);
	}

	private static String celula(int valor) {
		return "<td align='center'>" + valor + "</td>";
	}

	private class TratadorFechar implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}
}
