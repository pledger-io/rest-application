package com.jongsoft.finance.security;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.jboss.aerogear.security.otp.Totp;
import com.jongsoft.finance.domain.user.UserAccount;

public class TwoFactorHelper {

    private static final Pattern SECURITY_CODE_PATTER = Pattern.compile("[0-9]{6}");

    private static final String GENERATOR_URL = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=";

    public static String build2FactorQr(UserAccount userAccount) {
        return GENERATOR_URL + URLEncoder.encode(String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                "FinTrack", userAccount.getUsername(), userAccount.getSecret(), "FinTrack"), StandardCharsets.UTF_8);
    }

    public static boolean verifySecurityCode(String secret, String securityCode) {
        if (securityCode != null && SECURITY_CODE_PATTER.matcher(securityCode).matches()) {
            return new Totp(secret).verify(securityCode);
        }

        return false;
    }

}
