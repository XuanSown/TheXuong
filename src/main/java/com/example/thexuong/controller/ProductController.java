package com.example.thexuong.controller;

import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.entity.Size;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.SizeRepository; // THÊM IMPORT
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class ProductController {

    @Autowired
    private final ProductRepository productRepository;

    // THÊM REPOSITORY SIZE ĐỂ LẤY FULL SIZE TỪ DB
    @Autowired
    private final SizeRepository sizeRepository;

    @GetMapping(value = {"/", "/index"})
    public String home(Model model) {
        List<Product> newProducts = productRepository.findTop4ByOrderByIdDesc();
        model.addAttribute("products", newProducts);
        return "index";
    }

    @GetMapping("/products")
    public String showProductList(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String sport,
                                  @RequestParam(required = false) String brand,
                                  @RequestParam(required = false, defaultValue = "newest") String sort,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "12") int pageSize,
                                  Model model) {

        Sort sorting = Sort.by("id").descending();
        if ("price_asc".equals(sort)) {
            sorting = Sort.by("price").ascending();
        } else if ("price_desc".equals(sort)) {
            sorting = Sort.by("price").descending();
        }

        Pageable pageable = PageRequest.of(page, pageSize, sorting);
        Page<Product> productsPage;
        if (keyword != null && !keyword.isEmpty()) {
            productsPage = productRepository.findByNameContaining(keyword, pageable);
        } else if (sport != null && !sport.isEmpty()) {
            productsPage = productRepository.findBySport(sport, pageable);
        } else if (brand != null && !brand.isEmpty()) {
            productsPage = productRepository.findByBrand(brand, pageable);
        } else {
            productsPage = productRepository.findAll(pageable);
        }

        model.addAttribute("productsPage", productsPage);
        model.addAttribute("sort", sort);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sport", sport);
        model.addAttribute("brand", brand);

        return "products";
    }

    @GetMapping("/product-detail/{id}")
    public String showProductDetail(@PathVariable Long id,
                                    @RequestParam(required = false) String size,
                                    Model model) {

        // KHÓA BACKEND CHẶN ADMIN
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                return "redirect:/admin/products";
            }
        }

        Product product = productRepository.findByIdWithVariants(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));

        if(product.getViewCount() == null){
            product.setViewCount(0);
        }
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);

        // LOGIC MỚI: LẤY LIST SIZE DỰA TRÊN DANH MỤC
        List<Size> allDbSizes = sizeRepository.findAll();
        List<String> displaySizes = new ArrayList<>();
        String category = product.getCategory();

        for (Size s : allDbSizes) {
            String sizeName = s.getName().trim();
            // Kiểm tra xem tên size có phải là số không (VD: 39, 40)
            boolean isNumeric = sizeName.matches("\\d+");

            if (category != null && category.toLowerCase().contains("giày")) {
                if (isNumeric) displaySizes.add(sizeName);
            } else {
                // Nếu là quần áo hoặc balo, lấy size chữ (S, M, L, XL...)
                if (!isNumeric) displaySizes.add(sizeName);
            }
        }

        List<ProductVariant> variants = product.getVariants();
        int quantity = 0;
        Long selectedVariantId = null;

        if (size != null && !size.isBlank() && variants != null && !variants.isEmpty()) {
            ProductVariant variant = variants.stream()
                    .filter(v -> v != null && v.getSize() != null && v.getSize().getName() != null)
                    .filter(v -> size.equals(v.getSize().getName()))
                    .findFirst()
                    .orElse(null);

            if (variant != null) {
                quantity = variant.getQuantity();
                selectedVariantId = variant.getId();
            }
        }

        model.addAttribute("product", product);
        model.addAttribute("sizes", displaySizes); // Gửi list size thông minh ra view
        model.addAttribute("selectedSize", size);
        model.addAttribute("quantity", quantity); // Nếu chưa có Variant, quantity = 0 (Hết hàng)
        model.addAttribute("selectedVariantId", selectedVariantId);

        return "product-detail";
    }
}