package com.example.thexuong.repository;

import com.example.thexuong.entity.Order;
import com.example.thexuong.entity.OrderDetail;
import com.example.thexuong.entity.OrderStatus;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.Review;
import com.example.thexuong.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReviewRepositoryTest {

    @Autowired ReviewRepository reviewRepository;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderDetailRepository orderDetailRepository;

    private User user(String email) {
        return userRepository.save(User.builder()
                .email(email).username(email).fullName("User " + email)
                .role("CUSTOMER").active(true).build());
    }

    private Product product(String name) {
        return productRepository.save(Product.builder()
                .name(name).price(BigDecimal.valueOf(100)).viewCount(0).active(true).build());
    }

    private Review review(User u, Product p, int rating) {
        return reviewRepository.save(Review.builder().user(u).product(p).rating(rating).build());
    }

    private Order order(User u, OrderStatus status) {
        return orderRepository.save(Order.builder().user(u).status(status).build());
    }

    private void detail(Order o, Product p) {
        orderDetailRepository.save(OrderDetail.builder()
                .order(o).productId(p.getId()).productName(p.getName()).quantity(1).build());
    }

    private void pause() {
        try { Thread.sleep(15); } catch (InterruptedException e) { throw new RuntimeException(e); }
    }

    @Test
    void findByProductIdOrderByCreatedAtDesc_returnsNewestFirst() {
        User u = user("a@test.com");
        Product p = product("P1");
        Review r1 = review(u, p, 5);
        pause();
        Review r2 = review(u, p, 4);

        List<Review> result = reviewRepository.findByProductIdOrderByCreatedAtDesc(p.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(r2.getId());
        assertThat(result.get(1).getId()).isEqualTo(r1.getId());
    }

    @Test
    void existsByUserIdAndProductId_detectsExistingReview() {
        User u = user("b@test.com");
        Product p = product("P2");

        assertThat(reviewRepository.existsByUserIdAndProductId(u.getId(), p.getId())).isFalse();
        review(u, p, 3);
        assertThat(reviewRepository.existsByUserIdAndProductId(u.getId(), p.getId())).isTrue();
    }

    @Test
    void findByIdAndUserId_matchesOnlyOwnedReview() {
        User u1 = user("c@test.com");
        User u2 = user("d@test.com");
        Product p = product("P3");
        Review own = review(u1, p, 5);
        review(u2, p, 1);

        assertThat(reviewRepository.findByIdAndUserId(own.getId(), u1.getId())).isPresent();
        assertThat(reviewRepository.findByIdAndUserId(own.getId(), u2.getId())).isEmpty();
    }

    @Test
    void countByProductId_countsOnlyThatProduct() {
        User u = user("e@test.com");
        Product p1 = product("P4");
        Product p2 = product("P5");
        review(u, p1, 5);
        review(u, p1, 4);
        review(u, p2, 3);

        assertThat(reviewRepository.countByProductId(p1.getId())).isEqualTo(2);
        assertThat(reviewRepository.countByProductId(p2.getId())).isEqualTo(1);
    }

    @Test
    void countByRating_groupsByRating() {
        User u = user("f@test.com");
        Product p = product("P6");
        review(u, p, 5);
        review(u, p, 5);
        review(u, p, 2);

        List<Object[]> rows = reviewRepository.countByRating(p.getId());

        assertThat(rows).hasSize(2);
        for (Object[] row : rows) {
            if (((Number) row[0]).intValue() == 5) assertThat(((Number) row[1]).longValue()).isEqualTo(2);
            if (((Number) row[0]).intValue() == 2) assertThat(((Number) row[1]).longValue()).isEqualTo(1);
        }
    }

    @Test
    void existsPurchaseWithStatus_trueOnlyForCompletedOrders() {
        User u = user("g@test.com");
        Product p = product("P7");
        detail(order(u, OrderStatus.COMPLETED), p);
        detail(order(u, OrderStatus.PENDING), p);

        assertThat(orderDetailRepository.existsPurchaseWithStatus(p.getId(), u.getId(), OrderStatus.COMPLETED)).isTrue();
        assertThat(orderDetailRepository.existsPurchaseWithStatus(p.getId(), u.getId(), OrderStatus.PENDING)).isTrue();
        assertThat(orderDetailRepository.existsPurchaseWithStatus(p.getId(), u.getId(), OrderStatus.CANCELLED)).isFalse();
        assertThat(orderDetailRepository.existsPurchaseWithStatus(p.getId(), u.getId() + 999L, OrderStatus.COMPLETED)).isFalse();
    }
}
