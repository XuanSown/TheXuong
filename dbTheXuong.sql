CREATE DATABASE dbTheXuong;
GO

USE master

USE dbTheXuong;
GO

    SELECT * FROM Users;
    SELECT * FROM Products;
    SELECT * FROM Sizes;
    SELECT * FROM Carts;
    SELECT * FROM ProductVariants;
    SELECT * FROM CartItems;
    SELECT * FROM Orders;
    SELECT * FROM OrderDetails;
    SELECT * FROM Reviews;

DROP TABLE IF EXISTS Reviews;
DROP TABLE IF EXISTS OrderDetails;
DROP TABLE IF EXISTS Orders;
DROP TABLE IF EXISTS CartItems;
DROP TABLE IF EXISTS Carts;
DROP TABLE IF EXISTS ProductVariants;

DROP TABLE IF EXISTS Sizes;
DROP TABLE IF EXISTS Products;
DROP TABLE IF EXISTS Users;

CREATE TABLE Users
(
    id        BIGINT IDENTITY (1,1) PRIMARY KEY,
    username  NVARCHAR(50) UNIQUE,
    password  NVARCHAR(255),
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

CREATE TABLE Sizes (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(10) NOT NULL
);

CREATE TABLE ProductVariants (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT FOREIGN KEY REFERENCES Products(id),
    size_id BIGINT FOREIGN KEY REFERENCES Sizes(id),
    quantity INT DEFAULT 0, -- Số lượng tồn kho
    sku NVARCHAR(50) UNIQUE -- Mã kho hàng
);

CREATE TABLE Carts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT FOREIGN KEY REFERENCES Users(id)
);

CREATE TABLE CartItems (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    cart_id BIGINT FOREIGN KEY REFERENCES Carts(id),
    product_variant_id BIGINT FOREIGN KEY REFERENCES ProductVariants(id), -- Link tới biến thể cụ thể
    quantity INT DEFAULT 1
);

CREATE TABLE Orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT FOREIGN KEY REFERENCES Users(id),
    full_name NVARCHAR(100), -- Tên người nhận (có thể khác tên user)
    phone_number NVARCHAR(15),
    address NVARCHAR(MAX),
    total_money DECIMAL(18, 2),
    status NVARCHAR(20) DEFAULT 'PENDING', -- PENDING, SHIPPING, DELIVERED, CANCELLED
    payment_method NVARCHAR(20), -- COD, MOMO, VNPAY
    created_at DATETIME DEFAULT GETDATE()
);

CREATE TABLE OrderDetails (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT FOREIGN KEY REFERENCES Orders(id),
    product_id BIGINT, -- Lưu lại để query nhanh
    product_name NVARCHAR(100), -- Lưu cứng tên sp tại thời điểm mua (đề phòng sp bị đổi tên sau này)
    price DECIMAL(18, 2), -- Giá tại thời điểm mua
    quantity INT,
    total_price DECIMAL(18, 2)
);

CREATE TABLE Reviews (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT FOREIGN KEY REFERENCES Users(id),
    product_id BIGINT FOREIGN KEY REFERENCES Products(id) ON DELETE CASCADE,
    rating INT CHECK (rating >= 1 AND rating <= 5), -- Chấm điểm 1-5 sao
    comment NVARCHAR(MAX),
    created_at DATETIME DEFAULT GETDATE()
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
        N'Mũ Trucker Stadium là phụ kiện không thể thiếu, hoàn hảo cho phong cách thư giãn hằng ngày. Thiết kế dáng trucker thời thượng, chiếc mũ lưỡi trai này là sự kết hợp hoàn hảo với tủ đồ thể thao của bạn.');


-- 1. Thêm dữ liệu vào bảng Sizes
INSERT INTO Sizes (name)
VALUES
    -- Size quần áo
    ('S'), ('M'), ('L'), ('XL'),
    -- Size giày
    ('36'), ('37'), ('38'), ('39'), ('40'), ('41'), ('42'), ('43'),
    -- Size cho phụ kiện/khác
    ('FreeSize');

