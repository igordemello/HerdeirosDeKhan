package model;

/**
 * Tipos de peça de melhoria (pág. 16), com o que cada uma acrescenta à
 * coluna onde é colocada.
 *
 * Aqui só interessa o que afeta as ações da coluna (cavalos e punhos). A
 * compra das melhorias, a regra de cor e a melhoria grátis da coluna branca
 * ficam com a parte de melhorias.
 */
enum TipoMelhoria {
	MOEDA_VIRTUAL(0, 0),
	TESOURO_VIRTUAL(0, 0),
	ESPADA_VIRTUAL(0, 0),
	YURT_VIRTUAL(0, 0),
	PUNHO(0, 1),          // ação extra de pegar 1 peça de tributo
	DOIS_CAVALOS(2, 0);   // dois movimentos extras

	private final int cavalos;
	private final int punhos;

	TipoMelhoria(int cavalos, int punhos) {
		this.cavalos = cavalos;
		this.punhos = punhos;
	}

	int getCavalos() {
		return cavalos;
	}

	int getPunhos() {
		return punhos;
	}
}
