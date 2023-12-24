package com.jongsoft.finance;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "FinTrack",
                version = "2.0.0",
                license = @License(name = "MIT", url = "https://opensource.org/licenses/MIT"),
                contact = @Contact(
                        name = "Jong Soft Development",
                        url = "https://bitbucket.org/jongsoftdev/workspace/projects/FIN"
                )
        ),
        security = @SecurityRequirement(name = "bearer")
)
@SecurityScheme(
        name = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
        System.exit(0);
    }

}
