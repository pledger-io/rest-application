package com.jongsoft.finance;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SecurityScheme(
        name = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer")
@OpenAPIDefinition(
        security = @SecurityRequirement(name = "bearer"),
        info =
                @Info(
                        title = "Pledger.io",
                        version = "4.0.0",
                        description =
                                "Pledger.io is a self-hosted personal finance application that"
                                        + " helps you track your income and expenses.",
                        license =
                                @License(name = "MIT", url = "https://opensource.org/licenses/MIT"),
                        contact =
                                @Contact(
                                        name = "Jong Soft Development",
                                        url = "https://github.com/pledger-io/rest-application")))
public class Pledger {
    public static void main(String[] args) {
        Micronaut.run(Pledger.class, args);
        System.exit(0);
    }
}
