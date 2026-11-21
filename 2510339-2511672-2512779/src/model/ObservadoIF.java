package model;

/**
 * Observado do padrão Observer (Capítulo 17): registra os observadores e os
 * notifica quando o seu estado muda. No jogo, o observado é a partida
 * ({@link Jogo}).
 */
public interface ObservadoIF {
	void add(ObservadorIF o);

	void remove(ObservadorIF o);

	/** Callback do observador para obter um dado do estado (códigos em {@link Jogo}). */
	int get(int i);
}
