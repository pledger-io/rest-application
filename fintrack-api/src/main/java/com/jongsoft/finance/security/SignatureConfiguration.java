package com.jongsoft.finance.security;

import com.nimbusds.jose.JWSAlgorithm;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import io.micronaut.security.token.jwt.signature.SignatureGeneratorConfiguration;
import io.micronaut.security.token.jwt.signature.rsa.RSASignatureGenerator;
import io.micronaut.security.token.jwt.signature.rsa.RSASignatureGeneratorConfiguration;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;

@Slf4j
@Factory
public class SignatureConfiguration implements RSASignatureGeneratorConfiguration {

    private RSAPrivateKey rsaPrivateKey;
    private RSAPublicKey rsaPublicKey;

    public SignatureConfiguration(
            @Value("${micronaut.application.storage.location}/rsa-2048bit-key-pair.pem") String pemPath) {
        var keyPair = SignatureConfiguration.keyPair(pemPath);
        if (keyPair.isPresent()) {
            this.rsaPrivateKey = (RSAPrivateKey) keyPair.get().getPrivate();
            this.rsaPublicKey = (RSAPublicKey) keyPair.get().getPublic();
        }
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
        return rsaPrivateKey;
    }

    @Override
    public JWSAlgorithm getJwsAlgorithm() {
        return JWSAlgorithm.PS256;
    }

    @Override
    public RSAPublicKey getPublicKey() {
        return rsaPublicKey;
    }

    @Bean
    @Named("generator")
    SignatureGeneratorConfiguration signatureGeneratorConfiguration() {
        return new RSASignatureGenerator(this);
    }

    static Optional<KeyPair> keyPair(String pemPath) {
        // Load BouncyCastle as JCA provider
        Security.addProvider(new BouncyCastleProvider());

        // Parse the EC key pair
        try (var pemParser = new PEMParser(new InputStreamReader(Files.newInputStream(Paths.get(pemPath))))) {
            var pemKeyPair = (PEMKeyPair) pemParser.readObject();

            // Convert to Java (JCA) format
            var converter = new JcaPEMKeyConverter();
            var keyPair = converter.getKeyPair(pemKeyPair);

            return Optional.of(keyPair);
        } catch (FileNotFoundException e) {
            log.warn("file not found: {}", pemPath);
        } catch (PEMException e) {
            log.warn("PEMException {}", e.getMessage());
        } catch (IOException e) {
            log.warn("IOException {}", e.getMessage());
        }

        return Optional.empty();
    }

}