-- A. Thêm variants cho QUẦN ÁO (Category: 'Áo', 'Quần áo') -> Size: S, M, L, XL
INSERT INTO ProductVariants (product_id, size_id, quantity, sku)
SELECT
    p.id,
    s.id,
    100, -- Số lượng mặc định 100
    CONCAT('SKU-', p.id, '-', s.name) -- SKU ví dụ: SKU-1-S
FROM Products p
CROSS JOIN Sizes s
WHERE p.category IN (N'Áo', N'Quần áo')
  AND s.name IN ('S', 'M', 'L', 'XL');

-- B. Thêm variants cho GIÀY (Category: 'Giày') -> Size: 36 -> 43
INSERT INTO ProductVariants (product_id, size_id, quantity, sku)
SELECT
    p.id,
    s.id,
    50, -- Số lượng mặc định 50
    CONCAT('SKU-', p.id, '-', s.name)
FROM Products p
CROSS JOIN Sizes s
WHERE p.category = N'Giày'
  AND s.name IN ('36', '37', '38', '39', '40', '41', '42', '43');

-- C. Thêm variants cho PHỤ KIỆN / KHÁC (Category còn lại) -> Size: FreeSize
INSERT INTO ProductVariants (product_id, size_id, quantity, sku)
SELECT
    p.id,
    s.id,
    200, -- Số lượng mặc định 200
    CONCAT('SKU-', p.id, '-FREE')
FROM Products p
CROSS JOIN Sizes s
WHERE p.category NOT IN (N'Áo', N'Quần áo', N'Giày')
  AND s.name = 'FreeSize';





