package com.jongsoft.finance.security;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.user.UserAccount;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

import java.util.regex.Pattern;

public class TwoFactorHelper {

    private static final TimeProvider timeProvider = new SystemTimeProvider();
    private static final CodeGenerator codeGenerator = new DefaultCodeGenerator();

    private static final Pattern SECURITY_CODE_PATTER = Pattern.compile("[0-9]{6}");

    private TwoFactorHelper() {}

    public static byte[] build2FactorQr(UserAccount userAccount) {
        var qrData = new QrData.Builder()
                .label("Pledger.io: " + userAccount.getUsername())
                .secret(userAccount.getSecret())
                .issuer("Pledger.io")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
        try {
            var generator = new ZxingPngQrGenerator();
            generator.setImageSize(150);
            return generator.generate(qrData);
        } catch (QrGenerationException e) {
            throw StatusException.internalError("Could not successfully generate QR code");
        }
    }

    public static boolean verifySecurityCode(String secret, String securityCode) {
        if (securityCode != null && SECURITY_CODE_PATTER.matcher(securityCode).matches()) {
            var verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
            return verifier.isValidCode(secret, securityCode);
        }

        return false;
    }
}
