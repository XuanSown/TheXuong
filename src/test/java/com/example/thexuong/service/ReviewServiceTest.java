package com.example.thexuong.service;

import com.example.thexuong.dto.ReviewDto;
import com.example.thexuong.dto.ReviewListResponse;
import com.example.thexuong.dto.ReviewRequest;
import com.example.thexuong.entity.Order;
import com.example.thexuong.entity.OrderDetail;
import com.example.thexuong.entity.OrderStatus;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.Review;
import com.example.thexuong.entity.User;
import com.example.thexuong.exception.ReviewAlreadyExistsException;
import com.example.thexuong.exception.ReviewNotAllowedException;
import com.example.thexuong.exception.ReviewNotFoundException;
import com.example.thexuong.repository.OrderDetailRepository;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.ReviewRepository;
import com.example.thexuong.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(ReviewService.class)
class ReviewServiceTest {

    @Autowired ReviewService reviewService;
    @Autowired ReviewRepository reviewRepository;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderDetailRepository orderDetailRepository;

    private User customer(String email) {
        return userRepository.save(User.builder()
                .email(email).username(email).fullName("Khách " + email)
                .role("CUSTOMER").active(true).build());
    }

    private User admin(String email) {
        return userRepository.save(User.builder()
                .email(email).username(email).fullName("Admin")
                .role("ADMIN").active(true).build());
    }

    private Product product(String name) {
        return productRepository.save(Product.builder()
                .name(name).price(BigDecimal.valueOf(100)).viewCount(0).active(true).build());
    }

    private Order order(User u, OrderStatus status) {
        return orderRepository.save(Order.builder().user(u).status(status).build());
    }

    private void purchased(User u, Product p, OrderStatus status) {
        Order o = order(u, status);
        orderDetailRepository.save(OrderDetail.builder()
                .order(o).productId(p.getId()).productName(p.getName()).quantity(1).build());
    }

    private Review existing(User u, Product p, int rating, String comment) {
        return reviewRepository.save(Review.builder().user(u).product(p).rating(rating).comment(comment).build());
    }

    private ReviewRequest request(Long productId, int rating, String comment) {
        ReviewRequest r = new ReviewRequest();
        r.setProductId(productId);
        r.setRating(rating);
        r.setComment(comment);
        return r;
    }

    private void pause() {
        try { Thread.sleep(15); } catch (InterruptedException e) { throw new RuntimeException(e); }
    }

    @Test
    void createReview_notPurchased_throwsNotAllowed() {
        User u = customer("u1@test.com");
        Product p = product("P1");

        assertThatThrownBy(() -> reviewService.createReview(u.getEmail(), request(p.getId(), 5, "Tốt")))
                .isInstanceOf(ReviewNotAllowedException.class);
    }

