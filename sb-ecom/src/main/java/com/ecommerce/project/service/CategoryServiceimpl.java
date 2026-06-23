package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositery.CategoryRepository;
//import org.h2.engine.Mode;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceimpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;
    @Override
    public CategoryResponse getAllCategories(Integer PageNumber, Integer PageSize,String sortBy,String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable PageDetails = PageRequest.of(PageNumber,PageSize,sortByAndOrder);
        Page<Category> categoryPage = categoryRepository.findAll(PageDetails);

        List<Category> categories = categoryPage.getContent();
        if(categories.isEmpty()){
            throw  new APIException("No Category created till now");
        }
        List<CategoryDTO> categoryDTOS = categories.stream().
                map(category -> modelMapper.map(category,CategoryDTO.class)).
                toList();
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElement(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = modelMapper.map(categoryDTO,Category.class);
        Category category1 = categoryRepository.findByCategoryName(category.getCategoryName());
        if(category1 != null){
            throw new APIException("category with the name " + category.getCategoryName() + " already exists");
        }
        Category SavedCategory = categoryRepository.save(category);
        CategoryDTO categoryDTO1 = modelMapper.map(SavedCategory,CategoryDTO.class);
        return categoryDTO1;

    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        Optional<Category> savedCategoryOptional = categoryRepository.findById(categoryId);
        Category savedCategory = savedCategoryOptional.orElseThrow(() -> new ResourceNotFoundException("Category","CategoryId",categoryId));

        categoryRepository.delete(savedCategory);

        return modelMapper.map(savedCategory,CategoryDTO.class);
    }



    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO,Long categoryId) {
        Category category = modelMapper.map(categoryDTO,Category.class);
        Optional<Category> savedCategoryOptional = categoryRepository.findById(categoryId);
        Category savedCategory = savedCategoryOptional.orElseThrow(() -> new ResourceNotFoundException("Category", "CategoryId", categoryId));
        category.setCategoryId(categoryId);
        Category category1 = categoryRepository.save(category);
        return modelMapper.map(category1,CategoryDTO.class);

    }
}