INSERT INTO Products (name, brand, sport, category, price, image_url, description)
VALUES
       (N'Áo Thun Đồ Họa McLAREN RACING Nam', 'Puma', N'Đua xe', N'Áo', 1300000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…%C3%81o-Thun-%C4%90%E1%BB%93-H%E1%BB%8Da-McLAREN-RACING-Nam',
        N'Cảm nhận trọn vẹn nhịp đập của ngày đua. Áo thun đồ họa McLaren Racing tái hiện năng lượng F1® qua thiết kế đồ họa lấy cảm hứng từ xe đua đầy ấn tượng. Chất liệu mềm mại cùng phom dáng thoải mái mang lại cảm giác dễ chịu suốt ngày dài năng động. Khoác lên chiếc áo không chỉ là thể hiện sự ủng hộ, đây còn là tinh thần tốc độ và độ chính xác của bạn.'),
       (N'Quần McLAREN RACING Nam', 'Puma', N'Đua xe', N'Quần', 2200000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…21/01/mod01/fnd/VNM/fmt/png/Qu%E1%BA%A7n-McLAREN-RACING-Nam',
        N'Dành cho những người không ngại thể hiện đam mê hết mình. Những người ăn mừng từng khúc cua, từng pha vượt, từng chiến thắng với cảm xúc trọn vẹn. Bộ sưu tập Lifestyle PUMA x McLAREN RACING lần đầu ra mắt. Dành cho người hâm mộ và lấy cảm hứng từ những huyền thoại từ ký ức tuổi thơ, bộ sưu tập này đưa hơi thở quá khứ trở lại trong diện mạo hiện đại. Khi bạn chọn “Race Loud”, phong cách cũng cần thể hiện tương xứng.'),
       (N'Áo Khoác Thể Thao BMW M MOTORSPORT Sportswear MT7 Nam', 'Puma', N'Đua xe', N'Áo', 3100000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…C3%A1c-Th%E1%BB%83-Thao-BMW-M-MOTORSPORT-Sportswear-MT7-Nam',
        N'Lấy cảm hứng từ biểu tượng BMW M3 E46, bộ sưu tập PUMA x BMW M MOTORSPORT mang nguồn năng lượng phóng khoáng của đường đua đến với phong cách đường phố. Với phong cách thoải mái cùng sọc T7 kinh điển trong gam màu M, chiếc áo khoác thể thao này tôn vinh di sản motorsport theo cách thức hiện đại và đầy cá tính.'),
       (N'Áo khoác nỉ nam BMW M Motorsport MT7+', 'Puma', N'Đua xe', N'Áo', 3100000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…png/%C3%81o-kho%C3%A1c-n%E1%BB%89-nam-BMW-M-Motorsport-MT7+',
N'T7 - BST kinh điển từ PUMA lấy cảm hứng từ đường đua với điểm nhấn là hai sọc chạy dọc thân 7cm. Không chỉ khuấy đảo đường phố, BST còn là huyền thoại trên những sân tập. Ngày nay, các phong cách vẫn nổi bật như xưa. Áo khoác BMW M Motorsport MT7+ đã được tạo ra đặc biệt dành cho những người hâm mộ thể thao mô tô. Áo khoác có sọc tay áo, kiểu dáng đẹp và chi tiết thương hiệu BMW M Motorsport.'),
       (N'Áo Hoodie McLAREN RACING Nam', 'Puma', N'Đua xe', N'Áo', 2500000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…637507/03/fnd/VNM/fmt/png/%C3%81o-Hoodie-McLAREN-RACING-Nam',
        N'Dành cho những người không ngại thể hiện đam mê hết mình. Những người ăn mừng từng khúc cua, từng pha vượt, từng chiến thắng với cảm xúc trọn vẹn. Bộ sưu tập Lifestyle PUMA x McLAREN RACING lần đầu ra mắt. Dành cho người hâm mộ và lấy cảm hứng từ những huyền thoại gắn liền với ký ức tuổi trẻ, chiếc hoodie này đưa tinh thần quá khứ hòa nhịp cùng hiện tại. Khi bạn chọn “Race Loud”, phong cách cũng cần thể hiện tương xứng.'),
       (N'Áo thun nam PUMA x ASTON MARTIN ARAMCO F1® TEAM Essentials', 'Puma', N'Khác', N'Áo', 800000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…hun-nam-PUMA-x-ASTON-MARTIN-ARAMCO-F1%C2%AE-TEAM-Essentials',
        N'Bộ sưu tập PUMA x ASTON MARTIN ARAMCO F1® TEAM định nghĩa lại phong cách đường đua. Được tăng cường tốc độ và độ chính xác, bộ sưu tập này mang công nghệ đua vào chuyển động thường ngày. Thiết kế hợp lý và phong cách táo bạo được thể hiện trong từng chi tiết — được tạo ra để di chuyển nhanh, phong cách ấn tượng và làm chủ mọi khoảnh khắc.'),
       (N'Áo Dệt Kim Cổ Tròn PUMA x PORSCHE LEGACY Lifestyle Nam', 'Puma', N'Khác', N'Áo', 3100000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…im-C%E1%BB%95-Tr%C3%B2n-PUMA-x-PORSCHE-LEGACY-Lifestyle-Nam',
        N'Dành cho những ai di chuyển với sự tự tin dễ dàng, bộ sưu tập PUMA x PORSCHE LEGACY mới nhất kết hợp tính thẩm mỹ thể thao cổ điển với phong cách thời trang đường phố hiện đại. Những đường nét thoải mái, màu trung tính phai nắng và họa tiết cũ tạo nên cảm giác thoải mái, được yêu thích. Áo thun họa tiết, áo khoác ngoài oversize và da bạc màu mang đến vẻ ngoài mạnh mẽ nhưng tinh tế, hoàn hảo cho những ngày hè và đêm không nghỉ.'),
