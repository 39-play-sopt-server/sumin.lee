package org.sopt;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:assignment;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false",
        "app.auth.jwt-secret=YS10ZXN0LWtleS1vbmx5LW5ldmVyLXVzZS1pbi1wcm9kdWN0aW9u"
})
@AutoConfigureMockMvc
class AuthIntegrationTest {

    private static final String MEMBERS = "/api/v1/members";
    private static final String POSTS = "/api/v1/posts";
    private static final String ALLOWED_ORIGIN = "http://localhost:5173";
    private static final String PASSWORD = "review-password";
    private static final String POST_BODY = """
            {"title":"제목","content":"본문","category":"GENERAL","memberId":999999}
            """;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostRepository posts;

    @Test
    void rotatesRefreshTokenAndRevokesSessionOnLogout() throws Exception {
        signup("rotation");
        MvcResult initial = login("rotation");
        Cookie oldCookie = refreshCookie(initial);

        MvcResult refreshed = mvc.perform(post(AuthController.PATH + "/refresh")
                        .header(AuthRequestFilter.CSRF_HEADER, "1").cookie(oldCookie))
                .andExpect(status().isNoContent()).andReturn();
        Cookie currentCookie = refreshCookie(refreshed);
        assertNotEquals(oldCookie.getValue(), currentCookie.getValue());
        assertNotEquals(bearer(initial), bearer(refreshed));

        mvc.perform(post(AuthController.PATH + "/refresh")
                        .header(AuthRequestFilter.CSRF_HEADER, "1").cookie(oldCookie))
                .andExpect(status().isUnauthorized());
        mvc.perform(post(POSTS).header(HttpHeaders.AUTHORIZATION, "Bearer " + currentCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON).content(POST_BODY))
                .andExpect(status().isUnauthorized());

        mvc.perform(post(AuthController.PATH + "/logout")
                        .header(AuthRequestFilter.CSRF_HEADER, "1").cookie(currentCookie))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
        mvc.perform(post(POSTS).header(HttpHeaders.AUTHORIZATION, bearer(refreshed))
                        .contentType(MediaType.APPLICATION_JSON).content(POST_BODY))
                .andExpect(status().isUnauthorized());
        mvc.perform(post(AuthController.PATH + "/refresh")
                        .header(AuthRequestFilter.CSRF_HEADER, "1").cookie(currentCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void documentsSwaggerAuthenticationSchemes() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.accessToken.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.csrfProtection.name")
                        .value(AuthRequestFilter.CSRF_HEADER));
    }

    @Test
    void protectsCookieEndpointsWithCsrfHeaderAndRestrictedCors() throws Exception {
        signup("csrf-user");
        MvcResult login = login("csrf-user");

        mvc.perform(post(AuthController.PATH + "/refresh").cookie(refreshCookie(login)))
                .andExpect(status().isForbidden());
        mvc.perform(options(AuthController.PATH + "/refresh")
                        .header(HttpHeaders.ORIGIN, "https://untrusted.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, AuthRequestFilter.CSRF_HEADER))
                .andExpect(status().isForbidden());
        mvc.perform(options(AuthController.PATH + "/refresh")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, AuthRequestFilter.CSRF_HEADER))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    void usesAuthenticatedAuthorAndRevokesAllSessionsOnWithdrawal() throws Exception {
        long ownerId = signup("owner");
        MvcResult owner = login("owner");
        MvcResult secondOwnerSession = login("owner");
        signup("another");
        MvcResult anotherMember = login("another");

        mvc.perform(get(POSTS)).andExpect(status().isOk());
        mvc.perform(post(POSTS).contentType(MediaType.APPLICATION_JSON).content(POST_BODY))
                .andExpect(status().isUnauthorized());
        mvc.perform(post(POSTS).header(HttpHeaders.AUTHORIZATION, bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON).content(POST_BODY))
                .andExpect(status().isCreated());
        long postId = posts.findAll().getFirst().getId();

        mvc.perform(put(POSTS + "/" + postId).header(HttpHeaders.AUTHORIZATION, bearer(anotherMember))
                        .contentType(MediaType.APPLICATION_JSON).content(POST_BODY))
                .andExpect(status().isForbidden());
        mvc.perform(delete(POSTS + "/" + postId).header(HttpHeaders.AUTHORIZATION, bearer(anotherMember)))
                .andExpect(status().isForbidden());
        mvc.perform(put(POSTS + "/" + postId).header(HttpHeaders.AUTHORIZATION, bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON).content(POST_BODY))
                .andExpect(status().isOk());

        mvc.perform(delete(MEMBERS + "/" + ownerId).header(HttpHeaders.AUTHORIZATION, bearer(anotherMember)))
                .andExpect(status().isForbidden());
        mvc.perform(delete(MEMBERS + "/" + ownerId).header(HttpHeaders.AUTHORIZATION, bearer(owner)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
        mvc.perform(delete(POSTS + "/" + postId).header(HttpHeaders.AUTHORIZATION, bearer(secondOwnerSession)))
                .andExpect(status().isUnauthorized());
        mvc.perform(post(AuthController.PATH + "/refresh")
                        .header(AuthRequestFilter.CSRF_HEADER, "1").cookie(refreshCookie(secondOwnerSession)))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(POSTS + "/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authorName").value("탈퇴한 회원"));
    }

    private long signup(String username) throws Exception {
        MvcResult result = mvc.perform(post(MEMBERS).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"테스트 회원","username":"%s","password":"%s"}
                                """.formatted(username, PASSWORD)))
                .andExpect(status().isCreated()).andReturn();
        return ((Number) com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.data"))
                .longValue();
    }

    private MvcResult login(String username) throws Exception {
        return mvc.perform(post(AuthController.PATH + "/login").header(AuthRequestFilter.CSRF_HEADER, "1")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, PASSWORD)))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.AUTHORIZATION))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Strict")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/")))
                .andReturn();
    }

    private static Cookie refreshCookie(MvcResult result) {
        return result.getResponse().getCookie(AuthController.REFRESH_COOKIE);
    }

    private static String bearer(MvcResult result) {
        return result.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
    }
}
