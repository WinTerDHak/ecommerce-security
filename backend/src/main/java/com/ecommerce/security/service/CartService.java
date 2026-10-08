package com.ecommerce.security.service;

import com.ecommerce.security.dto.cart.CartItemRequestDTO;
import com.ecommerce.security.dto.cart.CartItemResponseDTO;
import com.ecommerce.security.entity.CartItem;
import com.ecommerce.security.entity.Product;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.repository.CartItemRepository;
import com.ecommerce.security.repository.ProductRepository;
import com.ecommerce.security.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<CartItemResponseDTO> getCartItems(Long userId) {
        return cartItemRepository.findByUser_Id(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CartItemResponseDTO addToCart(Long userId, CartItemRequestDTO request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }

        User user = userRepository.getReferenceById(userId);

        Optional<CartItem> existingItem = cartItemRepository.findByUser_IdAndProduct_Id(userId, request.getProductId());
        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }
            cartItem.setQuantity(newQuantity);
        } else {
            cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
        }

        CartItem saved = cartItemRepository.save(cartItem);
        return mapToDTO(saved);
    }

    @Transactional
    public CartItemResponseDTO updateCartItem(Long userId, Long cartItemId, CartItemRequestDTO request) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        // IDOR prevention: Ensure the cart item belongs to the authenticated user
        if (!cartItem.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied: You do not own this cart item");
        }

        Product product = cartItem.getProduct();
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }

        cartItem.setQuantity(request.getQuantity());
        return mapToDTO(cartItemRepository.save(cartItem));
    }

    @Transactional
    public void removeCartItem(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        // IDOR prevention: Ensure the cart item belongs to the authenticated user
        if (!cartItem.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied: You do not own this cart item");
        }

        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUser_Id(userId);
        cartItemRepository.deleteAll(items);
    }

    private CartItemResponseDTO mapToDTO(CartItem cartItem) {
        return new CartItemResponseDTO(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getPrice(),
                cartItem.getQuantity()
        );
    }
}
