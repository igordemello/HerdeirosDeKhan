package model;

import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/*
 * Tabuleiro principal: paradas, províncias, cidades e onde está cada peão.
 *
 * Karakorum e as 9 cidades existem em qualquer partida, então já nascem no
 * construtor. O resto (paradas, rotas e províncias) entra pelos métodos de
 * construção, que a FabricaTabuleiro e os testes usam. Dá pra mudar o mapa
 * sem mexer nesta classe (princípio aberto/fechado).
 *
 * Os métodos recebem o id da parada; id que não existe dá
 * IllegalArgumentException.
 */

class Tabuleiro {
	static final String ID_KARAKORUM = "KARAKORUM";
	// Cidades disponíveis para ataque ao mesmo tempo.
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

	static final String AREA_DE_MELHORIAS = "MELHORIAS";
	// id da província do Khan ou AREA_DE_MELHORIAS; null no começo (Khan fora do tabuleiro)
	private String lugarDoKhan = null;

	Tabuleiro() {
		karakorum = new Parada(ID_KARAKORUM, TipoParada.KARAKORUM, null);
		paradas.put(ID_KARAKORUM, karakorum);
		for (NomeCidade nome : NomeCidade.values()) {
			Cidade cidade = new Cidade(nome);
			cidades.put(nome, cidade);
			pilhaDeCidades.add(cidade);
		}
	}

	// construção do mapa

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

	// Cria uma rota entre duas paradas (a ligação vale nos dois sentidos).
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

	// Registra uma das 2 setas que partem de uma província do Khan.
	void definirAlvoDoKhan(String idProvinciaDoKhan, String idAlvo) {
		buscarProvincia(idProvinciaDoKhan).adicionarAlvoDoKhan(buscarProvincia(idAlvo));
	}

	// consultas

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

	// Se existe uma rota ligando diretamente as duas paradas.
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

	// "Dono" de uma parada: as cores dos yurts construídos nela. A lista é
	// vazia se não houver yurt e pode ter 2 elementos numa parada dupla.
	List<Cor> getDonosDosYurts(String idParada) {
		return buscarParada(idParada).getYurts();
	}

	int contarPeoes(String idParada) {
		return buscarParada(idParada).getNumeroDePeoes();
	}

	// null se o peão não está no mapa
	Parada getParadaDoPeao(Peao peao) {
		return posicaoDosPeoes.get(peao);
	}

	// Menor número de rotas até Karakorum (busca em largura), pro desempate do
	// vencedor (pág. 16). Integer.MAX_VALUE se não tiver caminho.
	int distanciaAteKarakorum(String idParada) {
		Parada alvo = buscarParada(idParada);
		Map<Parada, Integer> distancia = new HashMap<Parada, Integer>();
		Queue<Parada> fila = new LinkedList<Parada>();
		distancia.put(karakorum, 0);
		fila.add(karakorum);
		while (!fila.isEmpty()) {
			Parada atual = fila.remove();
			if (atual == alvo) {
				return distancia.get(atual);
			}
			for (Parada vizinha : atual.getVizinhas()) {
				if (!distancia.containsKey(vizinha)) {
					distancia.put(vizinha, distancia.get(atual) + 1);
					fila.add(vizinha);
				}
			}
		}
		return Integer.MAX_VALUE;
	}

	// Yurts do jogador numa região: os das paradas da região mais o yurt no
	// meio das cidades que ele conquistou ali. O yurt de Tolui em Karakorum
	// não conta para nenhuma região (pág. 20).
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

	// Todos os yurts do jogador no mapa (cartas abertas de 6 e 10 yurts).
	// Aqui o yurt de Tolui em Karakorum conta (pág. 20).
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

