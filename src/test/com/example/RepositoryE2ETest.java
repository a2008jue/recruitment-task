package com.example;

import com.example.dto.GitHubRepoResponse;
import com.example.entity.RepositoryCache;
import com.example.entity.RepositoryCacheRepository;
import com.example.client.GitHubApiClient;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * E2Test
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RepositoryE2ETest {
    private static final String TEST_OWNER = "octocat";
    private static final String TEST_REPO = "hello-world";
    private static final String FULL_NAME = TEST_OWNER + "/" + TEST_REPO;

    @LocalServerPort
    private int port;

    @MockBean
    private GitHubApiClient githubApiClient;

    @Autowired
    private RepositoryCacheRepository cacheRepository;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        cacheRepository.deleteAll();
        cacheRepository.flush();

        GitHubRepoResponse mockGitHubResponse = new GitHubRepoResponse();
        mockGitHubResponse.setFullName(FULL_NAME);
        mockGitHubResponse.setDescription("GitHub Hello World Repository");
        mockGitHubResponse.setCloneUrl("https://github.com/octocat/hello-world.git");
        mockGitHubResponse.setStars(12345);
        mockGitHubResponse.setCreatedAt("2011-01-26T19:01:12Z");

        when(githubApiClient.getRepository(eq(TEST_OWNER), eq(TEST_REPO)))
                .thenReturn(mockGitHubResponse);
    }

    @Test
    void testFirstCall_NotHitCache_CallGitHubApi_SaveCache() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/repositories/{owner}/{repository-name}", TEST_OWNER, TEST_REPO)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("fullName", equalTo(FULL_NAME))
                .body("description", equalTo("GitHub Hello World Repository"))
                .body("cloneUrl", equalTo("https://github.com/octocat/hello-world.git"))
                .body("stars", equalTo(12345))
                .body("createdAt", equalTo("2011-01-26T19:01:12Z"));

        RepositoryCache cachedRepo = cacheRepository.findByFullName(FULL_NAME)
                .orElseThrow(() -> new AssertionError("Cache is not put"));

        assert cachedRepo.getFullName().equals(FULL_NAME);
        assert cachedRepo.getStars() == 12345;
        assert cachedRepo.getCachedAt() != null;
    }

    @Test
    void testSecondCall_HitCache_NotCallGitHubApi_ReturnCacheData() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/repositories/{owner}/{repository-name}", TEST_OWNER, TEST_REPO)
                .then()
                .statusCode(HttpStatus.OK.value());

        RepositoryCache cachedRepo = cacheRepository.findByFullName(FULL_NAME)
                .orElseThrow(() -> new AssertionError("Cache not Hit"));
        cachedRepo.setStars(99999);
        cachedRepo.setDescription("Modified by E2E Test");
        cacheRepository.save(cachedRepo);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/repositories/{owner}/{repository-name}", TEST_OWNER, TEST_REPO)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("fullName", equalTo(FULL_NAME))
                .body("description", equalTo("Modified by E2E Test"))
                .body("stars", equalTo(99999));

        Mockito.verify(githubApiClient, Mockito.times(1))
                .getRepository(eq(TEST_OWNER), eq(TEST_REPO));
    }

    @Test
    void testGitHubApiCallFailed_ReturnInternalServerError() {
        when(githubApiClient.getRepository(eq(TEST_OWNER), eq(TEST_REPO)))
                .thenThrow(new RuntimeException("GitHub API timeout"));

        given()
                .when()
                .get("/repositories/{owner}/{repository-name}", TEST_OWNER, TEST_REPO)
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

        assert !cacheRepository.findByFullName(FULL_NAME).isPresent();
    }
}