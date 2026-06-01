import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class UpdateContent {
    public static void main(String[] args) throws Exception {
        Map<String, String> contents = new HashMap<>();

        contents.put("size-guide.html",
            "<div class=\"text-start mb-4\">\n" +
            "    <p>Việc chọn đúng kích cỡ giày và quần áo thể thao vô cùng quan trọng để đảm bảo sự thoải mái và hiệu suất khi vận động. TheXuong Sport xin gửi đến bạn hướng dẫn chi tiết:</p>\n" +
            "    <h5 class=\"fw-bold mt-4 text-primary\">1. Bảng size giày nam chuẩn</h5>\n" +
            "    <div class=\"table-responsive\">\n" +
            "        <table class=\"table table-bordered table-hover text-center\">\n" +
            "            <thead class=\"table-light\">\n" +
            "                <tr><th>Chiều dài chân (cm)</th><th>Size VN/EU</th><th>Size US</th></tr>\n" +
            "            </thead>\n" +
            "            <tbody>\n" +
            "                <tr><td>24.5</td><td>39</td><td>6.5</td></tr>\n" +
            "                <tr><td>25.0</td><td>40</td><td>7.0</td></tr>\n" +
            "                <tr><td>25.5</td><td>40.5</td><td>7.5</td></tr>\n" +
            "                <tr><td>26.0</td><td>41</td><td>8.0</td></tr>\n" +
            "                <tr><td>26.5</td><td>42</td><td>8.5</td></tr>\n" +
            "                <tr><td>27.0</td><td>42.5</td><td>9.0</td></tr>\n" +
            "                <tr><td>27.5</td><td>43</td><td>9.5</td></tr>\n" +
            "            </tbody>\n" +
            "        </table>\n" +
            "    </div>\n" +
            "    <h5 class=\"fw-bold mt-4 text-primary\">2. Hướng dẫn đo chiều dài chân</h5>\n" +
            "    <ul>\n" +
            "        <li><strong>Bước 1:</strong> Đặt một tờ giấy trắng sát vào tường phẳng.</li>\n" +
            "        <li><strong>Bước 2:</strong> Đứng thẳng, gót chân chạm tường và nằm gọn trên tờ giấy.</li>\n" +
            "        <li><strong>Bước 3:</strong> Đánh dấu điểm dài nhất của ngón chân cái. Đo khoảng cách từ gót đến điểm đánh dấu.</li>\n" +
            "    </ul>\n" +
            "    <p class=\"text-muted fst-italic\">* Lưu ý: Nếu chân bạn bè ngang hoặc mu bàn chân dày, vui lòng cộng thêm 0.5 - 1 size so với bảng tiêu chuẩn.</p>\n" +
            "</div>"
        );

        contents.put("return-policy.html",
            "<div class=\"text-start mb-4\">\n" +
            "    <p>TheXuong Sport luôn mong muốn mang đến trải nghiệm mua sắm tuyệt vời nhất. Nếu bạn không hài lòng với sản phẩm, chúng tôi áp dụng chính sách đổi trả dễ dàng như sau:</p>\n" +
            "    <h5 class=\"fw-bold mt-4 text-primary\">1. Thời gian áp dụng</h5>\n" +
            "    <p>Hỗ trợ đổi trả trong vòng <strong>30 ngày</strong> kể từ ngày quý khách nhận được hàng.</p>\n" +
            "    <h5 class=\"fw-bold mt-4 text-primary\">2. Điều kiện đổi trả</h5>\n" +
            "    <ul>\n" +
            "        <li>Sản phẩm còn nguyên tem, mác, hộp đựng (nếu có).</li>\n" +
            "        <li>Sản phẩm chưa qua sử dụng, giặt ủi, không bị bẩn hoặc hư hỏng.</li>\n" +
            "        <li>Có hóa đơn mua hàng hoặc thông tin số điện thoại đặt hàng.</li>\n" +
            "    </ul>\n" +
            "    <h5 class=\"fw-bold mt-4 text-primary\">3. Các trường hợp được đổi trả miễn phí</h5>\n" +
            "    <ul>\n" +
            "        <li>Sản phẩm bị lỗi từ nhà sản xuất (bong keo, đứt chỉ, phai màu).</li>\n" +
            "        <li>Giao sai sản phẩm, sai kích cỡ so với đơn đặt hàng ban đầu.</li>\n" +
            "    </ul>\n" +
            "    <p class=\"text-danger fw-bold mt-3\">Quy trình: Quý khách vui lòng đóng gói cẩn thận và gửi về địa chỉ: Q. 12, TP. Hồ Chí Minh. Chi phí vận chuyển chiều gửi lại TheXuong sẽ thanh toán nếu lỗi thuộc về chúng tôi.</p>\n" +
            "</div>"
        );

        contents.put("payment-methods.html",
            "<div class=\"text-start mb-4\">\n" +
            "    <p>Nhằm mang lại sự tiện lợi tối đa cho khách hàng, TheXuong Sport hỗ trợ đa dạng các phương thức thanh toán an toàn và nhanh chóng:</p>\n" +
            "    <div class=\"row mt-4\">\n" +
            "        <div class=\"col-md-6 mb-3\">\n" +
            "            <div class=\"p-3 border rounded h-100 bg-white shadow-sm\">\n" +
            "                <h5 class=\"fw-bold text-success\"><i class=\"fa-solid fa-truck-fast me-2\"></i>Thanh toán khi nhận hàng (COD)</h5>\n" +
            "                <p class=\"text-muted small\">Khách hàng thanh toán tiền mặt trực tiếp cho nhân viên giao hàng khi nhận và kiểm tra sản phẩm.</p>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "        <div class=\"col-md-6 mb-3\">\n" +
            "            <div class=\"p-3 border rounded h-100 bg-white shadow-sm\">\n" +
            "                <h5 class=\"fw-bold text-primary\"><i class=\"fa-solid fa-building-columns me-2\"></i>Chuyển khoản ngân hàng</h5>\n" +
            "                <p class=\"text-muted small\">Chuyển khoản trực tiếp vào tài khoản công ty. (Nội dung: Mã đơn hàng + Số điện thoại).</p>\n" +
            "                <ul class=\"list-unstyled mt-2 small\">\n" +
            "                    <li><strong>Ngân hàng:</strong> Vietcombank</li>\n" +
            "                    <li><strong>Số tài khoản:</strong> 1023456789</li>\n" +
            "                    <li><strong>Chủ tài khoản:</strong> THE XUONG SPORT</li>\n" +
            "                </ul>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "        <div class=\"col-md-6 mb-3\">\n" +
            "            <div class=\"p-3 border rounded h-100 bg-white shadow-sm\">\n" +
            "                <h5 class=\"fw-bold text-warning\"><i class=\"fa-solid fa-credit-card me-2\"></i>Thẻ tín dụng / Thẻ ghi nợ</h5>\n" +
            "                <p class=\"text-muted small\">Hỗ trợ thanh toán an toàn qua cổng VNPay/Momo với các loại thẻ Visa, Mastercard, JCB.</p>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "        <div class=\"col-md-6 mb-3\">\n" +
            "            <div class=\"p-3 border rounded h-100 bg-white shadow-sm\">\n" +
            "                <h5 class=\"fw-bold text-info\"><i class=\"fa-solid fa-wallet me-2\"></i>Ví điện tử</h5>\n" +
            "                <p class=\"text-muted small\">Thanh toán nhanh chóng bằng cách quét mã QR qua ứng dụng MoMo, ZaloPay, ViettelPay.</p>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</div>"
        );

        contents.put("order-tracking.html",
            "<div class=\"text-start mb-4\">\n" +
            "    <div class=\"alert alert-info border-0 shadow-sm rounded-3\">\n" +
            "        <h5 class=\"alert-heading fw-bold\"><i class=\"fa-solid fa-satellite-dish me-2\"></i>Theo dõi đơn hàng 24/7</h5>\n" +
            "        <p>Quý khách có thể kiểm tra lộ trình đơn hàng của mình một cách dễ dàng thông qua 2 cách sau đây:</p>\n" +
            "    </div>\n" +
            "    <h5 class=\"fw-bold mt-4 text-primary\">Cách 1: Sử dụng Hệ thống Web</h5>\n" +
            "    <p>Nếu quý khách đã có tài khoản tại TheXuong Sport:</p>\n" +
            "    <ol>\n" +
            "        <li>Đăng nhập vào tài khoản cá nhân.</li>\n" +
            "        <li>Truy cập mục <strong>\"Đơn hàng của tôi\"</strong> trên thanh công cụ.</li>\n" +
            "        <li>Hệ thống sẽ hiển thị trạng thái chi tiết: <em>Chờ xác nhận, Đang lấy hàng, Đang giao, Đã giao thành công</em>.</li>\n" +
            "    </ol>\n" +
            "    <h5 class=\"fw-bold mt-4 text-primary\">Cách 2: Tra cứu qua Trợ lý Chatbot AI</h5>\n" +
            "    <p>Nếu quý khách mua hàng không cần tạo tài khoản, vui lòng sử dụng TheXuong AI Bot:</p>\n" +
            "    <ul>\n" +
            "        <li>Bấm vào biểu tượng <strong>Robot</strong> ở góc phải bên dưới màn hình.</li>\n" +
            "        <li>Chọn mục <em>\"Tra cứu trạng thái đơn hàng\"</em> hoặc nhắn tin trực tiếp mã đơn hàng.</li>\n" +
            "        <li>Hệ thống tự động sẽ phản hồi ngay lập tức lộ trình gói hàng của bạn.</li>\n" +
            "    </ul>\n" +
            "</div>"
        );

        contents.put("privacy-policy.html",
            "<div class=\"text-start mb-4\">\n" +
            "    <p>TheXuong Sport cam kết tôn trọng và bảo vệ quyền riêng tư của khách hàng. Mọi thông tin cá nhân đều được lưu trữ và mã hóa an toàn.</p>\n" +
            "    <h5 class=\"fw-bold mt-4 text-primary\">1. Thu thập thông tin</h5>\n" +
            "    <p>Chúng tôi chỉ thu thập những thông tin cần thiết phục vụ cho quá trình giao dịch bao gồm: Họ tên, Số điện thoại, Địa chỉ giao hàng, Email.</p>\n" +
            "    <h5 class=\"fw-bold mt-4 text-primary\">2. Sử dụng thông tin</h5>\n" +
            "    <ul>\n" +
            "        <li>Xử lý và vận chuyển đơn hàng.</li>\n" +
            "        <li>Hỗ trợ khách hàng, giải quyết khiếu nại (nếu có).</li>\n" +
            "        <li>Gửi thông báo về các chương trình khuyến mãi, ưu đãi (chỉ khi khách hàng đồng ý).</li>\n" +
            "    </ul>\n" +
            "    <h5 class=\"fw-bold mt-4 text-primary\">3. Cam kết bảo mật</h5>\n" +
            "    <p>TheXuong Sport <strong>TUYỆT ĐỐI KHÔNG</strong> bán, chia sẻ hoặc trao đổi thông tin cá nhân của khách hàng cho bất kỳ bên thứ ba nào khác nhằm mục đích thương mại. Thông tin chỉ được chia sẻ cho đối tác vận chuyển (Viettel Post, GHTK) để thực hiện việc giao hàng.</p>\n" +
            "</div>"
        );

        contents.put("terms-of-service.html",
            "<div class=\"text-start mb-4\">\n" +
            "    <p>Chào mừng bạn đến với TheXuong Sport. Bằng việc truy cập và sử dụng dịch vụ tại website, bạn đồng ý tuân thủ các điều khoản sau:</p>\n" +
            "    <h5 class=\"fw-bold mt-4 text-primary\">1. Tài khoản của bạn</h5>\n" +
            "    <p>Người dùng tự chịu trách nhiệm bảo mật thông tin tài khoản và mật khẩu của mình. Trong trường hợp phát hiện truy cập trái phép, vui lòng liên hệ ngay với chúng tôi để được hỗ trợ khóa tài khoản tạm thời.</p>\n" +
            "    <h5 class=\"fw-bold mt-4 text-primary\">2. Quyền sở hữu trí tuệ</h5>\n" +
            "    <p>Mọi nội dung trên website bao gồm văn bản, hình ảnh, logo, video đều thuộc bản quyền của TheXuong Sport. Bất kỳ hành vi sao chép, phân phối vì mục đích thương mại mà không có sự đồng ý đều bị nghiêm cấm.</p>\n" +
            "    <h5 class=\"fw-bold mt-4 text-primary\">3. Từ chối bảo đảm</h5>\n" +
            "    <p>Mặc dù chúng tôi luôn nỗ lực đảm bảo tính chính xác của màu sắc và thông số sản phẩm, nhưng hình ảnh thực tế có thể chênh lệch 3-5% do điều kiện ánh sáng và màn hình thiết bị hiển thị.</p>\n" +
            "</div>"
        );

        contents.put("shipping-policy.html",
            "<div class=\"text-start mb-4\">\n" +
            "    <p>TheXuong Sport hợp tác cùng các đơn vị vận chuyển hàng đầu để mang sản phẩm đến tay khách hàng nhanh chóng và an toàn nhất.</p>\n" +
            "    <h5 class=\"fw-bold mt-4 text-primary\">1. Phí vận chuyển</h5>\n" +
            "    <ul>\n" +
            "        <li><strong>Nội thành TP.HCM:</strong> Phí giao hàng 20,000 VND.</li>\n" +
            "        <li><strong>Các Tỉnh/Thành khác:</strong> Phí giao hàng đồng giá 35,000 VND.</li>\n" +
            "        <li><span class=\"badge bg-success\">MIỄN PHÍ VẬN CHUYỂN</span> cho mọi đơn hàng có giá trị từ <strong>500,000 VND</strong> trở lên.</li>\n" +
            "    </ul>\n" +
            "    <h5 class=\"fw-bold mt-4 text-primary\">2. Thời gian giao hàng</h5>\n" +
            "    <div class=\"row mt-3 text-center\">\n" +
            "        <div class=\"col-4\">\n" +
            "            <div class=\"p-3 bg-light rounded border border-info h-100\">\n" +
            "                <i class=\"fa-solid fa-motorcycle fa-2x text-info mb-2\"></i>\n" +
            "                <h6 class=\"fw-bold\">Nội thành TP.HCM</h6>\n" +
            "                <p class=\"mb-0 small\">Trong vòng 24h</p>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "        <div class=\"col-4\">\n" +
            "            <div class=\"p-3 bg-light rounded border border-primary h-100\">\n" +
            "                <i class=\"fa-solid fa-truck fa-2x text-primary mb-2\"></i>\n" +
            "                <h6 class=\"fw-bold\">Miền Nam</h6>\n" +
            "                <p class=\"mb-0 small\">1 - 2 ngày làm việc</p>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "        <div class=\"col-4\">\n" +
            "            <div class=\"p-3 bg-light rounded border border-warning h-100\">\n" +
            "                <i class=\"fa-solid fa-plane fa-2x text-warning mb-2\"></i>\n" +
            "                <h6 class=\"fw-bold\">Miền Trung & Miền Bắc</h6>\n" +
            "                <p class=\"mb-0 small\">3 - 4 ngày làm việc</p>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</div>"
        );

        contents.put("partnership.html",
            "<div class=\"text-start mb-4\">\n" +
            "    <p>TheXuong Sport luôn mở rộng cánh cửa đón chào các cơ hội hợp tác kinh doanh để cùng nhau phát triển và mang lại những giá trị lớn nhất cho cộng đồng yêu thể thao.</p>\n" +
            "    <h5 class=\"fw-bold mt-4 text-primary\">1. Đại lý phân phối / Sỉ</h5>\n" +
            "    <p>Nếu bạn đang tìm kiếm nguồn hàng giày dép, dụng cụ thể thao chất lượng cao với mức chiết khấu hấp dẫn (lên đến 45%), TheXuong chính là đối tác tin cậy. Chúng tôi cung cấp chính sách hỗ trợ hình ảnh, marketing và đào tạo sản phẩm chuyên nghiệp.</p>\n" +
            "    <h5 class=\"fw-bold mt-4 text-primary\">2. Đối tác KOLs / Influencers</h5>\n" +
            "    <p>Bạn là người đam mê thể thao, có sức ảnh hưởng trên các nền tảng mạng xã hội (Tiktok, Youtube, Facebook)? Hãy gia nhập mạng lưới Affiliate của chúng tôi để nhận sản phẩm trải nghiệm miễn phí và mức hoa hồng cực tốt cho mỗi đơn hàng thành công.</p>\n" +
            "    <div class=\"bg-light p-4 rounded mt-4 border-start border-4 border-primary shadow-sm\">\n" +
            "        <h5 class=\"fw-bold mb-3\">Thông tin liên hệ trực tiếp:</h5>\n" +
            "        <ul class=\"list-unstyled mb-0\">\n" +
            "            <li class=\"mb-2\"><i class=\"fa-solid fa-envelope text-danger me-2\"></i><strong>Email:</strong> partnership@thexuong.vn</li>\n" +
            "            <li class=\"mb-2\"><i class=\"fa-solid fa-phone text-success me-2\"></i><strong>Hotline B2B:</strong> +84 909 123 456 (Zalo)</li>\n" +
            "            <li><i class=\"fa-solid fa-location-dot text-primary me-2\"></i><strong>Văn phòng:</strong> Q. 12, TP. Hồ Chí Minh</li>\n" +
            "        </ul>\n" +
            "    </div>\n" +
            "</div>"
        );

        String placeholder = "<p class=\"text-muted\"><em>(Nội dung chi tiết đang được bộ phận chuyên môn cập nhật. Xin cảm ơn sự quan tâm của bạn!)</em></p>";
        
        File dir = new File("C:/Users/ndnan/TheXuong/src/main/resources/templates/pages");
        for (File f : dir.listFiles()) {
            if (f.getName().endsWith(".html")) {
                String fileName = f.getName();
                if (contents.containsKey(fileName)) {
                    String content = new String(Files.readAllBytes(f.toPath()), "UTF-8");
                    content = content.replace(placeholder, contents.get(fileName));
                    Files.write(f.toPath(), content.getBytes("UTF-8"));
                }
            }
        }
        System.out.println("UPDATED ALL CONTENTS SUCCESSFULLY");
    }
}