(N'Quần short Scuderia Ferrari PM1 cho nam', 'Puma', N'Khác', N'Quần', 2100000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…VNM/fmt/png/Qu%E1%BA%A7n-short-Scuderia-Ferrari-PM1-cho-nam',
        N'Bộ sưu tập hợp tác giữa PUMA và Scuderia Ferrari là lời tôn vinh dành cho tinh thần thể thao tốc độ và di sản đua xe huyền thoại của Ferrari. Dòng sản phẩm bao gồm giày, trang phục và phụ kiện, kết hợp hài hòa giữa phong cách, sự thoải mái và hiệu suất, với những gam màu và chi tiết mang đậm dấu ấn Scuderia Ferrari, bạn có thể mang tinh thần Ferrari đến bất cứ nơi đâu. Quần short này cung cấp năng lượng cho môn thể thao đua xe phù hợp với tốc độ của bạn.'),
       (N'Quần Jogger CLOUDSPUN Nữ', 'Puma', N'Khác', N'Quần', 980000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…01/fnd/VNM/fmt/png/Qu%E1%BA%A7n-Jogger-CLOUDSPUN-N%E1%BB%AF',
        N'Khám phá sự thoải mái tối đa với CLOUDSPUN. Những sản phẩm hiệu suất cao này có chất liệu siêu mềm với độ co giãn bốn chiều để chuyển động không bị hạn chế. Với khả năng kiểm soát độ ẩm dryCELL và kiểu dáng cạp cao để che phủ và tự tin. Cổ tay áo co giãn giúp bạn trông chỉn chu, trong khi túi đựng những vật dụng cần thiết.'),
       (N'Quần Short 2 Trong1 PUMA x HYROX Nữ', 'Puma', N'Khác', N'Quần', 1550000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…fmt/png/Qu%E1%BA%A7n-Short-2-Trong1-PUMA-x-HYROX-N%E1%BB%AF',
        N'Cuộc đua thể lực đỉnh cao cần những trang bị xứng tầm. PUMA x HYROX trở lại với bộ sưu tập mới, được thiết kế riêng cho các vận động viên HYROX. Dù bạn đang chuẩn bị cho sự kiện HYROX đầu tiên hay quyết tâm chinh phục kỷ lục cá nhân mới, từng món đồ trong bộ sưu tập này đều được tạo ra để đáp ứng cường độ thi đấu khắt khe.'),
       (N'Quần Cargo FUTURE.PUMA.ARCHIVE Nữ', 'Puma', N'Khác', N'Quần', 1800000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…M/fmt/png/Qu%E1%BA%A7n-Cargo-FUTURE.PUMA.ARCHIVE-N%E1%BB%AF',
        N'Đây là FUTURE.PUMA.ARCHIVE. Bộ sưu tập lấy cảm hứng từ đường phố này kết hợp tinh hoa của quá khứ với những ranh giới vô tận của tương lai. Cổ điển nhưng vẫn hiện đại, những phong cách này ở đây để đưa ra một tuyên ngôn thời trang.'),
       (N'Áo Thun CLOUDSPUN Nam', 'Puma', N'Khác', N'Áo', 850000,
N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…global/527593/01/fnd/VNM/fmt/png/%C3%81o-Thun-CLOUDSPUN-Nam',
        N'Khám phá sự thoải mái tối đa với CLOUDSPUN. Những thiết kế hiệu suất cao này sử dụng chất liệu siêu mềm mại cùng khả năng co giãn 4 chiều, mang đến cảm giác thoải mái tối đa trong từng chuyển động. Công nghệ dryCELL giúp kiểm soát độ ẩm, giữ cho bạn luôn khô thoáng và tươi mới trong suốt mỗi bài tập.'),
       (N'Áo Thun Khóa Kéo 1/4 PUMA x HYROX CLOUDSPUN Nữ', 'Puma', N'Khác', N'Áo', 1750000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…un-Kh%C3%B3a-K%C3%A9o-1/4-PUMA-x-HYROX-CLOUDSPUN-N%E1%BB%AF',
        N'Giữ sự thoải mái và làm chủ từng chuyển động với chất liệu CLOUDSPUN mềm mịn vượt trội và độ co giãn 4 chiều linh hoạt. Áo khoác 1/4 zip phom rộng này được trang bị công nghệ dryCELL giúp hút ẩm hiệu quả, lý tưởng cho khởi động, thư giãn sau buổi tập, hoặc khi bạn đang chinh phục mục tiêu. Hiệu suất tối đa hòa quyện cùng sự linh hoạt cho mọi ngày.'),
       (N'Ba lô PUMA x HYROX 46L', 'Puma', N'Khác', N'Balo', 4030000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…lobal/091611/01/fnd/VNM/fmt/png/Ba-l%C3%B4-PUMA-x-HYROX-46L',
        N'Cuộc đua thể lực đỉnh cao cần những trang bị xứng tầm. PUMA x HYROX trở lại với bộ sưu tập mới, được thiết kế riêng cho các vận động viên HYROX. Cho dù bạn đang chuẩn bị cho sự kiện HYROX đầu tiên của mình hay đang theo đuổi kỷ lục cá nhân mới, thì mọi sản phẩm trong bộ sưu tập này đều được tạo ra để đáp ứng cường độ của cuộc thi tài'),
       (N'Balo trùm đầu họa tiết PUMA x RIPNDIP dành cho nam', 'Puma', N'Khác', N'Balo', 2300000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…u-h%E1%BB%8Da-ti%E1%BA%BFt-PUMA-x-RIPNDIP-d%C3%A0nh-cho-nam',
        N'Sự kết hợp giữa PUMA và RIPNDIP mang phong cách trượt ván đến công việc hàng ngày với thiết kế trùm đầu táo bạo, họa tiết nổi bật và ngăn chứa đồ thông minh. Phong cách hỗn loạn của Nermal? Hoàn hảo.'),

       (N'Giày Chạy Bộ Nike Air Zoom Pegasus 40', N'Nike', N'Chạy bộ', N'Giày', 3690000,
        N'https://runningstore.vn/wp-content/uploads/2024/09/z5835954124461_d185429b09576894463fdcb6a2ddb19a.jpg',
        N'Đôi giày chạy bộ quốc dân với đệm React êm ái, phù hợp cho mọi cự ly luyện tập hàng ngày.'),
