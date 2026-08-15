package com.salessystem.config;

import com.salessystem.entity.Category;
import com.salessystem.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Seeds the starter categories on first run; a no-op once they (or any others) already exist. */
@Component
public class CategoryDataSeeder implements CommandLineRunner {

    private static final List<String> DEFAULT_CATEGORIES = List.of("Roupas", "Brinquedos", "Acessórios");

    private final CategoryRepository repository;

    public CategoryDataSeeder(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        DEFAULT_CATEGORIES.forEach(name -> repository.save(new Category(name)));
    }
}
