package model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/*
  Cidade do mapa. As 9 cidades estão no tabuleiro, mas só podem
  ser atacadas quando a peça de cidade correspondente é revelada. Sempre há
  3 cidades disponíveis; quando uma é conquistada, outra é revelada
 
   Quem conquista a cidade coloca um yurt seu no meio dela. Esse yurt não
   ajuda no movimento, mas conta para a maioria de yurts da região.
 
 */


class Cidade {
	enum Estado {
		//Peça de cidade ainda virada para baixo, na pilha
		NA_PILHA,
		// Revelada e disponível para ataque
		DISPONIVEL,
		// Já conquistada por um jogador. 
		CONQUISTADA
	}

	private final NomeCidade nome;
	private Estado estado = Estado.NA_PILHA;
	private Cor conquistador;
	// Se o conquistador ainda tinha um yurt na reserva para colocar na cidade
	private boolean temYurtDoConquistador;

	private final Map<String, Parada> paradasAdjacentes = new HashMap<String, Parada>();

	Cidade(NomeCidade nome) {
		if (nome == null) {
			throw new IllegalArgumentException("Nome da cidade não pode ser nulo");
		}
		this.nome = nome;
	}

	NomeCidade getNome() {
		return nome;
	}

	Regiao getRegiao() {
		return nome.getRegiao();
	}

	Estado getEstado() {
		return estado;
	}

	/* return a cor de quem conquistou a cidade, ou {code null} */
	Cor getConquistador() {
		return conquistador;
	}

	// ---------------------------------------------------------- adjacências

	void adicionarParadaAdjacente(Parada parada) {
		paradasAdjacentes.put(parada.getId(), parada);
	}

	Collection<Parada> getParadasAdjacentes() {
		return Collections.unmodifiableCollection(paradasAdjacentes.values());
	}

	/*
	 Total de yurts do jogador nas paradas adjacentes à cidade (usado pelas
	 cartas secretas Cidadão e Prefeito). O yurt dentro da cidade não conta.
	 */
	int contarYurtsAdjacentes(Cor cor) {
		int total = 0;
		for (Parada p : paradasAdjacentes.values()) {
			total += p.contarYurts(cor);
		}
		return total;
	}

	// ------------------------------------------------------ revelar/conquistar

	void revelar() {
		if (estado != Estado.NA_PILHA) {
			throw new IllegalStateException(nome.getNome() + " já foi revelada");
		}
		estado = Estado.DISPONIVEL;
	}

	void conquistar(Cor cor, boolean colocouYurt) {
		if (cor == null) {
			throw new IllegalArgumentException("Cor nula");
		}
		if (estado != Estado.DISPONIVEL) {
			throw new IllegalStateException(nome.getNome() + " não está disponível para conquista");
		}
		estado = Estado.CONQUISTADA;
		conquistador = cor;
		temYurtDoConquistador = colocouYurt;
	}

	/* Se há um yurt do jogador no meio da cidade. */
	boolean temYurtDe(Cor cor) {
		return temYurtDoConquistador && conquistador == cor;
	}

	@Override
	public String toString() {
		return nome.getNome() + " (" + estado + ")";
	}
}
