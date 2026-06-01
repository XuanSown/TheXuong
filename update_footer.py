import os
import glob
import re

# 1. Update all templates
templates_dir = "C:/Users/ndnan/TheXuong/src/main/resources/templates"
html_files = glob.glob(f"{templates_dir}/*.html")

replacements = {
    'Hướng dẫn chọn size': '@{/pages/size-guide}',
    'Chính sách đổi trả': '@{/pages/return-policy}',
    'Phương thức thanh toán': '@{/pages/payment-methods}',
    'Kiểm tra đơn hàng': '@{/pages/order-tracking}',
    'Chính sách bảo mật': '@{/pages/privacy-policy}',
    'Điều khoản dịch vụ': '@{/pages/terms-of-service}',
    'Chính sách vận chuyển': '@{/pages/shipping-policy}',
    'Liên hệ hợp tác': '@{/pages/partnership}'
}

for file_path in html_files:
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    modified = False
    for text, href in replacements.items():
        # Match <a href="#" ...>Text</a> or <a ... href="#">Text</a>
        pattern = r'<a([^>]*)href="#"([^>]*)>' + re.escape(text) + r'</a>'
        def repl(m):
            return f'<a{m.group(1)}th:href="{href}"{m.group(2)}>{text}</a>'
        new_content, count = re.subn(pattern, repl, content)
        if count > 0:
            content = new_content
            modified = True
            
    if modified:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)

# 2. Extract Header and Footer from index.html
with open(f"{templates_dir}/index.html", 'r', encoding='utf-8') as f:
    index_content = f.read()

# Extract from <!DOCTYPE html> to </nav>
header_match = re.search(r'(?s)(<!DOCTYPE html>.*?</nav>)', index_content)
header_html = header_match.group(1)

# Extract from <footer to </html>
footer_match = re.search(r'(?s)(<footer.*</html>)', index_content)
footer_html = footer_match.group(1)

pages_dir = f"{templates_dir}/pages"
os.makedirs(pages_dir, exist_ok=True)

pages = [
    ('size-guide.html', 'Hướng dẫn chọn size', 'Thông tin và bảng kích cỡ (size chart) để bạn dễ dàng chọn được sản phẩm phù hợp nhất với cơ thể.'),
    ('return-policy.html', 'Chính sách đổi trả', 'Đổi trả miễn phí trong vòng 30 ngày cho mọi sản phẩm lỗi từ nhà sản xuất hoặc không vừa size.'),
    ('payment-methods.html', 'Phương thức thanh toán', 'Chúng tôi hỗ trợ đa dạng phương thức thanh toán: COD, Chuyển khoản ngân hàng, Thẻ tín dụng/ghi nợ, Ví điện tử.'),
    ('order-tracking.html', 'Kiểm tra đơn hàng', 'Bạn có thể kiểm tra trạng thái đơn hàng của mình tại đây hoặc liên hệ qua Telegram Bot để được hỗ trợ nhanh nhất.'),
    ('privacy-policy.html', 'Chính sách bảo mật', 'Chúng tôi cam kết bảo vệ thông tin cá nhân của bạn. Thông tin của bạn sẽ không bao giờ bị chia sẻ cho bên thứ 3.'),
    ('terms-of-service.html', 'Điều khoản dịch vụ', 'Vui lòng đọc kỹ các điều khoản và điều kiện trước khi sử dụng dịch vụ và mua sắm tại TheXuong Sport.'),
    ('shipping-policy.html', 'Chính sách vận chuyển', 'Giao hàng toàn quốc siêu tốc từ 1-3 ngày. Freeship cho đơn hàng từ 500,000 VND.'),
    ('partnership.html', 'Liên hệ hợp tác', 'TheXuong Sport luôn chào đón cơ hội hợp tác kinh doanh với các đối tác cung cấp, đại lý phân phối, và KOLs/Influencers.')
]

page_template = """{header}

<div class="container my-5 flex-grow-1 d-flex flex-column justify-content-center">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="card shadow-lg border-0" style="background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(10px); border-radius: 20px; overflow: hidden;">
                <div class="card-header border-0 text-center py-4" style="background: linear-gradient(135deg, #0088cc, #005f8c);">
                    <h2 class="fw-bold mb-0 text-uppercase text-white" style="letter-spacing: 1px;">{title}</h2>
                </div>
                <div class="card-body p-5 text-center">
                    <div class="mb-4 text-primary" style="font-size: 3rem;">
                        <i class="fa-solid fa-circle-info"></i>
                    </div>
                    <p class="lead text-dark mb-4 fw-bold">{desc}</p>
                    <p class="text-muted"><em>(Nội dung chi tiết các điều khoản đang được phòng pháp chế cập nhật. Xin cảm ơn sự quan tâm của bạn!)</em></p>
                    <a href="/" class="btn btn-outline-primary mt-4 fw-bold rounded-pill px-5 py-2" style="transition: all 0.3s ease;">
                        <i class="fa-solid fa-house me-2"></i> Trở về Trang Chủ
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

{footer}"""

for filename, title, desc in pages:
    content = page_template.format(header=header_html, title=title, desc=desc, footer=footer_html)
    with open(os.path.join(pages_dir, filename), 'w', encoding='utf-8') as f:
        f.write(content)

print("SUCCESS")
