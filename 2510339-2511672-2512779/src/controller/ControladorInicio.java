package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.SwingUtilities;

import model.Jogo;
import view.JanelaInicial;
import view.JanelaJogadores;
import view.JanelaPrincipal;
import view.SeletorPartida;

/**
 * Início do programa: janela inicial (novo jogo / continuar partida), escolha
 * dos jogadores e criação da partida, que depois passa para o
 * {@link ControladorJogo}. No fim de uma partida, "Jogar de novo" volta
 * direto para a escolha dos personagens (enunciado da 4ª iteração).
 */
public class ControladorInicio {
	private JanelaInicial janelaInicial;
	private JanelaJogadores janelaJogadores;

	/** Janela inicial: novo jogo ou continuar uma partida salva. */
	public void mostrarJanelaInicial() {
		janelaInicial = new JanelaInicial();
		janelaInicial.getBotaoNovoJogo().addActionListener(new TratadorNovoJogo());
		janelaInicial.getBotaoContinuar().addActionListener(new TratadorContinuar());
		janelaInicial.setVisible(true);
	}

	/** Janela de escolha dos personagens (novo jogo ou "Jogar de novo"). */
	public void mostrarEscolhaDosJogadores() {
		janelaJogadores = new JanelaJogadores();
		janelaJogadores.getBotaoJogar().addActionListener(new TratadorJogar());
		janelaJogadores.setVisible(true);
	}

	/** "Continuar partida": escolhe o arquivo salvo e abre o tabuleiro como estava. */
	private class TratadorContinuar implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			File arquivo = SeletorPartida.escolherParaAbrir(janelaInicial);
			if (arquivo == null) {
				return; // cancelou
			}
			try {
				Jogo.carregar(arquivo);
			} catch (IOException ex) {
				janelaInicial.mostrarAviso("Não deu para continuar: " + ex.getMessage());
				return;
			}
			ControladorJogo controlador = new ControladorJogo(new JanelaPrincipal());
			janelaInicial.dispose();
			controlador.iniciar();
		}
	}

	/** "Novo jogo": troca a janela inicial pela escolha dos jogadores. */
	private class TratadorNovoJogo implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			mostrarEscolhaDosJogadores();
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
				new ControladorInicio().mostrarJanelaInicial();
			}
		});
	}
}
