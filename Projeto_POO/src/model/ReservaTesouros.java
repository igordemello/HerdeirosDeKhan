package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Pilha de peças de tesouro viradas para baixo, ao lado do tabuleiro
 * (pág. 4). Começa com as 40 peças embaralhadas.
 *
 * É usada pelas peças de bônus de 3 tesouros e, na preparação das cidades,
 * para colocar 4 tesouros aleatórios em cada cidade revelada.
 */
class ReservaTesouros {
	private final List<TipoTesouro> pilha = new ArrayList<TipoTesouro>();

	ReservaTesouros(Random aleatorio) {
		if (aleatorio == null) {
			throw new IllegalArgumentException("Gerador aleatório nulo");
		}
		for (TipoTesouro tipo : TipoTesouro.values()) {
			for (int i = 0; i < TipoTesouro.PECAS_POR_TIPO; i++) {
				pilha.add(tipo);
			}
		}
		Collections.shuffle(pilha, aleatorio);
	}

	/** Tira até {@code quantidade} tesouros do topo (menos, se a pilha acabar). */
	List<TipoTesouro> comprar(int quantidade) {
		if (quantidade < 0) {
			throw new IllegalArgumentException("Quantidade negativa");
		}
		List<TipoTesouro> compradas = new ArrayList<TipoTesouro>();
		for (int i = 0; i < quantidade && !pilha.isEmpty(); i++) {
			compradas.add(pilha.remove(pilha.size() - 1));
		}
		return compradas;
	}

	/** Devolve tesouros ao fundo da pilha. */
	void devolver(List<TipoTesouro> tesouros) {
		pilha.addAll(0, tesouros);
	}

	int tamanho() {
		return pilha.size();
	}
}
