package com.example.service;

import com.example.client.GitHubApiClient;
import com.example.dto.GitHubRepoResponse;
import com.example.dto.RepositoryResponse;
import com.example.entity.RepositoryCache;
import com.example.entity.RepositoryCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class RepositoryService {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Autowired
    private GitHubApiClient githubApiClient;
    @Autowired
    private RepositoryCacheRepository cacheRepository;

    public RepositoryResponse getRepositoryDetails(String owner, String repoName) {
        // 1. build fullName
        String fullName = owner + "/" + repoName;

        // 2. query Cache
        RepositoryCache cachedRepo = cacheRepository.findByFullName(fullName).orElse(null);
        if (cachedRepo != null) {
            // hit cache, build response
            return convertToResponse(cachedRepo);
        }

        // 3. not hit ：call GitHub API
        GitHubRepoResponse githubResponse;
        try {
            githubResponse = githubApiClient.getRepository(owner, repoName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch repository from GitHub: " + e.getMessage());
        }

        // 4. put cache
        RepositoryCache newCache = new RepositoryCache();

        newCache.setFullName(githubResponse.getFullName());
        newCache.setDescription(githubResponse.getDescription());
        newCache.setCloneUrl(githubResponse.getCloneUrl());
        newCache.setStars(githubResponse.getStars());

        newCache.setCreatedAt(LocalDateTime.parse(githubResponse.getCreatedAt(), ISO_FORMATTER));
        newCache.setCachedAt(LocalDateTime.now());
        cacheRepository.save(newCache);

        // 5. Build Response DTO And Return
        return convertToResponse(newCache);
    }

    /**
     * convert
     * @param cache
     * @return response
     */
    private RepositoryResponse convertToResponse(RepositoryCache cache) {
        RepositoryResponse response = new RepositoryResponse();

        response.setFullName(cache.getFullName());
        response.setDescription(cache.getDescription());
        response.setCloneUrl(cache.getCloneUrl());
        response.setStars(cache.getStars());

        response.setCreatedAt(cache.getCreatedAt().format(ISO_FORMATTER));
        return response;
    }
}
