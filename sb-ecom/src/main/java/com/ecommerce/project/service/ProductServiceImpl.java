package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositery.CartRepository;
import com.ecommerce.project.repositery.CategoryRepository;
import com.ecommerce.project.repositery.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService{
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Autowired
    private CartService cartService;

    @Override
    public ProductDTO addProduct(ProductDTO productDTO, Long categoryId) {
        // check f product is already present or not
        Category category = categoryRepository.findById(categoryId).
                orElseThrow(()-> new ResourceNotFoundException("Category","categoryId",categoryId));

        Product product = modelMapper.map(productDTO,Product.class);
        boolean ispresent = false;
        List<Product> list = category.getProducts();
        for(int i = 0; i< list.size();i++){
            if(list.get(i).getProductName().equalsIgnoreCase(product.getProductName())){
                ispresent = true;
                break;
            }
        }
        if(ispresent){
            throw  new APIException("This product is already present");
        }
        else{
            product.setCategory(category);
            double sprice = product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice());
            product.setSpecialPrice(sprice);
            product.setImage("default.png");
            productRepository.save(product);
            return modelMapper.map(product,ProductDTO.class);
        }


    }

    @Override
    public ProductResponse getAllProduct(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByandOrder = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending():
                Sort.by(sortBy).descending();
        Pageable pageDetail = PageRequest.of(pageNumber,pageSize,sortByandOrder);
        Page<Product> productPage = productRepository.findAll(pageDetail);
        List<Product> productList = productPage.getContent();

        if(productList.isEmpty()){
            throw  new APIException("No product found " );
        }
        List<ProductDTO> productDTOList = productList.stream().
                map(product -> modelMapper.map(product,ProductDTO.class)).toList();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOList);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElement(productPage.getTotalElements());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;

    }

    @Override
    public ProductResponse getProductByCategoryId(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(()-> new ResourceNotFoundException("Category","CategoryId",categoryId));
        Sort sortByandOrder = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending():
                Sort.by(sortBy).descending();
        Pageable pageDetail = PageRequest.of(pageNumber,pageSize,sortByandOrder);
        Page<Product> productPage = productRepository.findByCategoryOrderByPriceAsc(category,pageDetail);
        List<Product> productList = productPage.getContent();
        if(productList.isEmpty()){
            throw  new APIException("No product found for this categoryId: " + categoryId);
        }
        List<ProductDTO> productDTOList = productList.stream().
                map(product -> modelMapper.map(product,ProductDTO.class)).toList();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOList);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElement(productPage.getTotalElements());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByandOrder = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending():
                Sort.by(sortBy).descending();
        Pageable pageDetail = PageRequest.of(pageNumber,pageSize,sortByandOrder);
        Page<Product> productPage = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%',pageDetail);
        List<Product> productList =productPage.getContent();
        if(productList.isEmpty()){
            throw  new APIException("No product found for this Keyword: " + keyword);
        }
        List<ProductDTO> productDTOList = productList.stream().
                map(product -> modelMapper.map(product,ProductDTO.class)).toList();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOList);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElement(productPage.getTotalElements());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO,Long productId) {

        Product productfromdb = productRepository.findById(productId).
                orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));
        Product product = modelMapper.map(productDTO,Product.class);
        productfromdb.setProductName(product.getProductName());
        productfromdb.setDescription(product.getDescription());
        productfromdb.setPrice(product.getPrice());
        productfromdb.setDiscount(product.getDiscount());
        productfromdb.setQuantity(product.getQuantity());
        double sprice = product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice());
        productfromdb.setSpecialPrice(sprice);
        productfromdb.setImage("default.png");
        productRepository.save(productfromdb);

        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
            List<ProductDTO> products = cart.getCartItems().stream().map(p ->
                    modelMapper.map(p.getProduct(),ProductDTO.class))
                    .toList();
            cartDTO.setProducts(products);
            return cartDTO;
        }).toList();
        cartDTOs.forEach(cart -> cartService.updateProductInCart(cart.getCartId(),productId));
        return modelMapper.map(productfromdb,ProductDTO.class);

    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId).
                orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));
        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(),productId));
        productRepository.delete(product);
        return modelMapper.map(product,ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        // Get the product from DB
        Product productfromedb = productRepository.findById(productId).
                orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));

        // Upload Image to server
        // Get the file name of uploaded image

        String fileName = fileService.uploadImage(path,image);

        // Updating the new file name to the product
        productfromedb.setImage(fileName);
        // save product
        Product updatedProduct = productRepository.save(productfromedb);

        // return dto after mapping product to DTOf
        return modelMapper.map(updatedProduct,ProductDTO.class);

    }




}
