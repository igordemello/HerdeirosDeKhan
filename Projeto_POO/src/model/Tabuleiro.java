package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*
 * Tabuleiro principal: o mapa com as paradas, as províncias e as cidades,
 * além da posição dos peões.
 *
 * Karakorum e as 9 cidades existem em qualquer partida e por isso são
 * criadas no construtor. As demais paradas, as rotas e as províncias são
 * acrescentadas pelos métodos de construção (usados pela
 * {@link FabricaTabuleiro} e pelos testes). Assim o mapa pode ser
 * completado ou corrigido sem alterar esta classe (aberto/fechado).
 *
 * As consultas recebem identificadores de parada; um identificador
 * inexistente gera {@link IllegalArgumentException}.
 */

class Tabuleiro {
	static final String ID_KARAKORUM = "KARAKORUM";
	/** Cidades disponíveis para ataque ao mesmo tempo. */
	static final int CIDADES_DISPONIVEIS = 3;

	private final Map<String, Parada> paradas = new HashMap<String, Parada>();
	private final Map<String, Provincia> provincias = new HashMap<String, Provincia>();
	private final Map<NomeCidade, Cidade> cidades = new HashMap<NomeCidade, Cidade>();
	private final Map<Peao, Parada> posicaoDosPeoes = new HashMap<Peao, Parada>();
	/*
	 * Pilha de peças de cidade viradas para baixo; o topo é o fim da lista.
	 * Aqui a ordem importa, por isso é uma lista e não um HashMap.
	 */
	private final List<Cidade> pilhaDeCidades = new ArrayList<Cidade>();

	private final Parada karakorum;
	private boolean preparado = false;

	Tabuleiro() {
		karakorum = new Parada(ID_KARAKORUM, TipoParada.KARAKORUM, null);
		paradas.put(ID_KARAKORUM, karakorum);
		for (NomeCidade nome : NomeCidade.values()) {
			Cidade cidade = new Cidade(nome);
			cidades.put(nome, cidade);
			pilhaDeCidades.add(cidade);
		}
	}

	// ================================================== construção do mapa

	Parada adicionarParada(String id, TipoParada tipo, Regiao regiao) {
		if (tipo == TipoParada.KARAKORUM) {
			throw new IllegalArgumentException("Karakorum é única e já faz parte do tabuleiro");
		}
		if (paradas.containsKey(id)) {
			throw new IllegalArgumentException("Parada repetida: " + id);
		}
		Parada parada = new Parada(id, tipo, regiao);
		paradas.put(id, parada);
		return parada;
	}

	Provincia adicionarProvincia(String id, TipoTributo tipoTributo, boolean doKhan) {
		if (provincias.containsKey(id)) {
			throw new IllegalArgumentException("Província repetida: " + id);
		}
		Provincia provincia = new Provincia(id, tipoTributo, doKhan);
		provincias.put(id, provincia);
		return provincia;
	}

	/** Cria uma rota entre duas paradas (a ligação vale nos dois sentidos). */
	void ligarParadas(String idA, String idB) {
		if (idA != null && idA.equals(idB)) {
			throw new IllegalArgumentException("Uma parada não pode ser ligada a si mesma: " + idA);
		}
		Parada a = buscarParada(idA);
		Parada b = buscarParada(idB);
		a.ligar(b);
		b.ligar(a);
	}

	void ligarParadaAProvincia(String idParada, String idProvincia) {
		Parada parada = buscarParada(idParada);
		Provincia provincia = buscarProvincia(idProvincia);
		parada.adicionarProvinciaAdjacente(provincia);
		provincia.adicionarParadaAdjacente(parada);
	}

	void ligarParadaACidade(String idParada, NomeCidade nomeCidade) {
		Parada parada = buscarParada(idParada);
		Cidade cidade = getCidade(nomeCidade);
		parada.adicionarCidadeAdjacente(cidade);
		cidade.adicionarParadaAdjacente(parada);
	}

	/** Registra uma das 2 setas que partem de uma província do Khan. */
	void definirAlvoDoKhan(String idProvinciaDoKhan, String idAlvo) {
		buscarProvincia(idProvinciaDoKhan).adicionarAlvoDoKhan(buscarProvincia(idAlvo));
	}

	// ============================================================ consultas

	boolean existeParada(String id) {
		return paradas.containsKey(id);
	}

