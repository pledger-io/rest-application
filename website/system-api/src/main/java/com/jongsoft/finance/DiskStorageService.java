package com.jongsoft.finance;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.configuration.SecuritySettings;
import com.jongsoft.finance.configuration.StorageSettings;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.messaging.commands.storage.ReplaceFileCommand;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.security.Encryption;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.control.Optional;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Singleton
@Named("storageService")
public class DiskStorageService implements StorageService {

    private final SecuritySettings securitySettings;
    private final AuthenticationFacade authenticationFacade;
    private final UserProvider userProvider;
    private final Path uploadRootDirectory;
    private final Encryption encryption;

    public DiskStorageService(
            SecuritySettings securitySettings,
            AuthenticationFacade authenticationFacade,
            UserProvider userProvider,
            StorageSettings storageLocation) {
        this.securitySettings = securitySettings;
        this.authenticationFacade = authenticationFacade;
        this.userProvider = userProvider;
        this.encryption = new Encryption();

        uploadRootDirectory = Path.of(storageLocation.getLocation(), "upload");
        if (Files.notExists(uploadRootDirectory)) {
            Control.Try(() -> Files.createDirectory(uploadRootDirectory));
        }
    }

    @Override
    public String store(byte[] content) {
        var token = UUID.randomUUID().toString();

        byte[] toStore;
        if (securitySettings.isEncrypt()) {
            toStore = encryption.encrypt(content, getEncryptionTokenForUser());
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
    public Optional<byte[]> read(String token) {
        try {
            var readResult = Files.readAllBytes(uploadRootDirectory.resolve(token));

            if (securitySettings.isEncrypt()) {
                readResult = encryption.decrypt(readResult, getEncryptionTokenForUser());
            }

            return Control.Option(readResult);
        } catch (IOException e) {
            throw StatusException.notFound("Cannot locate content for token " + token);
        } catch (IllegalStateException e) {
            throw StatusException.notAuthorized("Cannot access file with token " + token);
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

    @BusinessEventListener
    public void onStorageChangeEvent(ReplaceFileCommand event) {
        if (event.oldFileCode() != null) {
            this.remove(event.oldFileCode());
        }
    }

    private String getEncryptionTokenForUser() {
        return userProvider
                .lookup(new UserIdentifier(authenticationFacade.authenticated()))
                .map(UserAccount::getSecret)
                .getOrThrow(
                        () -> StatusException.internalError("Cannot correctly determine user."));
    }
}
