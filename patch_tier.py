import sys, re

with open('src/main/java/com/example/thexuong/service/PointTierService.java', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Inject dependencies
content = content.replace('private final UserPointsRepository userPointsRepository;', '''private final UserPointsRepository userPointsRepository;
    private final AuditLogService auditLogService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private String toJson(Object obj) {
        if (obj == null) return null;
        try { return objectMapper.writeValueAsString(obj); } catch (Exception e) { return null; }
    }''')

# 2. Add audit log to updateTierManually
pattern = r'(\.reason\(reason\)\s*\.createdAt\(LocalDateTime\.now\(\)\)\s*\.build\(\)\);)'
replacement = r'\1\n        auditLogService.logAction("USER", "UPDATE_TIER", String.valueOf(userId), "{\"tierCode\":\"" + oldTierCode + "\"}", "{\"tierCode\":\"" + newTierCode + "\"}", "Admin manually updated tier to: " + newTierCode + ". Reason: " + reason);'
content = re.sub(pattern, replacement, content)

# 3. Append CRUD methods
# remove the very last brace
content = re.sub(r'\}\s*$', '', content)
content += '''
    public java.util.List<PointTier> getAllTiers() {
        return pointTierRepository.findAll();
    }

    public String getBaseTierCode() {
        return "THUONG";
    }

    @org.springframework.transaction.annotation.Transactional
    public PointTier createTier(PointTier pointTier) {
        if (pointTierRepository.findByCode(pointTier.getCode()).isPresent()) {
            throw new RuntimeException("Ma hang da ton tai");
        }
        pointTier.setCreatedAt(java.time.LocalDateTime.now());
        PointTier saved = pointTierRepository.save(pointTier);
        auditLogService.logAction("TIER", "CREATE", String.valueOf(saved.getId()), null, toJson(saved), "Created tier " + saved.getCode());
        return saved;
    }

    @org.springframework.transaction.annotation.Transactional
    public PointTier updateTier(Long id, PointTier pointTierDetails) {
        PointTier pointTier = pointTierRepository.findById(id).orElseThrow(() -> new RuntimeException("Khong tim thay hang: " + id));
        String oldState = toJson(pointTier);
        if (!pointTier.getCode().equals(pointTierDetails.getCode()) && pointTierRepository.findByCode(pointTierDetails.getCode()).isPresent()) {
            throw new RuntimeException("Ma hang da ton tai");
        }
        pointTier.setCode(pointTierDetails.getCode());
        pointTier.setName(pointTierDetails.getName());
        pointTier.setMinTotalSpent(pointTierDetails.getMinTotalSpent());
        pointTier.setMinTotalPoints(pointTierDetails.getMinTotalPoints());
        pointTier.setBenefits(pointTierDetails.getBenefits());
        pointTier.setBonusPercentage(pointTierDetails.getBonusPercentage());
        pointTier.setAutoDiscountPercent(pointTierDetails.getAutoDiscountPercent());
        pointTier.setRewardVoucherId(pointTierDetails.getRewardVoucherId());
        PointTier saved = pointTierRepository.save(pointTier);
        auditLogService.logAction("TIER", "UPDATE", String.valueOf(saved.getId()), oldState, toJson(saved), "Updated tier " + saved.getCode());
        return saved;
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteTier(Long id) {
        PointTier pointTier = pointTierRepository.findById(id).orElseThrow(() -> new RuntimeException("Khong tim thay hang: " + id));
        String oldState = toJson(pointTier);
        pointTierRepository.delete(pointTier);
        auditLogService.logAction("TIER", "DELETE", String.valueOf(id), oldState, null, "Deleted tier " + pointTier.getCode());
    }
}
'''

with open('src/main/java/com/example/thexuong/service/PointTierService.java', 'w', encoding='utf-8') as f:
    f.write(content)
