package com.example.thexuong.controller;

import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    //1. api ds sp trang chu
    // GET: http://localhost:8080/products
    @GetMapping("/products")
    public String showProductList(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "products";
    }

    //2. chi tiet' sp
    // GET: http://localhost:8080/api/products/1
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
        // nếu đã chọn size, lấy sl tồn kho
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
        //trạng thái hiện tại
        model.addAttribute("selectedSize", size);
        model.addAttribute("quantity", quantity);
        model.addAttribute("selectedVariantId", selectedVariantId);
        return "product-detail";
    }
}