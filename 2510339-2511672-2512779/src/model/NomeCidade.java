package model;

/*
 As 9 cidades do mapa (uma peça de cidade para cada) e a região em que
 cada uma fica.
 */

enum NomeCidade {
	KIEV("Kiev", Regiao.RUSSIA),
	MOSCOU("Moscou", Regiao.RUSSIA),
	SARAI("Sarai", Regiao.RUSSIA),
	SAMARCANDA("Samarcanda", Regiao.PERSIA),
	BAGDA("Bagdá", Regiao.PERSIA),
	CABUL("Cabul", Regiao.PERSIA),
	PEQUIM("Pequim", Regiao.CHINA),
	KAESONG("Kaesong", Regiao.CHINA),
	CANTAO("Cantão", Regiao.CHINA);

	private final String nome;
	private final Regiao regiao;

	NomeCidade(String nome, Regiao regiao) {
		this.nome = nome;
		this.regiao = regiao;
	}

	String getNome() {
		return nome;
	}

	Regiao getRegiao() {
		return regiao;
	}
}
