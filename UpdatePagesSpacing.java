import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UpdatePagesSpacing {
    public static void main(String[] args) throws Exception {
        File dir = new File("C:/Users/ndnan/TheXuong/src/main/resources/templates/pages");
        for (File f : dir.listFiles()) {
            if (f.getName().endsWith(".html")) {
                String content = new String(Files.readAllBytes(f.toPath()), "UTF-8");
                content = content.replace("<div class=\"container my-5 flex-grow-1 d-flex flex-column justify-content-center\">",
                        "<div style=\"height: 100px;\"></div>\n<div class=\"container mb-5 flex-grow-1 d-flex flex-column justify-content-center\">");
                Files.write(f.toPath(), content.getBytes("UTF-8"));
            }
        }
        System.out.println("FIXED SPACING");
    }
}
