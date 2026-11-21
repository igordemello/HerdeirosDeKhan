package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cria os jogadores da partida, distribui seus recursos iniciais e controla
 * a ordem de jogo (sentido horário).
 *
 * Preparação (pág. 3): cada jogador escolhe uma cor e recebe o tabuleiro
 * associado, 4 peças de ativação, 12 yurts e moedas: 1 moeda para o 1º e o
 * 2º a jogar, e 2 moedas para os demais.
 */
class GerenciadorJogadores {
	static final int MIN_JOGADORES = 2;
	static final int MAX_JOGADORES = 5;

	private final Map<Cor, Jogador> jogadores = new HashMap<Cor, Jogador>();
	/** Ordem de jogo (sentido horário). O índice 0 é quem começa. */
	private final List<Cor> ordem = new ArrayList<Cor>();
	private int indiceDaVez = 0;

	/**
	 * Cria os jogadores na ordem em que vão jogar. Pelas regras, começa quem
	 * tiver o maior número de descendentes; essa decisão é tomada fora do
	 * Model, que apenas recebe a ordem já definida.
	 *
	 * @param coresEmOrdemDeJogo cores escolhidas, na ordem de jogo
	 */
	GerenciadorJogadores(List<Cor> coresEmOrdemDeJogo) {
		if (coresEmOrdemDeJogo == null) {
			throw new IllegalArgumentException("Lista de cores nula");
		}
		int n = coresEmOrdemDeJogo.size();
		if (n < MIN_JOGADORES || n > MAX_JOGADORES) {
			throw new IllegalArgumentException("O jogo aceita de " + MIN_JOGADORES + " a "
					+ MAX_JOGADORES + " jogadores (recebidos: " + n + ")");
		}
		for (Cor cor : coresEmOrdemDeJogo) {
			if (cor == null) {
				throw new IllegalArgumentException("Cor nula");
			}
			if (jogadores.containsKey(cor)) {
				throw new IllegalArgumentException("Cor repetida: " + cor);
			}
			jogadores.put(cor, new Jogador(cor));
			ordem.add(cor);
		}
		distribuirMoedasIniciais();
	}

	/** 1 moeda para o 1º e o 2º a jogar, 2 moedas para os demais. */
	private void distribuirMoedasIniciais() {
		for (int posicao = 0; posicao < ordem.size(); posicao++) {
			int moedas = (posicao < 2) ? 1 : 2;
			jogadores.get(ordem.get(posicao)).adicionarTributo(TipoTributo.MOEDA, moedas);
		}
	}

	int getNumeroDeJogadores() {
		return ordem.size();
	}

	/** @return o jogador da cor dada, ou {@code null} se ela não está em jogo */
	Jogador getJogador(Cor cor) {
		return jogadores.get(cor);
	}

	/** Jogadores na ordem de jogo. */
	List<Jogador> getJogadoresEmOrdem() {
		List<Jogador> lista = new ArrayList<Jogador>();
		for (Cor cor : ordem) {
			lista.add(jogadores.get(cor));
		}
		return Collections.unmodifiableList(lista);
	}

	Jogador getJogadorDaVez() {
		return jogadores.get(ordem.get(indiceDaVez));
	}

	Jogador getPrimeiroJogador() {
		return jogadores.get(ordem.get(0));
	}

	/** Passa a vez para o próximo jogador no sentido horário. */
	Jogador passarVez() {
		indiceDaVez = (indiceDaVez + 1) % ordem.size();
		return getJogadorDaVez();
	}
}
