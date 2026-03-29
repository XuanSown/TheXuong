package com.example.thexuong.controller;

import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.entity.Size;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.ProductVariantRepository;
import com.example.thexuong.repository.SizeRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
    @Autowired
    private final ProductRepository productRepository;
    @Autowired
    private final SizeRepository sizeRepository;
    @Autowired
    private final ProductVariantRepository productVariantRepository;

    @GetMapping
    public String showProductList(Model model) {
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return "admin/products";
    }

    // Lấy danh sách Size + Số lượng mặc định là 0
    private List<SizeQuantityDTO> getSizesWithQuantities(Long productId) {
        List<Size> allSizes = sizeRepository.findAll();
        List<SizeQuantityDTO> sizeQuantities = new ArrayList<>();

        if (productId == null) {
            // Khi thêm mới, mặc định số lượng các size là rỗng (null hoặc 0)
            for (Size size : allSizes) {
                sizeQuantities.add(new SizeQuantityDTO(size.getId(), size.getName(), null));
            }
        } else {
            // Khi sửa, lấy số lượng hiện có từ DB
            List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
            // Chuyển List Variant thành Map<SizeId, Quantity> để lookup cho nhanh
            Map<Long, Integer> quantityMap = variants.stream()
                    .collect(Collectors.toMap(v -> v.getSize().getId(), ProductVariant::getQuantity));

            for (Size size : allSizes) {
                Integer qty = quantityMap.getOrDefault(size.getId(), null);
                sizeQuantities.add(new SizeQuantityDTO(size.getId(), size.getName(), qty));
            }
        }
        return sizeQuantities;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        // Lấy tất cả size, quantity mặc định rỗng
        model.addAttribute("sizeQuantities", getSizesWithQuantities(null));
        return "admin/products-edit";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        // Lấy tất cả size, kèm theo quantity hiện có (nếu có)
        model.addAttribute("sizeQuantities", getSizesWithQuantities(id));
        return "admin/products-edit";
    }

    // LƯU SẢN PHẨM: Nhận mảng sizeIds và mảng quantities tương ứng
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product,
                              @RequestParam(value = "sizeIds", required = false) Long[] sizeIds,
                              @RequestParam(value = "quantities", required = false) Integer[] quantities,
                              RedirectAttributes redirectAttributes) {
        try {
            Product savedProduct = productRepository.save(product);

            if (sizeIds != null && quantities != null && sizeIds.length == quantities.length) {
                for (int i = 0; i < sizeIds.length; i++) {
                    Long sizeId = sizeIds[i];
                    Integer quantity = quantities[i];

                    // Bỏ qua các size không nhập số lượng hoặc nhập số lượng < 0
                    if (quantity == null || quantity < 0) continue;

                    Size size = sizeRepository.findById(sizeId).orElse(null);
                    if (size != null) {
                        Optional<ProductVariant> existingVariant = productVariantRepository
                                .findByProductIdAndSizeId(savedProduct.getId(), sizeId);

                        ProductVariant variant;
                        if (existingVariant.isPresent()) {
                            variant = existingVariant.get();
                            variant.setQuantity(quantity); // Ghi đè số lượng
                        } else {
                            variant = new ProductVariant();
                            variant.setProduct(savedProduct);
                            variant.setSize(size);
                            variant.setQuantity(quantity);
                            String autoSku = "SKU-" + savedProduct.getId() + "-" + size.getId() + "-" + System.currentTimeMillis();
                            variant.setSku(autoSku);
                        }
                        productVariantRepository.save(variant);
                    }
                }
            }

            redirectAttributes.addFlashAttribute("success", "Lưu sản phẩm và số lượng các size thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            List<ProductVariant> variants = productVariantRepository.findByProductId(id);
            productVariantRepository.deleteAll(variants);
            productRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa sản phẩm này (đã có đơn hàng).");
        }
        return "redirect:/admin/products";
    }

    // --- DTO Class để hỗ trợ truyền dữ liệu ra màn hình ---
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SizeQuantityDTO {
        private Long sizeId;
        private String sizeName;
        private Integer quantity;
    }
}