package model;

/*
 * Observador do Observer (Capítulo 17). É avisado quando a partida muda e
 * pede ao observado o que precisar. No jogo são as partes da interface
 * (tabuleiro, área lateral e as janelas).
 */
public interface ObservadorIF {
	void notify(ObservadoIF o);
}
