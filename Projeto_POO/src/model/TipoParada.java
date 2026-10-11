package model;

/**
 * Tipos de parada do mapa e as regras de capacidade de cada um.
 *
 * SIMPLES: termina o movimento nela no máximo 1 peão; aceita 1 yurt

 DUPLA: cabem 2 peões e 2 yurts (podem ser do mesmo jogador); é uma
 * parada de conselheiro 
 * KARAKORUM: a capital; cabem todos os peões, não pode ter yurts
 * (exceto o yurt inicial de Tolui) e também tem um conselheiro.
 * 
 */
enum TipoParada {
	SIMPLES(1, 1, false),
	DUPLA(2, 2, true),
	KARAKORUM(Integer.MAX_VALUE, 0, true);

	private final int capacidadePeoes;
	private final int capacidadeYurts;
	private final boolean temConselheiro;

	TipoParada(int capacidadePeoes, int capacidadeYurts, boolean temConselheiro) {
		this.capacidadePeoes = capacidadePeoes;
		this.capacidadeYurts = capacidadeYurts;
		this.temConselheiro = temConselheiro;
	}

	/** Quantos peões podem terminar o movimento nesta parada. */
	int getCapacidadePeoes() {
		return capacidadePeoes;
	}

	/** Quantos yurts podem ser construídos nesta parada. */
	int getCapacidadeYurts() {
		return capacidadeYurts;
	}

	/** Se é possível entregar tesouros a um conselheiro nesta parada. */
	boolean temConselheiro() {
		return temConselheiro;
	}
}
