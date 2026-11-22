package model;

/*
 * O que vem impresso na base de cada coluna. Todo tabuleiro tem os mesmos
 * três conjuntos nas coloridas, só muda a cor em que cada um aparece (a
 * tabela está no TabuleiroJogador). A branca é igual em todos.
 */
enum AcoesColuna {
	// 1 cavalo, 2 punhos e o Khan.
	CAVALO_DOIS_PUNHOS_KHAN(1, 2, 0, true),
	// 2 cavalos e 1 punho.
	DOIS_CAVALOS_PUNHO(2, 1, 0, false),
	// 1 cavalo, 1 punho e 1 moeda virtual.
	CAVALO_PUNHO_MOEDA(1, 1, 1, false),
	// Coluna branca: 2 cavalos, o Khan e recuperar as fichas.
	BRANCA(2, 0, 0, true);

	private final int cavalos;
	private final int punhos;
	private final int moedasVirtuais;
	private final boolean usaKhan;

	AcoesColuna(int cavalos, int punhos, int moedasVirtuais, boolean usaKhan) {
		this.cavalos = cavalos;
		this.punhos = punhos;
		this.moedasVirtuais = moedasVirtuais;
		this.usaKhan = usaKhan;
	}

	int getCavalos() {
		return cavalos;
	}

	int getPunhos() {
		return punhos;
	}

	int getMoedasVirtuais() {
		return moedasVirtuais;
	}

	// Se a coluna tem o ícone do Khan (usar o Khan é obrigatório, pág. 9).
	boolean usaKhan() {
		return usaKhan;
	}
}
