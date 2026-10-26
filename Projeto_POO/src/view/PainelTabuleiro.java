package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import model.Jogo;

/**
 * Tabuleiro principal. Desenha com {@code drawImage()} a imagem do mapa
 * ({@code imagens/tabuleiro.jpg}, do EAD) e os peões por cima, nas
 * coordenadas de {@link CoordenadasParadas}.
 *
 * A imagem é reduzida para caber no painel e desenhada a partir do canto
 * (0, 0); a escala usada fica guardada para converter os cliques em paradas
 * ({@link #paradaEm(int, int)}).
 */
public class PainelTabuleiro extends JPanel {
	private static final long serialVersionUID = 1L;

	/** Diâmetro do peão, em pixels da imagem original. */
	private static final int DIAMETRO_PEAO = 100;

	private final Jogo jogo;
	private final Image imagemTabuleiro = ImagensTabuleiroJogador.carregarArquivo("imagens/", "tabuleiro");
	/** larguraDesenhada / LARGURA_IMAGEM, calculada a cada pintura. */
	private double escala = 1;

	public PainelTabuleiro(Jogo jogo) {
		this.jogo = jogo;
	}

	/** Parada na posição (x, y) do painel, ou {@code null} se o clique não foi numa parada. */
	public String paradaEm(int x, int y) {
		return CoordenadasParadas.paradaEm(x, y, escala);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		escala = Math.min((double) getWidth() / CoordenadasParadas.LARGURA_IMAGEM,
				(double) getHeight() / CoordenadasParadas.ALTURA_IMAGEM);
		int largura = (int) Math.round(CoordenadasParadas.LARGURA_IMAGEM * escala);
		int altura = (int) Math.round(CoordenadasParadas.ALTURA_IMAGEM * escala);

		if (imagemTabuleiro != null) {
			g2.drawImage(imagemTabuleiro, 0, 0, largura, altura, this);
		} else {
			g2.drawString("Imagem imagens/tabuleiro.jpg não encontrada", 20, 30);
		}
		desenharPeoes(g2);
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
		g2.draw(circulo);
	}
}
