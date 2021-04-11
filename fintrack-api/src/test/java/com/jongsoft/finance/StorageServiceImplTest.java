package com.jongsoft.finance;

import com.jongsoft.finance.configuration.SecuritySettings;
import com.jongsoft.finance.configuration.StorageSettings;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.commands.storage.ReplaceFileCommand;
import com.jongsoft.finance.security.CurrentUserProvider;
import org.assertj.core.api.Assertions;
import org.jboss.aerogear.security.otp.api.Base32;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.security.GeneralSecurityException;

class StorageServiceImplTest {

    private StorageServiceImpl subject;

    @Mock
    private SecuritySettings securitySettings;
    @Mock
    private StorageSettings storageSettings;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @BeforeEach
    void setUp() throws GeneralSecurityException {
        MockitoAnnotations.openMocks(this);
        Mockito.when(securitySettings.getSecret()).thenReturn("mySalt");
        Mockito.when(storageSettings.getLocation()).thenReturn(System.getProperty("java.io.tmpdir"));

        subject = new StorageServiceImpl(securitySettings, currentUserProvider, storageSettings);

        Mockito.when(currentUserProvider.currentUser()).thenReturn(
                UserAccount.builder()
                        .secret(Base32.random())
                        .build());
    }

    @Test
    void unencryptedStore() {
        var storageKey = subject.store("My private text".getBytes());
        var read = subject.read(storageKey)
                .blockingGet();

        Assertions.assertThat(new String(read)).isEqualTo("My private text");
        Assertions.assertThat(new File(System.getProperty("java.io.tmpdir") + "/upload/" + storageKey)).exists();
    }

    @Test
    void encryptedStore() {
        Mockito.when(securitySettings.isEncrypt()).thenReturn(true);

        var storageKey = subject.store("My private text".getBytes());
        var read = subject.read(storageKey).blockingGet();

        Assertions.assertThat(new String(read)).isEqualTo("My private text");
        Assertions.assertThat(new File(System.getProperty("java.io.tmpdir") + "/upload/" + storageKey)).exists();
        subject.remove(storageKey);
    }

    @Test
    void storageChange() {
        var storageKey = subject.store("My private text".getBytes());

        var command = Mockito.mock(ReplaceFileCommand.class);
        Mockito.when(command.oldFileCode()).thenReturn(storageKey);
        subject.onStorageChangeEvent(command);
        Assertions.assertThat(new File(System.getProperty("java.io.tmpdir") + "/upload/" + storageKey)).doesNotExist();
    }
}
