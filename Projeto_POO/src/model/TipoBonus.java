package model;

/**
 * As 7 peças de bônus (pág. 17), uma de cada tipo. Ficam sobre os 7
 * conselheiros; quem faz a primeira entrega a um conselheiro leva a peça e
 * pode usá-la uma única vez, quando quiser, no seu turno.
 */
enum TipoBonus {
	VOTO("1 voto na trilha do Kurultai"),
	DUAS_ESPADAS("2 peças de tributo de espada"),
	DUAS_MOEDAS("2 peças de tributo de moeda"),
	DOIS_YURTS("2 peças de tributo de yurt"),
	TRES_MOVIMENTOS_E_PUNHO("3 movimentos extras e 1 ação extra de pegar tributos"),
	DOIS_PUNHOS("2 ações extras de pegar tributos"),
	TRES_TESOUROS("Pegue 3 tesouros da pilha e fique com 1");

	private final String descricao;

	TipoBonus(String descricao) {
		this.descricao = descricao;
	}

	String getDescricao() {
		return descricao;
	}
}
