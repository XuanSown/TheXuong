-- Tạo Database
CREATE DATABASE dbTheXuong;
GO
USE dbTheXuong;
GO

-- Bảng User
CREATE TABLE Users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) UNIQUE NOT NULL,
    password NVARCHAR(255), -- Nullable nếu login bằng Google
    email NVARCHAR(100) UNIQUE NOT NULL,
    provider NVARCHAR(20) DEFAULT 'LOCAL', -- LOCAL hoặc GOOGLE
    role NVARCHAR(20) DEFAULT 'USER'
);

-- Bảng Sản phẩm (Đồ thể thao)
CREATE TABLE Products (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    category NVARCHAR(50), -- Giày, Quần áo, Phụ kiện
    price DECIMAL(18, 2),
    image_url NVARCHAR(255),
    description NVARCHAR(MAX)
);

select * from Users
select * from Products

-- Data mẫu (Style Decathlon/Puma)
INSERT INTO Products (name, category, price, image_url, description) VALUES
(N'Giày Chạy Bộ Puma Nitro', N'Giày', 2500000, 'https://example.com/shoe1.jpg', N'Đệm êm, phản hồi lực tốt'),
(N'Áo Thun Thể Thao Dry-Fit', N'Quần áo', 350000, 'https://example.com/shirt1.jpg', N'Thoáng khí, thấm hút mồ hôi'),
(N'Bóng Đá Số 5 FIFA Quality', N'Phụ kiện', 550000, 'https://example.com/ball1.jpg', N'Tiêu chuẩn thi đấu chuyên nghiệp');