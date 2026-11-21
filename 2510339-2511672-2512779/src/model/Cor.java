package model;

/**
 * Cores dos jogadores. Segundo as regras, cada jogador escolhe uma cor e
 * recebe o tabuleiro de jogador (herdeiro) associado a essa cor.
 *
 * ATENÇÃO: a associação cor -> herdeiro abaixo deve ser conferida com os
 * tabuleiros físicos do jogo; basta alterar o herdeiro de cada constante.
 */
enum Cor {
	AZUL(Herdeiro.ALTANI),
	VERMELHO(Herdeiro.CHAGATAI),
	VERDE(Herdeiro.JOCHI),
	AMARELO(Herdeiro.OGEDEI),
	ROXO(Herdeiro.TOLUI);

	private final Herdeiro herdeiro;

	Cor(Herdeiro herdeiro) {
		this.herdeiro = herdeiro;
	}

	Herdeiro getHerdeiro() {
		return herdeiro;
	}
}
