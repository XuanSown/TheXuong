package com.example.thexuong.service;

import com.example.thexuong.dto.ReviewDto;
import com.example.thexuong.dto.ReviewListResponse;
import com.example.thexuong.dto.ReviewRequest;
import com.example.thexuong.dto.ReviewSummaryDto;
import com.example.thexuong.entity.OrderStatus;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.Review;
import com.example.thexuong.entity.User;
import com.example.thexuong.exception.ReviewAlreadyExistsException;
import com.example.thexuong.exception.ReviewNotAllowedException;
import com.example.thexuong.exception.ReviewNotFoundException;
import com.example.thexuong.exception.UserNotFoundException;
import com.example.thexuong.repository.OrderDetailRepository;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.ReviewRepository;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Transactional(readOnly = true)
    public ReviewListResponse getProductReviews(Long productId, String viewerEmail) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ReviewNotFoundException("Không tìm thấy sản phẩm: " + productId));

        User viewer = viewerEmail == null ? null : userRepository.findByEmail(viewerEmail).orElse(null);
        boolean viewerIsAdmin = viewer != null && isAdmin(viewer);

        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);

        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int stars = 5; stars >= 1; stars--) distribution.put(stars, 0L);
        long total = 0;
        double weighted = 0;
        for (Object[] row : reviewRepository.countByRating(productId)) {
            int stars = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            distribution.put(stars, count);
            total += count;
            weighted += (double) stars * count;
        }
        double average = total == 0 ? 0 : Math.round((weighted / total) * 10.0) / 10.0;

        ReviewSummaryDto summary = ReviewSummaryDto.builder()
                .averageRating(average)
                .totalCount(total)
                .distribution(distribution)
                .build();

        List<ReviewDto> dtos = reviews.stream()
                .map(r -> toDto(r, viewerEmail, viewerIsAdmin))
                .toList();

        return ReviewListResponse.builder().summary(summary).reviews(dtos).build();
    }

    @Transactional
    public ReviewDto createReview(String email, ReviewRequest request) {
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Thiếu productId.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ReviewNotFoundException("Không tìm thấy sản phẩm: " + request.getProductId()));

        boolean purchased = orderDetailRepository.existsPurchaseWithStatus(
                request.getProductId(), user.getId(), OrderStatus.COMPLETED);
        if (!purchased) {
            throw new ReviewNotAllowedException("Bạn cần mua sản phẩm để đánh giá.");
        }
        if (reviewRepository.existsByUserIdAndProductId(user.getId(), request.getProductId())) {
            throw new ReviewAlreadyExistsException("Bạn đã đánh giá sản phẩm này rồi.");
        }

        Review review = reviewRepository.save(Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(normalizeComment(request.getComment()))
                .build());
        return toDto(review, email, isAdmin(user));
    }

    @Transactional
    public ReviewDto updateReview(String email, Long reviewId, ReviewRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Không tìm thấy đánh giá: " + reviewId));

        boolean owner = review.getUser().getId().equals(user.getId());
        boolean admin = isAdmin(user);
        if (!owner && !admin) {
            throw new ReviewNotAllowedException("Bạn chỉ có thể sửa đánh giá của chính mình.");
        }

        review.setRating(request.getRating());
        review.setComment(normalizeComment(request.getComment()));
        Review saved = reviewRepository.save(review);
        return toDto(saved, email, admin);
    }

    @Transactional
    public void deleteReview(String email, Long reviewId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Không tìm thấy đánh giá: " + reviewId));
        if (!isAdmin(user)) {
            throw new ReviewNotAllowedException("Bạn không có quyền xóa đánh giá.");
        }
        reviewRepository.delete(review);
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equals(user.getRole()) || "BOTH".equals(user.getRole());
    }

    private String normalizeComment(String comment) {
        if (comment == null || comment.isBlank()) return null;
        return comment.trim();
    }

    private ReviewDto toDto(Review review, String viewerEmail, boolean viewerIsAdmin) {
        User author = review.getUser();
        String authorName = (author.getFullName() != null && !author.getFullName().isBlank())
                ? author.getFullName() : author.getUsername();
        return ReviewDto.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .authorName(authorName)
                .verifiedBuyer(true)
                .mine(viewerEmail != null && viewerEmail.equalsIgnoreCase(author.getEmail()))
                .canModerate(viewerIsAdmin)
                .build();
    }
}
