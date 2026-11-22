package model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/*
 * Província: a área entre as rotas onde ficam as peças de tributo. Cada uma
 * tem um tipo de tributo impresso e cabe no máximo 3 peças. As 4 do Khan têm
 * setas pra 2 vizinhas; quando o Khan vai pra ela, ela e as 2 ganham 1 peça.
 */
class Provincia {
	static final int MAX_PECAS_TRIBUTO = 3;
	static final int ALVOS_DO_KHAN = 2;

	private final String id;
	private final TipoTributo tipoTributo;
	private final boolean doKhan;
	private int pecasDeTributo = 0;

	private final Map<String, Parada> paradasAdjacentes = new HashMap<String, Parada>();
	// as das setas do Khan (só nas províncias do Khan)
	private final Map<String, Provincia> alvosDoKhan = new HashMap<String, Provincia>();

	Provincia(String id, TipoTributo tipoTributo, boolean doKhan) {
		if (id == null || id.isEmpty()) {
			throw new IllegalArgumentException("Identificador da província não pode ser vazio");
		}
		if (tipoTributo == null) {
			throw new IllegalArgumentException("Tipo de tributo da província não pode ser nulo");
		}
		this.id = id;
		this.tipoTributo = tipoTributo;
		this.doKhan = doKhan;
	}

	String getId() {
		return id;
	}

	TipoTributo getTipoTributo() {
		return tipoTributo;
	}

	boolean isDoKhan() {
		return doKhan;
	}

	// adjacências

	void adicionarParadaAdjacente(Parada parada) {
		paradasAdjacentes.put(parada.getId(), parada);
	}

	Collection<Parada> getParadasAdjacentes() {
		return Collections.unmodifiableCollection(paradasAdjacentes.values());
	}

	void adicionarAlvoDoKhan(Provincia alvo) {
		if (!doKhan) {
			throw new IllegalStateException("A província " + id + " não é uma província do Khan");
		}
		if (alvo == null || alvo == this) {
			throw new IllegalArgumentException("Alvo do Khan inválido");
		}
		if (alvosDoKhan.size() >= ALVOS_DO_KHAN && !alvosDoKhan.containsKey(alvo.getId())) {
			throw new IllegalStateException("Uma província do Khan aponta para no máximo "
					+ ALVOS_DO_KHAN + " províncias");
		}
		alvosDoKhan.put(alvo.getId(), alvo);
	}

	Collection<Provincia> getAlvosDoKhan() {
		return Collections.unmodifiableCollection(alvosDoKhan.values());
	}

	// peças de tributo

	int getPecasDeTributo() {
		return pecasDeTributo;
	}

	boolean estaVazia() {
		return pecasDeTributo == 0;
	}

	// false se já tem 3
	boolean adicionarTributo() {
		if (pecasDeTributo >= MAX_PECAS_TRIBUTO) {
			return false;
		}
		pecasDeTributo++;
		return true;
	}

	// se não tiver peças suficientes devolve false e não tira nada
	boolean retirarTributo(int quantidade) {
		if (quantidade <= 0) {
			throw new IllegalArgumentException("Quantidade deve ser positiva: " + quantidade);
		}
		if (quantidade > pecasDeTributo) {
			return false;
		}
		pecasDeTributo -= quantidade;
		return true;
	}

	@Override
	public String toString() {
		return id + " (" + tipoTributo + (doKhan ? ", Khan" : "") + ": " + pecasDeTributo + ")";
	}

	// usado ao carregar uma partida salva
	void definirPecasDeTributo(int quantidade) {
		if (quantidade < 0 || quantidade > MAX_PECAS_TRIBUTO) {
			throw new IllegalArgumentException("A província " + id + " não pode ter " + quantidade + " peças");
		}
		pecasDeTributo = quantidade;
	}
}
