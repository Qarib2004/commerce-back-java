package e_commerce.back.service;


import e_commerce.back.entity.Category;
import e_commerce.back.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }



  @Transactional(readOnly = true)
    public List<Category> findAll(){
        return  categoryRepository.findAll();
  }


  @Transactional
    public Category saveCategory(Category category){
        return categoryRepository.save(category);
  }


  @Transactional(readOnly = true)
    public Optional<Category> findById(Long id){
        return categoryRepository.findById(id);
  }


  @Transactional
    public Optional<Category> updateCategory(Long id,Category categoryDetails){
        return categoryRepository.findById(id)
                .map(existingCategory -> {
                    existingCategory.setName(categoryDetails.getName());
                    existingCategory.setDescription(categoryDetails.getDescription());
                    existingCategory.setSlug(categoryDetails.getSlug());
                    existingCategory.setImageUrl(categoryDetails.getImageUrl());
                    return existingCategory;
                });
  }


    @Transactional
    public boolean deleteProduct(Long id){
        return categoryRepository.findById(id)
                .map(category -> {
                    categoryRepository.delete(category);
                    return true;
                }).orElse(false);
    }






}
