-- Admin User
INSERT IGNORE INTO users (username, email, password, role)
VALUES
('admin', 'admin@equestria.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN');

-- Sample Horses
INSERT IGNORE INTO horses
(id, name, breed, age, speed, owner, status, notes)
VALUES
(1, 'Thunderbolt', 'Thoroughbred', 5, 72.5, 'Trần Minh', 'active', 'Ngựa đua hàng đầu, đã thắng 12 giải'),
(2, 'Storm Runner', 'Arabian', 4, 68.3, 'Nguyễn Văn Hùng', 'active', 'Ngựa trẻ tiềm năng'),
(3, 'Golden Wind', 'Quarter Horse', 6, 70.1, 'Lê Thị Mai', 'active', 'Chuyên gia đường ngắn'),
(4, 'Dark Shadow', 'Thoroughbred', 7, 69.8, 'Phạm Đức Anh', 'rest', 'Đang nghỉ ngơi sau mùa giải'),
(5, 'Fire Phoenix', 'Arabian', 3, 65.2, 'Hoàng Minh Tuấn', 'active', 'Ngựa mới, đang huấn luyện'),
(6, 'Silver Arrow', 'Standardbred', 5, 67.9, 'Võ Thanh Hải', 'injured', 'Chấn thương nhẹ ở chân trước'),
(7, 'Royal Knight', 'Thoroughbred', 4, 71.0, 'Đặng Quốc Bảo', 'active', 'Ứng viên vô địch mùa này'),
(8, 'Wind Dancer', 'Arabian', 6, 66.5, 'Bùi Thị Lan', 'active', 'Ngựa bền bỉ, phù hợp đường dài');

-- Sample Jockeys
INSERT IGNORE INTO jockeys
(id, name, country, wins, races, bio)
VALUES
(1, 'Nguyễn Văn An', 'Việt Nam', 45, 120, 'Kỵ sĩ hàng đầu Việt Nam với 15 năm kinh nghiệm'),
(2, 'Trần Đức Mạnh', 'Việt Nam', 32, 98, 'Chuyên gia đường ngắn, kỹ thuật xuất sắc'),
(3, 'Lê Hoàng Nam', 'Việt Nam', 28, 85, 'Kỵ sĩ trẻ tài năng, đang lên'),
(4, 'Phạm Quốc Huy', 'Việt Nam', 51, 150, 'Kỵ sĩ kỳ cựu, nhiều kinh nghiệm quốc tế'),
(5, 'John Smith', 'Australia', 67, 200, 'Kỵ sĩ quốc tế, từng vô địch Melbourne Cup'),
(6, 'Tanaka Kenji', 'Japan', 38, 110, 'Kỵ sĩ Nhật Bản, chuyên gia đường dài');

-- Sample Schedules
INSERT IGNORE INTO schedules
(id, name, race_date, venue, horse_count, prize, status)
VALUES
(1, 'Royal Cup Championship 2026', '2026-06-15 14:00:00', 'Phú Thọ Racetrack, HCM', 8, '₫ 500,000,000', 'upcoming'),
(2, 'Saigon Grand Prix', '2026-06-22 15:00:00', 'Phú Thọ Racetrack, HCM', 6, '₫ 300,000,000', 'upcoming'),
(3, 'Mekong Delta Derby', '2026-07-01 14:30:00', 'Cần Thơ Racetrack', 7, '₫ 200,000,000', 'upcoming'),
(4, 'Hanoi Classic', '2026-07-10 13:00:00', 'Hà Nội Racetrack', 8, '₫ 400,000,000', 'upcoming'),
(5, 'Spring Festival Race 2026', '2026-05-20 14:00:00', 'Phú Thọ Racetrack, HCM', 6, '₫ 250,000,000', 'done');

-- Sample Results
INSERT IGNORE INTO results
(id, race_name, race_date, venue, winner, jockey, time, prize)
VALUES
(1, 'Royal Cup Championship 2025', '2025-06-15 14:00:00', 'Phú Thọ Racetrack, HCM', 'Thunderbolt', 'Nguyễn Văn An', '1:42.35', '₫ 500,000,000'),
(2, 'Saigon Grand Prix 2025', '2025-06-22 15:00:00', 'Phú Thọ Racetrack, HCM', 'Golden Wind', 'Trần Đức Mạnh', '1:38.92', '₫ 300,000,000'),
(3, 'Mekong Delta Derby 2025', '2025-07-01 14:30:00', 'Cần Thơ Racetrack', 'Storm Runner', 'Lê Hoàng Nam', '1:45.18', '₫ 200,000,000'),
(4, 'Spring Festival Race 2026', '2026-05-20 14:00:00', 'Phú Thọ Racetrack, HCM', 'Royal Knight', 'Phạm Quốc Huy', '1:40.55', '₫ 250,000,000');