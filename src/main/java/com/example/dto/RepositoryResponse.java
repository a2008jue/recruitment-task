package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryResponse {
    private String fullName;
    private String description;
    private String cloneUrl;
    private Integer stars;
    private String createdAt;
}
