package db.migration;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

@Slf4j
public class V20200503171321__MigrateToDecryptDatabase extends BaseJavaMigration {

  private JdbcTemplate jdbcTemplate;

  //    private final TextEncryptor encryptor;

  public V20200503171321__MigrateToDecryptDatabase() {
    //        this.encryptor = Encryptors.queryableText(securityKey,
    // "46415339383033346a77464153723938346a776166616d6466");
  }

  @Override
  public void migrate(Context context) throws Exception {
    //        jdbcTemplate = new JdbcTemplate(context.getConnection());

    //        jdbcTemplate.query("select id, username from user_account", this::processUser);
  }

  //    private synchronized void processUser(ResultSet rs) throws SQLException {
  //        var id = rs.getLong("id");
  //        var username = rs.getString("username");
  //
  //        log.info("Start encryption of database tables for {}", username);
  //
  //        jdbcTemplate.query("select id, name, iban, bic, number from account where user_id = "
  // +
  // id, this::encryptAccount);
  //    }
  //
  //    private void encryptAccount(ResultSet rs) throws SQLException {
  //        var name = API.Option(rs.getString("name")).map(encryptor::decrypt).getOrSupply(() ->
  // null);
  //        var iban = API.Option(rs.getString("iban")).map(encryptor::decrypt).getOrSupply(() ->
  // null);
  //        var bic = API.Option(rs.getString("bic")).map(encryptor::decrypt).getOrSupply(() ->
  // null);
  //        var number = API.Option(rs.getString("number")).map(encryptor::decrypt).getOrSupply(()
  // -> null);
  //
  //        var sql = "update account set name = ?";
  //        if (iban != null) {
  //            sql += ", iban = ?";
  //        }
  //        if (bic != null) {
  //            sql += ", bic = ?";
  //        }
  //        if (number != null) {
  //            sql += ", number = ?";
  //        }
  //        sql += " where id = " + rs.getLong("id");
  //
  //        jdbcTemplate.update(sql, preparedStatement -> {
  //            var index = 1;
  //            preparedStatement.setString(index, name);
  //            if (iban != null) {
  //                index++;
  //                preparedStatement.setString(index, iban);
  //            }
  //            if (bic != null) {
  //                index++;
  //                preparedStatement.setString(index, bic);
  //            }
  //            if (number != null) {
  //                index++;
  //                preparedStatement.setString(index, number);
  //            }
  //        });
  //    }

}
