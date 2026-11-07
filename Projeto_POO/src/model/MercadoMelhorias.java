package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Área de melhorias (págs. 4, 11 e 16): a pilha das 44 peças de melhoria,
 * viradas para baixo, e as peças disponíveis, viradas para cima.
 *
 * Regras:
 * <ul>
 * <li>na preparação, 5 peças são reveladas;</li>
 * <li>o jogador compra uma das disponíveis ou uma aleatória do topo da
 * pilha;</li>
 * <li>depois de uma compra as restantes vão para a direita, e nenhuma peça
 * nova é revelada; isso só acontece quando alguém coloca o Khan na área de
 * melhorias ({@link #renovar()}).</li>
 * </ul>
 *
 * Quantidades por tipo (pág. 16): moeda, espada e yurt virtuais 8 cada,
 * punho 6, dois cavalos 9 e 1 de cada tesouro virtual. O manual não diz
 * quantas peças de cada tipo há em cada cor; a tabela {@link #QUANTIDADES}
 * reparte as peças igualmente entre as três cores e pode ser corrigida com a
 * caixa do jogo em mãos.
 */
class MercadoMelhorias {
	static final int PECAS_REVELADAS = 5;

	/** { tipo, amarelas, vermelhas, verdes } */
	private static final Object[][] QUANTIDADES = {
			{ TipoMelhoria.MOEDA_VIRTUAL, 3, 3, 2 },
			{ TipoMelhoria.ESPADA_VIRTUAL, 3, 2, 3 },
			{ TipoMelhoria.YURT_VIRTUAL, 2, 3, 3 },
			{ TipoMelhoria.PUNHO, 2, 2, 2 },
			{ TipoMelhoria.DOIS_CAVALOS, 3, 3, 3 },
	};

	private final List<PecaMelhoria> pilha = new ArrayList<PecaMelhoria>();
	/** Peças viradas para cima; o índice 0 é a mais à esquerda. */
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

	/** Total de peças ainda no mercado (pilha + disponíveis). */
	int getTotal() {
		return pilha.size() + disponiveis.size();
	}

	/** Peça disponível na posição dada, sem retirá-la, ou {@code null}. */
	PecaMelhoria verDisponivel(int indice) {
		return indice >= 0 && indice < disponiveis.size() ? disponiveis.get(indice) : null;
	}

	/** Peça do topo da pilha, sem retirá-la, ou {@code null} se a pilha acabou. */
	PecaMelhoria verTopo() {
		return pilha.isEmpty() ? null : pilha.get(pilha.size() - 1);
	}

	/** Retira a peça disponível da posição dada; as outras vão para a direita. */
	PecaMelhoria retirarDisponivel(int indice) {
		if (indice < 0 || indice >= disponiveis.size()) {
			throw new IllegalArgumentException("Não há melhoria disponível na posição " + indice);
		}
		return disponiveis.remove(indice);
	}

	/** Retira a peça do topo da pilha (compra às cegas). */
	PecaMelhoria retirarDoTopo() {
		if (pilha.isEmpty()) {
			throw new IllegalStateException("A pilha de melhorias acabou");
		}
		return pilha.remove(pilha.size() - 1);
	}

	/** Devolve uma peça ao fundo da pilha. */
	void devolverAoFundo(PecaMelhoria peca) {
		pilha.add(0, peca);
	}

	/**
	 * Renova as peças disponíveis (efeito do Khan na área de melhorias): as
	 * que estavam viradas para cima vão para o fundo da pilha e 5 novas são
	 * reveladas.
	 */
	void renovar() {
		pilha.addAll(0, disponiveis);
		disponiveis.clear();
		revelar(PECAS_REVELADAS);
	}
}