	Collection<Cidade> getCidades() {
		return Collections.unmodifiableCollection(cidades.values());
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

	// Uma das condições de fim de jogo (pág. 14).
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

	// alterações

	// Coloca no mapa um peão que ainda não está nele.
	void colocarPeao(Peao peao, String idParada) {
		if (posicaoDosPeoes.containsKey(peao)) {
			throw new IllegalStateException(peao + " já está no tabuleiro");
		}
		Parada parada = buscarParada(idParada);
		parada.adicionarPeao(peao);
		posicaoDosPeoes.put(peao, parada);
	}

	// Move o peão pelo caminho dado (pág. 7). Cada passo vai pra uma vizinha e
	// gasta 1 movimento; pular yurt próprio não gasta. Pode passar por parada
	// ocupada, só o destino precisa ter lugar. O caminho não tem a origem.
	// Se não valer, devolve false e não mexe em nada.
	boolean moverPeao(Peao peao, List<String> caminho, int movimentos) {
		Parada origem = posicaoDosPeoes.get(peao);
		if (origem == null) {
			throw new IllegalStateException(peao + " não está no tabuleiro");
		}
		if (caminho == null || caminho.isEmpty()) {
			throw new IllegalArgumentException("Caminho vazio");
		}
		Parada atual = origem;
		int gastos = 0;
		for (int i = 0; i < caminho.size(); i++) {
			Parada proxima = buscarParada(caminho.get(i));
			if (!atual.ehVizinhaDe(proxima.getId())) {
				return false;
			}
			boolean ehDestino = (i == caminho.size() - 1);
			if (ehDestino || !proxima.temYurtDe(peao.getCor())) {
				gastos++;
			}
			atual = proxima;
		}
		if (gastos > movimentos || !atual.podeReceberPeao()) {
			return false;
		}
		origem.removerPeao(peao);
		atual.adicionarPeao(peao);
		posicaoDosPeoes.put(peao, atual);
		return true;
	}

	// Move o peão até a parada clicada (pág. 7). O jogador só diz o destino,
	// então o caminho é procurado aqui com as mesmas regras do moverPeao, e
	// fica o que gasta menos movimentos.
	// Devolve as paradas em que o peão esteve, da origem ao destino, que depois
	// servem pra pegar tributo e construir yurt. Os yurts próprios que ele
	// pulou não entram, porque ele nunca esteve neles (pág. 8). Se não der pra
	// chegar, devolve lista vazia.
	List<String> moverPeaoAte(Peao peao, String idDestino, int movimentos) {
		List<Parada> caminho = buscarCaminho(peao, idDestino, movimentos);
		if (caminho == null) {
			return Collections.emptyList();
		}
		Parada origem = caminho.get(0);
		Parada destino = caminho.get(caminho.size() - 1);
		List<String> visitadas = new ArrayList<String>();
		for (Parada parada : caminho) {
			if (parada == origem || parada == destino || !parada.temYurtDe(peao.getCor())) {
				visitadas.add(parada.getId());
			}
		}
		origem.removerPeao(peao);
		destino.adicionarPeao(peao);
		posicaoDosPeoes.put(peao, destino);
		return Collections.unmodifiableList(visitadas);
	}

	// só testa, não move
	boolean podeMoverPeaoAte(Peao peao, String idDestino, int movimentos) {
		return buscarCaminho(peao, idDestino, movimentos) != null;
	}

	// Põe o yurt na parada tirando da reserva do jogador. Pagar a peça de
	// tributo e ver se ele passou pela parada é com quem chama (o Jogo).
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

	// Pegaram o último tesouro: o jogador põe um yurt no meio da cidade (se
	// ainda tiver) e a próxima da pilha é revelada (pág. 10). Devolve a cidade
	// revelada, ou null se a pilha acabou.
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

	// Khan

	String getLugarDoKhan() {
		return lugarDoKhan;
	}

	// as 4 províncias do Khan + a área de melhorias
	List<String> getLugaresDoKhan() {
		List<String> lugares = new ArrayList<String>();
		for (Provincia provincia : provincias.values()) {
			if (provincia.isDoKhan()) {
				lugares.add(provincia.getId());
			}
		}
		Collections.sort(lugares);
		lugares.add(AREA_DE_MELHORIAS);
		return lugares;
	}

	// o Khan nunca pode ficar onde já está
	boolean podeMoverKhanPara(String lugar) {
		return lugar != null && !lugar.equals(lugarDoKhan) && getLugaresDoKhan().contains(lugar);
	}

	// Só muda o Khan de lugar. Os efeitos (tributos, melhorias) ficam no Jogo,
	// porque dependem da reserva de peças e da área de melhorias.
	boolean moverKhan(String lugar) {
		if (!podeMoverKhanPara(lugar)) {
			return false;
		}
		lugarDoKhan = lugar;
		return true;
	}

	// a própria província do Khan e as 2 das setas
	List<Provincia> getProvinciasDoKhan(String idProvinciaDoKhan) {
		Provincia provincia = buscarProvincia(idProvinciaDoKhan);
		List<Provincia> lista = new ArrayList<Provincia>();
		lista.add(provincia);
		List<Provincia> alvos = new ArrayList<Provincia>(provincia.getAlvosDoKhan());
		Collections.sort(alvos, new java.util.Comparator<Provincia>() {
			@Override
			public int compare(Provincia a, Provincia b) {
				return a.getId().compareTo(b.getId());
			}
		});
		lista.addAll(alvos);
		return lista;
	}

	// preparação

	// Preparação (pág. 4): embaralha as cidades e revela 3, põe todos os peões
	// em Karakorum (Altani tem 2), o Tolui já põe 1 yurt lá e cada província
	// ganha 1 tributo. Tesouros, bônus e melhorias o Jogo prepara.
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

	// apoio

	private Cidade revelarProximaCidade() {
		if (pilhaDeCidades.isEmpty()) {
			return null;
		}
		Cidade cidade = pilhaDeCidades.remove(pilhaDeCidades.size() - 1);
		cidade.revelar();
		return cidade;
	}

	// Caminho mais barato até o destino. É uma BFS 0-1: entrar numa parada do
	// meio que tem yurt próprio custa 0 (vai pro começo da fila) e nas outras
	// custa 1 (vai pro fim). Chegar no destino sempre custa 1.
	// Devolve origem ... destino, ou null se não dá com esses movimentos.
	private List<Parada> buscarCaminho(Peao peao, String idDestino, int movimentos) {
		Parada origem = posicaoDosPeoes.get(peao);
		if (origem == null) {
			throw new IllegalStateException(peao + " não está no tabuleiro");
		}
		Parada destino = buscarParada(idDestino);
		if (destino == origem || movimentos <= 0 || !destino.podeReceberPeao()) {
			return null;
		}
		Map<Parada, Integer> custo = new HashMap<Parada, Integer>();
		Map<Parada, Parada> anterior = new HashMap<Parada, Parada>();
		Deque<Parada> fila = new ArrayDeque<Parada>();
		custo.put(origem, 0);
		fila.add(origem);
		int melhor = Integer.MAX_VALUE;
		Parada antesDoDestino = null;
		while (!fila.isEmpty()) {
			Parada atual = fila.pollFirst();
			int gasto = custo.get(atual);
			if (gasto >= movimentos) {
				continue; // daqui o destino custaria mais do que o jogador tem
			}
			for (Parada vizinha : vizinhasEmOrdem(atual)) {
				if (vizinha == destino) {
					if (gasto + 1 < melhor) {
						melhor = gasto + 1;
						antesDoDestino = atual;
					}
					continue;
				}
				int passo = vizinha.temYurtDe(peao.getCor()) ? 0 : 1;
				Integer conhecido = custo.get(vizinha);
				if (conhecido == null || gasto + passo < conhecido) {
					custo.put(vizinha, gasto + passo);
					anterior.put(vizinha, atual);
					if (passo == 0) {
						fila.addFirst(vizinha);
					} else {
						fila.addLast(vizinha);
					}
				}
			}
		}
		if (antesDoDestino == null || melhor > movimentos) {
			return null;
		}
		List<Parada> caminho = new ArrayList<Parada>();
		caminho.add(destino);
		for (Parada p = antesDoDestino; p != null; p = anterior.get(p)) {
			caminho.add(p);
		}
		Collections.reverse(caminho);
		return caminho;
	}

	// ordena pelo id pra o caminho escolhido ser sempre o mesmo
	private List<Parada> vizinhasEmOrdem(Parada parada) {
		List<String> ids = new ArrayList<String>();
		for (Parada vizinha : parada.getVizinhas()) {
			ids.add(vizinha.getId());
		}
		Collections.sort(ids);
		List<Parada> vizinhas = new ArrayList<Parada>();
		for (String id : ids) {
			vizinhas.add(paradas.get(id));
		}
		return vizinhas;
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

	// salvar e carregar
	// Parte do Arthur na 4ª iteração: tudo o que fica no mapa vai na seção
	// [MAPA]. O que só vale durante um turno não entra, porque o jogo só
	// salva antes de um jogador começar a jogar. Exemplo:
	//
	//   khan=PV05                        (vazio = Khan ainda fora do tabuleiro)
	//   pilhaDeCidades=SARAI,KIEV        (do fundo pro topo da pilha)
	//   tributos=PV01:1,PV02:0,...
	//   peao.AZUL.1=R08
	//   yurts.R08=AZUL                   (só as paradas que têm yurt)
	//   cidade.KIEV=DISPONIVEL
	//   cidade.KIEV.tesouros=SEDA,BOI,BOI,FERRO
	//   cidade.MOSCOU.conquistador=VERDE (só nas conquistadas)
	//   cidade.MOSCOU.yurt=SIM

	static final int MAX_TESOUROS_NA_CIDADE = 4;

	void salvar(PrintWriter saida, Collection<Jogador> jogadores) {
		ArquivoPartida.linha(saida, "khan", lugarDoKhan == null ? "" : lugarDoKhan);
		List<String> pilha = new ArrayList<String>();
		for (Cidade c : pilhaDeCidades) {
			pilha.add(c.getNome().name());
		}
		ArquivoPartida.linha(saida, "pilhaDeCidades", ArquivoPartida.juntar(pilha));

		List<String> ids = new ArrayList<String>(provincias.keySet());
		Collections.sort(ids);
		List<String> tributos = new ArrayList<String>();
		for (String id : ids) {
			tributos.add(id + ":" + provincias.get(id).getPecasDeTributo());
		}
		ArquivoPartida.linha(saida, "tributos", ArquivoPartida.juntar(tributos));

		for (Jogador j : jogadores) {
			for (int n = 1; n <= j.getHerdeiro().getNumeroDePeoes(); n++) {
				Parada p = posicaoDosPeoes.get(new Peao(j.getCor(), n));
				ArquivoPartida.linha(saida, "peao." + j.getCor().name() + "." + n, p.getId());
			}
		}

		ids = new ArrayList<String>(paradas.keySet());
		Collections.sort(ids);
		for (String id : ids) {
			List<Cor> donos = paradas.get(id).getYurts();
			if (!donos.isEmpty()) {
				ArquivoPartida.linha(saida, "yurts." + id, ArquivoPartida.juntar(donos));
			}
		}

		for (NomeCidade nome : NomeCidade.values()) {
			Cidade c = cidades.get(nome);
			String chave = "cidade." + nome.name();
			ArquivoPartida.linha(saida, chave, c.getEstado().name());
			ArquivoPartida.linha(saida, chave + ".tesouros", ArquivoPartida.juntar(c.getTesouros()));
			if (c.getEstado() == Cidade.Estado.CONQUISTADA) {
				ArquivoPartida.linha(saida, chave + ".conquistador", c.getConquistador().name());
				ArquivoPartida.linha(saida, chave + ".yurt", c.temYurtDe(c.getConquistador()) ? "SIM" : "NAO");
			}
		}
	}

	// Troca o mapa pelo da seção [MAPA]. Primeiro lê e confere tudo e só depois
	// mexe no tabuleiro, assim um arquivo errado não deixa o mapa pela metade.
	void carregar(Map<String, String> secao, Collection<Jogador> jogadores) {
		Map<Cor, Jogador> porCor = new HashMap<Cor, Jogador>();
		for (Jogador j : jogadores) {
			porCor.put(j.getCor(), j);
		}

		// ---- Khan e tributos
		String khan = ArquivoPartida.valor(secao, "khan");
		if (!khan.isEmpty() && !getLugaresDoKhan().contains(khan)) {
			throw new IllegalArgumentException("Lugar do Khan inválido: " + khan);
		}
		Map<String, Integer> tributos = ArquivoPartida.pares(secao, "tributos");
		for (Provincia p : provincias.values()) {
			Integer n = tributos.get(p.getId());
			if (n == null || n < 0 || n > Provincia.MAX_PECAS_TRIBUTO) {
				throw new IllegalArgumentException("Tributos inválidos na província " + p.getId());
			}
		}

		// ---- peões (todos têm que estar no mapa, sem passar do limite da parada)
		Map<Peao, Parada> peoes = new HashMap<Peao, Parada>();
		Map<Parada, Integer> ocupacao = new HashMap<Parada, Integer>();
		for (Jogador j : jogadores) {
			for (int n = 1; n <= j.getHerdeiro().getNumeroDePeoes(); n++) {
				String id = ArquivoPartida.valor(secao, "peao." + j.getCor().name() + "." + n);
				Parada p = paradaDoArquivo(id);
				int jaTem = ocupacao.containsKey(p) ? ocupacao.get(p) : 0;
				if (jaTem >= p.getTipo().getCapacidadePeoes()) {
					throw new IllegalArgumentException("Peões demais na parada " + id);
				}
				ocupacao.put(p, jaTem + 1);
				peoes.put(new Peao(j.getCor(), n), p);
			}
		}

		// ---- yurts
		Map<Parada, List<Cor>> yurts = new HashMap<Parada, List<Cor>>();
		for (String chave : secao.keySet()) {
			if (!chave.startsWith("yurts.")) {
				continue;
			}
			Parada p = paradaDoArquivo(chave.substring("yurts.".length()));
			List<Cor> donos = new ArrayList<Cor>();
			for (String c : ArquivoPartida.lista(secao, chave)) {
				donos.add(corDoArquivo(c, porCor));
			}
			if (p.isKarakorum()) { // em Karakorum só pode o yurt do começo do Tolui
				if (donos.size() > 1 || (donos.size() == 1 && !donos.get(0).getHerdeiro().comecaComYurtEmKarakorum())) {
					throw new IllegalArgumentException("Karakorum só pode ter o yurt do Tolui");
				}
			} else if (donos.size() > p.getTipo().getCapacidadeYurts()) {
				throw new IllegalArgumentException("Yurts demais na parada " + p.getId());
			}
			yurts.put(p, donos);
		}

		// ---- cidades
		Map<NomeCidade, Cidade.Estado> estados = new HashMap<NomeCidade, Cidade.Estado>();
		Map<NomeCidade, List<TipoTesouro>> tesouros = new HashMap<NomeCidade, List<TipoTesouro>>();
		Map<NomeCidade, Cor> conquistadores = new HashMap<NomeCidade, Cor>();
		Set<NomeCidade> comYurt = new HashSet<NomeCidade>();
		int disponiveis = 0;
		int naPilha = 0;
		for (NomeCidade nome : NomeCidade.values()) {
			String chave = "cidade." + nome.name();
			Cidade.Estado estado;
			try {
				estado = Cidade.Estado.valueOf(ArquivoPartida.valor(secao, chave));
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Estado inválido (ou faltando) da cidade " + nome.name());
			}
			List<TipoTesouro> lista = new ArrayList<TipoTesouro>();
			for (String t : ArquivoPartida.lista(secao, chave + ".tesouros")) {
				try {
					lista.add(TipoTesouro.valueOf(t));
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("Tesouro inválido em " + nome.name() + ": " + t);
				}
			}
			if (estado == Cidade.Estado.DISPONIVEL) {
				disponiveis++;
				if (lista.isEmpty() || lista.size() > MAX_TESOUROS_NA_CIDADE) {
					throw new IllegalArgumentException(nome.name() + " está disponível com " + lista.size() + " tesouros");
				}
			} else if (!lista.isEmpty()) {
				throw new IllegalArgumentException("Só cidade disponível tem tesouro (" + nome.name() + ")");
			}
			if (estado == Cidade.Estado.NA_PILHA) {
				naPilha++;
			}
			if (estado == Cidade.Estado.CONQUISTADA) {
				conquistadores.put(nome, corDoArquivo(ArquivoPartida.valor(secao, chave + ".conquistador"), porCor));
				String yurt = ArquivoPartida.valor(secao, chave + ".yurt");
				if (!yurt.equals("SIM") && !yurt.equals("NAO")) {
					throw new IllegalArgumentException("Use SIM ou NAO em " + chave + ".yurt");
				}
				if (yurt.equals("SIM")) {
					comYurt.add(nome);
				}
			}
			estados.put(nome, estado);
			tesouros.put(nome, lista);
		}

		List<Cidade> pilha = new ArrayList<Cidade>();
		Set<NomeCidade> vistas = new HashSet<NomeCidade>();
		for (String id : ArquivoPartida.lista(secao, "pilhaDeCidades")) {
			NomeCidade nome;
			try {
				nome = NomeCidade.valueOf(id);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Cidade inválida na pilha: " + id);
			}
			if (estados.get(nome) != Cidade.Estado.NA_PILHA || !vistas.add(nome)) {
				throw new IllegalArgumentException("A pilha de cidades não bate com o estado das cidades (" + id + ")");
			}
			pilha.add(cidades.get(nome));
		}
		if (pilha.size() != naPilha) {
			throw new IllegalArgumentException("Faltam cidades na pilha");
		}
		// enquanto tiver cidade na pilha, sempre são 3 disponíveis
		if (disponiveis > CIDADES_DISPONIVEIS || (naPilha > 0 && disponiveis != CIDADES_DISPONIVEIS)) {
			throw new IllegalArgumentException("Tem que haver " + CIDADES_DISPONIVEIS + " cidades disponíveis");
		}

		// ---- tudo certo: agora sim troca o mapa
		for (Parada p : paradas.values()) {
			p.esvaziar();
		}
		posicaoDosPeoes.clear();
		for (Map.Entry<Peao, Parada> e : peoes.entrySet()) {
			e.getValue().adicionarPeao(e.getKey());
			posicaoDosPeoes.put(e.getKey(), e.getValue());
		}
		for (Map.Entry<Parada, List<Cor>> e : yurts.entrySet()) {
			for (Cor c : e.getValue()) {
				if (e.getKey().isKarakorum()) {
					e.getKey().adicionarYurtDeTolui(c);
				} else {
					e.getKey().adicionarYurt(c);
				}
			}
		}
		for (Provincia p : provincias.values()) {
			p.definirPecasDeTributo(tributos.get(p.getId()));
		}
		for (NomeCidade nome : NomeCidade.values()) {
			cidades.get(nome).restaurar(estados.get(nome), conquistadores.get(nome), comYurt.contains(nome),
					tesouros.get(nome));
		}
		pilhaDeCidades.clear();
		pilhaDeCidades.addAll(pilha);
		lugarDoKhan = khan.isEmpty() ? null : khan;
		preparado = true;
	}

	private Parada paradaDoArquivo(String id) {
		Parada p = paradas.get(id);
		if (p == null) {
			throw new IllegalArgumentException("Parada inexistente no arquivo: " + id);
		}
		return p;
	}

	private static Cor corDoArquivo(String nome, Map<Cor, Jogador> jogadores) {
		Cor cor;
		try {
			cor = Cor.valueOf(nome);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Cor inválida no arquivo: " + nome);
		}
		if (!jogadores.containsKey(cor)) {
			throw new IllegalArgumentException("A cor " + nome + " não está nesta partida");
		}
		return cor;
	}
}
