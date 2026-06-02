import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UpdateBotLinks {
    public static void main(String[] args) throws Exception {
        String searchStr = "<div class=\"bot-options\">\n" +
                "            <a href=\"javascript:void(0)\" onclick=\"openTelegramBot('Cách chọn size giày thể thao?')\" class=\"bot-option-btn\">\n" +
                "                <span>Hướng dẫn chọn size giày</span>\n" +
                "                <i class=\"fa-solid fa-chevron-right\"></i>\n" +
                "            </a>\n" +
                "            <a href=\"javascript:void(0)\" onclick=\"openTelegramBot('Kiểm tra trạng thái đơn hàng của tôi')\" class=\"bot-option-btn\">\n" +
                "                <span>Tra cứu trạng thái đơn hàng</span>\n" +
                "                <i class=\"fa-solid fa-chevron-right\"></i>\n" +
                "            </a>\n" +
                "            <a href=\"javascript:void(0)\" onclick=\"openTelegramBot('Cho tôi hỏi về chính sách đổi trả')\" class=\"bot-option-btn\">\n" +
                "                <span>Chính sách đổi trả 30 ngày</span>\n" +
                "                <i class=\"fa-solid fa-chevron-right\"></i>\n" +
                "            </a>\n" +
                "            <a href=\"javascript:void(0)\" onclick=\"openTelegramBot('Gặp nhân viên tư vấn')\" class=\"bot-option-btn\">\n" +
                "                <span>Gặp nhân viên tư vấn trực tiếp</span>\n" +
                "                <i class=\"fa-solid fa-chevron-right\"></i>\n" +
                "            </a>\n" +
                "        </div>";

        String replaceStr = "<div class=\"bot-options\">\n" +
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
                "        </div>";

        File templatesDir = new File("C:/Users/ndnan/TheXuong/src/main/resources/templates");
        List<File> allHtmlFiles = new ArrayList<>();
        collectHtmlFiles(templatesDir, allHtmlFiles);

        int count = 0;
        for (File f : allHtmlFiles) {
            String content = new String(Files.readAllBytes(f.toPath()), "UTF-8");
            
            // Normalize line endings to avoid matching issues
            String normalizedContent = content.replace("\r\n", "\n");
            String normalizedSearch = searchStr.replace("\r\n", "\n");
            String normalizedReplace = replaceStr.replace("\r\n", "\n");

            if (normalizedContent.contains(normalizedSearch)) {
                normalizedContent = normalizedContent.replace(normalizedSearch, normalizedReplace);
                
                // Convert back to CRLF for windows if needed, but not strictly required
                Files.write(f.toPath(), normalizedContent.getBytes("UTF-8"));
                count++;
            }
        }
        System.out.println("UPDATED " + count + " FILES SUCCESSFULLY");
    }

    private static void collectHtmlFiles(File dir, List<File> htmlFiles) {
        if (dir.exists() && dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    collectHtmlFiles(f, htmlFiles);
                } else if (f.getName().endsWith(".html")) {
                    htmlFiles.add(f);
                }
            }
        }
    }
}
