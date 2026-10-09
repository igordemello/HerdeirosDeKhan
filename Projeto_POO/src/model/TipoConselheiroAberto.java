package model;

/**
 * As 14 cartas de conselheiro aberto (pág. 18). O primeiro jogador a cumprir
 * a demanda de uma carta revelada ganha 1 voto e retira a carta do jogo.
 */
enum TipoConselheiroAberto {
	ESPADAS_4("Primeiro a ter 4 espadas (não precisa gastá-las)", 4),
	YURTS_NO_MAPA_6("Primeiro a ter 6 yurts no mapa", 6),
	YURTS_NO_MAPA_10("Primeiro a ter 10 yurts no mapa", 10),
	TESOUROS_4("Primeiro a ter 4 tesouros (não precisa gastá-los)", 4),
	TESOUROS_DIFERENTES_5("Primeiro a ter 5 tesouros diferentes (não precisa gastá-los)", 5),
	YURT_EM_CADA_REGIAO_1("Primeiro a ter 1 yurt em cada região", 1),
	YURTS_EM_CADA_REGIAO_2("Primeiro a ter 2 yurts em cada região", 2),
	PECAS_BONUS_2("Primeiro a conseguir 2 peças de bônus", 2),
	PECAS_BONUS_3("Primeiro a conseguir 3 peças de bônus", 3),
	CIDADES_CONQUISTADAS_2("Primeiro a conquistar 2 cidades", 2),
	CIDADES_CONQUISTADAS_3("Primeiro a conquistar 3 cidades", 3),
	LINHA_DE_MELHORIAS("Primeiro a completar uma linha de melhorias", 1),
	DUAS_COLUNAS_COM_2_MELHORIAS("Primeiro a ter 2 colunas com 2 melhorias cada", 2),
	QUATRO_MELHORIAS_EM_UMA_COLUNA("Primeiro a ter 4 melhorias em uma só coluna", 4);

	private final String descricao;
	private final int quantidade;

	TipoConselheiroAberto(String descricao, int quantidade) {
		this.descricao = descricao;
		this.quantidade = quantidade;
	}

	String getDescricao() {
		return descricao;
	}

	/** Número impresso na carta (quantidade exigida pela demanda). */
	int getQuantidade() {
		return quantidade;
	}
}
