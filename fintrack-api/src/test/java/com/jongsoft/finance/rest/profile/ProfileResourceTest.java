package com.jongsoft.finance.rest.profile;

import com.jongsoft.finance.domain.user.SessionToken;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.jboss.aerogear.security.otp.Totp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.mockito.Mockito.verify;

@DisplayName("Profile resource")
class ProfileResourceTest extends TestSetup {

    @Test
    @DisplayName("Get the current profile")
    public void get(RequestSpecification spec) {
        // @formatter:off
        spec.when()
                .get("/api/profile")
            .then()
                .statusCode(200)
                .body("currency", Matchers.equalTo("EUR"))
                .body("profilePicture", Matchers.nullValue())
                .body("theme", Matchers.equalTo("dark"))
                .body("mfa", Matchers.equalTo(false));
        // @formatter:on
    }

    @Test
    @DisplayName("Patch the current profile")
    public void patch(RequestSpecification spec) {
        var request = new PatchProfileRequest("updated-password", "USD", "light");

        // @formatter:off
        spec.given()
                .body(request)
            .when()
                .patch("/api/profile")
            .then()
                .statusCode(200)
                .body("currency", Matchers.equalTo("USD"))
                .body("profilePicture", Matchers.nullValue())
                .body("theme", Matchers.equalTo("light"))
                .body("mfa", Matchers.equalTo(false));
        // @formatter:on
    }

    @Test
    @DisplayName("Get active sessions for the user")
    public void sessions(RequestSpecification spec) {
        Mockito.when(userProvider.tokens(ACTIVE_USER.getUsername()))
                .thenReturn(Collections.List(
                        SessionToken.builder()
                                .id(1L)
                                .description("Sample session token")
                                .validity(Dates.range(LocalDateTime.now(), ChronoUnit.DAYS))
                                .build()));

        // @formatter:off
        spec.when()
                .get("/api/profile/sessions")
            .then()
                .statusCode(200)
                .body("size()", Matchers.equalTo(1))
                .body("[0].description", Matchers.equalTo("Sample session token"));
        // @formatter:on
    }

    @Test
    @DisplayName("Create a new session token")
    public void createSession(RequestSpecification spec) {
        Mockito.when(userProvider.tokens(ACTIVE_USER.getUsername()))
                .thenReturn(Collections.List(
                        SessionToken.builder()
                                .id(1L)
                                .description("Sample session token")
                                .validity(Dates.range(LocalDateTime.now(), ChronoUnit.DAYS))
                                .build()));

        // @formatter:off
        spec.given()
                .body(new TokenCreateRequest("sample description", LocalDate.now().plusDays(1)))
            .when()
                .put("/api/profile/sessions")
            .then()
                .statusCode(200)
                .body("size()", Matchers.equalTo(1))
                .body("[0].description", Matchers.equalTo("Sample session token"));
        // @formatter:on
    }

    @Test
    @DisplayName("Revoke a session token")
    public void revokeSession(RequestSpecification spec) {
        var token = Mockito.spy(SessionToken.builder()
                .id(1L)
                .description("Sample session token")
                .validity(Dates.range(LocalDateTime.now(), ChronoUnit.DAYS))
                .build());

        Mockito.when(userProvider.tokens(ACTIVE_USER.getUsername())).thenReturn(Collections.List(token));

        // @formatter:off
        spec.when()
                .delete("/api/profile/sessions/1")
            .then()
                .statusCode(204);
        // @formatter:on

        verify(token).revoke();
    }

    @Test
    @DisplayName("Enable MFA")
    public void enableMfa(RequestSpecification spec) {
        final Totp totp = new Totp(ACTIVE_USER.getSecret());
        var request = new MultiFactorRequest(totp.now());

        // @formatter:off
        spec.given()
                .body(request)
            .when()
                .post("/api/profile/multi-factor/enable")
            .then()
                .statusCode(204);
        // @formatter:on
    }

//    @Test
//    public void disableMfa() {
//        subject.disableMfa();
//
//        var captor = ArgumentCaptor.forClass(UserAccountMultiFactorEvent.class);
//        Mockito.verify(eventPublisher).publishEvent(captor.capture());
//
//        Assertions.assertThat(captor.getValue().getUsername()).isEqualTo(ACTIVE_USER.getUsername());
//        Assertions.assertThat(captor.getValue().isEnabled()).isEqualTo(false);
//    }
}
