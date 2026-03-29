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
    private final SizeRepository  sizeRepository;
    @Autowired
    private final ProductVariantRepository productVariantRepository;

    @GetMapping
    public String showProductList(Model model) {
        // Lấy tất cả sản phẩm, sắp xếp mới nhất lên đầu
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return "admin/products"; // Trả về file templates/admin/products.html
    }

    // 2. MỞ FORM THÊM MỚI
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("sizes", sizeRepository.findAll());
        return "admin/products-edit"; // Trả về file form (dùng chung cho thêm và sửa)
    }

    // 3. MỞ FORM CHỈNH SỬA
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

    // 4. LƯU SẢN PHẨM (Xử lý cho cả Thêm và Sửa)
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product,
                              @RequestParam(value = "sizeId", required = false) Long sizeId,
                              @RequestParam(value = "quantity", required = false, defaultValue = "0") Integer quantity,
                              RedirectAttributes redirectAttributes) {
        try {
            // A. Lưu thông tin chung của Product trước
            Product savedProduct = productRepository.save(product);

            // B. Lưu thông tin Biến thể (Size + Số lượng)
            if (sizeId != null && quantity != null) {
                Size size = (Size) sizeRepository.findById(sizeId).orElse(null);

                if (size != null) {
                    // Kiểm tra xem biến thể này đã tồn tại chưa để update hay insert
                    Optional<ProductVariant> existingVariant = productVariantRepository
                            .findByProductIdAndSizeId(savedProduct.getId(), sizeId);
                    ProductVariant variant;
                    if (existingVariant.isPresent()) {
                        variant = existingVariant.get();
                        // Ghi đè số lượng mới
                        variant.setQuantity(quantity);
                    } else {
                        variant = new ProductVariant();
                        variant.setProduct(savedProduct);
                        variant.setSize(size);
                        variant.setQuantity(quantity);

                        // FIX LỖI SQL SERVER: Sinh mã SKU ngẫu nhiên dựa trên ID Sản phẩm + Size + Thời gian
                        // Để đảm bảo không bao giờ bị NULL và không bao giờ trùng lặp
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
        return "redirect:/admin/products"; // Lưu xong quay về trang danh sách
    }

    // 5. XÓA SẢN PHẨM
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Cần xóa các variant trước khi xóa product (nếu chưa set Cascade)
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
