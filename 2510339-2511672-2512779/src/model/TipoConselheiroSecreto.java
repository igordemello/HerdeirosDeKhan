package model;

/**
 * As 10 cartas de conselheiro secreto (pág. 19). Cada uma dá 1, 2 ou 3 votos
 * no final do jogo conforme a quantidade atingida pelo jogador ultrapasse
 * os três limiares impressos na carta.
 */
enum TipoConselheiroSecreto {
	VIAJANTE("Viajante", "Ter um grupo de yurts conectados", 3, 6, 8),
	SOLDADO("Soldado", "Ter peças de melhoria", 4, 7, 10),
	COLECIONADOR("Colecionador", "Ter peças de melhoria com símbolos diferentes", 4, 6, 7),
	SACERDOTE("Sacerdote", "Ter pares de melhorias com o mesmo símbolo", 1, 2, 4),
	GOVERNADOR("Governador", "Regiões com ao menos 2 yurts e 2 melhorias na coluna associada", 1, 2, 3),
	BAJULADOR("Bajulador", "Províncias de Khan com ao menos 2 yurts adjacentes", 2, 3, 4),
	PREFEITO("Prefeito", "Cidades com ao menos 2 yurts adjacentes", 2, 3, 4),
	CIDADAO("Cidadão", "Cidades com ao menos 1 yurt adjacente", 3, 6, 8),
	CAVALEIRO("Cavaleiro", "Peças de melhoria com símbolos de cavalo ou punho", 2, 4, 5),
	MENSAGEIRO("Mensageiro", "Paradas duplas com ao menos 1 yurt (exceto Karakorum)", 2, 4, 6);

	private final String nome;
	private final String descricao;
	private final int[] limiares; // quantidade mínima para 1, 2 e 3 votos

	TipoConselheiroSecreto(String nome, String descricao, int paraUmVoto, int paraDoisVotos, int paraTresVotos) {
		this.nome = nome;
		this.descricao = descricao;
		this.limiares = new int[] { paraUmVoto, paraDoisVotos, paraTresVotos };
	}

	String getNome() {
		return nome;
	}

	String getDescricao() {
		return descricao;
	}

	/** Quantidade mínima necessária para obter {@code votos} votos (1 a 3). */
	int getLimiar(int votos) {
		if (votos < 1 || votos > 3) {
			throw new IllegalArgumentException("Votos devem estar entre 1 e 3");
		}
		return limiares[votos - 1];
	}

	/**
	 * Calcula quantos votos (0 a 3) a carta concede para a quantidade
	 * atingida pelo jogador na condição da carta.
	 */
	int calcularVotos(int quantidadeAtingida) {
		int votos = 0;
		for (int limiar : limiares) {
			if (quantidadeAtingida >= limiar) {
				votos++;
			}
		}
		return votos;
	}
}
