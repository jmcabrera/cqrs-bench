package authorization.cqrs.command;

public class CreateCard {

	private final String	pan;
	private final String	embossedDate;

	public CreateCard(String pan, String embossedDate) {
		this.pan = pan;
		this.embossedDate = embossedDate;
	}

	public String getPan() {
		return pan;
	}

	public String getEmbossedDate() {
		return embossedDate;
	}

}
