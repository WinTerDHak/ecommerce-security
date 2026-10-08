package com.ecommerce.security.controller;

import com.ecommerce.security.dto.cart.CartItemRequestDTO;
import com.ecommerce.security.dto.cart.CartItemResponseDTO;
import com.ecommerce.security.security.CustomUserDetails;
import com.ecommerce.security.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponseDTO>> getCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCartItems(userDetails.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemResponseDTO> addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CartItemRequestDTO request) {
        return ResponseEntity.ok(cartService.addToCart(userDetails.getId(), request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartItemResponseDTO> updateCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemRequestDTO request) {
        return ResponseEntity.ok(cartService.updateCartItem(userDetails.getId(), itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itemId) {
        cartService.removeCartItem(userDetails.getId(), itemId);
        return ResponseEntity.noContent().build();
    }
}
