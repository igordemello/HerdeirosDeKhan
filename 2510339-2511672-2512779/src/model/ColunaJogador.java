package model;

/**
 * As 4 colunas do tabuleiro do jogador (pág. 6), da esquerda para a direita,
 * como nos tabuleiros reais: vermelha, verde, amarela e branca.
 *
 * As ações impressas na base de cada coluna mudam de herdeiro para herdeiro
 * (ver {@link AcoesColuna} e {@link TabuleiroJogador}); aqui fica só o que
 * é igual em todos os tabuleiros.
 *
 * A sigla é a letra usada nas peças de melhoria da cor da coluna
 * (R = vermelha, V = verde, A = amarela); a branca não tem peças próprias.
 */
enum ColunaJogador {
	VERMELHA("R"),
	VERDE("V"),
	AMARELA("A"),
	BRANCA(null);

	private final String sigla;

	ColunaJogador(String sigla) {
		this.sigla = sigla;
	}

	/** Letra da cor (R, V ou A), ou {@code null} para a coluna branca. */
	String getSigla() {
		return sigla;
	}

	/** As três primeiras colunas são coloridas; a branca não. */
	boolean ehColorida() {
		return this != BRANCA;
	}

	/** Só a coluna branca recupera as fichas de ativação (pág. 9). */
	boolean recuperaFichas() {
		return this == BRANCA;
	}
}
