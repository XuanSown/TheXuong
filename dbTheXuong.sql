CREATE DATABASE dbTheXuong;
GO

USE dbTheXuong;
GO

SELECT *
FROM Users;
SELECT *
FROM Products;

-- =============================================
-- 2. TẠO BẢNG (Cấu trúc chuẩn)
-- =============================================

-- Bảng User
CREATE TABLE Users
(
    id        BIGINT IDENTITY (1,1) PRIMARY KEY,
    username  NVARCHAR(50) UNIQUE,          -- Nullable (cho trường hợp login Google)
    password  NVARCHAR(255),                -- Nullable (cho trường hợp login Google)
    email     NVARCHAR(100) UNIQUE NOT NULL,
    provider  NVARCHAR(20) DEFAULT 'LOCAL', -- 'LOCAL' hoặc 'GOOGLE'
    role      NVARCHAR(20) DEFAULT 'USER',
    full_name NVARCHAR(100)                 -- Lưu tên hiển thị
);

-- Bảng Sản phẩm (Đã gộp cột BRAND vào đây luôn)
CREATE TABLE Products
(
    id          BIGINT IDENTITY (1,1) PRIMARY KEY,
    name        NVARCHAR(100) NOT NULL,
    brand       NVARCHAR(50),  -- Thương hiệu (Nike, Adidas...)
    sport       NVARCHAR(50),
    category    NVARCHAR(50),  -- Loại (Giày, Quần áo...)
    price       DECIMAL(18, 2) CHECK (price >= 0),
    image_url   NVARCHAR(MAX), -- Dùng MAX để link ảnh dài không bị lỗi
    description NVARCHAR(MAX)
);

