package db.migration;

import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Random;

/**
 * This class is used to generate randomized transactions for the Pledger application. It should
 * only be used for the demo mode.
 */
public class RandomizedTransactions {

    private static final Logger log =
            org.slf4j.LoggerFactory.getLogger(RandomizedTransactions.class);

    static String url =
            "jdbc:h2:mem:Pledger;DB_CLOSE_DELAY=-1;MODE=MariaDB"; // replace with your database url
    static String user = "fintrack"; // replace with your database user
    static String password = "fintrack"; // replace with your database password

    static String loremIpsum =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor"
                    + " incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis"
                    + " nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
                    + " Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu"
                    + " fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in"
                    + " culpa qui officia deserunt mollit anim id est laborum.";

    static Random rand = new Random();

    public static void create(
            int source,
            int destination,
            int transactionsPerMonth,
            double lower,
            double upper,
            String description,
            String category) {
        var transactionSql =
                "INSERT INTO transaction_journal(user_id, created, updated, t_date, description,"
                        + " category_id, type, currency_id) VALUES (1, ?, ?, ?, ?, (SELECT id FROM"
                        + " category WHERE label = ?), 'CREDIT', 1)";
        var partSql =
                """
INSERT INTO transaction_part(journal_id, created, updated, account_id, description, amount)
VALUES (
    (SELECT id FROM transaction_journal WHERE t_date = ? AND description = ? AND type = 'CREDIT' AND user_id = 1 limit 1),
    ?,?,?,?,?)""";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            try (PreparedStatement journalStatement = conn.prepareStatement(transactionSql);
                    PreparedStatement partStatement = conn.prepareStatement(partSql)) {
                for (int year = 2016; year <= LocalDate.now().getYear(); year++) {
                    log.info(
                            "Creating transactions for year {} and description {}",
                            year,
                            description);
                    for (int month = 1; month <= 12; month++) {
                        var lastDayOfMonth = LocalDate.of(year, month, 1).lengthOfMonth();
                        var updatedDescription = description.replaceAll(
                                "\\{month}",
                                LocalDate.of(year, month, 1)
                                        .format(java.time.format.DateTimeFormatter.ofPattern(
                                                "MMMM")));

                        for (int i = 0; i < transactionsPerMonth; i++) {
                            int day = rand.nextInt(lastDayOfMonth) + 1;
                            double amount = rand.nextDouble(lower, upper);

                            String date = String.format("%d-%02d-%02d", year, month, day);

                            // create the transaction
                            journalStatement.setString(1, date);
                            journalStatement.setString(2, date);
                            journalStatement.setString(3, date);
                            journalStatement.setString(4, updatedDescription);
                            journalStatement.setString(5, category);
                            journalStatement.executeUpdate();

                            // create the transaction parts
                            partStatement.setString(1, date);
                            partStatement.setString(2, updatedDescription);
                            partStatement.setString(3, date);
                            partStatement.setString(4, date);
                            partStatement.setInt(5, source);
                            partStatement.setString(6, updatedDescription);
                            partStatement.setDouble(7, -amount);
                            partStatement.executeUpdate();
                            partStatement.setInt(5, destination);
                            partStatement.setDouble(7, amount);
                            partStatement.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Failed to create randomized transaction: {}", e.getMessage());
        }
    }

    public static void createContract(
            String name, int companyId, String start, String end, double amount) {
        var contractSql =
                "INSERT INTO contract(user_id, name, company_id, start_date, end_date, description)"
                        + " VALUES (1, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            log.info("Creating contract for company {} and name {}", companyId, name);
            try (PreparedStatement contractStatement = conn.prepareStatement(contractSql)) {
                contractStatement.setString(1, name);
                contractStatement.setInt(2, companyId);
                contractStatement.setString(3, start);
                contractStatement.setString(4, end);
                contractStatement.setString(5, loremIpsum);
                contractStatement.executeUpdate();
            }

            // update the transaction journal set the contract id by the company id
            var updateSql =
                    """
                    UPDATE transaction_journal
                    SET contract_id = (SELECT id FROM contract WHERE company_id = ? and name = ?)
                    WHERE exists (
                        select 1
                        from transaction_part
                        where
                            transaction_part.journal_id = transaction_journal.id
                            and transaction_part.account_id = ?)""";
            try (PreparedStatement updateStatement = conn.prepareStatement(updateSql)) {
                updateStatement.setInt(1, companyId);
                updateStatement.setString(2, name);
                updateStatement.setInt(3, companyId);
                updateStatement.executeUpdate();
                log.debug("Updated transaction journal with contract id");
            }
        } catch (SQLException e) {
            log.error("Failed to create contract: {}", e.getMessage());
        }
    }
}
