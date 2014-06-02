package io.cqrs.bench.jpa;

import io.cqrs.bench.jpa.Card.CardId;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQuery;
import javax.persistence.Version;

@Entity
@IdClass(CardId.class)
@NamedQuery(name = "findAll", query = "SELECT c FROM Card c")
public class Card {

	public static final class CardId implements Serializable {

		private static final long	serialVersionUID	= -7676062786613078292L;

		private String						pan;

		private String						embossedDate;

		public CardId() {}

		public CardId(String pan, String embossedDate) {
			this.pan = pan;
			this.embossedDate = embossedDate;
		}

		@Override
		public int hashCode() {
			return pan.hashCode() ^ embossedDate.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (null == obj) return false;
			if (this == obj) return true;
			if (this.hashCode() != obj.hashCode()) return false;
			if (obj instanceof CardId) {
				CardId that = (CardId) obj;
				return this.pan.equals(that.pan) && this.embossedDate.equals(that.embossedDate);
			}
			return false;
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName() + "(pan='" + pan + "', embossedDate='" + embossedDate + "')";
		}
	}

	@Id
	private String	pan;

	@Id
	private String	embossedDate;

	@Version
	private long		version;

	private long		authorizedAmount;

	public Card() {}

	public Card(String pan, String embossedDate) {
		this.pan = pan;
		this.embossedDate = embossedDate;
	}

	public String getPan() {
		return pan;
	}

	public String getEmbossedDate() {
		return embossedDate;
	}

	public long getAuthorizedAmount() {
		return authorizedAmount;
	}

	public void setAuthorizedAmount(long authorizedAmount) {
		this.authorizedAmount = authorizedAmount;
	}

}
