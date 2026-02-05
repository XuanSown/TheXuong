package com.example.thexuong.controller;

import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
@RequiredArgsConstructor
@Controller
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    // Trang chủ: Chỉ load 4 sản phẩm mới nhất
    @GetMapping(value = {"/", "/index"})
    public String home(Model model) {
        List<Product> newProducts = productRepository.findTop4ByOrderByIdDesc();
        model.addAttribute("products", newProducts);
        return "index";
    }

    // Trang danh sách tất cả sản phẩm
    @GetMapping("/products")
    public String showProductList(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false, defaultValue = "newest") String sort,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "12") int size,
                                  Model model) {
        // 1. Xác định kiểu sắp xếp
        Sort sorting = Sort.by("id").descending(); // Mặc định là mới nhất

        if ("price_asc".equals(sort)) {
            sorting = Sort.by("price").ascending();
        } else if ("price_desc".equals(sort)) {
            sorting = Sort.by("price").descending();
        }
        Pageable pageable = PageRequest.of(page, size, sorting);
        // 2. Lấy danh sách sản phẩm
        Page<Product> productsPage;
        if (keyword != null && !keyword.isEmpty()) {
            // Nếu có tìm kiếm -> Tìm theo tên + Sắp xếp
            productsPage = productRepository.findByNameContaining(keyword, pageable);
        } else {
            // Nếu không tìm kiếm -> Lấy tất cả + Sắp xếp
            productsPage = productRepository.findAll(pageable);
        }

        // 3. Truyền dữ liệu ra View
        model.addAttribute("productsPage", productsPage);
        model.addAttribute("sort", sort); // Để giữ trạng thái dropdown
        model.addAttribute("keyword", keyword); // Để giữ từ khóa tìm kiếm

        return "products";
    }

    // Chi tiết sản phẩm
    @GetMapping("/product-detail/{id}")
    public String showProductDetail(@PathVariable Long id,
                                    @RequestParam(required = false) String size,
                                    Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));

        List<String> allSizes = product.getVariants().stream()
                .map(v -> v.getSize().getName())
                .distinct()
                .collect(Collectors.toList());

        int quantity = 0;
        Long selectedVariantId = null;

        if (size != null) {
            ProductVariant variant = product.getVariants().stream()
                    .filter(v -> v.getSize().getName().equals(size))
                    .findFirst()
                    .orElse(null);

            if (variant != null) {
                quantity = variant.getQuantity();
                selectedVariantId = variant.getId();
            }
        }
        model.addAttribute("product", product);
        model.addAttribute("sizes", allSizes);
        model.addAttribute("selectedSize", size);
        model.addAttribute("quantity", quantity);
        model.addAttribute("selectedVariantId", selectedVariantId);
        return "product-detail";
    }
}