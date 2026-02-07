package com.example.ecommerce.api;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.api.dto.CartCreateRequest;
import com.example.ecommerce.api.dto.CartItemAddRequest;
import com.example.ecommerce.api.dto.CartItemResponse;
import com.example.ecommerce.api.dto.CartResponse;
import com.example.ecommerce.domain.Cart;
import com.example.ecommerce.domain.CartItem;
import com.example.ecommerce.domain.CartStatus;
import com.example.ecommerce.domain.Customer;
import com.example.ecommerce.domain.Product;
import com.example.ecommerce.exception.NotFoundException;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.CustomerRepository;
import com.example.ecommerce.repository.ProductRepository;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public CartController(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @PostMapping
    public ResponseEntity<CartResponse> createCart(@Valid @RequestBody CartCreateRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer not found: " + request.getCustomerId()));

        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setCreatedAt(OffsetDateTime.now());
        cart.setUpdatedAt(OffsetDateTime.now());
        Cart saved = cartRepository.save(cart);

        CartResponse response = new CartResponse();
        response.setId(saved.getId());
        response.setCustomerId(customer.getId());
        response.setItems(List.of());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{cartId}/items")
    public CartResponse addItem(
            @PathVariable Long cartId,
            @Valid @RequestBody CartItemAddRequest request
    ) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart not found: " + cartId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found: " + request.getProductId()));

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(request.getQuantity());
        item.setCreatedAt(OffsetDateTime.now());
        cartItemRepository.save(item);

        cart.setUpdatedAt(OffsetDateTime.now());
        cartRepository.save(cart);

        return toResponse(cart);
    }

    @GetMapping("/{cartId}")
    public CartResponse getCart(@PathVariable Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart not found: " + cartId));
        return toResponse(cart);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cartItemRepository.findByCartId(cart.getId()).stream()
                .map(this::toItemResponse)
                .toList();

        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setCustomerId(cart.getCustomer().getId());
        response.setItems(items);
        return response;
    }

    private CartItemResponse toItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.setProductId(item.getProduct().getId());
        response.setQuantity(item.getQuantity());
        return response;
    }
}