	Parada getParada(String id) {
		return buscarParada(id);
	}

	Parada getKarakorum() {
		return karakorum;
	}

	Collection<Parada> getParadas() {
		return Collections.unmodifiableCollection(paradas.values());
	}

	/** Se existe uma rota ligando diretamente as duas paradas. */
	boolean saoVizinhas(String idA, String idB) {
		Parada b = buscarParada(idB);
		return buscarParada(idA).ehVizinhaDe(b.getId());
	}

	Collection<Parada> getVizinhas(String idParada) {
		return buscarParada(idParada).getVizinhas();
	}

	Collection<Provincia> getProvinciasAdjacentes(String idParada) {
		return buscarParada(idParada).getProvinciasAdjacentes();
	}

	Collection<Cidade> getCidadesAdjacentes(String idParada) {
		return buscarParada(idParada).getCidadesAdjacentes();
	}

	/**
	 * "Dono" de uma parada: as cores dos yurts construídos nela. A lista é
	 * vazia se não houver yurt e pode ter 2 elementos numa parada dupla.
	 */
	List<Cor> getDonosDosYurts(String idParada) {
		return buscarParada(idParada).getYurts();
	}

	int contarPeoes(String idParada) {
		return buscarParada(idParada).getNumeroDePeoes();
	}

	/** @return a parada onde o peão está, ou {@code null} se ele não está no mapa */
	Parada getParadaDoPeao(Peao peao) {
		return posicaoDosPeoes.get(peao);
	}

	/**
	 * Yurts do jogador numa região: os das paradas da região mais o yurt no
	 * meio das cidades que ele conquistou ali. O yurt de Tolui em Karakorum
	 * não conta para nenhuma região (pág. 20).
	 */
	int contarYurtsNaRegiao(Cor cor, Regiao regiao) {
		if (cor == null || regiao == null) {
			throw new IllegalArgumentException("Cor e região não podem ser nulas");
		}
		int total = 0;
		for (Parada parada : paradas.values()) {
			if (parada.getRegiao() == regiao) {
				total += parada.contarYurts(cor);
			}
		}
		for (Cidade cidade : cidades.values()) {
			if (cidade.getRegiao() == regiao && cidade.temYurtDe(cor)) {
				total++;
			}
		}
		return total;
	}

	/**
	 * Todos os yurts do jogador no mapa (cartas abertas de 6 e 10 yurts).
	 * Aqui o yurt de Tolui em Karakorum conta (pág. 20).
	 */
	int contarYurtsNoMapa(Cor cor) {
		if (cor == null) {
			throw new IllegalArgumentException("Cor nula");
		}
		int total = 0;
		for (Parada parada : paradas.values()) {
			total += parada.contarYurts(cor);
		}
		for (Cidade cidade : cidades.values()) {
			if (cidade.temYurtDe(cor)) {
				total++;
			}
		}
		return total;
	}

	Provincia getProvincia(String id) {
		return buscarProvincia(id);
	}

	Collection<Provincia> getProvincias() {
		return Collections.unmodifiableCollection(provincias.values());
	}

	Cidade getCidade(NomeCidade nome) {
		if (nome == null) {
			throw new IllegalArgumentException("Nome de cidade nulo");
		}
		return cidades.get(nome);
	}

	Collection<Cidade> getCidadesDisponiveis() {
		List<Cidade> disponiveis = new ArrayList<Cidade>();
		for (Cidade cidade : cidades.values()) {
			if (cidade.getEstado() == Cidade.Estado.DISPONIVEL) {
				disponiveis.add(cidade);
			}
		}
		return Collections.unmodifiableList(disponiveis);
	}

	int getNumeroDeCidadesNaPilha() {
		return pilhaDeCidades.size();
	}

	/** Uma das condições de fim de jogo (pág. 14). */
	boolean todasAsCidadesConquistadas() {
		for (Cidade cidade : cidades.values()) {
			if (cidade.getEstado() != Cidade.Estado.CONQUISTADA) {
				return false;
			}
		}
		return true;
	}

	boolean isPreparado() {
		return preparado;
	}

	// =========================================================== alterações

	/** Coloca no mapa um peão que ainda não está nele. */
	void colocarPeao(Peao peao, String idParada) {
		if (posicaoDosPeoes.containsKey(peao)) {
			throw new IllegalStateException(peao + " já está no tabuleiro");
		}
		Parada parada = buscarParada(idParada);
		parada.adicionarPeao(peao);
		posicaoDosPeoes.put(peao, parada);
	}

