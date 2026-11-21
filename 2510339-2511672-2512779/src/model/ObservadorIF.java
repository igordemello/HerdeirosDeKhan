package model;

/**
 * Observador do padrão Observer (Capítulo 17): é notificado quando o estado
 * da partida muda e pede ao observado os dados de que precisa. No jogo, os
 * observadores são as partes da interface gráfica (tabuleiro, área lateral e
 * janelas).
 */
public interface ObservadorIF {
	void notify(ObservadoIF o);
}
