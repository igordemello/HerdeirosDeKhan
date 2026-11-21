package model;

/*
 Monta o tabuleiro principal da partida a partir de tabelas de dados.

 Os dados do mapa ficam separados da lógica do {Tabuleiro}: para
 completar ou corrigir o mapa basta editar as tabelas abaixo, sem mexer em
 nenhuma regra (princípio aberto/fechado).

 Mapa transcrito do tabuleiro físico. Identificadores:
   R01..R11 = paradas da Rússia (amarelas), P01..P11 = Pérsia (verdes),
   C01..C11 = China (vermelhas), KARAKORUM = a capital (Tabuleiro.ID_KARAKORUM);
   PV01..PV12 = províncias.
 São 34 paradas (6 duplas, uma ao lado de cada conselheiro), 54 rotas,
 12 províncias (4 delas do Khan, cada uma com 2 setas) e as 9 cidades com
 as 3 paradas do anel em volta de cada uma.

 As coordenadas de cada parada na imagem do tabuleiro ficam na View
 (view.CoordenadasParadas), pois são informação de desenho.
 */

final class FabricaTabuleiro {

	/** { id, TipoParada (SIMPLES ou DUPLA), Regiao } */
	private static final String[][] PARADAS = {
			// Rússia
			{ "R01", "SIMPLES", "RUSSIA" },   // à esquerda de Kiev
			{ "R02", "SIMPLES", "RUSSIA" },   // acima de Kiev, à direita
			{ "R03", "SIMPLES", "RUSSIA" },   // abaixo de Kiev, à direita
			{ "R04", "DUPLA", "RUSSIA" },     // conselheiro do alto (pergaminhos)
			{ "R05", "SIMPLES", "RUSSIA" },   // à esquerda de Moscou
			{ "R06", "SIMPLES", "RUSSIA" },   // acima de Moscou, à direita
			{ "R07", "SIMPLES", "RUSSIA" },   // abaixo de Moscou
			{ "R08", "SIMPLES", "RUSSIA" },   // à esquerda de Sarai, acima de Karakorum
			{ "R09", "SIMPLES", "RUSSIA" },   // acima de Sarai
			{ "R10", "SIMPLES", "RUSSIA" },   // abaixo de Sarai, à direita
			{ "R11", "DUPLA", "RUSSIA" },     // conselheiro do alto à direita (bigornas)
			// Pérsia
			{ "P01", "DUPLA", "PERSIA" },     // conselheiro da esquerda (cavalos)
			{ "P02", "SIMPLES", "PERSIA" },   // acima de Samarcanda
			{ "P03", "SIMPLES", "PERSIA" },   // à esquerda de Samarcanda
			{ "P04", "SIMPLES", "PERSIA" },   // acima de Bagdá, à esquerda
			{ "P05", "SIMPLES", "PERSIA" },   // abaixo de Samarcanda, à direita
			{ "P06", "SIMPLES", "PERSIA" },   // à direita de Bagdá
			{ "P07", "SIMPLES", "PERSIA" },   // abaixo de Bagdá, à esquerda
			{ "P08", "DUPLA", "PERSIA" },     // conselheiro de baixo à esquerda (tigelas)
			{ "P09", "SIMPLES", "PERSIA" },   // acima de Cabul
			{ "P10", "SIMPLES", "PERSIA" },   // à direita de Cabul
			{ "P11", "SIMPLES", "PERSIA" },   // abaixo de Cabul
			// China
			{ "C01", "SIMPLES", "CHINA" },    // acima de Kaesong, à esquerda
			{ "C02", "SIMPLES", "CHINA" },    // à direita de Kaesong
			{ "C03", "SIMPLES", "CHINA" },    // abaixo de Kaesong
			{ "C04", "SIMPLES", "CHINA" },    // acima de Pequim
			{ "C05", "DUPLA", "CHINA" },      // conselheiro da direita (bois)
			{ "C06", "SIMPLES", "CHINA" },    // à esquerda de Pequim
			{ "C07", "SIMPLES", "CHINA" },    // abaixo de Pequim, à direita
			{ "C08", "SIMPLES", "CHINA" },    // acima de Cantão, à direita
			{ "C09", "DUPLA", "CHINA" },      // conselheiro de baixo (baús)
			{ "C10", "SIMPLES", "CHINA" },    // à esquerda de Cantão
			{ "C11", "SIMPLES", "CHINA" },    // abaixo de Cantão, à direita
	};

	/** Rotas: { idParadaA, idParadaB }. Cada rota aparece uma única vez. */
	private static final String[][] ROTAS = {
			// Rússia
			{ "R01", "R02" }, { "R01", "R03" }, { "R02", "R03" },     // anel de Kiev
			{ "R02", "R04" }, { "R02", "R05" }, { "R04", "R06" },
			{ "R05", "R06" }, { "R05", "R07" }, { "R06", "R07" },     // anel de Moscou
			{ "R06", "R08" },
			{ "R08", "R09" }, { "R09", "R10" }, { "R08", "R10" },     // anel de Sarai
			{ "R10", "R11" },
			{ "R08", "KARAKORUM" }, { "R11", "KARAKORUM" },
			// Rússia - Pérsia e Rússia - China
			{ "R03", "P01" }, { "R05", "P01" }, { "R07", "P02" },
			{ "R11", "C01" },
			// Pérsia
			{ "P01", "P02" }, { "P01", "P04" },
			{ "P02", "P03" }, { "P02", "P05" }, { "P03", "P05" },     // anel de Samarcanda
			{ "P03", "P04" },
			{ "P04", "P06" }, { "P04", "P07" }, { "P06", "P07" },     // anel de Bagdá
			{ "P06", "P08" }, { "P08", "P09" }, { "P05", "P09" },
			{ "P09", "P10" }, { "P09", "P11" }, { "P10", "P11" },     // anel de Cabul
			{ "P05", "KARAKORUM" },
			// Pérsia - China
			{ "P09", "C06" }, { "P10", "C09" },
			// China
			{ "C01", "C02" }, { "C01", "C03" }, { "C02", "C03" },     // anel de Kaesong
			{ "C03", "C04" }, { "C03", "C05" },
			{ "C04", "C06" }, { "C04", "C07" }, { "C06", "C07" },     // anel de Pequim
			{ "C06", "C09" }, { "C07", "C08" }, { "C05", "C08" },
			{ "C08", "C10" }, { "C08", "C11" }, { "C10", "C11" },     // anel de Cantão
			{ "C09", "C10" },
			{ "C04", "KARAKORUM" },
	};

