import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InjectBotAllPages {

    public static void main(String[] args) {
        String botSnippet = "\n" +
                "<!-- Khung Bot AI Widget -->\n" +
                "<div class=\"bot-widget-container\" id=\"botWidget\">\n" +
                "    <!-- Popup Menu -->\n" +
                "    <div class=\"bot-menu-popup\" id=\"botMenuPopup\">\n" +
                "        <div class=\"bot-header\">\n" +
                "            <h5>TheXuong AI Bot</h5>\n" +
                "            <p>Trợ lý thông minh 24/7</p>\n" +
                "        </div>\n" +
                "        <div class=\"bot-options\">\n" +
                "            <a th:href=\"@{/pages/size-guide}\" class=\"bot-option-btn\">\n" +
                "                <span>Hướng dẫn chọn size giày</span>\n" +
                "                <i class=\"fa-solid fa-link text-muted\"></i>\n" +
                "            </a>\n" +
                "            <a th:href=\"@{/pages/order-tracking}\" class=\"bot-option-btn\">\n" +
                "                <span>Tra cứu trạng thái đơn hàng</span>\n" +
                "                <i class=\"fa-solid fa-link text-muted\"></i>\n" +
                "            </a>\n" +
                "            <a th:href=\"@{/pages/return-policy}\" class=\"bot-option-btn\">\n" +
                "                <span>Chính sách đổi trả 30 ngày</span>\n" +
                "                <i class=\"fa-solid fa-link text-muted\"></i>\n" +
                "            </a>\n" +
                "            <a href=\"javascript:void(0)\" onclick=\"openTelegramBot('Gặp nhân viên tư vấn')\" class=\"bot-option-btn\">\n" +
                "                <span>Gặp nhân viên tư vấn qua Telegram</span>\n" +
                "                <i class=\"fa-brands fa-telegram text-primary fs-5\"></i>\n" +
                "            </a>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    \n" +
                "    <!-- Nút Float Toggle -->\n" +
                "    <button class=\"telegram-float-toggle\" onclick=\"toggleBotMenu()\" title=\"Mở danh sách câu hỏi AI\">\n" +
                "        <i class=\"fa-solid fa-robot\"></i>\n" +
                "    </button>\n" +
                "</div>\n" +
                "\n" +
                "<script>\n" +
                "    function toggleBotMenu() {\n" +
                "        const widget = document.getElementById('botWidget');\n" +
                "        const popup = document.getElementById('botMenuPopup');\n" +
                "        widget.classList.toggle('open');\n" +
                "        popup.classList.toggle('active');\n" +
                "    }\n" +
                "\n" +
                "    function openTelegramBot(message) {\n" +
                "        // Thay YOUR_TELEGRAM_BOT_USERNAME bằng username của bot bạn nhé\n" +
                "        const botUsername = 'TheXuong_tananh_bot';\n" +
                "        const encodedMessage = encodeURIComponent(message);\n" +
                "        const url = `https://t.me/${botUsername}?text=${encodedMessage}`;\n" +
                "        window.open(url, '_blank');\n" +
                "        toggleBotMenu(); // Đóng menu sau khi click\n" +
                "    }\n" +
                "</script>\n";

        String templatesPath = "C:/Users/ndnan/TheXuong/src/main/resources/templates";
        
        try (Stream<Path> paths = Files.walk(Paths.get(templatesPath))) {
            List<Path> htmlFiles = paths.filter(Files::isRegularFile)
                                        .filter(p -> p.toString().endsWith(".html"))
                                        .filter(p -> !p.toString().contains("\\admin\\") && !p.toString().contains("/admin/"))
                                        .collect(Collectors.toList());

            int updatedCount = 0;
            for (Path path : htmlFiles) {
                String content = new String(Files.readAllBytes(path), "UTF-8");
                if (!content.contains("id=\"botWidget\"")) {
                    content = content.replace("</body>", botSnippet + "</body>");
                    Files.write(path, content.getBytes("UTF-8"));
                    updatedCount++;
                    System.out.println("Injected into: " + path.getFileName());
                }
            }
            System.out.println("Total files updated: " + updatedCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
