public class Test {
    public static void main(String[] args) {
        String message = "có nón không";
        String cleaned = message.toLowerCase().trim();
        String[] stopWords = {"tại sao", "trong", "của", "chúng tôi", "có", "không", "mà", "tôi", "hỏi", "muốn", "mua", "tìm", "xem", "bên", "shop", "cho", "mình", "cái", "chiếc", "đôi", "loại", "này", "kia", "nhé", "nha", "ạ", "thấy"};
        for (String word : stopWords) {
            cleaned = cleaned.replaceAll("(?U)\\b" + word + "(?U)\\b", "");
        }
        cleaned = cleaned.replaceAll("(?U)\\bnón(?U)\\b", "mũ");
        cleaned = cleaned.replaceAll("(?U)\\bgiầy(?U)\\b", "giày");
        cleaned = cleaned.replaceAll("(?U)\\bbanh(?U)\\b", "bóng");
        cleaned = cleaned.replaceAll("(?U)\\bvớ(?U)\\b", "tất");
        
        System.out.println("CLEANED: [" + cleaned.replaceAll("\\s+", " ").trim() + "]");
    }
}
