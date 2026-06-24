package com.example.thexuong.service;

import com.example.thexuong.dto.*;
import com.example.thexuong.entity.Voucher;
import com.example.thexuong.entity.VoucherAuditLog;
import com.example.thexuong.repository.UserVoucherRepository;
import com.example.thexuong.repository.VoucherAuditLogRepository;
import com.example.thexuong.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service quản lý Voucher catalog (admin CRUD + bulk + stats).
 * Theo ADMIN_VOUCHER_REQUIREMENTS.md → Backend Service layer.
 *
 * Workflow:
 * - CREATE: validate → generate code (nếu null) → save → audit log
 * - UPDATE: validate → partial update → save → audit log
 * - DELETE: soft delete (status → EXPIRED) nếu có user đã claim, ngược lại hard delete
 * - BULK: LOCK / UNLOCK / DELETE (soft) / SET_VIP
 * - STATS: aggregate theo status, vipOnly, totalClaimed
 *
 * NOTE về snapshot pattern (requirement: "Edit KHÔNG ảnh hưởng UserVouchers đã issue"):
 * - UserVoucher entity hiện KHÔNG có snapshot fields (discountAmountSnapshot, etc.)
 * - Edit catalog CÓ THỂ ảnh hưởng UserVoucher đã issue (vì UserVoucher chỉ lưu voucherId reference).
 * - TODO: thêm snapshot fields vào UserVoucher + migration script khi implement redeem flow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final VoucherAuditLogRepository auditLogRepository;
    private final VoucherValidator validator;

    // ==================== READ ====================

    /**
     * Lấy danh sách voucher (paginated + filter).
     * @param page 1-based page number (AdminVoucher.vue gửi page=1)
     */
    public VoucherListResponse getVouchers(int page, int size, String search,
                                            String status, Boolean vipOnly,
                                            Integer minPoints, Integer maxPoints,
                                            String sortBy, String sortDir) {
        Voucher.Status statusEnum = parseStatus(status);
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Page<Voucher> voucherPage = voucherRepository.search(search, statusEnum, vipOnly, minPoints, maxPoints, pageable);

        List<VoucherResponse> vouchers = voucherPage.getContent().stream()
                .map(v -> VoucherResponse.from(v, (int) userVoucherRepository.countByVoucherId(v.getId())))
                .toList();

        return VoucherListResponse.builder()
                .vouchers(vouchers)
                .total(voucherPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(voucherPage.getTotalPages())
                .build();
    }

    /**
     * Lấy chi tiết 1 voucher. Throw IllegalArgumentException nếu không tồn tại.
     */
    public VoucherResponse getVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher với ID: " + id));
        int claimedCount = (int) userVoucherRepository.countByVoucherId(id);
        return VoucherResponse.from(voucher, claimedCount);
    }

    /**
     * Stats tổng quan catalog.
     */
    public VoucherStats getStats() {
        long total = voucherRepository.count();
        long active = voucherRepository.countByStatus(Voucher.Status.ACTIVE);
        long locked = voucherRepository.countByStatus(Voucher.Status.LOCKED);
        long expired = voucherRepository.countByStatus(Voucher.Status.EXPIRED);
        long vip = voucherRepository.countByVipOnly(true);

        // totalClaimed = SUM(claimedCount) — query đơn giản vì đã có countByVoucherId
        // (optimize sau nếu cần: dùng SELECT SUM)
        List<Voucher> all = voucherRepository.findAll();
        int totalClaimed = all.stream()
                .mapToInt(v -> (int) userVoucherRepository.countByVoucherId(v.getId()))
                .sum();

        Map<String, Integer> byStatus = new HashMap<>();
        byStatus.put("ACTIVE", (int) active);
        byStatus.put("LOCKED", (int) locked);
        byStatus.put("EXPIRED", (int) expired);

        return VoucherStats.builder()
                .totalVouchers((int) total)
                .activeVouchers((int) active)
                .lockedVouchers((int) locked)
                .expiredVouchers((int) expired)
                .vipVouchers((int) vip)
                .totalClaimed(totalClaimed)
                .byStatus(byStatus)
                .build();
    }

    // ==================== CREATE ====================

    @Transactional
    public VoucherResponse createVoucher(VoucherCreateRequest request, String adminUsername) {
        validator.validateCreate(request);

        String code = (request.getCode() == null || request.getCode().isBlank())
                ? validator.generateUniqueCode()
                : request.getCode();

        Voucher voucher = Voucher.builder()
                .code(code)
                .discountAmount(request.getDiscountAmount())
                .requiredPoints(request.getRequiredPoints())
                .minOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : BigDecimal.ZERO)
                .applicableCategoryIds(toJsonArray(request.getApplicableCategoryIds()))
                .applicableProductIds(toJsonArray(request.getApplicableProductIds()))
                .vipOnly(Boolean.TRUE.equals(request.getVipOnly()))
                .status(parseStatusOrDefault(request.getStatus(), Voucher.Status.ACTIVE))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        voucher = voucherRepository.save(voucher);

        // Audit log
        saveAuditLog(voucher.getId(), adminUsername, "CREATE",
                null, snapshotVoucher(voucher), List.of("code", "discount_amount", "required_points", "status"),
                request.getAdminNote());

        log.info("Voucher created: id={}, code={}, admin={}", voucher.getId(), code, adminUsername);
        return VoucherResponse.from(voucher, 0);
    }

    // ==================== UPDATE ====================

    @Transactional
    public VoucherResponse updateVoucher(Long id, VoucherUpdateRequest request, String adminUsername) {
        validator.validateUpdate(id, request);

        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher với ID: " + id));

        Map<String, Object> oldSnapshot = snapshotVoucher(voucher);
        List<String> changedFields = new ArrayList<>();

        // Partial update — chỉ áp dụng field != null
        if (request.getDiscountAmount() != null && !request.getDiscountAmount().equals(voucher.getDiscountAmount())) {
            voucher.setDiscountAmount(request.getDiscountAmount());
            changedFields.add("discount_amount");
        }
        if (request.getRequiredPoints() != null && !request.getRequiredPoints().equals(voucher.getRequiredPoints())) {
            voucher.setRequiredPoints(request.getRequiredPoints());
            changedFields.add("required_points");
        }
        if (request.getMinOrderAmount() != null && !request.getMinOrderAmount().equals(voucher.getMinOrderAmount())) {
            voucher.setMinOrderAmount(request.getMinOrderAmount());
            changedFields.add("min_order_amount");
        }
        if (request.getApplicableCategoryIds() != null) {
            voucher.setApplicableCategoryIds(toJsonArray(request.getApplicableCategoryIds()));
            changedFields.add("applicable_category_ids");
        }
        if (request.getApplicableProductIds() != null) {
            voucher.setApplicableProductIds(toJsonArray(request.getApplicableProductIds()));
            changedFields.add("applicable_product_ids");
        }
        if (request.getVipOnly() != null && !request.getVipOnly().equals(voucher.getVipOnly())) {
            voucher.setVipOnly(request.getVipOnly());
            changedFields.add("vip_only");
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            Voucher.Status newStatus = parseStatusOrDefault(request.getStatus(), voucher.getStatus());
            if (newStatus != voucher.getStatus()) {
                voucher.setStatus(newStatus);
                changedFields.add("status");
            }
        }

        voucher.setUpdatedAt(LocalDateTime.now());
        voucher = voucherRepository.save(voucher);

        // Audit log (chỉ log nếu có thay đổi)
        if (!changedFields.isEmpty()) {
            Map<String, Object> newSnapshot = snapshotVoucher(voucher);
            saveAuditLog(voucher.getId(), adminUsername, "UPDATE",
                    oldSnapshot, newSnapshot, changedFields, request.getAdminNote());
        }

        log.info("Voucher updated: id={}, fields={}, admin={}", id, changedFields, adminUsername);
        int claimedCount = (int) userVoucherRepository.countByVoucherId(id);
        return VoucherResponse.from(voucher, claimedCount);
    }

    // ==================== DELETE ====================

    /**
     * Soft delete: chuyển status → EXPIRED nếu có user đã claim, ngược lại hard delete.
     * Theo ADMIN_VOUCHER_REQUIREMENTS.md → Edge Cases: "Delete voucher có claimedCount > 0 → Soft delete (EXPIRED)".
     */
    @Transactional
    public void deleteVoucher(Long id, String adminUsername) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher với ID: " + id));

        boolean hasClaims = userVoucherRepository.existsByVoucherId(id);

        if (hasClaims) {
            // Soft delete: chuyển EXPIRED
            Map<String, Object> oldSnapshot = snapshotVoucher(voucher);
            voucher.setStatus(Voucher.Status.EXPIRED);
            voucher.setUpdatedAt(LocalDateTime.now());
            voucherRepository.save(voucher);

            saveAuditLog(id, adminUsername, "DELETE",
                    oldSnapshot, Map.of("status", "EXPIRED"), List.of("status"),
                    "Soft delete: đã có user claim");
            log.info("Voucher soft-deleted: id={}, admin={}", id, adminUsername);
        } else {
            // Hard delete
            Map<String, Object> oldSnapshot = snapshotVoucher(voucher);
            voucherRepository.delete(voucher);

            saveAuditLog(id, adminUsername, "DELETE",
                    oldSnapshot, null, List.of("*"), "Hard delete: chưa có user claim");
            log.info("Voucher hard-deleted: id={}, admin={}", id, adminUsername);
        }
    }

    // ==================== BULK ====================

    /**
     * Bulk action: LOCK / UNLOCK / DELETE / SET_VIP.
     * Trả về danh sách failures để admin xử lý thủ công.
     */
    @Transactional
    public BulkVoucherResponse bulkAction(BulkVoucherRequest request, String adminUsername) {
        String action = request.getAction();
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action không được để trống");
        }

        List<BulkVoucherResponse.BulkResult> failures = new ArrayList<>();
        int success = 0;

        for (Long id : request.getIds()) {
            try {
                switch (action.toUpperCase()) {
                    case "LOCK" -> setStatus(id, Voucher.Status.LOCKED, adminUsername,
                            "BULK_LOCK", request.getAdminNote());
                    case "UNLOCK" -> setStatus(id, Voucher.Status.ACTIVE, adminUsername,
                            "BULK_UNLOCK", request.getAdminNote());
                    case "DELETE" -> deleteVoucher(id, adminUsername);
                    case "SET_VIP" -> setVip(id, Boolean.TRUE.equals(request.getValue()),
                            adminUsername, request.getAdminNote());
                    default -> throw new IllegalArgumentException(
                            "Action không hợp lệ: " + action + " (LOCK/UNLOCK/DELETE/SET_VIP)");
                }
                success++;
            } catch (Exception e) {
                failures.add(BulkVoucherResponse.BulkResult.builder()
                        .id(id)
                        .error(e.getMessage())
                        .build());
            }
        }

        return BulkVoucherResponse.builder()
                .totalRequested(request.getIds().size())
                .successCount(success)
                .failureCount(failures.size())
                .failures(failures)
                .build();
    }

    private void setStatus(Long id, Voucher.Status newStatus, String adminUsername, String action, String note) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher với ID: " + id));
        Map<String, Object> oldSnapshot = snapshotVoucher(voucher);
        voucher.setStatus(newStatus);
        voucher.setUpdatedAt(LocalDateTime.now());
        voucherRepository.save(voucher);
        saveAuditLog(id, adminUsername, action, oldSnapshot, Map.of("status", newStatus.name()),
                List.of("status"), note);
    }

    private void setVip(Long id, boolean vipOnly, String adminUsername, String note) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher với ID: " + id));
        Map<String, Object> oldSnapshot = snapshotVoucher(voucher);
        voucher.setVipOnly(vipOnly);
        voucher.setUpdatedAt(LocalDateTime.now());
        voucherRepository.save(voucher);
        saveAuditLog(id, adminUsername, "BULK_SET_VIP", oldSnapshot, Map.of("vip_only", vipOnly),
                List.of("vip_only"), note);
    }

    // ==================== HELPERS ====================

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        // Default sort
        String field = sortBy != null ? sortBy : "code";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, field);
        // page 1-based (AdminVoucher.vue gửi page=1) → Spring 0-based
        return PageRequest.of(Math.max(0, page - 1), size, sort);
    }

    private Voucher.Status parseStatus(String status) {
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            return null;
        }
        try {
            return Voucher.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status không hợp lệ: " + status);
        }
    }

    private Voucher.Status parseStatusOrDefault(String status, Voucher.Status fallback) {
        if (status == null || status.isBlank()) return fallback;
        try {
            return Voucher.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status không hợp lệ: " + status);
        }
    }

    /**
     * Convert List<Integer> → JSON array string "[1,2,3]". Null → null.
     */
    private String toJsonArray(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return null;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(ids.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Snapshot entity Voucher → Map để serialize vào audit log JSON.
     */
    private Map<String, Object> snapshotVoucher(Voucher v) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", v.getCode());
        map.put("discount_amount", v.getDiscountAmount());
        map.put("required_points", v.getRequiredPoints());
        map.put("min_order_amount", v.getMinOrderAmount());
        map.put("applicable_category_ids", v.getApplicableCategoryIds());
        map.put("applicable_product_ids", v.getApplicableProductIds());
        map.put("vip_only", v.getVipOnly());
        map.put("status", v.getStatus() != null ? v.getStatus().name() : null);
        return map;
    }

    /**
     * Lưu audit log. Best-effort: nếu fail thì log warning nhưng không throw
     * (audit log không nên block business operation).
     */
    private void saveAuditLog(Long voucherId, String adminId, String action,
                              Map<String, Object> oldValues, Map<String, Object> newValues,
                              List<String> changedFields, String note) {
        try {
            // changedFields: lưu dạng CSV string đơn giản (vd: "discount_amount,status")
            // Frontend có thể split dễ dàng khi cần hiển thị.
            String changedFieldsStr = (changedFields == null || changedFields.isEmpty())
                    ? null
                    : String.join(",", changedFields);

            VoucherAuditLog auditLog = VoucherAuditLog.builder()
                    .voucherId(voucherId)
                    .adminId(adminId != null ? adminId : "unknown")
                    .action(action)
                    .oldValues(toJsonString(oldValues))
                    .newValues(toJsonString(newValues))
                    .changedFields(changedFieldsStr)
                    .note(note)
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("Failed to save voucher audit log: voucherId={}, action={}, error={}",
                    voucherId, action, e.getMessage());
        }
    }

    /**
     * Convert Map → JSON string (dùng simple manual serializer tránh thêm Jackson dependency).
     */
    private String toJsonString(Map<String, Object> map) {
        if (map == null) return null;
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            Object val = entry.getValue();
            if (val == null) {
                sb.append("null");
            } else if (val instanceof Number || val instanceof Boolean) {
                sb.append(val);
            } else {
                sb.append("\"").append(val.toString().replace("\"", "\\\"")).append("\"");
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
