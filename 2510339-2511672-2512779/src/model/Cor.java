package model;

/*
 * Cores dos jogadores. Pela regra cada um escolhe uma cor e recebe o
 * tabuleiro (herdeiro) daquela cor. O manual não diz qual herdeiro é de qual
 * cor, então a gente escolheu; pra trocar é só mudar aqui.
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
