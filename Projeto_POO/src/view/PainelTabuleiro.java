package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import model.Jogo;
import model.ObservadoIF;
import model.ObservadorIF;

/*
 * Tabuleiro principal. Desenha a imagem do mapa com drawImage() e, por cima,
 * as peças que mudam na partida: bônus nos conselheiros, melhorias da área
 * de baixo, tributos, tesouros, yurts, o Khan e os peões.
 *
 * Também marca o que o jogador da vez pode clicar agora (amarelo = pegar
 * tributo / construir yurt, vermelho = atacar, laranja = lugar do Khan).
 *
 * A imagem é reduzida pra caber no painel; a escala fica guardada pra
 * converter o clique de volta em coordenada da imagem (os métodos ...Em).
 * Quem decide o que fazer com o clique é o Controller.
 *
 * É observador da partida: quando o estado muda (um peão anda, um tributo é
 * pego...), o Model avisa e o painel se redesenha.
 */
public class PainelTabuleiro extends JPanel implements ObservadorIF {
	private static final long serialVersionUID = 1L;

	// medidas em pixels da imagem original
	private static final int DIAMETRO_PEAO = 100;
	private static final int DESLOCAMENTO_PILHA = 9; // grossura de cada peça na pilha

	private static final Color DESTAQUE = new Color(255, 236, 120);
	private static final Color DESTAQUE_ATAQUE = new Color(255, 70, 40);
	private static final Color LUGAR_DO_KHAN = new Color(255, 140, 0);
	private static final Color CONTORNO = new Color(40, 30, 20);

	private final Jogo jogo;
	private final Image imagemTabuleiro = ImagensTabuleiroJogador.carregarArquivo("imagens/", "tabuleiro");
	private double escala = 1; // recalculada em cada paintComponent

	public PainelTabuleiro(Jogo jogo) {
		this.jogo = jogo;
		jogo.add(this);
	}

	@Override
	public void notify(ObservadoIF o) {
		repaint();
	}

	// ---------------------------------------- o que tem no ponto clicado
	// (todos devolvem null se não tiver nada ali)

	public String paradaEm(int x, int y) {
		return CoordenadasParadas.paradaEm(x, y, escala);
	}

	public String provinciaEm(int x, int y) {
		return CoordenadasMapa.provinciaEm(x / escala, y / escala);
	}

	public String lugarDoKhanEm(int x, int y) {
		return CoordenadasMapa.lugarDoKhanEm(x / escala, y / escala);
	}

	public String cidadeEm(int x, int y) {
		return CoordenadasMapa.cidadeEm(x / escala, y / escala);
	}

	/** Tipo do tesouro (ex.: "SEDA") clicado em cima da cidade. */
	public String tesouroEm(String idCidade, int x, int y) {
		int i = CoordenadasMapa.tesouroEm(idCidade, x / escala, y / escala);
		List<String> tesouros = jogo.getTesourosDaCidade(idCidade);
		return i >= 0 && i < tesouros.size() ? tesouros.get(i) : null;
	}

	// ------------------------------------------------------------ desenho

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		escala = Math.min((double) getWidth() / CoordenadasParadas.LARGURA_IMAGEM,
				(double) getHeight() / CoordenadasParadas.ALTURA_IMAGEM);
		int largura = (int) Math.round(CoordenadasParadas.LARGURA_IMAGEM * escala);
		int altura = (int) Math.round(CoordenadasParadas.ALTURA_IMAGEM * escala);

