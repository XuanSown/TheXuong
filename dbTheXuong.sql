-- Tạo Database
CREATE
DATABASE dbTheXuong;
GO
USE dbTheXuong;
GO

select *
from Users
select *
from Products

-- Bảng User
CREATE TABLE Users
(
    id       BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) UNIQUE NOT NULL,
    password NVARCHAR(255),                -- Nullable nếu login bằng Google
    email    NVARCHAR(100) UNIQUE NOT NULL,
    provider NVARCHAR(20) DEFAULT 'LOCAL', -- LOCAL hoặc GOOGLE
    role     NVARCHAR(20) DEFAULT 'USER'
);

-- Bảng Sản phẩm (Đồ thể thao)
CREATE TABLE Products
(
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    name        NVARCHAR(100) NOT NULL,
    category    NVARCHAR(50), -- Giày, Quần áo, Phụ kiện
    price       DECIMAL(18, 2),
    image_url   NVARCHAR(255),
    description NVARCHAR(MAX)
);



-- Data mẫu (Style Decathlon/Puma)
    INSERT
INTO Products (name, category, price, image_url, description)
VALUES
    (N'Giày Chạy Bộ Puma Nitro', N'Giày', 2500000, 'https://example.com/shoe1.jpg', N'Đệm êm, phản hồi lực tốt'),
    (N'Áo Thun Thể Thao Dry-Fit', N'Quần áo', 350000, 'https://example.com/shirt1.jpg', N'Thoáng khí, thấm hút mồ hôi'),
    (N'Bóng Đá Số 5 FIFA Quality', N'Phụ kiện', 550000, 'https://example.com/ball1.jpg', N'Tiêu chuẩn thi đấu chuyên nghiệp');

UPDATE Products
SET image_url = 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?q=80&w=600&auto=format&fit=crop'
WHERE name = N'Giày Chạy Bộ Puma Nitro';

UPDATE Products
SET image_url = 'https://images.unsplash.com/photo-1581655353564-df123a1eb820?q=80&w=600&auto=format&fit=crop'
WHERE name = N'Áo Thun Thể Thao Dry-Fit';

UPDATE Products
SET image_url = 'https://images.unsplash.com/photo-1614632537190-23e4146777db?q=80&w=600&auto=format&fit=crop'
WHERE name = N'Bóng Đá Số 5 FIFA Quality';

-- 2. THÊM 5 SẢN PHẨM MỚI
INSERT INTO Products (name, category, price, image_url, description) VALUES
(N'Găng Tay Thủ Môn Predator', N'Phụ kiện', 850000, 'https://images.unsplash.com/photo-1592657788533-314643b17926?q=80&w=600&auto=format&fit=crop', N'Độ bám dính cực tốt, bảo vệ ngón tay'),
(N'Thảm Tập Yoga Định Tuyến', N'Phụ kiện', 450000, 'https://images.unsplash.com/photo-1599447292180-45fd84092efd?q=80&w=600&auto=format&fit=crop', N'Chống trượt, chất liệu TPE an toàn'),
(N'Tạ Tay Đơn 5kg (Bọc Cao Su)', N'Dụng cụ', 120000, 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?q=80&w=600&auto=format&fit=crop', N'Lõi gang đúc nguyên khối, bọc cao su êm ái'),
(N'Giày Bóng Rổ Jordan One', N'Giày', 3200000, 'https://images.unsplash.com/photo-1579338559194-a162d8417876?q=80&w=600&auto=format&fit=crop', N'Huyền thoại trở lại, đệm khí Air Zoom'),
(N'Bình Nước Thể Thao 1L', N'Phụ kiện', 99000, 'https://images.unsplash.com/photo-1602143407151-11115cd30c4e?q=80&w=600&auto=format&fit=crop', N'Nhựa BPA Free, chịu nhiệt tốt');

-- Xem lại kết quả
SELECT * FROM Products;

-- 1. Sửa lỗi Găng Tay Thủ Môn
UPDATE Products
SET image_url = 'https://images.unsplash.com/photo-1631527459158-450f61d5b3c8?auto=format&fit=crop&w=600&q=80'
WHERE name = N'Găng Tay Thủ Môn Predator';

-- 2. Sửa lỗi Thảm Tập Yoga
UPDATE Products
SET image_url = 'https://images.unsplash.com/photo-1593164842264-85460449a6a0?auto=format&fit=crop&w=600&q=80'
WHERE name = N'Thảm Tập Yoga Định Tuyến';

-- 3. Sửa lỗi Giày Bóng Rổ Jordan
UPDATE Products
SET image_url = 'https://images.unsplash.com/photo-1552346154-21d32810aba3?auto=format&fit=crop&w=600&q=80'
WHERE name = N'Giày Bóng Rổ Jordan One';

-- 4. Sửa lỗi Bình Nước
UPDATE Products
SET image_url = 'https://images.unsplash.com/photo-1602143407151-11115cd30c4e?auto=format&fit=crop&w=600&q=80'
WHERE name = N'Bình Nước Thể Thao 1L';
