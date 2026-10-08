package com.ecommerce.security.repository;
import com.ecommerce.security.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoryRepository extends JpaRepository<Category, Long> {}
