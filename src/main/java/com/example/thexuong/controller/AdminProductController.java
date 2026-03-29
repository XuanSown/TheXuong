package com.example.thexuong.controller;

import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.entity.Size;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.ProductVariantRepository;
import com.example.thexuong.repository.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

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

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("sizes", sizeRepository.findAll());
        return "admin/products-edit";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("sizes", sizeRepository.findAll());

        List<ProductVariant> variants = productVariantRepository.findByProductId(id);
        if (!variants.isEmpty()) {
            model.addAttribute("currentQuantity", variants.get(0).getQuantity());
            model.addAttribute("currentSizeId", variants.get(0).getSize().getId());
        }
        return "admin/products-edit";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product,
                              @RequestParam(value = "sizeId", required = false) Long sizeId,
                              @RequestParam(value = "quantity", required = false, defaultValue = "0") Integer quantity,
                              RedirectAttributes redirectAttributes) {
        try {
            Product savedProduct = productRepository.save(product);

            if (sizeId != null && quantity != null) {
                Size size = sizeRepository.findById(sizeId).orElse(null);

                if (size != null) {
                    Optional<ProductVariant> existingVariant = productVariantRepository
                            .findByProductIdAndSizeId(savedProduct.getId(), sizeId);

                    ProductVariant variant;
                    if (existingVariant.isPresent()) {
                        variant = existingVariant.get();
                        variant.setQuantity(quantity);
                    } else {
                        variant = new ProductVariant();
                        variant.setProduct(savedProduct);
                        variant.setSize(size);
                        variant.setQuantity(quantity);

                        // FIX SQL SERVER ERROR: Auto-generate SKU để tránh lỗi dính NULL Unique Key
                        String autoSku = "SKU-" + savedProduct.getId() + "-" + size.getId() + "-" + System.currentTimeMillis();
                        variant.setSku(autoSku);
                    }
                    productVariantRepository.save(variant);
                }
            }

            redirectAttributes.addFlashAttribute("success", "Lưu sản phẩm và số lượng thành công!");
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
}