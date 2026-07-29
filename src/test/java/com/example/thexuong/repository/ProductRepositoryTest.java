package com.example.thexuong.repository;

import com.example.thexuong.entity.Brand;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.Sport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProductRepository productRepository;

    private Sport sportFootball;
    private Sport sportBadminton;
    private Brand brandNike;
    private Brand brandYonex;

    private void seed() {
        sportFootball = new Sport();
        sportFootball.setName("Bóng đá");
        em.persist(sportFootball);

        sportBadminton = new Sport();
        sportBadminton.setName("Cầu lông");
        em.persist(sportBadminton);

        brandNike = new Brand();
        brandNike.setName("Nike");
        em.persist(brandNike);

        brandYonex = new Brand();
        brandYonex.setName("Yonex");
        em.persist(brandYonex);

        // Sản phẩm: sport football + Nike
        Product p1 = new Product();
        p1.setName("Giày Nike Football");
        p1.setSport(sportFootball);
        p1.setBrand(brandNike);
        em.persist(p1);

        // Sản phẩm: sport badminton + Yonex
        Product p2 = new Product();
        p2.setName("Vợt Yonex Cầu lông");
        p2.setSport(sportBadminton);
        p2.setBrand(brandYonex);
        em.persist(p2);

        // Cross-product: sport football + Yonex (chỉ thỏa sport-only)
        Product p3 = new Product();
        p3.setName("Giày Yonex Football");
        p3.setSport(sportFootball);
        p3.setBrand(brandYonex);
        em.persist(p3);

        em.flush();
        em.clear();
    }

    @Test
    void findByFilters_noFilter_returnsAll() {
        seed();
        Page<Product> page = productRepository.findByFilters(null, null, null, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(3);
    }

    @Test
    void findByFilters_sportOnly() {
        seed();
        Page<Product> page = productRepository.findByFilters(null, "Bóng đá", null, PageRequest.of(0, 10));
        List<Product> result = page.getContent();
        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(p -> assertThat(p.getSport().getName()).isEqualTo("Bóng đá"));
    }

    @Test
    void findByFilters_brandOnly() {
        seed();
        Page<Product> page = productRepository.findByFilters(null, null, "Yonex", PageRequest.of(0, 10));
        List<Product> result = page.getContent();
        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(p -> assertThat(p.getBrand().getName()).isEqualTo("Yonex"));
    }

    @Test
    void findByFilters_sportAndBrand_returnsOnlyMatchingBoth() {
        seed();
        Page<Product> page = productRepository.findByFilters(null, "Bóng đá", "Yonex", PageRequest.of(0, 10));
        List<Product> result = page.getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Giày Yonex Football");
    }

    @Test
    void findByFilters_keyword_sport_and_brand() {
        seed();
        Page<Product> page = productRepository.findByFilters("vợt", "Cầu lông", "Yonex", PageRequest.of(0, 10));
        List<Product> result = page.getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Vợt Yonex Cầu lông");
    }
}