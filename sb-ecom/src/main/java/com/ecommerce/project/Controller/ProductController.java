package com.ecommerce.project.Controller;

import com.ecommerce.project.config.AppConstant;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.service.ProductService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ProductController {
    private ProductService productService;
    public ProductController(ProductService productService){
        this.productService = productService;
    }
    @PostMapping("/admin/categories/{categoryId}/products")
    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO productDTO,
                                                 @PathVariable Long categoryId){
        ProductDTO productDTO1 = productService.addProduct(productDTO,categoryId);
        return new ResponseEntity<>(productDTO1,HttpStatus.CREATED);
    }
    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProduct(@RequestParam(name = "PageNumber",defaultValue = AppConstant.PAGE_NUMBER,required = false) Integer PageNumber,
                                                         @RequestParam(name = "PageSize",defaultValue = AppConstant.PAGE_SIZE  ,required = false) Integer PageSize,
                                                         @RequestParam(name = "sortBy",defaultValue = AppConstant.SORT_PRODUCT_BY,required = false) String sortBy,
                                                         @RequestParam(name = "sortOrder",defaultValue = AppConstant.SORT_DIR,required = false) String sortOrder
                                                         ){
        ProductResponse productResponse = productService.getAllProduct(PageNumber,PageSize,sortBy,sortOrder);
        return new ResponseEntity<>(productResponse,HttpStatus.OK);
    }
    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductByCategoryId(@RequestParam(name = "PageNumber",defaultValue = AppConstant.PAGE_NUMBER,required = false) Integer PageNumber,
                                                                  @RequestParam(name = "PageSize",defaultValue = AppConstant.PAGE_SIZE  ,required = false) Integer PageSize,
                                                                  @RequestParam(name = "sortBy",defaultValue = AppConstant.SORT_PRODUCT_BY,required = false) String sortBy,
                                                                  @RequestParam(name = "sortOrder",defaultValue = AppConstant.SORT_DIR,required = false) String sortOrder,
                                                                  @PathVariable Long categoryId){
        ProductResponse productResponse = productService.getProductByCategoryId(categoryId,PageNumber,PageSize,sortBy,sortOrder);
        return new ResponseEntity<>(productResponse,HttpStatus.OK);
    }
    @GetMapping("/public/categories/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductsByKeyword(@RequestParam(name = "PageNumber",defaultValue = AppConstant.PAGE_NUMBER,required = false) Integer PageNumber,
                                                                @RequestParam(name = "PageSize",defaultValue = AppConstant.PAGE_SIZE  ,required = false) Integer PageSize,
                                                                @RequestParam(name = "sortBy",defaultValue = AppConstant.SORT_PRODUCT_BY,required = false) String sortBy,
                                                                @RequestParam(name = "sortOrder",defaultValue = AppConstant.SORT_DIR,required = false) String sortOrder,
                                                                @PathVariable String keyword){
        ProductResponse productResponse = productService.searchProductByKeyword(keyword,PageNumber,PageSize,sortBy,sortOrder);

        return new ResponseEntity<>(productResponse,HttpStatus.FOUND);
    }
    @PutMapping("products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@Valid @RequestBody ProductDTO productDTO,
                                                 @PathVariable Long productId){
        ProductDTO productDTO1 = productService.updateProduct(productDTO,productId);
        return new ResponseEntity<>(productDTO1,HttpStatus.OK);
    }
    @DeleteMapping("admin/products/{productId}")
    public  ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId){
        ProductDTO productDTO = productService.deleteProduct(productId);
        return new ResponseEntity<>(productDTO,HttpStatus.OK);

    }
    @PutMapping("/products/{productId}/image")
    public ResponseEntity<ProductDTO> updateProductImage(@PathVariable Long productId,
                                                         @RequestParam("Image") MultipartFile image) throws IOException {
        ProductDTO updatedproduct =  productService.updateProductImage(productId,image);
        return new ResponseEntity<>(updatedproduct,HttpStatus.OK);
    }


}