		if (imagemTabuleiro != null) {
			g2.drawImage(imagemTabuleiro, 0, 0, largura, altura, this);
		} else {
			g2.drawString("Imagem imagens/tabuleiro.jpg não encontrada", 20, 30);
		}
		desenharBonusDosConselheiros(g2);
		desenharAreaDeMelhorias(g2);
		desenharTributos(g2);
		desenharCidades(g2);
		desenharParadasDoTurno(g2);
		desenharYurts(g2);
		desenharKhan(g2);
		desenharPeoes(g2);
	}

	// bônus que ninguém pegou ainda
	private void desenharBonusDosConselheiros(Graphics2D g2) {
		for (String id : jogo.getConselheiros()) {
			String bonus = jogo.getBonusDoConselheiro(id);
			Point p = CoordenadasMapa.getConselheiro(id);
			if (bonus == null || p == null) {
				continue;
			}
			int lado = s(CoordenadasMapa.LADO_BONUS);
			int x = s(p.x) - lado / 2;
			int y = s(p.y) - lado / 2;
			Image img = ImagensTabuleiroJogador.getBonus(bonus);
			if (img != null) {
				g2.drawImage(img, x, y, lado, lado, this);
			} else {
				g2.setColor(new Color(235, 220, 180));
				g2.fillRect(x, y, lado, lado);
				ImagensTabuleiroJogador.desenharTextoCentralizado(g2, Textos.bonus(bonus), x, y, lado, lado, CONTORNO);
			}
			g2.setColor(CONTORNO);
			g2.setStroke(new BasicStroke(1.5f));
			g2.drawRect(x, y, lado, lado);
		}
	}

	// Depois de uma compra as melhorias andam pra direita, então quem sobra
	// fica nos últimos espaços e os da esquerda ficam vazios (aparecendo os
	// ícones de moeda e espada que importam pro Khan).
	private void desenharAreaDeMelhorias(Graphics2D g2) {
		List<String> ids = jogo.getMelhoriasDisponiveis();
		int espacos = CoordenadasMapa.getNumeroDeEspacosMelhoria();
		int lado = s(CoordenadasMapa.LADO_MELHORIA);
		for (int i = 0; i < ids.size() && i < espacos; i++) {
			Point p = CoordenadasMapa.getEspacoMelhoria(espacos - ids.size() + i);
			int x = s(p.x) - lado / 2;
			int y = s(p.y) - lado / 2;
			Image img = ImagensTabuleiroJogador.getMelhoria(ids.get(i));
			if (img != null) {
				g2.drawImage(img, x, y, lado, lado, this);
			} else {
				g2.setColor(new Color(255, 248, 225));
				g2.fillRect(x, y, lado, lado);
				ImagensTabuleiroJogador.desenharTextoCentralizado(g2, ids.get(i), x, y, lado, lado, CONTORNO);
			}
		}
	}

	// tributos empilhados em cima do ícone da província
	private void desenharTributos(Graphics2D g2) {
		Set<String> alcancaveis = new HashSet<String>(jogo.getProvinciasAlcancaveis());
		for (String id : jogo.getProvincias()) {
			Point p = CoordenadasMapa.getProvincia(id);
			if (p == null) {
				continue;
			}
			int n = jogo.getTributosNaProvincia(id);
			Image img = ImagensTabuleiroJogador.getTributo(jogo.getTipoTributoDaProvincia(id));
			int lado = s(CoordenadasMapa.LADO_TRIBUTO);
			int passo = Math.max(2, s(DESLOCAMENTO_PILHA));
			int x = s(p.x) - lado / 2;
			int y = s(p.y) - lado / 2;
			// Pilha reta tipo moedas: a peça de cima fica bem em cima do ícone e
			// as de baixo aparecem só como a "grossura" da pilha. Antes ela ia na
			// diagonal e com 3 peças ficava torta e saía do ícone.
			for (int k = n - 1; k >= 0; k--) {
				desenharFichaRedonda(g2, img, x, y + k * passo, lado, Color.LIGHT_GRAY);
			}
			if (alcancaveis.contains(id)) {
				int folga = s(14);
				desenharAnel(g2, new Ellipse2D.Double(x - folga, y - folga, lado + 2 * folga,
						lado + 2 * folga + (n - 1) * passo), DESTAQUE);
			}
			if (n > 1) {
				desenharContagem(g2, n, x + lado - s(8), y + s(8));
			}
		}
	}

	// Cidade disponível: os tesouros em 2x2 por cima dela. Conquistada: o
	// yurt de quem conquistou. As que estão na pilha ficam como no tabuleiro.
	private void desenharCidades(Graphics2D g2) {
		for (String id : jogo.getCidades()) {
			Point centro = CoordenadasMapa.getCidade(id);
			if (centro == null) {
				continue;
			}
			String estado = jogo.getEstadoDaCidade(id);
			if ("DISPONIVEL".equals(estado)) {
				List<String> tesouros = jogo.getTesourosDaCidade(id);
				int lado = s(CoordenadasMapa.LADO_TESOURO);
				for (int i = 0; i < tesouros.size() && i < 4; i++) {
					Point canto = CoordenadasMapa.getTesouro(id, i);
					int x = s(canto.x);
					int y = s(canto.y);
					desenharFichaRedonda(g2, ImagensTabuleiroJogador.getTesouro(tesouros.get(i)), x, y, lado,
							new Color(200, 170, 90));
					if (jogo.podeAtacar(id, tesouros.get(i))) {
						desenharAnel(g2, new Ellipse2D.Double(x - 2, y - 2, lado + 4, lado + 4), DESTAQUE_ATAQUE);
					} else if (jogo.podePegarTesouroComMoeda(id, tesouros.get(i))) {
						// Ogedei pode comprar esse com moeda (botão direito)
						desenharAnel(g2, new Ellipse2D.Double(x - 2, y - 2, lado + 4, lado + 4), DESTAQUE);
					}
				}
			} else if ("CONQUISTADA".equals(estado) && jogo.cidadeTemYurt(id)) {
				int lado = s(CoordenadasMapa.LADO_YURT_CIDADE);
				g2.drawImage(ImagensMapa.getYurt(jogo.getConquistadorDaCidade(id)), s(centro.x) - lado / 2,
						s(centro.y) - lado / 2, lado, lado, this);
			}
		}
	}

	// Tracejado nas paradas por onde o peão passou no turno. Fica amarelo
	// onde dá pra construir yurt (no Tolui entram as vizinhas também).
	private void desenharParadasDoTurno(Graphics2D g2) {
		Set<String> paraYurt = new HashSet<String>(jogo.getParadasParaYurt());
		Set<String> paradas = new HashSet<String>(jogo.getParadasDoTurno());
		paradas.addAll(paraYurt);
		for (String id : paradas) {
			Point c = CoordenadasParadas.getPonto(id, escala);
			if (c == null || "KARAKORUM".equals(id)) {
				continue;
			}
			int d = s(DIAMETRO_PEAO + 36);
			Stroke antes = g2.getStroke();
			g2.setStroke(new BasicStroke(Math.max(2f, s(8)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f,
					new float[] { s(14), s(10) }, 0f));
			g2.setColor(paraYurt.contains(id) ? DESTAQUE : Color.WHITE);
			g2.draw(new Ellipse2D.Double(c.x - d / 2.0, c.y - d / 2.0, d, d));
			g2.setStroke(antes);
		}
	}

	// Yurts um do lado do outro, um pouco acima da parada (a dupla pode ter
	// 2). O do Tolui em Karakorum vai do lado do brasão pra não cobrir os peões.
	private void desenharYurts(Graphics2D g2) {
		int lado = s(CoordenadasMapa.LADO_YURT);
		for (String id : jogo.getParadas()) {
			List<String> donos = jogo.getYurtsNaParada(id);
			if (donos.isEmpty()) {
				continue;
			}
			Point c = CoordenadasParadas.getPonto(id, escala);
			int cx = c.x;
			int cy = c.y - s(64);
			if ("KARAKORUM".equals(id)) {
				cx = c.x - s(190);
				cy = c.y + s(70);
			}
			for (int i = 0; i < donos.size(); i++) {
				int x = cx - lado / 2 + (int) Math.round((i - (donos.size() - 1) / 2.0) * lado);
				g2.drawImage(ImagensMapa.getYurt(donos.get(i)), x, cy - lado / 2, lado, lado, this);
			}
		}
	}

	private void desenharKhan(Graphics2D g2) {
		int lado = s(CoordenadasMapa.LADO_KHAN);
		if (jogo.khanPendente()) {
			for (String lugar : jogo.getLugaresDoKhan()) {
				Point p = CoordenadasMapa.getLugarDoKhan(lugar);
				if (p != null && jogo.podeColocarKhan(lugar)) {
					int d = lado + s(10);
					Stroke antes = g2.getStroke();
					g2.setStroke(new BasicStroke(Math.max(3f, s(14)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
							1f, new float[] { s(26), s(18) }, 0f));
					g2.setColor(LUGAR_DO_KHAN);
					g2.draw(new Ellipse2D.Double(s(p.x) - d / 2.0, s(p.y) - d / 2.0, d, d));
					g2.setStroke(antes);
				}
			}
		}
		String lugar = jogo.getLugarDoKhan();
		Point p = lugar == null ? null : CoordenadasMapa.getLugarDoKhan(lugar);
		if (p == null) {
			return;
		}
		// disco vermelho com borda dourada pra não confundir com as cabeças impressas no mapa
		int x = s(p.x) - lado / 2;
		int y = s(p.y) - lado / 2;
		g2.setColor(new Color(0, 0, 0, 110));
		g2.fill(new Ellipse2D.Double(x + s(10), y + s(14), lado, lado));
		g2.setColor(new Color(110, 20, 20));
		g2.fill(new Ellipse2D.Double(x, y, lado, lado));
		int margem = s(22);
		desenharFichaRedonda(g2, ImagensMapa.getKhan(), x + margem, y + margem, lado - 2 * margem,
				new Color(120, 60, 30));
		g2.setStroke(new BasicStroke(Math.max(2.5f, s(12))));
		g2.setColor(new Color(232, 186, 48));
		g2.draw(new Ellipse2D.Double(x, y, lado, lado));
		g2.setStroke(new BasicStroke(1f));
		g2.setColor(CONTORNO);
		g2.draw(new Ellipse2D.Double(x - s(6), y - s(6), lado + s(12), lado + s(12)));
	}

	/** Desenha os peões; os que estão na mesma parada ficam lado a lado. */
	private void desenharPeoes(Graphics2D g2) {
		Map<String, List<String>> coresPorParada = new HashMap<String, List<String>>();
		for (String cor : jogo.getCores()) {
			for (int n = 1; n <= jogo.getNumeroDePeoes(cor); n++) {
				String parada = jogo.getParadaDoPeao(cor, n);
				if (!coresPorParada.containsKey(parada)) {
					coresPorParada.put(parada, new ArrayList<String>());
				}
				coresPorParada.get(parada).add(cor);
			}
		}

		int d = (int) Math.round(DIAMETRO_PEAO * escala);
		for (Map.Entry<String, List<String>> e : coresPorParada.entrySet()) {
			Point centro = CoordenadasParadas.getPonto(e.getKey(), escala);
			List<String> cores = e.getValue();
			for (int i = 0; i < cores.size(); i++) {
				int x = centro.x - d / 2 + (int) Math.round((i - (cores.size() - 1) / 2.0) * d);
				desenharPeao(g2, cores.get(i), x, centro.y - d / 2, d);
			}
		}
	}

	/** O peão é a imagem da ficha da cor do jogador, recortada em círculo. */
	private void desenharPeao(Graphics2D g2, String cor, int x, int y, int d) {
		Ellipse2D circulo = new Ellipse2D.Double(x, y, d, d);
		Shape recorteAnterior = g2.getClip();
		g2.clip(circulo);
		g2.drawImage(ImagensTabuleiroJogador.getFicha(cor), x, y, d, d, this);
		g2.setClip(recorteAnterior);
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(1f));
		g2.draw(circulo);
	}

	// -------------------------------------------------------------- apoio

	// medida da imagem original -> pixels do painel
	private int s(double medidaNaImagem) {
		return (int) Math.round(medidaNaImagem * escala);
	}

	// As fotos das peças redondas (tributo, tesouro, Khan) são quadradas, então
	// recortamos num círculo. Se a imagem não carregar, pinta um círculo liso.
	private void desenharFichaRedonda(Graphics2D g2, Image img, int x, int y, int lado, Color semImagem) {
		Ellipse2D circulo = new Ellipse2D.Double(x, y, lado, lado);
		g2.setColor(new Color(0, 0, 0, 80));
		g2.fill(new Ellipse2D.Double(x + 2, y + 3, lado, lado));
		if (img != null) {
			Shape recorteAnterior = g2.getClip();
			g2.clip(circulo);
			double margem = lado * 0.03; // corta a borda branca da foto
			g2.drawImage(img, (int) Math.round(x - margem), (int) Math.round(y - margem),
					(int) Math.round(lado + 2 * margem), (int) Math.round(lado + 2 * margem), this);
			g2.setClip(recorteAnterior);
		} else {
			g2.setColor(semImagem);
			g2.fill(circulo);
		}
		g2.setColor(CONTORNO);
		g2.setStroke(new BasicStroke(1.2f));
		g2.draw(circulo);
	}

	// bolinha preta com o número de peças da pilha
	private void desenharContagem(Graphics2D g2, int n, int cx, int cy) {
		int d = Math.max(14, s(54));
		g2.setColor(new Color(30, 30, 30, 220));
		g2.fill(new Ellipse2D.Double(cx - d / 2.0, cy - d / 2.0, d, d));
		g2.setColor(Color.WHITE);
		g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(9, (int) (d * 0.7))));
		FontMetrics fm = g2.getFontMetrics();
		String texto = String.valueOf(n);
		g2.drawString(texto, cx - fm.stringWidth(texto) / 2, cy + (fm.getAscent() - fm.getDescent()) / 2);
	}

	private void desenharAnel(Graphics2D g2, Shape forma, Color cor) {
		Stroke antes = g2.getStroke();
		g2.setStroke(new BasicStroke(Math.max(3f, s(16))));
		g2.setColor(new Color(0, 0, 0, 110));
		g2.draw(forma);
		g2.setStroke(new BasicStroke(Math.max(2f, s(9))));
		g2.setColor(cor);
		g2.draw(forma);
		g2.setStroke(antes);
	}
}
