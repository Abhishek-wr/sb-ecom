package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositery.CartItemRepository;
import com.ecommerce.project.repositery.CartRepository;
import com.ecommerce.project.repositery.ProductRepository;
import jakarta.transaction.Transactional;
import org.aspectj.asm.IModelFilter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.ecommerce.project.util.AuthUtil;
import org.springframework.util.AutoPopulatingList;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    AuthUtil authUtil;

    @Autowired
    private ModelMapper modelMapper;

    private CartRepository cartRepository;
    private CartItemRepository cartItemRepository;
    private ProductRepository productRepository;
    public CartServiceImpl(CartRepository cartRepository,CartItemRepository cartItemRepository,ProductRepository productRepository){
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository=productRepository;
    }
    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        Cart cart = createCart();
        //Retriever product through productid
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","ProductId",productId));
        // perform validation
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(
                cart.getCartId(),
                productId
        );
        if(cartItem != null){
            throw new APIException("Product" + product.getProductName() + "already exist");
        }
        if(product.getQuantity() == 0){
            throw new APIException(product.getProductName() + "is not available");

        }if(product.getQuantity() < quantity){
            throw new APIException("Please ,make an order of the " + product.getProductName()
             + "less than or equal to the quantity" + product.getQuantity() + ".");

        }
        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());
        cartItemRepository.save(newCartItem);
        product.setQuantity(product.getQuantity());
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cartRepository.save(cart);
        CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();
        Stream<ProductDTO> productStream= cartItems.stream().map(item ->{
            ProductDTO map = modelMapper.map(item.getProduct(),ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });
        cartDTO.setProducts(productStream.toList());
        return cartDTO;
        //return upated cart
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if(carts.isEmpty()){
            throw new APIException("No cart exist");
        }
        List<CartDTO> cartDTOS = carts.stream()
                .map(cart -> {
                    CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
                    List<ProductDTO> products = cart.getCartItems().stream()
                            .map(p-> modelMapper.map(p.getProduct(),ProductDTO.class))
                            .toList();
                    cartDTO.setProducts(products);
                    return cartDTO;
                }).toList();
        return cartDTOS;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailandCartId(emailId,cartId);
        if(cart == null){
            throw  new ResourceNotFoundException("Cart","cartId",cartId);
        }
        CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
        cart.getCartItems().forEach(c -> c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDTO> productDTOList = cart.getCartItems().stream().map(
                p -> modelMapper.map(p.getProduct(),ProductDTO.class))
                        .toList();

        cartDTO.setProducts(productDTOList);
        return cartDTO;
    }
    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        String email = authUtil.loggedInEmail();
        Cart usercart = cartRepository.findCartByEmail(email);
        Long cartId = usercart.getCartId();
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()->new ResourceNotFoundException("Cart","cartId",cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","ProductId",productId));
        if(product.getQuantity() == 0){
            throw new APIException(product.getProductName() + "is not available");

        }if(product.getQuantity() < quantity){
            throw new APIException("Please ,make an order of the " + product.getProductName()
                    + "less than or equal to the quantity" + product.getQuantity() + ".");

        }
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);
        if(cartItem == null){
            throw new APIException("Product " + product.getProductName() + "not available in the cart !!!!");
        }
        int newQuantity = cartItem.getQuantity() + quantity;

        // 2. Handle Deletion (if result is <= 0)
        if(newQuantity < 0){
            throw  new APIException("The resulting quantity cannot be negative");
        }
        if (newQuantity == 0) {
            deleteProductFromCart(cartId, productId);
        }else{
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            cartRepository.save(cart);
        }

        CartItem updatedItem = cartItemRepository.save(cartItem);
        if(updatedItem.getQuantity() == 0){
            cartItemRepository.deleteById(updatedItem.getCartItemId());
        }

        CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();
        Stream<ProductDTO> productDTOStream = cartItems.stream().map(item -> {
            ProductDTO prd = modelMapper.map(item.getProduct(),ProductDTO.class);
            prd.setQuantity(item.getQuantity());
            return prd;

        });
        cartDTO.setProducts(productDTOStream.toList());
        return cartDTO;
    }
    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {


        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()->new ResourceNotFoundException("Cart","cartId",cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","ProductId",productId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);
        if(cartItem == null){
            throw new APIException("Product " + product.getProductName() + "not available in the cart !!!!");
        }
        cart.setTotalPrice(cart.getTotalPrice()-(cartItem.getProductPrice() * cartItem.getQuantity()) );
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId,productId);
//        cartRepository.save(cart);

        return "product" + product.getProductName() + "has been removed from your cart";
    }

    @Override
    public void updateProductInCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()->new ResourceNotFoundException("Cart","cartId",cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","ProductId",productId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);
        if(cartItem == null){
            throw new APIException("Product " + product.getProductName() + "not available in the cart !!!!");
        }
        double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());
        cartItem.setProductPrice(product.getSpecialPrice());
        cart.setTotalPrice(cartPrice +(cartItem.getProductPrice() * cartItem.getQuantity()));
        cartItem = cartItemRepository.save(cartItem);

    }

    private Cart createCart(){
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart != null){
            return userCart;
        }else{
            Cart cart = new Cart();
            cart.setTotalPrice(0.00);
            cart.setUser(authUtil.loggedInUser());
            Cart newCart = cartRepository.save(cart);
            return newCart;
        }
    }
}
