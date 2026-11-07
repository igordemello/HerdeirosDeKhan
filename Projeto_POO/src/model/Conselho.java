package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Os 7 conselheiros do mapa, guardados por id da parada.
 *
 * Pedidos transcritos do tabuleiro principal (imagem do EAD):
 * <ul>
 * <li>R04: 2 sedas; R11: 2 ferros; P01: 2 peles; P08: 2 especiarias;
 * C05: 2 bois — 1 voto por entrega;</li>
 * <li>C09: 3 tesouros de qualquer tipo — 1 voto por entrega;</li>
 * <li>Karakorum: 1 ferro, 1 pele e 1 boi (pedido impresso no tabuleiro;
 * a peça opcional de Karakorum, pág. 4, não foi usada) — 2 votos por
 * entrega.</li>
 * </ul>
 * Na preparação, as 7 peças de bônus são distribuídas aleatoriamente, uma em
 * cada conselheiro (pág. 4).
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

	/** Distribui as 7 peças de bônus, uma por conselheiro, ao acaso. */
	void distribuirBonus(Random aleatorio) {
		List<TipoBonus> pecas = new ArrayList<TipoBonus>(Arrays.asList(TipoBonus.values()));
		Collections.shuffle(pecas, aleatorio);
		List<String> ids = new ArrayList<String>(conselheiros.keySet());
		Collections.sort(ids); // ordem fixa, para a mesma semente dar o mesmo resultado
		for (int i = 0; i < ids.size(); i++) {
			conselheiros.get(ids.get(i)).colocarBonus(pecas.get(i));
		}
	}

	/** @return o conselheiro da parada, ou {@code null} se ela não tem conselheiro */
	Conselheiro getConselheiro(String idParada) {
		return conselheiros.get(idParada);
	}

	Collection<Conselheiro> getConselheiros() {
		return Collections.unmodifiableCollection(conselheiros.values());
	}
}