	/**
	 * Constrói um yurt do jogador na parada, tirando-o da reserva dele.
	 * O pagamento da peça de tributo de yurt e a verificação de que o
	 * jogador passou pela parada ficam a cargo de quem chama.
	 *
	 * @return {@code false} se a parada não aceita mais yurts ou se o
	 *         jogador não tem yurts na reserva (nada é alterado)
	 */
	boolean construirYurt(Jogador jogador, String idParada) {
		if (jogador == null) {
			throw new IllegalArgumentException("Jogador nulo");
		}
		Parada parada = buscarParada(idParada);
		if (!parada.podeReceberYurt()) {
			return false;
		}
		if (!jogador.retirarYurt()) {
			return false;
		}
		parada.adicionarYurt(jogador.getCor());
		return true;
	}

	/**
	 * Registra a conquista de uma cidade disponível (o último tesouro foi
	 * tomado): o jogador coloca um yurt seu no meio dela, se ainda tiver, e
	 * a próxima peça de cidade é revelada (pág. 10).
	 *
	 * @return a cidade revelada no lugar, ou {@code null} se a pilha acabou
	 */
	Cidade conquistarCidade(NomeCidade nome, Jogador jogador) {
		if (jogador == null) {
			throw new IllegalArgumentException("Jogador nulo");
		}
		Cidade cidade = getCidade(nome);
		if (cidade.getEstado() != Cidade.Estado.DISPONIVEL) {
			throw new IllegalStateException(nome.getNome() + " não está disponível para conquista");
		}
		cidade.conquistar(jogador.getCor(), jogador.retirarYurt());
		return revelarProximaCidade();
	}

	// ============================================================ preparação

	/**
	 * Preparação do tabuleiro:
	 * embaralha as peças de cidade e revela 3;
	 * coloca os peões de todos os jogadores em Karakorum (Altani tem 2);
	 * Tolui coloca 1 yurt da sua reserva em Karakorum;
	 * coloca 1 peça de tributo em cada província.
	 * Tesouros, peças de bônus e melhorias serão tratados depois.
	 */
	void preparar(Collection<Jogador> jogadores, Random aleatorio) {
		if (preparado) {
			throw new IllegalStateException("O tabuleiro já foi preparado");
		}
		if (aleatorio == null) {
			throw new IllegalArgumentException("Gerador aleatório nulo");
		}
		if (jogadores == null || jogadores.size() < GerenciadorJogadores.MIN_JOGADORES
				|| jogadores.size() > GerenciadorJogadores.MAX_JOGADORES) {
			throw new IllegalArgumentException("Número de jogadores inválido");
		}

		Collections.shuffle(pilhaDeCidades, aleatorio);
		for (int i = 0; i < CIDADES_DISPONIVEIS; i++) {
			revelarProximaCidade();
		}

		for (Jogador jogador : jogadores) {
			Herdeiro herdeiro = jogador.getHerdeiro();
			for (int n = 1; n <= herdeiro.getNumeroDePeoes(); n++) {
				colocarPeao(new Peao(jogador.getCor(), n), ID_KARAKORUM);
			}
			if (herdeiro.comecaComYurtEmKarakorum() && jogador.retirarYurt()) {
				karakorum.adicionarYurtDeTolui(jogador.getCor());
			}
		}

		for (Provincia provincia : provincias.values()) {
			provincia.adicionarTributo();
		}
		preparado = true;
	}

	// ============================================================== apoio

	private Cidade revelarProximaCidade() {
		if (pilhaDeCidades.isEmpty()) {
			return null;
		}
		Cidade cidade = pilhaDeCidades.remove(pilhaDeCidades.size() - 1);
		cidade.revelar();
		return cidade;
	}

	private Parada buscarParada(String id) {
		Parada parada = paradas.get(id);
		if (parada == null) {
			throw new IllegalArgumentException("Parada inexistente: " + id);
		}
		return parada;
	}

	private Provincia buscarProvincia(String id) {
		Provincia provincia = provincias.get(id);
		if (provincia == null) {
			throw new IllegalArgumentException("Província inexistente: " + id);
		}
		return provincia;
	}
}