	/**
	 * { id, TipoTributo, "KHAN" para província do Khan ou "" }. O tipo de
	 * tributo é o do ícone impresso na província (nas do Khan, o ícone fica ao
	 * lado da cabeça do Khan).
	 */
	private static final String[][] PROVINCIAS = {
			{ "PV01", "YURT", "KHAN" },   // entre Kiev e Moscou
			{ "PV02", "ESPADA", "" },     // acima de Moscou
			{ "PV03", "MOEDA", "" },      // entre Moscou e o conselheiro P01
			{ "PV04", "YURT", "" },       // entre Moscou e Karakorum
			{ "PV05", "MOEDA", "KHAN" },  // abaixo de Sarai
			{ "PV06", "ESPADA", "" },     // entre Karakorum e Kaesong
			{ "PV07", "ESPADA", "" },     // acima de Samarcanda, à esquerda
			{ "PV08", "MOEDA", "KHAN" },  // entre Samarcanda, Bagdá e Cabul
			{ "PV09", "YURT", "" },       // abaixo de Karakorum
			{ "PV10", "MOEDA", "" },      // entre Pequim e o conselheiro C05
			{ "PV11", "YURT", "KHAN" },   // entre Pequim e Cantão
			{ "PV12", "ESPADA", "" },     // entre Cabul e Pequim
	};

	/** { idParada, idProvincia }: paradas na borda de cada província. */
	private static final String[][] PARADA_PROVINCIA = {
			{ "R02", "PV01" }, { "R03", "PV01" }, { "R05", "PV01" }, { "P01", "PV01" },
			{ "R02", "PV02" }, { "R04", "PV02" }, { "R05", "PV02" }, { "R06", "PV02" },
			{ "R05", "PV03" }, { "R07", "PV03" }, { "P01", "PV03" }, { "P02", "PV03" },
			{ "R06", "PV04" }, { "R07", "PV04" }, { "R08", "PV04" }, { "KARAKORUM", "PV04" },
			{ "P02", "PV04" }, { "P05", "PV04" },
			{ "R08", "PV05" }, { "R10", "PV05" }, { "R11", "PV05" }, { "KARAKORUM", "PV05" },
			{ "R11", "PV06" }, { "KARAKORUM", "PV06" }, { "C01", "PV06" }, { "C03", "PV06" },
			{ "C04", "PV06" },
			{ "P01", "PV07" }, { "P02", "PV07" }, { "P03", "PV07" }, { "P04", "PV07" },
			{ "P03", "PV08" }, { "P04", "PV08" }, { "P05", "PV08" }, { "P06", "PV08" },
			{ "P08", "PV08" }, { "P09", "PV08" },
			{ "KARAKORUM", "PV09" }, { "P05", "PV09" }, { "P09", "PV09" }, { "C04", "PV09" },
			{ "C06", "PV09" },
			{ "C03", "PV10" }, { "C04", "PV10" }, { "C05", "PV10" }, { "C07", "PV10" },
			{ "C08", "PV10" },
			{ "C06", "PV11" }, { "C07", "PV11" }, { "C08", "PV11" }, { "C09", "PV11" },
			{ "C10", "PV11" },
			{ "P09", "PV12" }, { "P10", "PV12" }, { "C06", "PV12" }, { "C09", "PV12" },
	};

	/** { idParada, NomeCidade }: as 3 paradas do anel em volta de cada cidade. */
	private static final String[][] PARADA_CIDADE = {
			{ "R01", "KIEV" }, { "R02", "KIEV" }, { "R03", "KIEV" },
			{ "R05", "MOSCOU" }, { "R06", "MOSCOU" }, { "R07", "MOSCOU" },
			{ "R08", "SARAI" }, { "R09", "SARAI" }, { "R10", "SARAI" },
			{ "P02", "SAMARCANDA" }, { "P03", "SAMARCANDA" }, { "P05", "SAMARCANDA" },
			{ "P04", "BAGDA" }, { "P06", "BAGDA" }, { "P07", "BAGDA" },
			{ "P09", "CABUL" }, { "P10", "CABUL" }, { "P11", "CABUL" },
			{ "C01", "KAESONG" }, { "C02", "KAESONG" }, { "C03", "KAESONG" },
			{ "C04", "PEQUIM" }, { "C06", "PEQUIM" }, { "C07", "PEQUIM" },
			{ "C08", "CANTAO" }, { "C10", "CANTAO" }, { "C11", "CANTAO" },
	};

	/** Setas do Khan: { idProvinciaDoKhan, idProvinciaAlvo } */
	private static final String[][] ALVOS_DO_KHAN = {
			{ "PV01", "PV02" }, { "PV01", "PV03" },
			{ "PV05", "PV04" }, { "PV05", "PV06" },
			{ "PV08", "PV07" }, { "PV08", "PV09" },
			{ "PV11", "PV10" }, { "PV11", "PV12" },
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