INSERT INTO Products (name, brand, sport, category, price, image_url, description)
VALUES (N'McLAREN RACING Speedcat', 'Puma', N'Khác', N'Giày', 2800000,
        'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_2000,h_2000/global/309452/01/sv01/fnd/VNM/fmt/png/McLAREN-RACING-Speedcat-Sneakers-Unisex',
        N'Sneakers Unisex'),
       (N'Áo Đấu Sân Nhà Manchester United 25/26', 'Adidas', N'Bóng đá', N'Áo', 2200000,
        'https://assets.adidas.com/images/h_2000,f_auto,q_auto,fl_lossy,c_fill,g_auto/9ae59d2a8c6249c9a3b1fefc31a9d595_9366/Ao_DJau_San_Nha_Manchester_United_25-26_DJo_JI7428_21_model.jpg',
        N'Công nghệ AEROREADY'),
       (N'Nike Structure Plus', 'Nike', N'Chạy bộ', N'Giày', 4699000,
        'https://static.nike.com/a/images/t_web_pdp_535_v2/f_auto,u_9ddf04c7-2a9a-4d76-add1-d15af8f0263d,c_scale,fl_relative,w_1.0,h_1.0,fl_layer_apply/bfc8e6ce-b8d7-4dac-814b-b44e7bfd4da6/NIKE+STRUCTURE+PLUS.png',
        N'Men''s Road Running Shoes'),
       (N'Li-Ning P-AATU048-4V', 'Li-Ning', N'Cầu lông & Pickleball', N'Quần áo', 567000,
        'https://cdn.hstatic.net/products/1000362402/6c5344f9e6addb65d25b803bb28c29c7_2fcad81faa904f3f98de84f55047c6b2_db69cbcdfa7e404fa2011a75bc9256a7_master.jpg',
        N'Chất liệu cao cấp – Nhẹ và thoáng khí'),
       (N'Giày Bóng Đá Puma Ultra 5 Ultimate', 'Puma', N'Bóng đá', N'Giày', 3120000,
        'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_2000,h_2000/global/108159/01/sv01/fnd/VNM/fmt/png/Gi%C3%A0y-b%C3%B3ng-%C4%91%C3%A1-ULTRA-5-ULTIMATE-FG',
        N'Dòng giày tốc độ siêu nhẹ, đế FG bám sân cực tốt'),
       (N'Giày Đá Banh Mizuno Morelia Neo III Pro', 'Mizuno', N'Bóng đá', N'Giày', 2190000,
        'https://product.hstatic.net/1000061481/product/anh_sp_add_w735eb_2-01-02-03737-02-02-02-01-02-01-02-02_7ca9babcea514f018c516c82a963bb96_1024x1024.jpg',
        N'Da Kangaroo mềm mại, cảm giác bóng chân thật, form bè hợp chân người Việt'),
       (N'Giày Chạy Bộ Asics Novablast 4', 'Asics', N'Chạy bộ', N'Giày', 3280000,
        'https://bizweb.dktcdn.net/100/340/361/products/1011b945-400-sl-lt-glb.jpg?v=1726030000737',
        N'Công nghệ đệm FF BLAST™ PLUS ECO giúp bước chạy nảy và êm ái hơn'),
       (N'Giày Cầu Lông Yonex 65Z3 Men', 'Yonex', N'Cầu lông', N'Giày', 2750000,
        'https://cdn.shopvnb.com/uploads/gallery/giay-cau-long-yonex-shb-65z3-men-trang-xanh-new-2023-2.webp',
        N'Công nghệ Power Cushion+ giảm chấn động, bảo vệ đầu gối tối đa'),
       (N'Mũ lưỡi trai in họa tiết PUMA x RIPNDIP', 'Puma', N'Khác', N'Phụ kiện', 1200000,
        'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_2000,h_2000/global/026595/02/fnd/VNM/fmt/png/M%C5%A9-l%C6%B0%E1%BB%A1i-trai-in-h%E1%BB%8Da-ti%E1%BA%BFt-PUMA-x-RIPNDIP-d%C3%A0nh-cho-nam',
        N'Nermal đã trở lại và làm mọi thứ trở nên hoàn hảo. Chiếc mũ lưỡi trai này từ PUMA x RIPNDIP mang đến năng lượng trượt ván nổi loạn cho phong cách thường ngày của bạn, với họa tiết vui nhộn, điểm nhấn chức năng và phong cách đường phố.'),
       (N'Bình nước thể thao 1L', 'Puma', N'Khác', N'Phụ kiện', 300000,
        'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_2000,h_2000/global/053811/01/fnd/VNM/fmt/png/B%C3%ACnh-n%C6%B0%E1%BB%9Bc-th%E1%BB%83-thao-1L',
        N'Duy trì năng lượng trong lúc tập luyện'),
       (N'Ba Lô Thể Thao Thời Trang Nữ Nike W Nsw Futura365 Mini Bpk-Swirl', 'Nike', N'Khác', N'Khác', 1159000,
        'https://ash.vn/cdn/shop/files/AURORA_HV6622-126_PHSYD001-2000_800x.jpg?v=1759477210',
        N'Chiếc ba lô mini này là người bạn đồng hành hoàn hảo cho mọi chuyến đi. Ngăn chính có đủ chỗ để đựng điện thoại, ví và chìa khóa, trong khi ngăn trước giúp bạn sắp xếp gọn gàng những món nhỏ như son dưỡng hay dây buộc tóc.'),
       (N'Vớ Thể Thao Chạy Bộ Unisex Nike U Nk Ltwt Run Ns 1Pr - 200', 'Nike', N'Chạy b', N'Phụ kiện', 509000,
        'https://ash.vn/cdn/shop/files/AURORA_HV6931-101_PHCFH001-2000_800x.jpg?v=1764438496',
        N'Thoáng khí vượt trội'),
       (N'Mũ Trucker Stadium', 'Adidas', N'Khác', N'Phụ kiện', 500000,
        'https://assets.adidas.com/images/h_2000,f_auto,q_auto,fl_lossy,c_fill,g_auto/f092e600d2774d09ae0fb7ffa4732174_9366/Mu_Trucker_Stadium_trang_KF1577_01_00_standard.jpg',
        N'Mũ Trucker Stadium là phụ kiện không thể thiếu, hoàn hảo cho phong cách thư giãn hằng ngày. Thiết kế dáng trucker thời thượng, chiếc mũ lưỡi trai này là sự kết hợp hoàn hảo với tủ đồ thể thao của bạn.'),
		(N'Giày thể thao Speedcat Lux Unisex', 'Puma', N'Khác', N'Giày', 2800000,
		'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_2000,h_2000/global/408198/01/sv01/fnd/VNM/fmt/png/Gi%C3%A0y-th%E1%BB%83-thao-Speedcat-Lux-Unisex', 
		N'Một mẫu giày cổ điển của PUMA lấy cảm hứng từ đường đua: Speedcat. Sản phẩm nổi bật với hình dáng lấy cảm hứng từ giày đua và những đường nét mạnh mẽ, táo bạo. Mang phong cách thể thao vào diện mạo của bạn và làm chủ xu hướng giày dáng thấp với phiên bản mới của kiểu dáng biểu tượng này.'),
		(N'Giày Chạy Bộ Velocity NITRO 4 Nữ', 'Puma', N'Khác', N'Giày', 3350000,
		'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_2000,h_2000/global/311141/01/sv01/fnd/VNM/fmt/png/Gi%C3%A0y-Ch%E1%BA%A1y-B%E1%BB%99-Velocity-NITRO%E2%84%A2-4-N%E1%BB%AF',
		N'Mỗi người có cảm giác chạy khác nhau – và đó chính xác là điều bạn nên có. Cho dù bạn đang theo đuổi kỷ lục cá nhân hay chỉ muốn có được cảm giác tốc độ, Velocity 4 đều được thiết kế để di chuyển cùng bạn. Bất kỳ người chạy bộ nào, bất kỳ lúc nào, bất kỳ khoảng cách nào, mẫu giày này đều có thể hỗ trợ, mang lại hiệu quả và vượt qua sự mong đợi. Velocity nhẹ nhất từ trước đến nay của chúng tôi có đế giữa NITROFOAM™ toàn bộ giày, khả năng thoáng khí được cải thiện nhờ lớp lưới mới ở phần thân trên và đế ngoài PUMAGRIP mang lại lực kéo tốt nhất khi chạy.'),
		(N'Giày Thể Thao Bella UT LEA Nữ', 'Puma', N'Khác', N'Giày', 2100000,
		'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_2000,h_2000/global/405256/01/sv01/fnd/VNM/fmt/png/Gi%C3%A0y-Th%E1%BB%83-Thao-Bella-UT-LEA-N%E1%BB%AF',
		N'Đơn giản, thanh lịch và dễ mang. Mùa này, chúng tôi sẽ mang trở lại đôi giày thể thao Bella yêu thích của mình với phong cách cổ điển và các chi tiết vượt thời gian. Lấy cảm hứng từ các thiết kế trong kho lưu trữ của PUMA, phiên bản này đang là xu hướng với phần trên bóng bẩy, kiểu dáng thấp và dải PUMA mang tính biểu tượng ở bên hông.'),
		(N'Giày thể thao Palermo Premium', 'Puma', N'Khác', N'Giày', 2700000,
		'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_2000,h_2000/global/401744/01/sv01/fnd/VNM/fmt/png/Gi%C3%A0y-th%E1%BB%83-thao-Palermo-Premium',
		N'Bước ra ngay từ kho lưu trữ, đó chính là PUMA Palermo. Đôi giày kiểu dáng hoạt động ngoài trời cổ điển này được làm mới lại với cấu trúc mũi giày T đặc trưng, kết hợp với sự phối màu táo bạo và chất liệu mềm mại, sang trọng.'),
		(N'Giày Thể Thao FENTY x PUMA Avanti LS Unisex', 'Puma', N'Khác', N'Giày', 3000000,
		'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_2000,h_2000/global/404808/01/sv01/fnd/VNM/fmt/png/Gi%C3%A0y-Th%E1%BB%83-Thao-FENTY-x-PUMA-Avanti-LS-Unisex',
		N'Bộ sưu tập mới của FENTY x PUMA tái hiện lại phong cách bóng đá truyền thống thông qua gu thẩm mỹ đặc trưng của Rihanna. Kết hợp giữa thể thao và phong cách độc đáo của riêng Rihanna, bộ sưu tập tự hào với bảng màu rực rỡ nổi bật trên sân cỏ, trên phố - bất cứ nơi nào bạn đến.');