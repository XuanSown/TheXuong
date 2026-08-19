package com.example.thexuong.controller;

import com.example.thexuong.dto.ApiResponse;
import com.example.thexuong.dto.ReviewDto;
import com.example.thexuong.dto.ReviewListResponse;
import com.example.thexuong.dto.ReviewRequest;
import com.example.thexuong.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewRestController {

    private final ReviewService reviewService;

    /**
     * GET /api/v1/reviews/product/{productId} — Public.
     * Nếu request có đăng nhập thì trả kèm cờ isMine / canModerate theo user đang xem.
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ReviewListResponse> getProductReviews(
            @PathVariable Long productId,
            Authentication authentication) {
        String viewerEmail = (authentication == null || authentication instanceof AnonymousAuthenticationToken)
                ? null : authentication.getName();
        return ResponseEntity.ok(reviewService.getProductReviews(productId, viewerEmail));
    }

    /**
     * POST /api/v1/reviews — Authenticated. Body: { productId, rating, comment }
     */
    @PostMapping
    public ResponseEntity<ReviewDto> createReview(Authentication authentication,
                                                  @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(authentication.getName(), request));
    }

    /**
     * PUT /api/v1/reviews/{id} — Chủ review hoặc ADMIN/BOTH. Body: { rating, comment }
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> updateReview(Authentication authentication,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(authentication.getName(), id, request));
    }

    /**
     * DELETE /api/v1/reviews/{id} — Chỉ ADMIN/BOTH.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(Authentication authentication,
                                                          @PathVariable Long id) {
        reviewService.deleteReview(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa đánh giá thành công."));
    }
}
