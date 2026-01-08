package com.example.client;

import com.example.config.GitHubApiConfig;
import com.example.dto.GitHubRepoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "githubApiClient",
        url = "${github.api.base-url}",
        configuration = GitHubApiConfig.class
)
public interface GitHubApiClient {

    /**
     * Call GitHub API Get Repository Detail
     * @param owner Repository owner
     * @param repo Repository name
     * @return GitHub API Response
     */
    @GetMapping("/repos/{owner}/{repo}")
    GitHubRepoResponse getRepository(
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo
    );
}