(N'Áo Đấu Sân Nhà Manchester City 2025', N'Puma', N'Bóng đá', N'Áo', 1950000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…C4%90%E1%BA%A5u-S%C3%A2n-Nh%C3%A0-Manchester-City-25/26-Nam',
        N'Áo đấu chính hãng mùa giải mới nhất của The Citizens, chất liệu thoáng khí, logo thêu sắc nét.'),

       (N'Giày Bóng Rổ Nike LeBron XX', N'Nike', N'Bóng rổ', N'Giày', 5200000,
        N'https://static.nike.com/a/images/t_web_pw_592_v2/f_auto/u_9…ly/81561a39-7164-4274-b067-e2ad649e2f43/LEBRON+XXIII+EP.png',
        N'Giày signature đời thứ 20 của LeBron James, thiết kế cổ thấp siêu nhẹ và độ bám sàn cực đỉnh.'),

       (N'Quần Ngắn Thể Thao Adidas Tiro 23', N'Adidas', N'Bóng đá', N'Quần áo', 1100000,
        N'https://assets.adidas.com/images/h_2000,f_auto,q_auto,fl_lo…6/Quan_Short_Tiro_23_League_Nhieu_mau_IC7488_01_laydown.jpg',
        N'Quần tập luyện quốc dân với khóa kéo ở ống chân, form ôm gọn gàng, chất vải co giãn tốt.'),

       (N'Áo Polo Golf Modalon Nam', N'Puma', N'Khác', N'Áo', 1100000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…bal/635477/04/fnd/VNM/fmt/png/%C3%81o-Polo-Golf-Modalon-Nam',
        N'Quần tập luyện quốc dân với khóa kéo ở ống chân, form ôm gọn gàng, chất vải co giãn tốt.'),

       (N'Áo sơ mi nam PALAIS ARTISAN', N'Puma', N'Khác', N'Áo', 2500000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…mod01/fnd/VNM/fmt/png/%C3%81o-s%C6%A1-mi-nam-PALAIS-ARTISAN',
        N'Khám phá bản sắc của Morocco với bộ sưu tập Palais Artisan của PUMA. Chiếc áo sơ mi này gói gọn được tinh thần sôi động của nền văn hóa Ma-rốc, kết hợp giữa họa tiết tự nhiên với phong cách thời trang đường phố hiện đại.'),

       (N'Quần Short Jeans Nam PUMA x A$AP ROCKY', N'Puma', N'Khác', N'Quần', 5500000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…/VNM/fmt/png/Qu%E1%BA%A7n-Short-Jeans-Nam-PUMA-x-A$AP-ROCKY',
        N'Quần tập luyện quốc dân với khóa kéo ở ống chân, form ôm gọn gàng, chất vải co giãn tốt.'),

       (N'Váy A$AP ROCKY x PUMA dành cho nữ', N'Puma', N'Khác', N'Váy', 2300000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…fmt/png/V%C3%A1y-A$AP-ROCKY-x-PUMA-d%C3%A0nh-cho-n%E1%BB%AF',
N'A$AP ROCKY x PUMA trở lại với bộ sưu tập mới lấy cảm hứng từ thời kỳ phục hưng nhạc jazz của Harlem. Họa tiết da rắn, da báo và chất liệu thô ráp kết hợp với quần denim, áo phông họa tiết và các trang phục đường phố chủ đạo khác. Kết quả là sự kết hợp giữa các họa tiết đậm nét, thương hiệu đột phá và sự pha trộn giữa phong cách quá khứ và hiện tại, tất cả đều mang hơi hướng Flacko.'),

       (N'Áo khoác thể thao PUMA x ROSÉ T7 dành cho nữ', N'Puma', N'Khác', N'Áo', 3000000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…%E1%BB%83-thao-PUMA-x-ROS%C3%89-T7-d%C3%A0nh-cho-n%E1%BB%AF',
        N'Giới thiệu bộ sưu tập đầu tiên của PUMA x ROSÉ. Bắt nguồn từ phong cách riêng biệt của siêu sao nhạc pop, bộ sưu tập kết hợp trang phục đường phố cổ điển với phong cách câu lạc bộ đồng quê cao cấp và những điểm nhấn cá nhân. Giống như một bông hồng luôn nở rộ, bộ sưu tập này mời gọi mọi người lột bỏ mọi lớp mặt nạ, khám phá phong cách cá nhân và thể hiện con người thật của mình. Thân ái, Rosé.'),

       (N'Áo khoác thể thao FENTY x PUMA T7 Unisex', N'Puma', N'Khác', N'Áo', 3200000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…/%C3%81o-kho%C3%A1c-th%E1%BB%83-thao-FENTY-x-PUMA-T7-Unisex',
        N'Bộ sưu tập mới của FENTY x PUMA tái hiện lại phong cách bóng đá truyền thống thông qua gu thẩm mỹ đặc trưng của Rihanna. Kết hợp giữa thể thao và phong cách độc đáo của riêng Rihanna, bộ sưu tập tự hào với bảng màu rực rỡ nổi bật trên sân cỏ, trên phố - bất cứ nơi nào bạn đến.'),

       (N'Áo thun chạy bộ Boxy coolCELL cho nam', N'Puma', N'Chạy bộ', N'Áo', 1100000,
        N'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fa…/%C3%81o-thun-ch%E1%BA%A1y-b%E1%BB%99-Boxy-coolCELL-cho-nam',
        N'Cảm thấy thoải mái tức thì khi mặc chiếc áo chạy bộ này. Cấu trúc vật liệu kỹ thuật mang lại cảm giác mát lạnh tức thì qua tiếp xúc trong khi bạn vượt qua mọi khoảng cách và nhiệt độ. Túi đựng đồ tiện dụng giúp bạn cất giữ những vật dụng cần thiết, giúp bạn tập trung vào việc chạy bộ.'),

       (N'Giày bóng rổ Scoot Zeros II Crystal Unisex', N'Puma', N'Bóng rổ', N'Giày', 2800000,
N'https://assets.adidas.com/images/h_2000,f_auto,q_auto,fl_lo…6/Quan_Short_Tiro_23_League_Nhieu_mau_IC7488_01_laydown.jpg',
        N'Phiên bản mới nhất của mẫu giày đặc trưng của Scoot Henderson đã ra mắt: Scoot Zeros II Crystal. Chiếc vương miện với các màu bạc, xanh lam và xanh ngọc này được thiết kế để vinh danh nguồn cảm hứng lớn nhất của Scoot – mẹ anh, bà Crystal. Lấy cảm hứng từ câu thần chú của chính anh (ODD – Quyết tâm thống trị) và phong cách chơi linh hoạt, Scoot Zeros II Caution có đệm NITROFOAM™ thiết kế tinh tế, hợp lý và đế ngoài bằng cao su hiệu suất cao cho lối chơi toàn diện từ đầu chí cuối.');