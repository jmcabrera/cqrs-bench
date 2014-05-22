package authorization.jdbc;

public class CardManager {

	public static String createCard(Card c) {
		CardRepository.store(c);
		return "00";
	}

	public static String authorization(String pan, String embossedDate, long amount) {
		Card card = CardRepository.find(pan, embossedDate);
		card.setAuthorizedAmount(card.getAuthorizedAmount() + amount);
		CardRepository.update(card);
		return "00";
	}
}
