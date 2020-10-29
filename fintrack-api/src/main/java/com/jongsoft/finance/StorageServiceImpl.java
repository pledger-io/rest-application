package com.jongsoft.finance;

import com.jongsoft.finance.configuration.SecuritySettings;
import com.jongsoft.finance.configuration.StorageSettings;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.security.Encryption;
import com.jongsoft.lang.API;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.UUID;

@Singleton
@Named("storageService")
public class StorageServiceImpl implements StorageService {

    private final SecuritySettings securitySettings;
    private final CurrentUserProvider currentUserProvider;
    private final Path uploadRootDirectory;
    private final Encryption encryption;

    public StorageServiceImpl(
            SecuritySettings securitySettings,
            CurrentUserProvider currentUserProvider,
            StorageSettings storageLocation) throws GeneralSecurityException {
        this.securitySettings = securitySettings;
        this.currentUserProvider = currentUserProvider;
        this.encryption = new Encryption(securitySettings);

        uploadRootDirectory = Path.of(storageLocation.getLocation(), "upload");
        if (Files.notExists(uploadRootDirectory)) {
            API.Try(() -> Files.createDirectory(uploadRootDirectory));
        }
    }

    @Override
    public String store(byte[] content) {
        var token = UUID.randomUUID().toString();

        byte[] toStore;
        if (securitySettings.isEncrypt()) {
            toStore = encryption.encrypt(
                    content,
                    currentUserProvider.currentUser().getSecret());
        } else {
            toStore = content;
        }

        try {
            Files.write(
                    uploadRootDirectory.resolve(token),
                    toStore,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE_NEW);
            return token;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public byte[] read(String token) {
        try {
            var readResult = Files.readAllBytes(uploadRootDirectory.resolve(token));

            if (securitySettings.isEncrypt()) {
                readResult = encryption.decrypt(
                        readResult,
                        currentUserProvider.currentUser().getSecret());
            }

            return readResult;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot locate content for token " + token);
        }
    }

    @Override
    public void remove(String token) {
        try {
            Files.deleteIfExists(uploadRootDirectory.resolve(token));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot locate content for token " + token);
        }
    }

}
