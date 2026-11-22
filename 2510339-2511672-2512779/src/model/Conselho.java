package model;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*
 * Os 7 conselheiros do mapa, pelo id da parada. Os pedidos a gente copiou
 * da imagem do tabuleiro:
 *   R04 2 sedas, R11 2 ferros, P01 2 peles, P08 2 especiarias, C05 2 bois
 *   (1 voto cada entrega)
 *   C09 3 tesouros quaisquer (1 voto)
 *   Karakorum 1 ferro, 1 pele e 1 boi (2 votos). Não usamos a peça opcional
 *   de Karakorum da pág. 4, vale o que está impresso.
 * Na preparação cada um recebe uma peça de bônus sorteada (pág. 4).
 */
class Conselho {
	static final String ID_KARAKORUM = Tabuleiro.ID_KARAKORUM;

	private final Map<String, Conselheiro> conselheiros = new HashMap<String, Conselheiro>();

	Conselho() {
		adicionar(Conselheiro.comPedido("R04", 1, Arrays.asList(TipoTesouro.SEDA, TipoTesouro.SEDA)));
		adicionar(Conselheiro.comPedido("R11", 1, Arrays.asList(TipoTesouro.FERRO, TipoTesouro.FERRO)));
		adicionar(Conselheiro.comPedido("P01", 1, Arrays.asList(TipoTesouro.PELE, TipoTesouro.PELE)));
		adicionar(Conselheiro.comPedido("P08", 1, Arrays.asList(TipoTesouro.ESPECIARIA, TipoTesouro.ESPECIARIA)));
		adicionar(Conselheiro.comPedido("C05", 1, Arrays.asList(TipoTesouro.BOI, TipoTesouro.BOI)));
		adicionar(Conselheiro.deQualquerTipo("C09", 1, 3));
		adicionar(Conselheiro.comPedido(ID_KARAKORUM, 2,
				Arrays.asList(TipoTesouro.FERRO, TipoTesouro.PELE, TipoTesouro.BOI)));
	}

	private void adicionar(Conselheiro c) {
		conselheiros.put(c.getIdParada(), c);
	}

	void distribuirBonus(Random aleatorio) {
		List<TipoBonus> pecas = new ArrayList<TipoBonus>(Arrays.asList(TipoBonus.values()));
		Collections.shuffle(pecas, aleatorio);
		List<String> ids = new ArrayList<String>(conselheiros.keySet());
		Collections.sort(ids); // ordem fixa, para a mesma semente dar o mesmo resultado
		for (int i = 0; i < ids.size(); i++) {
			conselheiros.get(ids.get(i)).colocarBonus(pecas.get(i));
		}
	}

	// null se a parada não tem conselheiro
	Conselheiro getConselheiro(String idParada) {
		return conselheiros.get(idParada);
	}

	Collection<Conselheiro> getConselheiros() {
		return Collections.unmodifiableCollection(conselheiros.values());
	}

	// salvar e carregar (4ª it.)

	// pra cada conselheiro: o bônus que ainda está nele e quantas entregas teve
	void salvar(PrintWriter saida) {
		List<String> ids = new ArrayList<String>(conselheiros.keySet());
		Collections.sort(ids);
		for (String id : ids) {
			Conselheiro c = conselheiros.get(id);
			ArquivoPartida.linha(saida, id + ".bonus", c.getBonus() == null ? "" : c.getBonus().name());
			ArquivoPartida.linha(saida, id + ".entregas", c.getNumeroDeEntregas());
		}
	}

	void carregar(Map<String, String> secao) {
		for (Conselheiro c : conselheiros.values()) {
			String bonus = ArquivoPartida.valor(secao, c.getIdParada() + ".bonus");
			c.restaurar(bonus.isEmpty() ? null : TipoBonus.valueOf(bonus),
					ArquivoPartida.inteiro(secao, c.getIdParada() + ".entregas"));
		}
	}
}
