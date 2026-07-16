package com.example.thexuong.repository;

import com.example.thexuong.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {
    List<Faq> findByTopic(String topic);
    List<Faq> findAllByOrderByTopicAscIdAsc();
}
