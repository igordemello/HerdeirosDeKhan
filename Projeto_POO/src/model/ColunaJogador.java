package model;

/**
 * As 4 colunas do tabuleiro do jogador (pág. 6), da esquerda para a direita:
 * três coloridas e a branca.
 *
 * Cada coluna tem, na base, as ações iniciais impressas no tabuleiro. Ao
 * ativar a coluna, o jogador faz essas ações mais as das melhorias que
 * colocou acima delas. Os cavalos dão o número de movimentos do turno.
 *
 * Valores transcritos do tabuleiro de Tolui mostrado nas págs. 6 e 12 do
 * manual: amarela = 1 cavalo, 1 punho e 1 moeda virtual; verde = 2 cavalos
 * e 1 punho; vermelha = 1 cavalo, 2 punhos e o Khan; branca = 2 cavalos, o
 * Khan e recuperar as fichas. Para corrigir, basta alterar as constantes.
 */
enum ColunaJogador {
	AMARELA(1, 1, 1, false, false),
	VERDE(2, 1, 0, false, false),
	VERMELHA(1, 2, 0, true, false),
	BRANCA(2, 0, 0, true, true);

	private final int cavalos;
	private final int punhos;
	private final int moedasVirtuais;
	private final boolean usaKhan;
	private final boolean recuperaFichas;

	ColunaJogador(int cavalos, int punhos, int moedasVirtuais, boolean usaKhan, boolean recuperaFichas) {
		this.cavalos = cavalos;
		this.punhos = punhos;
		this.moedasVirtuais = moedasVirtuais;
		this.usaKhan = usaKhan;
		this.recuperaFichas = recuperaFichas;
	}

	/** Cavalos impressos na base da coluna (sem contar melhorias). */
	int getCavalosIniciais() {
		return cavalos;
	}

	/** Punhos impressos na base da coluna (ações de pegar tributo). */
	int getPunhosIniciais() {
		return punhos;
	}

	int getMoedasVirtuaisIniciais() {
		return moedasVirtuais;
	}

	/** Se a coluna tem o ícone do Khan (usar o Khan é obrigatório, pág. 9). */
	boolean usaKhan() {
		return usaKhan;
	}

	/** Só a coluna branca recupera as fichas de ativação (pág. 9). */
	boolean recuperaFichas() {
		return recuperaFichas;
	}
}
