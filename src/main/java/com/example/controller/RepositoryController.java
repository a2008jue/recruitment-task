package com.example.controller;

import com.example.dto.RepositoryResponse;
import com.example.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/repositories")
public class RepositoryController {

    @Autowired
    private RepositoryService repositoryService;

    @GetMapping("/{owner}/{repository-name}")
    public ResponseEntity<RepositoryResponse> getRepository(
            @PathVariable("owner") String owner,
            @PathVariable("repository-name") String repoName
    ) {
        RepositoryResponse response = repositoryService.getRepositoryDetails(owner, repoName);
        return ResponseEntity.ok(response);
    }
}
