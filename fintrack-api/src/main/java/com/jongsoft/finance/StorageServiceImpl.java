package com.jongsoft.finance;

import com.jongsoft.finance.configuration.SecuritySettings;
import com.jongsoft.finance.configuration.StorageSettings;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.security.Encryption;
import com.jongsoft.lang.API;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.UUID;

@Singleton
@Named("storageService")
public class StorageServiceImpl implements StorageService {

    private final SecuritySettings securitySettings;
    private final CurrentUserProvider currentUserProvider;
    private final File uploadRootDirectory;
    private final Encryption encryption;

    public StorageServiceImpl(
            SecuritySettings securitySettings,
            CurrentUserProvider currentUserProvider,
            StorageSettings storageLocation) {
        this.securitySettings = securitySettings;
        this.currentUserProvider = currentUserProvider;
        this.encryption = new Encryption(securitySettings);

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
            toStore = encryption.encrypt(
                    content,
                    currentUserProvider.currentUser().getSecret());
        } else {
            toStore = content;
        }

        var fileCreated = API.Try(() -> new FileOutputStream(new File(uploadRootDirectory, token), false))
                .consume(os -> {
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
            result = encryption.decrypt(
                    result,
                    currentUserProvider.currentUser().getSecret());
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

}
