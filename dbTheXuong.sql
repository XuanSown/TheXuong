CREATE DATABASE dbTheXuong;
GO

USE master

USE dbTheXuong;
GO


-- ============================================================
-- PHẦN I: ROLE-BASED ACCESS CONTROL (RBAC)
-- ============================================================

-- 1. Bảng Chức danh (RoleGroups)
CREATE TABLE RoleGroups (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    name        NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(255)
);

-- 2. Bảng Quyền (Roles)
CREATE TABLE Roles (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    name        NVARCHAR(50)  NOT NULL UNIQUE,
    description NVARCHAR(255)
);

-- 3. Bảng Người dùng
CREATE TABLE Users (
    id            BIGINT IDENTITY (1,1) PRIMARY KEY,
    username      NVARCHAR(50) UNIQUE,
    password      NVARCHAR(255),
    email         NVARCHAR(100) UNIQUE NOT NULL,
    provider      NVARCHAR(20) DEFAULT 'LOCAL', -- 'LOCAL' hoặc 'GOOGLE'
    provider_id   NVARCHAR(255),                -- Lưu ID của Google
    full_name     NVARCHAR(100),                -- Lưu tên hiển thị
    phone_number  NVARCHAR(15),                 -- Số điện thoại
    address       NVARCHAR(MAX),                -- Địa chỉ
    active        BIT NOT NULL DEFAULT 1,       -- 1 = Hoạt động, 0 = Bị khóa
    role_group_id BIGINT FOREIGN KEY REFERENCES RoleGroups(id) NULL
);

-- 4. Bảng trung gian RoleGroup - Roles
CREATE TABLE role_group_roles (
    role_group_id BIGINT NOT NULL FOREIGN KEY REFERENCES RoleGroups(id),
    role_id       BIGINT NOT NULL FOREIGN KEY REFERENCES Roles(id),
    PRIMARY KEY (role_group_id, role_id)
);

-- 5. Bảng trung gian User - Roles (Quyền riêng)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL FOREIGN KEY REFERENCES Users(id),
    role_id BIGINT NOT NULL FOREIGN KEY REFERENCES Roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- ============================================================
-- PHẦN II: SẢN PHẨM & ĐƠN HÀNG
-- ============================================================

-- Bảng Sản phẩm
CREATE TABLE Products
(
    id          BIGINT IDENTITY (1,1) PRIMARY KEY,
    name        NVARCHAR(100) NOT NULL,
    brand       NVARCHAR(50),
    sport       NVARCHAR(50),
    category    NVARCHAR(50),
    price       DECIMAL(18, 2) CHECK (price >= 0),
    image_url   NVARCHAR(MAX),
    description NVARCHAR(MAX),
    view_count  INT DEFAULT 0
);

CREATE TABLE Sizes (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(10) NOT NULL
);

CREATE TABLE ProductVariants (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT FOREIGN KEY REFERENCES Products(id),
    size_id BIGINT FOREIGN KEY REFERENCES Sizes(id),
    quantity INT DEFAULT 0,
    sku NVARCHAR(50) UNIQUE,
    image_url NVARCHAR(MAX)
);

CREATE TABLE Carts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT FOREIGN KEY REFERENCES Users(id)
);

CREATE TABLE CartItems (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    cart_id BIGINT FOREIGN KEY REFERENCES Carts(id),
    product_variant_id BIGINT FOREIGN KEY REFERENCES ProductVariants(id),
    quantity INT DEFAULT 1
);

CREATE TABLE Orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT FOREIGN KEY REFERENCES Users(id),
    full_name NVARCHAR(100),
    phone_number NVARCHAR(15),
    address NVARCHAR(MAX),
    total_money DECIMAL(18, 2),
    status NVARCHAR(20) DEFAULT 'PENDING',
    payment_method NVARCHAR(20),
    created_at DATETIME DEFAULT GETDATE()
);

CREATE TABLE OrderDetails (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT FOREIGN KEY REFERENCES Orders(id),
    product_id BIGINT,
    product_name NVARCHAR(100),
    size NVARCHAR(50),
    price DECIMAL(18, 2),
    quantity INT,
    total_price DECIMAL(18, 2)
);

CREATE TABLE Reviews (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT FOREIGN KEY REFERENCES Users(id),
    product_id BIGINT FOREIGN KEY REFERENCES Products(id) ON DELETE CASCADE,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    comment NVARCHAR(MAX),
    created_at DATETIME DEFAULT GETDATE()
);