    @Test
    void createReview_purchasedCompleted_savesReview() {
        User u = customer("u2@test.com");
        Product p = product("P2");
        purchased(u, p, OrderStatus.COMPLETED);

        ReviewDto dto = reviewService.createReview(u.getEmail(), request(p.getId(), 4, "  Ổn áp  "));

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getRating()).isEqualTo(4);
        assertThat(dto.getComment()).isEqualTo("Ổn áp"); // trimmed
        assertThat(dto.getAuthorName()).isEqualTo("Khách u2@test.com");
        assertThat(dto.isMine()).isTrue();
        assertThat(dto.isVerifiedBuyer()).isTrue();
        assertThat(dto.isCanModerate()).isFalse();
    }

    @Test
    void createReview_pendingOrderOnly_throwsNotAllowed() {
        User u = customer("u3@test.com");
        Product p = product("P3");
        purchased(u, p, OrderStatus.PENDING);

        assertThatThrownBy(() -> reviewService.createReview(u.getEmail(), request(p.getId(), 5, null)))
                .isInstanceOf(ReviewNotAllowedException.class);
    }

    @Test
    void createReview_duplicate_throwsAlreadyExists() {
        User u = customer("u4@test.com");
        Product p = product("P4");
        purchased(u, p, OrderStatus.COMPLETED);
        reviewService.createReview(u.getEmail(), request(p.getId(), 5, "Lần 1"));

        assertThatThrownBy(() -> reviewService.createReview(u.getEmail(), request(p.getId(), 3, "Lần 2")))
                .isInstanceOf(ReviewAlreadyExistsException.class);
    }

    @Test
    void createReview_productNotFound_throwsNotFound() {
        User u = customer("u5@test.com");

        assertThatThrownBy(() -> reviewService.createReview(u.getEmail(), request(99999L, 5, null)))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void updateReview_owner_succeeds() {
        User u = customer("u6@test.com");
        Product p = product("P6");
        purchased(u, p, OrderStatus.COMPLETED);
        Review saved = existing(u, p, 5, "Cũ");

        ReviewDto dto = reviewService.updateReview(u.getEmail(), saved.getId(), request(p.getId(), 2, "Mới"));

        assertThat(dto.getRating()).isEqualTo(2);
        assertThat(dto.getComment()).isEqualTo("Mới");
    }

    @Test
    void updateReview_otherCustomer_throwsNotAllowed() {
        User owner = customer("u7@test.com");
        User other = customer("u8@test.com");
        Product p = product("P7");
        Review saved = existing(owner, p, 5, "Cũ");

        assertThatThrownBy(() -> reviewService.updateReview(other.getEmail(), saved.getId(), request(p.getId(), 1, "Hack")))
                .isInstanceOf(ReviewNotAllowedException.class);
    }

    @Test
    void updateReview_admin_updatesAnyReview() {
        User owner = customer("u9@test.com");
        User boss = admin("boss@test.com");
        Product p = product("P9");
        Review saved = existing(owner, p, 5, "Cũ");

        ReviewDto dto = reviewService.updateReview(boss.getEmail(), saved.getId(), request(p.getId(), 1, "Admin sửa"));

        assertThat(dto.getRating()).isEqualTo(1);
        assertThat(dto.isCanModerate()).isTrue();
        assertThat(dto.isMine()).isFalse();
    }

    @Test
    void updateReview_notFound_throwsNotFound() {
        User u = customer("u10@test.com");

        assertThatThrownBy(() -> reviewService.updateReview(u.getEmail(), 99999L, request(1L, 5, null)))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void deleteReview_customer_throwsNotAllowed() {
        User u = customer("u11@test.com");
        Product p = product("P11");
        Review saved = existing(u, p, 5, "Tôi xóa");

        assertThatThrownBy(() -> reviewService.deleteReview(u.getEmail(), saved.getId()))
                .isInstanceOf(ReviewNotAllowedException.class);
        assertThat(reviewRepository.countByProductId(p.getId())).isEqualTo(1);
    }

    @Test
    void deleteReview_admin_deletes() {
        User u = customer("u12@test.com");
        User boss = admin("boss2@test.com");
        Product p = product("P12");
        Review saved = existing(u, p, 5, "Xóa đi");

        reviewService.deleteReview(boss.getEmail(), saved.getId());

        assertThat(reviewRepository.countByProductId(p.getId())).isZero();
    }

    @Test
    void deleteReview_notFound_throwsNotFound() {
        User boss = admin("boss3@test.com");

        assertThatThrownBy(() -> reviewService.deleteReview(boss.getEmail(), 99999L))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void getProductReviews_emptyProduct_returnsZeroSummary() {
        Product p = product("P13");

        ReviewListResponse res = reviewService.getProductReviews(p.getId(), null);

        assertThat(res.getSummary().getTotalCount()).isZero();
        assertThat(res.getSummary().getAverageRating()).isZero();
        assertThat(res.getReviews()).isEmpty();
    }

    @Test
    void getProductReviews_computesSummaryAndDistribution() {
        User u1 = customer("u14@test.com");
        User u2 = customer("u15@test.com");
        User u3 = customer("u16@test.com");
        Product p = product("P14");
        existing(u1, p, 5, "R1");
        existing(u2, p, 4, "R2");
        existing(u3, p, 3, "R3");

        ReviewListResponse res = reviewService.getProductReviews(p.getId(), null);

        assertThat(res.getSummary().getTotalCount()).isEqualTo(3);
        assertThat(res.getSummary().getAverageRating()).isEqualTo(4.0);
        assertThat(res.getSummary().getDistribution())
                .containsEntry(5, 1L).containsEntry(4, 1L).containsEntry(3, 1L)
                .containsEntry(2, 0L).containsEntry(1, 0L);
        assertThat(res.getReviews()).hasSize(3);
    }

    @Test
    void getProductReviews_roundsAverageToOneDecimal() {
        User u1 = customer("u17@test.com");
        User u2 = customer("u18@test.com");
        Product p = product("P15");
        existing(u1, p, 5, "R1");
        existing(u2, p, 4, "R2");

        ReviewListResponse res = reviewService.getProductReviews(p.getId(), null);

        assertThat(res.getSummary().getAverageRating()).isEqualTo(4.5);
    }

    @Test
    void getProductReviews_returnsNewestFirst() {
        User u1 = customer("u19@test.com");
        User u2 = customer("u20@test.com");
        Product p = product("P16");
        Review first = existing(u1, p, 5, "Cũ hơn");
        pause();
        Review second = existing(u2, p, 2, "Mới hơn");

        ReviewListResponse res = reviewService.getProductReviews(p.getId(), null);

        assertThat(res.getReviews().get(0).getId()).isEqualTo(second.getId());
        assertThat(res.getReviews().get(1).getId()).isEqualTo(first.getId());
    }

    @Test
    void getProductReviews_setsViewerFlags() {
        User owner = customer("u21@test.com");
        User other = customer("u22@test.com");
        User boss = admin("boss4@test.com");
        Product p = product("P17");
        Review mine = existing(owner, p, 5, "Của tôi");
        existing(other, p, 3, "Của người khác");

        ReviewListResponse asOwner = reviewService.getProductReviews(p.getId(), owner.getEmail());
        assertThat(asOwner.getReviews().stream().filter(r -> r.getId().equals(mine.getId())).findFirst().get().isMine()).isTrue();
        assertThat(asOwner.getReviews().stream().allMatch(r -> !r.isCanModerate())).isTrue();

        ReviewListResponse asAdmin = reviewService.getProductReviews(p.getId(), boss.getEmail());
        assertThat(asAdmin.getReviews().stream().allMatch(r -> r.isCanModerate())).isTrue();

        ReviewListResponse asAnonymous = reviewService.getProductReviews(p.getId(), null);
        assertThat(asAnonymous.getReviews().stream().allMatch(r -> !r.isMine() && !r.isCanModerate())).isTrue();
    }

    @Test
    void getProductReviews_productNotFound_throwsNotFound() {
        assertThatThrownBy(() -> reviewService.getProductReviews(99999L, null))
                .isInstanceOf(ReviewNotFoundException.class);
    }
}
