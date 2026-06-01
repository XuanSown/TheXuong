import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateFooter {
    public static void main(String[] args) throws Exception {
        String templatesDir = "C:/Users/ndnan/TheXuong/src/main/resources/templates";
        File dir = new File(templatesDir);
        
        Map<String, String> replacements = new HashMap<>();
        replacements.put("Hướng dẫn chọn size", "@{/pages/size-guide}");
        replacements.put("Chính sách đổi trả", "@{/pages/return-policy}");
        replacements.put("Phương thức thanh toán", "@{/pages/payment-methods}");
        replacements.put("Kiểm tra đơn hàng", "@{/pages/order-tracking}");
        replacements.put("Chính sách bảo mật", "@{/pages/privacy-policy}");
        replacements.put("Điều khoản dịch vụ", "@{/pages/terms-of-service}");
        replacements.put("Chính sách vận chuyển", "@{/pages/shipping-policy}");
        replacements.put("Liên hệ hợp tác", "@{/pages/partnership}");
        
        for (File f : dir.listFiles()) {
            if (f.getName().endsWith(".html")) {
                String content = new String(Files.readAllBytes(f.toPath()), "UTF-8");
                boolean modified = false;
                for (Map.Entry<String, String> entry : replacements.entrySet()) {
                    String text = entry.getKey();
                    String href = entry.getValue();
                    // Match <a href="#" ...>Text</a> or <a ... href="#">Text</a>
                    String patternStr = "<a([^>]*)href=\"#\"([^>]*)>" + Pattern.quote(text) + "</a>";
                    Pattern p = Pattern.compile(patternStr);
                    Matcher m = p.matcher(content);
                    StringBuffer sb = new StringBuffer();
                    while (m.find()) {
                        m.appendReplacement(sb, "<a" + m.group(1) + "th:href=\"" + href + "\"" + m.group(2) + ">" + text + "</a>");
                        modified = true;
                    }
                    m.appendTail(sb);
                    content = sb.toString();
                }
                if (modified) {
                    Files.write(f.toPath(), content.getBytes("UTF-8"));
                }
            }
        }
        
        String indexContent = new String(Files.readAllBytes(Paths.get(templatesDir, "index.html")), "UTF-8");
        
        Pattern headerPattern = Pattern.compile("(?s)(<!DOCTYPE html>.*?</nav>)");
        Matcher headerMatcher = headerPattern.matcher(indexContent);
        headerMatcher.find();
        String headerHtml = headerMatcher.group(1);
        
        Pattern footerPattern = Pattern.compile("(?s)(<footer.*</html>)");
        Matcher footerMatcher = footerPattern.matcher(indexContent);
        footerMatcher.find();
        String footerHtml = footerMatcher.group(1);
        
        File pagesDir = new File(templatesDir, "pages");
        pagesDir.mkdirs();
        
        String[][] pages = {
            {"size-guide.html", "Hướng dẫn chọn size", "Thông tin và bảng kích cỡ (size chart) để bạn dễ dàng chọn được sản phẩm phù hợp nhất với cơ thể."},
            {"return-policy.html", "Chính sách đổi trả", "Đổi trả miễn phí trong vòng 30 ngày cho mọi sản phẩm lỗi từ nhà sản xuất hoặc không vừa size."},
            {"payment-methods.html", "Phương thức thanh toán", "Chúng tôi hỗ trợ đa dạng phương thức thanh toán: COD, Chuyển khoản ngân hàng, Thẻ tín dụng/ghi nợ, Ví điện tử."},
            {"order-tracking.html", "Kiểm tra đơn hàng", "Bạn có thể kiểm tra trạng thái đơn hàng của mình tại đây hoặc liên hệ qua Telegram Bot để được hỗ trợ nhanh nhất."},
            {"privacy-policy.html", "Chính sách bảo mật", "Chúng tôi cam kết bảo vệ thông tin cá nhân của bạn. Thông tin của bạn sẽ không bao giờ bị chia sẻ cho bên thứ 3."},
            {"terms-of-service.html", "Điều khoản dịch vụ", "Vui lòng đọc kỹ các điều khoản và điều kiện trước khi sử dụng dịch vụ và mua sắm tại TheXuong Sport."},
            {"shipping-policy.html", "Chính sách vận chuyển", "Giao hàng toàn quốc siêu tốc từ 1-3 ngày. Freeship cho đơn hàng từ 500,000 VND."},
            {"partnership.html", "Liên hệ hợp tác", "TheXuong Sport luôn chào đón cơ hội hợp tác kinh doanh với các đối tác cung cấp, đại lý phân phối, và KOLs/Influencers."}
        };
        
        String template = headerHtml + "\n\n" +
            "<div class=\"container my-5 flex-grow-1 d-flex flex-column justify-content-center\">\n" +
            "    <div class=\"row justify-content-center\">\n" +
            "        <div class=\"col-lg-8\">\n" +
            "            <div class=\"card shadow-lg border-0\" style=\"background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(10px); border-radius: 20px; overflow: hidden;\">\n" +
            "                <div class=\"card-header border-0 text-center py-4\" style=\"background: linear-gradient(135deg, #0088cc, #005f8c);\">\n" +
            "                    <h2 class=\"fw-bold mb-0 text-uppercase text-white\" style=\"letter-spacing: 1px;\">%s</h2>\n" +
            "                </div>\n" +
            "                <div class=\"card-body p-5 text-center\">\n" +
            "                    <div class=\"mb-4 text-primary\" style=\"font-size: 3rem;\">\n" +
            "                        <i class=\"fa-solid fa-circle-info\"></i>\n" +
            "                    </div>\n" +
            "                    <p class=\"lead text-dark mb-4 fw-bold\">%s</p>\n" +
            "                    <p class=\"text-muted\"><em>(Nội dung chi tiết đang được bộ phận chuyên môn cập nhật. Xin cảm ơn sự quan tâm của bạn!)</em></p>\n" +
            "                    <a href=\"/\" class=\"btn btn-outline-primary mt-4 fw-bold rounded-pill px-5 py-2\" style=\"transition: all 0.3s ease;\">\n" +
            "                        <i class=\"fa-solid fa-house me-2\"></i> Trở về Trang Chủ\n" +
            "                    </a>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</div>\n\n" +
            footerHtml;
            
        for (String[] page : pages) {
            String filename = page[0];
            String title = page[1];
            String desc = page[2];
            String html = String.format(template, title, desc);
            Files.write(Paths.get(pagesDir.getAbsolutePath(), filename), html.getBytes("UTF-8"));
        }
        System.out.println("SUCCESS");
    }
}
