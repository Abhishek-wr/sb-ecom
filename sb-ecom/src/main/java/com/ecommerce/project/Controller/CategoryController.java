package com.ecommerce.project.Controller;

import com.ecommerce.project.config.AppConstant;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;
@RequestMapping("/api")
@RestController
public class CategoryController {
    private CategoryService categoryService;
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;

    }
    @GetMapping("/public/categories")
    private ResponseEntity<CategoryResponse> getCategories(@RequestParam(name = "PageNumber",defaultValue = AppConstant.PAGE_NUMBER,required = false) Integer PageNumber,
                                                           @RequestParam(name = "PageSize",defaultValue = AppConstant.PAGE_SIZE  ,required = false) Integer PageSize,
                                                           @RequestParam(name = "sortBy",defaultValue = AppConstant.SORT_CATEGORIES_BY,required = false) String sortBy,
                                                           @RequestParam(name = "sortOrder",defaultValue = AppConstant.SORT_DIR,required = false) String sortOrder){
        CategoryResponse categoryResponse = categoryService.getAllCategories(PageNumber,PageSize,sortBy,sortOrder);
        return ResponseEntity.ok(categoryResponse);
    }

    @PostMapping("/public/categories")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO){
        CategoryDTO categoryDTO1 = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(categoryDTO1,HttpStatus.ACCEPTED);
    }
    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId){

        CategoryDTO categoryDTO = categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(categoryDTO,HttpStatus.OK);

    }
    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO,@PathVariable Long categoryId){

        CategoryDTO  categoryDTO1= categoryService.updateCategory(categoryDTO,categoryId);
        return new ResponseEntity<>(categoryDTO1,HttpStatus.OK);
    }
}
