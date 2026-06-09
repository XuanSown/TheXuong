const fs = require('fs');

const myOrderDetailPath = 'c:/Users/ndnan/TheXuong/src/main/resources/templates/my-order-detail.html';
const adminOrderDetailPath = 'c:/Users/ndnan/TheXuong/src/main/resources/templates/admin/order-detail.html';
const adminOrdersPath = 'c:/Users/ndnan/TheXuong/src/main/resources/templates/admin/orders.html';

let content = fs.readFileSync(myOrderDetailPath, 'utf8');

// Replace return link
content = content.replace('@{/orders}', '@{/admin/orders}');

// Replace update form action
content = content.replace('@{/order/update}', '@{/admin/orders/save}');

// Remove th:if="${order.status == 'PENDING'}" from update button to allow admin to update anytime
content = content.replace(/<button type="submit" class="btn btn-primary w-100 rounded-pill fw-bold mb-2"([\s\S]*?)th:if="\$\{order\.status == 'PENDING'\}"/, '<button type="submit" class="btn btn-primary w-100 rounded-pill fw-bold mb-2"$1');

// Replace customer cancel button with Admin Status Change
const adminActions = `
                    <form th:action="@{/admin/orders/status/{id}(id=\${order.id})}" method="post" class="mt-3">
                        <div class="input-group">
                            <select name="status" class="form-select bg-light border-0 fw-bold">
                                <option value="PENDING" th:selected="\${order.status == 'PENDING'}">Chờ duyệt</option>
                                <option value="APPROVED" th:selected="\${order.status == 'APPROVED'}">Đã duyệt</option>
                                <option value="SHIPPED" th:selected="\${order.status == 'SHIPPED'}">Đã giao</option>
                                <option value="CANCELLED" th:selected="\${order.status == 'CANCELLED'}">Đã hủy</option>
                            </select>
                            <button type="submit" class="btn btn-dark fw-bold px-3">Đổi trạng thái</button>
                        </div>
                    </form>
`;

content = content.replace(/<div th:if="\$\{order\.status == 'PENDING'\}"\>[\s\S]*?<\/div>\s*<div class="alert alert-secondary border-0 rounded-3 small mt-3" th:unless="\$\{order\.status == 'PENDING'\}"\>[\s\S]*?<\/div>/g, adminActions);

// Write to admin order-detail
fs.writeFileSync(adminOrderDetailPath, content, 'utf8');
console.log('Created admin/order-detail.html');

// Add view details button to admin/orders.html
let ordersContent = fs.readFileSync(adminOrdersPath, 'utf8');

const detailBtn = `
                    <!-- Xem chi tiet -->
                    <a th:href="@{/admin/orders/{id}(id=\${order.id})}" class="btn btn-secondary btn-sm" title="Xem chi tiết">
                        <i class="fa-solid fa-eye"></i>
                    </a>
                    
                    <!-- Duyệt đơn -->`;
ordersContent = ordersContent.replace('<!-- Duyệt đơn -->', detailBtn);

fs.writeFileSync(adminOrdersPath, ordersContent, 'utf8');
console.log('Updated admin/orders.html');
