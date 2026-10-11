package model;

/*
 Monta o tabuleiro principal da partida a partir de tabelas de dados.
 
 Os dados do mapa ficam separados da lógica do {Tabuleiro}: para
 completar ou corrigir o mapa basta editar as tabelas abaixo, sem mexer em
 nenhuma regra (princípio aberto/fechado).
 
 TODO: transcrever as tabelas a partir do tabuleiro físico. Convenção
 * sugerida para os identificadores: R01, R02... (Rússia), C01... (China),
 * P01... (Pérsia) para paradas; PV01, PV02... para províncias. Karakorum e
 * as 9 cidades já são criadas pelo Tabuleiro (use Tabuleiro.ID_KARAKORUM e
 * os nomes de {@link NomeCidade}). Devem existir 6 paradas DUPLA
 * (conselheiros) e 4 províncias do Khan, cada uma com 2 alvos.
 */

final class FabricaTabuleiro {

	/** { id, TipoParada (SIMPLES ou DUPLA), Regiao } */
	private static final String[][] PARADAS = {
			// { "R01", "SIMPLES", "RUSSIA" },
			// { "R02", "DUPLA", "RUSSIA" },
	};

	/** Rotas: { idParadaA, idParadaB }. Cada rota aparece uma única vez. */
	private static final String[][] ROTAS = {
			// { "R01", "R02" },
			// { "R02", "KARAKORUM" },
	};

	/** { id, TipoTributo, "KHAN" para província do Khan ou "" } */
	private static final String[][] PROVINCIAS = {
			// { "PV01", "ESPADA", "" },
			// { "PV02", "MOEDA", "KHAN" },
	};

	/** { idParada, idProvincia } */
	private static final String[][] PARADA_PROVINCIA = {
			// { "R01", "PV01" },
	};

	/** { idParada, NomeCidade } */
	private static final String[][] PARADA_CIDADE = {
			// { "R01", "KIEV" },
	};

	/** Setas do Khan: { idProvinciaDoKhan, idProvinciaAlvo } */
	private static final String[][] ALVOS_DO_KHAN = {
			// { "PV02", "PV01" },
	};

	private FabricaTabuleiro() {
	}

	/** Cria o tabuleiro da partida, ainda não preparado. */
	static Tabuleiro criarTabuleiroPadrao() {
		Tabuleiro tabuleiro = new Tabuleiro();
		for (String[] linha : PARADAS) {
			tabuleiro.adicionarParada(linha[0], TipoParada.valueOf(linha[1]), Regiao.valueOf(linha[2]));
		}
		for (String[] linha : ROTAS) {
			tabuleiro.ligarParadas(linha[0], linha[1]);
		}
		for (String[] linha : PROVINCIAS) {
			tabuleiro.adicionarProvincia(linha[0], TipoTributo.valueOf(linha[1]), "KHAN".equals(linha[2]));
		}
		for (String[] linha : PARADA_PROVINCIA) {
			tabuleiro.ligarParadaAProvincia(linha[0], linha[1]);
		}
		for (String[] linha : PARADA_CIDADE) {
			tabuleiro.ligarParadaACidade(linha[0], NomeCidade.valueOf(linha[1]));
		}
		for (String[] linha : ALVOS_DO_KHAN) {
			tabuleiro.definirAlvoDoKhan(linha[0], linha[1]);
		}
		return tabuleiro;
	}
}
