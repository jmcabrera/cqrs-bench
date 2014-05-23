package io.cqrs.bench.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CardRepository extends Repository {

	private static final Map<String, Card>	CACHE	= new ConcurrentHashMap<>();

	static {
		clear();
	}

	public static void clear() {
		CACHE.clear();
		try (Connection c = getConnection()) {
			try (Statement st = c.createStatement()) {
				st.execute("DROP TABLE card;");
				c.commit();
			} catch (SQLException e) {
				c.rollback();
			}
		} catch (SQLException e) {}
		try (Connection c = getConnection()) {
			try (Statement st = c.createStatement()) {
				st.execute("CREATE TABLE card (" //
						+ " pan VARCHAR(19) NOT NULL," //
						+ " embossed_date varchar(4) NOT NULL," //
						+ " authorized_amount bigint(20) DEFAULT 0," //
						+ " PRIMARY KEY (pan, embossed_date)" //
						+ ");");
				c.commit();
			} catch (SQLException e) {
				c.rollback();
				throw new RuntimeException(e);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static Card find(String pan, String embossedDate) {
		Card card;
		if (null != (card = CACHE.get(pan + "/" + embossedDate))) { return card; }
		try (Connection c = getConnection()) {
			try (PreparedStatement ps = c.prepareStatement("select authorized_amount from card where pan=? and embossed_date=?;")) {
				ps.setString(1, pan);
				ps.setString(2, embossedDate);
				ResultSet res = ps.executeQuery();
				res.next();
				card = new Card(pan, embossedDate);
				card.setAuthorizedAmount(res.getLong(1));
				c.commit();
				return card;
			} catch (SQLException e) {
				c.rollback();
				throw new RuntimeException(e);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void store(Card card) {
		CACHE.remove(card.getPan() + "/" + card.getEmbossedDate());
		try (Connection c = getConnection()) {
			try (PreparedStatement ps = c.prepareStatement("insert into card values(?,?,?);")) {
				ps.setString(1, card.getPan());
				ps.setString(2, card.getEmbossedDate());
				ps.setLong(3, card.getAuthorizedAmount());
				ps.execute();
				c.commit();
			} catch (SQLException e) {
				c.rollback();
				throw new RuntimeException(e);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void update(Card card) {
		CACHE.remove(card.getPan() + "/" + card.getEmbossedDate());
		try (Connection c = getConnection()) {
			try (PreparedStatement ps = c.prepareStatement("update card set authorized_amount=? where pan=? and embossed_date=?;")) {
				ps.setLong(1, card.getAuthorizedAmount());
				ps.setString(2, card.getPan());
				ps.setString(3, card.getEmbossedDate());
				ps.execute();
				c.commit();
			} catch (SQLException e) {
				c.rollback();
				throw new RuntimeException(e);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
