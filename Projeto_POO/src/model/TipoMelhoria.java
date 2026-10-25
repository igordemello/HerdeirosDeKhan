package model;

/**
 * Tipos de peça de melhoria (pág. 16), com o que cada uma acrescenta à
 * coluna onde é colocada.
 *
 * A sigla é a usada nos nomes das imagens das peças (ex.: CAV-V = dois
 * cavalos de fundo verde). Os tesouros virtuais existem numa cor só (1 peça
 * por tesouro), por isso têm a cor fixa; os demais existem nas três cores.
 *
 * A compra das melhorias e a melhoria grátis da coluna branca ficam com a
 * parte de melhorias.
 */
enum TipoMelhoria {
	MOEDA_VIRTUAL("MOE", 0, 0, null),
	ESPADA_VIRTUAL("ESP", 0, 0, null),
	YURT_VIRTUAL("CAS", 0, 0, null),
	PUNHO("MAO", 0, 1, null),          // ação extra de pegar 1 peça de tributo
	DOIS_CAVALOS("CAV", 2, 0, null),   // dois movimentos extras
	TESOURO_VIRTUAL_BOI("BOI", 0, 0, ColunaJogador.AMARELA),
	TESOURO_VIRTUAL_ESPECIARIA("CUI", 0, 0, ColunaJogador.AMARELA),
	TESOURO_VIRTUAL_FERRO("MOB", 0, 0, ColunaJogador.VERMELHA),
	TESOURO_VIRTUAL_PELE("MOP", 0, 0, ColunaJogador.VERDE),
	TESOURO_VIRTUAL_SEDA("PER", 0, 0, ColunaJogador.VERDE);

	private final String sigla;
	private final int cavalos;
	private final int punhos;
	private final ColunaJogador corFixa;

	TipoMelhoria(String sigla, int cavalos, int punhos, ColunaJogador corFixa) {
		this.sigla = sigla;
		this.cavalos = cavalos;
		this.punhos = punhos;
		this.corFixa = corFixa;
	}

	String getSigla() {
		return sigla;
	}

	int getCavalos() {
		return cavalos;
	}

	int getPunhos() {
		return punhos;
	}

	/** Cor da única peça deste tipo, ou {@code null} se existe nas três cores. */
	ColunaJogador getCorFixa() {
		return corFixa;
	}
}
