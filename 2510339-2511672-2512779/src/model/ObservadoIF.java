package model;

/*
 * Observado do Observer (Capítulo 17). Guarda os observadores e avisa
 * quando o estado muda. No jogo quem implementa é o Jogo.
 */
public interface ObservadoIF {
	void add(ObservadorIF o);

	void remove(ObservadorIF o);

	// o observador pede um dado pelo código (os códigos ficam no Jogo)
	int get(int i);
}
