package com.jongsoft.finance;

import com.jongsoft.finance.configuration.SecuritySettings;
import com.jongsoft.finance.configuration.StorageSettings;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.API;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;
import java.util.UUID;

@Singleton
@Named("storageService")
public class StorageServiceImpl implements StorageService {

    private final SecuritySettings securitySettings;
    private final CurrentUserProvider currentUserProvider;
    private final File uploadRootDirectory;
    private final byte[] securitySalt;

    public StorageServiceImpl(
            SecuritySettings securitySettings,
            CurrentUserProvider currentUserProvider,
            StorageSettings storageLocation) {
        this.securitySettings = securitySettings;
        this.currentUserProvider = currentUserProvider;
        this.securitySalt = Hex.encode(securitySettings.getSecret().getBytes());

        uploadRootDirectory = new File(storageLocation.getLocation(), "upload");
        if (!uploadRootDirectory.exists()) {
            API.Try(() -> Files.createDirectory(uploadRootDirectory.toPath()));
        }
    }

    @Override
    public String store(byte[] content) {
        var token = UUID.randomUUID().toString();

        byte[] toStore;
        if (securitySettings.isEncrypt()) {
            toStore = generateCipher(Cipher.ENCRYPT_MODE, content);
        } else {
            toStore = content;
        }

        var fileCreated = API.Try(() -> new FileOutputStream(new File(uploadRootDirectory, token), false))
                .consume((os) -> {
                    os.write(toStore);
                    os.close();
                })
                .isSuccess();

        return fileCreated ? token : null;
    }

    @Override
    public byte[] read(String token) {
        var file = new File(uploadRootDirectory, token);

        if (!file.isFile()) {
            throw new IllegalStateException("Cannot locate content for token " + token);
        }

        var readResult = API.Try(() -> {
            FileInputStream fis = new FileInputStream(file);
            byte[] content = fis.readAllBytes();
            fis.close();
            return content;
        });

        byte[] result = readResult.isSuccess() ? readResult.get() : new byte[0];
        if (securitySettings.isEncrypt()) {
            result = generateCipher(Cipher.DECRYPT_MODE, result);
        }

        return result;
    }

    @Override
    public void remove(String token) {
        var file = new File(uploadRootDirectory, token);

        if (!file.isFile()) {
            throw new IllegalStateException("Cannot locate content for token " + token);
        }

        API.Try(() -> Files.delete(file.toPath()));
    }

    private byte[] generateCipher(int opmode, byte[] raw) {
        var security = currentUserProvider.currentUser().getSecret().toCharArray();
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(security, securitySalt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(opmode, secret);
            return cipher.doFinal(raw);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not setup storage encryption", e);
        }
    }

}
