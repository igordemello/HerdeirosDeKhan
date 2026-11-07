package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

/**
 * Peça do jogo desenhada com {@code drawImage()} e que pode ser clicada,
 * usada para escolher melhorias, tesouros e peças de bônus. Quando não há
 * imagem, desenha o verso de uma peça com um texto (ex.: o topo da pilha).
 */
class BlocoImagem extends JComponent {
	private static final long serialVersionUID = 1L;
	private static final int ALTURA_LEGENDA = 18;

	private final Image imagem;
	private final String legenda;
	private final String textoSemImagem;
	private boolean selecionado = false;
	private ActionListener ouvinte;

	BlocoImagem(Image imagem, String legenda, String textoSemImagem, int largura, int alturaImagem) {
		this.imagem = imagem;
		this.legenda = legenda;
		this.textoSemImagem = textoSemImagem;
		setPreferredSize(new Dimension(largura, alturaImagem + (legenda == null ? 0 : ALTURA_LEGENDA)));
		setToolTipText(legenda);
		addMouseListener(new TratadorClique());
	}

	void setOuvinte(ActionListener ouvinte) {
		this.ouvinte = ouvinte;
		setCursor(ouvinte == null ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	void setSelecionado(boolean selecionado) {
		this.selecionado = selecionado;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		int w = getWidth();
		int h = getHeight() - (legenda == null ? 0 : ALTURA_LEGENDA);

		if (imagem != null) {
			int iw = imagem.getWidth(this);
			int ih = imagem.getHeight(this);
			double escala = Math.min((w - 6.0) / Math.max(1, iw), (h - 6.0) / Math.max(1, ih));
			int dw = (int) Math.round(iw * escala);
			int dh = (int) Math.round(ih * escala);
			g2.drawImage(imagem, (w - dw) / 2, (h - dh) / 2, dw, dh, this);
		} else {
			g2.setColor(new Color(58, 74, 96));
			g2.fill(new RoundRectangle2D.Double(3, 3, w - 6, h - 6, 10, 10));
			g2.setColor(new Color(220, 228, 238));
			ImagensTabuleiroJogador.desenharTextoCentralizado(g2, textoSemImagem == null ? "" : textoSemImagem, 3,
					3, w - 6, h - 6, new Color(230, 236, 244));
		}
		if (selecionado) {
			g2.setColor(new Color(214, 120, 20));
			g2.setStroke(new BasicStroke(3.5f));
			g2.draw(new RoundRectangle2D.Double(1.5, 1.5, w - 3, h - 3, 10, 10));
		}
		if (legenda != null) {
			g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
			FontMetrics fm = g2.getFontMetrics();
			String texto = legenda;
			while (fm.stringWidth(texto) > w && texto.length() > 3) {
				texto = texto.substring(0, texto.length() - 2);
			}
			g2.setColor(getForeground());
			g2.drawString(texto, (w - fm.stringWidth(texto)) / 2, h + fm.getAscent() + 2);
		}
		g2.dispose();
	}

	private class TratadorClique extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (ouvinte != null && isEnabled()) {
				ouvinte.actionPerformed(new ActionEvent(BlocoImagem.this, ActionEvent.ACTION_PERFORMED, legenda));
			}
		}
	}
}
