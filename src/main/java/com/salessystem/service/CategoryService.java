package com.salessystem.service;

import com.salessystem.entity.Category;
import com.salessystem.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public List<String> listAllNames() {
        return repository.findAllByOrderByNameAsc().stream().map(Category::getName).toList();
    }

    /** Returns the matching category's name if it already exists (case-insensitive), otherwise registers it. */
    public String getOrCreateName(String name) {
        String trimmed = name.trim();
        return repository.findByNameIgnoreCase(trimmed)
                .map(Category::getName)
                .orElseGet(() -> repository.save(new Category(trimmed)).getName());
    }
}
