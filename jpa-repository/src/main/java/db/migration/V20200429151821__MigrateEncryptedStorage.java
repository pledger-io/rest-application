package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class V20200429151821__MigrateEncryptedStorage extends BaseJavaMigration {

    private JdbcTemplate jdbcTemplate;
    private String userSecret;

//    private final String securitySalt;
//    private final String storageLocation;

    public V20200429151821__MigrateEncryptedStorage() {
//        this.securitySalt = String.valueOf(Hex.encode(securitySalt.getBytes(StandardCharsets.UTF_8)));
//        this.storageLocation = storageLocation;
    }

    @Override
    public void migrate(Context context) throws Exception {
        jdbcTemplate = new JdbcTemplate(context.getConnection());

//        jdbcTemplate.query("select id, username, two_factor_secret from user_account", this::processUser);
    }

//    private synchronized void processUser(ResultSet rs) throws SQLException {
//        var id = rs.getLong("id");
//        var username = rs.getString("username");
//        userSecret = rs.getString("two_factor_secret");
//
//        log.info("Start encryption of files for {}", username);
//
//        jdbcTemplate.query("select id, file_code from import_config where user_id = " + id, this::encrypt);
//        jdbcTemplate.query("select id, file_code from import where user_id = " + id, this::encrypt);
//        jdbcTemplate.query("select id, file_token as file_code from contract where user_id = " + id, this::encrypt);
//    }
//
//    private void encrypt(ResultSet rs) throws SQLException {
//        try {
//            final Path filePath = Paths.get(this.storageLocation + "/upload/" + rs.getString("file_code"));
//            byte[] content = Files.readString(filePath).getBytes(StandardCharsets.UTF_8);
//
//            var encryptor = Encryptors.standard(userSecret, securitySalt);
//            var encrypted = encryptor.encrypt(content);
//
//            try (var output = new FileOutputStream(new File(this.storageLocation + "/upload/" + rs.getString("file_code")), false)) {
//                output.write(encrypted);
//            }
//        } catch (IOException e) {
//            log.warn("Skipping file {} for encryption migration", rs.getString("file_code"));
//        }
//    }

}