ALTER TABLE Carts ADD token VARCHAR(255) NULL;

-- ============================================================
-- PHẦN III: SEED DỮ LIỆU MẪU
-- ============================================================

-- DỮ LIỆU RBAC ---
INSERT INTO Roles (name, description) VALUES
    ('USER',  N'Khách hàng thông thường'),
    ('ADMIN', N'Quản trị viên hệ thống'),
    ('BOTH',  N'Vừa là quản trị, vừa là khách hàng');

INSERT INTO RoleGroups (name, description) VALUES
    (N'Khách hàng',    N'Chức danh mặc định cho khách hàng thông thường'),
    (N'Quản trị viên', N'Chức danh cho nhân viên quản trị hệ thống');

-- Gán quyền cho Chức danh
-- Khách hàng -> USER
INSERT INTO role_group_roles (role_group_id, role_id) VALUES (1, 1);
-- Quản trị viên -> ADMIN & BOTH
INSERT INTO role_group_roles (role_group_id, role_id) VALUES (2, 2);
INSERT INTO role_group_roles (role_group_id, role_id) VALUES (2, 3);

-- Tạo 2 User mẫu (Password mặc định: 123 - sẽ cần chạy BCrypt nếu muốn đăng nhập)
-- Xin lưu ý: Password thật sự nên tạo trên web qua màn hình đăng ký để được Mã hóa.
-- Admin
INSERT INTO Users (username, email, password, full_name, active, role_group_id)
VALUES ('admin', 'admin@thexuong.com', '$2a$10$X8...bcrypt_hash...', N'Quản Trị Viên Nhóm', 1, 2);

-- Khách hàng
INSERT INTO Users (username, email, password, full_name, active, role_group_id)
VALUES ('user1', 'khachhang@thexuong.com', '$2a$10$X8H.2U0.w6sD2bU.FMyfLe2I9w8.2ZROm/.wT4i2vQJ7QySjK/J6G', N'Khách Hàng VIP', 1, 1);

-- Khách hàng kiêm chức năng Quản trị (Quyền BOTH) để test
INSERT INTO Users (username, email, password, full_name, active, role_group_id)
VALUES ('bothtester', 'both@thexuong.com', '$2a$10$X8H.2U0.w6sD2bU.FMyfLe2I9w8.2ZROm/.wT4i2vQJ7QySjK/J6G', N'Người Dùng Test', 1, NULL);

-- Gán Role trực tiếp cho User (theo mô hình mới)
-- Admin -> Có quyền BOTH
INSERT INTO user_roles (user_id, role_id) VALUES (1, 3);
-- User1 -> Có quyền USER
INSERT INTO user_roles (user_id, role_id) VALUES (2, 1);
-- bothtester -> Có quyền BOTH
INSERT INTO user_roles (user_id, role_id) VALUES (3, 3);


-- DỮ LIỆU SẢN PHẨM ---
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

INSERT INTO Sizes (name)
VALUES
    ('S'), ('M'), ('L'), ('XL'),
    ('36'), ('37'), ('38'), ('39'), ('40'), ('41'), ('42'), ('43'),
    ('FreeSize');

INSERT INTO ProductVariants (product_id, size_id, quantity, sku)
SELECT p.id, s.id, 100, CONCAT('SKU-', p.id, '-', s.name)
FROM Products p CROSS JOIN Sizes s
WHERE p.category IN (N'Áo', N'Quần áo') AND s.name IN ('S', 'M', 'L', 'XL');

INSERT INTO ProductVariants (product_id, size_id, quantity, sku)
SELECT p.id, s.id, 50, CONCAT('SKU-', p.id, '-', s.name)
FROM Products p CROSS JOIN Sizes s
WHERE p.category = N'Giày' AND s.name IN ('36', '37', '38', '39', '40', '41', '42', '43');

INSERT INTO ProductVariants (product_id, size_id, quantity, sku)
SELECT p.id, s.id, 200, CONCAT('SKU-', p.id, '-FREE')
FROM Products p CROSS JOIN Sizes s
WHERE p.category NOT IN (N'Áo', N'Quần áo', N'Giày') AND s.name = 'FreeSize';

    ALTER TABLE OrderDetails ADD size NVARCHAR(50);
