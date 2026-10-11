package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

 /*
 Parada do mapa: os pontos ligados pelas rotas por onde os peões se movem, 
 uma parada não tem dono fixo. Ela guarda os peões  que estão nela 
 e os yurts construídos nela, que indicam a presença de um jogador.
 
 Cada parada conhece:
  
 as paradas vizinhas (ligadas diretamente por uma rota);
 as províncias adjacentes (de onde se pegam peças de tributo);
 as cidades adjacentes (que podem ser atacadas a partir dela).
 */

class Parada {
	private final String id;
	private final TipoParada tipo;
	/* Região da parada; {@code null} apenas para Karakorum (spawn). */
	private final Regiao regiao;

	private final Map<String, Parada> vizinhas = new HashMap<String, Parada>();
	private final Map<String, Provincia> provinciasAdjacentes = new HashMap<String, Provincia>();
	private final Map<NomeCidade, Cidade> cidadesAdjacentes = new HashMap<NomeCidade, Cidade>();

	private final Set<Peao> peoes = new HashSet<Peao>();
	/* Dono de cada yurt construído aqui (uma parada dupla pode ter 2 do mesmo jogador). */
	private final List<Cor> yurts = new ArrayList<Cor>();

	Parada(String id, TipoParada tipo, Regiao regiao) {
		if (id == null || id.isEmpty()) {
			throw new IllegalArgumentException("Identificador da parada não pode ser vazio");
		}
		if (tipo == null) {
			throw new IllegalArgumentException("Tipo da parada não pode ser nulo");
		}
		if (tipo == TipoParada.KARAKORUM && regiao != null) {
			throw new IllegalArgumentException("Karakorum não pertence a nenhuma região");
		}
		if (tipo != TipoParada.KARAKORUM && regiao == null) {
			throw new IllegalArgumentException("A parada " + id + " precisa de uma região");
		}
		this.id = id;
		this.tipo = tipo;
		this.regiao = regiao;
	}

	String getId() {
		return id;
	}

	TipoParada getTipo() {
		return tipo;
	}

	Regiao getRegiao() {
		return regiao;
	}

	boolean isKarakorum() {
		return tipo == TipoParada.KARAKORUM;
	}

	boolean temConselheiro() {
		return tipo.temConselheiro();
	}

	// ---------------------------------------------------------- adjacências
	// A simetria das ligações é garantida pelo Tabuleiro.

	void ligar(Parada outra) {
		vizinhas.put(outra.getId(), outra);
	}

	void adicionarProvinciaAdjacente(Provincia provincia) {
		provinciasAdjacentes.put(provincia.getId(), provincia);
	}

	void adicionarCidadeAdjacente(Cidade cidade) {
		cidadesAdjacentes.put(cidade.getNome(), cidade);
	}

	boolean ehVizinhaDe(String idOutra) {
		return vizinhas.containsKey(idOutra);
	}

	Collection<Parada> getVizinhas() {
		return Collections.unmodifiableCollection(vizinhas.values());
	}

	Collection<Provincia> getProvinciasAdjacentes() {
		return Collections.unmodifiableCollection(provinciasAdjacentes.values());
	}

	Collection<Cidade> getCidadesAdjacentes() {
		return Collections.unmodifiableCollection(cidadesAdjacentes.values());
	}

	// ---------------------------------------------------------------- peões

	/**
	 * Indica se mais um peão pode terminar o movimento aqui. Passar pela
	 * parada é sempre permitido; esta regra vale só para onde o peão para.
	 */
	boolean podeReceberPeao() {
		return peoes.size() < tipo.getCapacidadePeoes();
	}

	void adicionarPeao(Peao peao) {
		if (peao == null) {
			throw new IllegalArgumentException("Peão nulo");
		}
		if (!podeReceberPeao()) {
			throw new IllegalStateException("A parada " + id + " já está lotada");
		}
		peoes.add(peao);
	}

	boolean removerPeao(Peao peao) {
		return peoes.remove(peao);
	}

	boolean temPeao(Peao peao) {
		return peoes.contains(peao);
	}

	int getNumeroDePeoes() {
		return peoes.size();
	}

	Collection<Peao> getPeoes() {
		return Collections.unmodifiableCollection(peoes);
	}

	// ---------------------------------------------------------------- yurts

	boolean podeReceberYurt() {
		return yurts.size() < tipo.getCapacidadeYurts();
	}

	void adicionarYurt(Cor cor) {
		if (cor == null) {
			throw new IllegalArgumentException("Cor nula");
		}
		if (!podeReceberYurt()) {
			throw new IllegalStateException("A parada " + id + " não aceita mais yurts");
		}
		yurts.add(cor);
	}

	/**
	 * Habilidade de Tolui (págs. 6 e 20): no início do jogo ele coloca 1 yurt
	 * em Karakorum, única exceção à regra de que Karakorum não tem yurts.
	 */
	void adicionarYurtDeTolui(Cor cor) {
		if (!isKarakorum()) {
			throw new IllegalStateException("O yurt inicial de Tolui só vai em Karakorum");
		}
		if (cor == null || !cor.getHerdeiro().comecaComYurtEmKarakorum()) {
			throw new IllegalArgumentException("Apenas Tolui coloca yurt em Karakorum");
		}
		if (!yurts.isEmpty()) {
			throw new IllegalStateException("Karakorum já tem o yurt de Tolui");
		}
		yurts.add(cor);
	}

	/** Donos dos yurts desta parada (lista vazia se não houver yurt). */
	List<Cor> getYurts() {
		return Collections.unmodifiableList(yurts);
	}

	int getNumeroDeYurts() {
		return yurts.size();
	}

	int contarYurts(Cor cor) {
		int total = 0;
		for (Cor dono : yurts) {
			if (dono == cor) {
				total++;
			}
		}
		return total;
	}

	boolean temYurtDe(Cor cor) {
		return yurts.contains(cor);
	}

	@Override
	public String toString() {
		return id + " (" + tipo + (regiao == null ? "" : ", " + regiao.getNome()) + ")";
	}
}
