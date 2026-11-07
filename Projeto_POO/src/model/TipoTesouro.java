package model;

/**
 * Os 5 tipos de tesouro (pág. 2: 40 peças, 8 de cada tipo). Os tesouros são
 * saqueados das cidades e entregues aos conselheiros em troca de votos.
 */
enum TipoTesouro {
	BOI("Boi"),
	ESPECIARIA("Especiaria"),
	FERRO("Ferro"),
	PELE("Pele"),
	SEDA("Seda");

	/** Peças de cada tipo na caixa do jogo. */
	static final int PECAS_POR_TIPO = 8;

	private final String nome;

	TipoTesouro(String nome) {
		this.nome = nome;
	}

	String getNome() {
		return nome;
	}
}
