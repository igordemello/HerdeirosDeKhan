package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.SwingUtilities;

import model.Jogo;
import view.JanelaInicial;
import view.JanelaJogadores;
import view.JanelaPrincipal;

/**
 * Início do programa: janela inicial (novo jogo / continuar), escolha dos
 * jogadores e criação da partida, que depois passa para o
 * {@link ControladorJogo}.
 */
public class ControladorInicio {
	private final JanelaInicial janelaInicial = new JanelaInicial();
	private JanelaJogadores janelaJogadores;

	public ControladorInicio() {
		janelaInicial.getBotaoNovoJogo().addActionListener(new TratadorNovoJogo());
		janelaInicial.setVisible(true);
	}

	/** "Novo jogo": troca a janela inicial pela escolha dos jogadores. */
	private class TratadorNovoJogo implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			janelaJogadores = new JanelaJogadores();
			janelaJogadores.getBotaoJogar().addActionListener(new TratadorJogar());
			janelaJogadores.setVisible(true);
			janelaInicial.dispose();
		}
	}

	/** "Jogar": cria a partida (Singleton) e abre a janela do tabuleiro. */
	private class TratadorJogar implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			List<String> cores = janelaJogadores.getCoresEscolhidas();
			if (cores.size() < 2) {
				janelaJogadores.mostrarAviso("Escolha pelo menos 2 jogadores.");
				return;
			}
			Jogo.novaPartida(cores);
			ControladorJogo controlador = new ControladorJogo(new JanelaPrincipal());
			janelaJogadores.dispose();
			controlador.iniciar();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new ControladorInicio();
			}
		});
	}
}
