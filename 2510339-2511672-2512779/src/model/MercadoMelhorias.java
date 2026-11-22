package model;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/*
 * Área de melhorias (págs. 4, 11 e 16): a pilha com as 44 peças viradas pra
 * baixo e as 5 viradas pra cima. O jogador compra uma das viradas ou uma às
 * cegas do topo. Depois da compra as que sobram andam pra direita e não
 * aparece peça nova; isso só acontece quando o Khan vai pra área de
 * melhorias.
 *
 * Quantidades (pág. 16): moeda, espada e yurt virtuais 8 de cada, punho 6,
 * dois cavalos 9 e 1 de cada tesouro virtual. O manual não fala quantas de
 * cada cor, então dividimos igual entre as três (tabela QUANTIDADES).
 */
class MercadoMelhorias {
	static final int PECAS_REVELADAS = 5;

	// { tipo, amarelas, vermelhas, verdes }
	private static final Object[][] QUANTIDADES = {
			{ TipoMelhoria.MOEDA_VIRTUAL, 3, 3, 2 },
			{ TipoMelhoria.ESPADA_VIRTUAL, 3, 2, 3 },
			{ TipoMelhoria.YURT_VIRTUAL, 2, 3, 3 },
			{ TipoMelhoria.PUNHO, 2, 2, 2 },
			{ TipoMelhoria.DOIS_CAVALOS, 3, 3, 3 },
	};

	private final List<PecaMelhoria> pilha = new ArrayList<PecaMelhoria>();
	// viradas pra cima; índice 0 = a mais da esquerda
	private final List<PecaMelhoria> disponiveis = new ArrayList<PecaMelhoria>();

	MercadoMelhorias(Random aleatorio) {
		if (aleatorio == null) {
			throw new IllegalArgumentException("Gerador aleatório nulo");
		}
		ColunaJogador[] cores = { ColunaJogador.AMARELA, ColunaJogador.VERMELHA, ColunaJogador.VERDE };
		for (Object[] linha : QUANTIDADES) {
			TipoMelhoria tipo = (TipoMelhoria) linha[0];
			for (int c = 0; c < cores.length; c++) {
				for (int i = 0; i < (Integer) linha[c + 1]; i++) {
					pilha.add(new PecaMelhoria(tipo, cores[c]));
				}
			}
		}
		for (TipoMelhoria tipo : TipoMelhoria.values()) {
			if (tipo.getCorFixa() != null) {
				pilha.add(new PecaMelhoria(tipo, tipo.getCorFixa())); // tesouros virtuais: 1 de cada
			}
		}
		Collections.shuffle(pilha, aleatorio);
		revelar(PECAS_REVELADAS);
	}

	private void revelar(int quantidade) {
		for (int i = 0; i < quantidade && !pilha.isEmpty(); i++) {
			disponiveis.add(pilha.remove(pilha.size() - 1));
		}
	}

	List<PecaMelhoria> getDisponiveis() {
		return Collections.unmodifiableList(disponiveis);
	}

	int getTamanhoDaPilha() {
		return pilha.size();
	}

	int getTotal() {
		return pilha.size() + disponiveis.size();
	}

	PecaMelhoria verDisponivel(int indice) {
		return indice >= 0 && indice < disponiveis.size() ? disponiveis.get(indice) : null;
	}

	PecaMelhoria verTopo() {
		return pilha.isEmpty() ? null : pilha.get(pilha.size() - 1);
	}

	// as outras andam pra direita
	PecaMelhoria retirarDisponivel(int indice) {
		if (indice < 0 || indice >= disponiveis.size()) {
			throw new IllegalArgumentException("Não há melhoria disponível na posição " + indice);
		}
		return disponiveis.remove(indice);
	}

	PecaMelhoria retirarDoTopo() {
		if (pilha.isEmpty()) {
			throw new IllegalStateException("A pilha de melhorias acabou");
		}
		return pilha.remove(pilha.size() - 1);
	}

	void devolverAoFundo(PecaMelhoria peca) {
		pilha.add(0, peca);
	}

	// as 5 viradas vão pro fundo da pilha e saem 5 novas
	void renovar() {
		pilha.addAll(0, disponiveis);
		disponiveis.clear();
		revelar(PECAS_REVELADAS);
	}

	// Espaços numerados de 0 a 4 da esquerda pra direita. Como as peças andam
	// pra direita depois de uma compra, os vazios são sempre os da esquerda.
	// No tabuleiro, o espaço 1 tem o desenho de uma moeda e o 3 de uma espada.
	static final int ESPACO_DA_MOEDA = 1;
	static final int ESPACO_DA_ESPADA = 3;

	boolean espacoVazio(int espaco) {
		return espaco < PECAS_REVELADAS - disponiveis.size();
	}

	// Usado quando o Khan vai pra área de melhorias: só completa os espaços
	// vazios (diferente do renovar(), que troca todas).
	void reporVazios() {
		int vazios = PECAS_REVELADAS - disponiveis.size();
		for (int i = 0; i < vazios && !pilha.isEmpty(); i++) {
			disponiveis.add(0, pilha.remove(pilha.size() - 1));
		}
	}

	// salvar e carregar (4ª it.)

	// pilha do fundo pro topo; disponíveis da esquerda pra direita
	void salvar(PrintWriter saida) {
		ArquivoPartida.linha(saida, "disponiveis", ArquivoPartida.juntar(PecaMelhoria.ids(disponiveis)));
		ArquivoPartida.linha(saida, "pilha", ArquivoPartida.juntar(PecaMelhoria.ids(pilha)));
	}

	void carregar(Map<String, String> secao) {
		List<PecaMelhoria> novasDisponiveis = PecaMelhoria.deIds(ArquivoPartida.lista(secao, "disponiveis"));
		if (novasDisponiveis.size() > PECAS_REVELADAS) {
			throw new IllegalArgumentException("Melhorias disponíveis demais no arquivo");
		}
		List<PecaMelhoria> novaPilha = PecaMelhoria.deIds(ArquivoPartida.lista(secao, "pilha"));
		disponiveis.clear();
		disponiveis.addAll(novasDisponiveis);
		pilha.clear();
		pilha.addAll(novaPilha);
	}
}
