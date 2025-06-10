SET NAMES utf8mb4;


-- 役職データ
INSERT IGNORE INTO roles (name) VALUES ('ROLE_行政'), ('ROLE_町内会長'), ('ROLE_委員長'), ('ROLE_委員'), ('ROLE_区長'), ('ROLE_会員');


-- 支払方法データ
INSERT IGNORE INTO payment_methods (id, name) VALUES (1, 'クレジットカード'), (2, '銀行振込'), (3, 'QR決済'),(4, '現金決済');


-- 町内会データ
INSERT IGNORE INTO districts (id, name, region_code, association, area, description, created_at, updated_at) VALUES
(1, '旭町', 'RC001', '旭福住町内会', '1区', '都心部の中心地域', NOW(), NOW()),
(2, '旭町', 'RC001', '旭福住町内会', '2区', '都心部の中心地域', NOW(), NOW()),
(3, '旭町', 'RC001', '旭福住町内会', '3区', '都心部の中心地域', NOW(), NOW()),
(4, '福住町', 'RC001', '旭福住町内会', '4区', '静かな住宅街', NOW(), NOW()),
(5, '福住町', 'RC001', '旭福住町内会', '5区', '静かな住宅街', NOW(), NOW()),
(6, '福住町', 'RC001', '旭福住町内会', '6区', '静かな住宅街', NOW(), NOW()),
(7, '黄金町', 'RC003', '黄金蜂町内会', '北区', '商業施設が多い地域', NOW(), NOW()),
(8, '福町', 'RC004', '福町町内会', '西区', '公園が豊富なエリア', NOW(), NOW()),
(9, '蜂町', 'RC005', '黄金蜂町内会', '東区', '新興住宅地', NOW(), NOW()),
(10, '御徒町', 'RC001', '旭福住町内会', '7区', '静かな住宅街', NOW(), NOW()),
(11, '駿河町', 'RC001', '旭福住町内会', '8区', '商業施設が多い地域', NOW(), NOW()),
(12, '馬町', 'RC001', '旭福住町内会', '9区', '公園が豊富なエリア', NOW(), NOW()),
(13, '人形町', 'RC001', '旭福住町内会', '10区', '新興住宅地', NOW(), NOW()),
(14, '阿曽町', 'RC001', '阿曽合同町内会', '8区', '商業施設が多い地域', NOW(), NOW()),
(15, '桧町', 'RC001', '阿曽合同町内会', '9区', '公園が豊富なエリア', NOW(), NOW()),
(16, '有珠町', 'RC001', '阿曽合同町内会', '8区', '商業施設が多い地域', NOW(), NOW()),
(17, '柊町', 'RC001', '阿曽合同町内会', '9区', '公園が豊富なエリア', NOW(), NOW());

 -- 委員データ
 INSERT IGNORE INTO committee_classification (id, district_id, name)
VALUES 
(1, 1, '体育委員'),
(2, 1, '保健委員'),
(3, 1, '防犯委員');

-- ユーザーデータ
INSERT IGNORE INTO users (email, user_id, password, name, furigana, phone_number, postal_code, address, district_id, role_id, enabled) 
VALUES
('admin@example.com', 'admin001', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '管理者', 'カンリシャ', '080-1111-2222', '100-0001', '東京都千代田区', 1, 2, true),
('mayor@example.com', 'mayor001', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '町内会長', 'チョウナイカイチョウ', '080-2222-3333', '100-0002', '東京都新宿区', 1, 2, true),
('districthead@example.com', 'districthead001', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '区長', 'クチョウ', '080-5555-6666', '100-0005', '東京都品川区', 1, 5, true),
('resident@example.com', 'resident001', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '会員', 'カイイン', '080-6666-7777', '100-0006', '東京都目黒区', 1, 6, true),
('administration@example.com', 'administration001', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '行政', 'ギョウセイ', '080-6666-7777', '100-0006', '東京都目黒区', 1, 1, true),
('user01@example.com', 'user01', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '田中角栄', 'タナカカクエイ', '080-6666-7777', '100-0006', '東京都目黒区', 1, 6, true),
('user02@example.com', 'user02', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '田中真紀子', 'タナカマキコ', '080-6666-7777', '100-0006', '東京都目黒区', 1, 6, true),
('user03@example.com', 'user03', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '伊藤博文', 'イトウヒロフミ', '080-6666-7777', '100-0006', '東京都目黒区', 2, 6, true),
('user05@example.com', 'user05', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '坂本龍馬', 'サカモトリョウマ', '080-6666-7777', '100-0006', '東京都目黒区', 4, 6, true),
('user06@example.com', 'user06', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '桂小五郎', 'カツラコゴロウ', '080-6666-7777', '100-0006', '東京都目黒区', 4, 6, true),
('resident33@example.com', 'resident33001', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '削除予定', 'カイイン', '080-6666-7777', '100-0006', '東京都目黒区', 1, 6, true),
('user04@example.com', 'user04', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '大久保利通', 'オオクボトシミチ', '080-6666-7777', '100-0006', '東京都目黒区', 2, 6, true),
('user99@example.com', 'user99', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '福島正則', 'フクシママサノリ', '080-6666-7777', '100-0006', '東京都目黒区', 7, 6, true);

-- ユーザーデータ
INSERT IGNORE INTO users (email, user_id, password, name, furigana, phone_number, postal_code, address, district_id, role_id, committee_id, enabled) 
VALUES
('chairman@example.com', 'chairman001', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '委員長', 'イインチョウ', '080-3333-4444', '100-0003', '東京都渋谷区', 1, 3, 1, true),
('member@example.com', 'member001', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '委員', 'イイン', '080-4444-5555', '100-0004', '東京都港区', 1, 4, 1, true),
('chairman02@example.com', 'chairman002', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '久坂玄瑞', 'クサカケンズイ', '080-3333-4444', '100-0003', '東京都渋谷区', 1, 3, 2, true),
('member02@example.com', 'member002', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '井伊直弼', 'イイナオスケ', '080-4444-5555', '100-0004', '東京都港区', 1, 4, 2, true),
('chairman03@example.com', 'chairman003', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '坂上田村麻呂', '坂上田村麻呂', '080-3333-4444', '100-0003', '東京都渋谷区', 1, 3, 3, true),
('member03@example.com', 'member003', '$2a$10$HjOxJNNsIR4T3CtCjus4G.8pezyuZqdiIHAlD3AFjevozQJ.Up9aS', '徳川家光', '徳川家光', '080-4444-5555', '100-0004', '東京都港区', 1, 4, 3, true);

-- 回覧板データを追加（仮のデータ）
INSERT IGNORE INTO circulars (id, name, description, author_id, district_id ,created_at, updated_at, is_urgent) VALUES
(1, '防災訓練のお知らせ', '来月の防災訓練の詳細です。参加をお願いします。', 1, 2, NOW(), NOW(), FALSE),
(2, '町内清掃活動', '今週末に町内清掃を行います。軍手を持参してください。', 1, 2, NOW(), NOW(), FALSE),
(3, '夏祭り開催決定！', '今年の夏祭りの開催日が決まりました！詳細は後日お知らせします。', 1, 2, NOW(), NOW(), TRUE),
(4, '工事のお知らせ！', '△公園にて水道工事が行われます。詳細は後日お知らせします。', 1, 2, NOW(), NOW(), TRUE),
(5, '250429_回覧板', 'ごみ収集日に変更があります。詳細は下記PDFをご覧ください', 1, 2, NOW(), NOW(), TRUE),
(6, '250429_回覧板！', '急遽工事を行うことになりました。詳細は下記PDFをご覧ください', 1, 2, NOW(), NOW(), TRUE);
-- 回覧板ファイルデータを追加（仮のデータ）
INSERT IGNORE INTO circular_files (circular_id, file_name, file_path, uploaded_at) VALUES
(1, '防災マニュアル.pdf', '/uploads/bousai_manual.pdf', NOW()),
(2, '清掃マップ.jpg', '/uploads/seisou_map.jpg', NOW()),
(3, '夏祭りポスター.png', '/uploads/natsu_matsuri_poster.png', NOW()),
(5, '250429_回覧板PDF.pdf', '/uploads/bousai_manual.pdf', NOW()),
(6, '工事のお知らせ.jpg', '/uploads/kouji_info.jpg', NOW()),
(6, '工事場所一覧.png', '/uploads/kouji_poster.png', NOW());

INSERT IGNORE INTO board_types (name) VALUES
('行政会長掲示板'),
('会長委員長掲示板'),
('委員長委員掲示板'),
('会長区長掲示板'),
('区長会員掲示板');

-- 掲示板のダミーデータ挿入

-- スレッド（親メッセージ）データ
INSERT IGNORE INTO messages (user_id, board_type_id, committee_type, title, comment, parent_id, status, created_at)
VALUES
(1, 1, NULL, '町内会イベントの開催について',
 REPEAT('これは町内会イベントに関するお知らせです。詳細は以下の通りです。', 40), -- 約1000文字
 NULL, '公開', NOW()),
(2, 2, NULL, 'ゴミ出しルールの変更について',
 REPEAT('今後のゴミ出しルールが変更になります。皆様のご理解とご協力をお願いします。', 40), -- 約1000文字
 NULL, '公開', NOW()),
(3, 3, NULL, '防災訓練の参加者募集',
 REPEAT('防災訓練を実施します。いざという時に備えましょう！', 40), -- 約1000文字
 NULL, '公開', NOW());

-- リプライ（子メッセージ）データ
INSERT IGNORE INTO messages (user_id, board_type_id, committee_type, title, comment, parent_id, status, created_at)
VALUES
(4, 1, NULL, 'Re: 町内会イベントの開催について',
 REPEAT('イベントの詳細を確認しました。とても楽しみです！', 15), -- 約400文字
 1, '公開', NOW()),
(5, 1, NULL, 'Re: 町内会イベントの開催について',
 REPEAT('家族で参加したいのですが、申し込み方法を教えてください。', 20), -- 約600文字
 1, '公開', NOW()),
(6, 2, NULL, 'Re: ゴミ出しルールの変更について',
 REPEAT('新しいルールを理解しました。問題なく対応できそうです。', 20), -- 約600文字
 2, '公開', NOW()),
(7, 3, NULL, 'Re: 防災訓練の参加者募集',
 REPEAT('防災訓練に参加します！具体的なスケジュールを教えてください。', 25), -- 約800文字
 3, '公開', NOW());

-- 会計カテゴリ
INSERT IGNORE INTO accounting_categories (id, name, created_at) VALUES
(1, '町内会費収入', NOW()),
(2, '寄付金収入', NOW()),
(3, '補助金収入', NOW()),
(4, 'イベント開催費', NOW()),
(5, '備品購入費', NOW()),
(6, '施設使用料', NOW()),
(7, '印刷費・通信費', NOW()),
(8, '保険料', NOW()),
(9, '雑費', NOW());


-- 会計データ
INSERT IGNORE INTO accounting (id, district_id, recorded_by, type, account_category_id, amount, description, transaction_date, created_at, updated_at)
VALUES 
(1, 1, 1, '収入', 1, 10000, '町内会費集金', '2025-04-01 10:00:00', NOW(), NOW()),
(2, 1, 1, '支出', 2, 5000, 'イベント用消耗品購入', '2025-04-02 15:00:00', NOW(), NOW()),
(3, 1, 1, '支出', 3, 3000, '掲示板メンテナンス費', '2025-04-05 09:30:00', NOW(), NOW()),
(4, 1, 1, '収入', 2, 2000, '寄付金（個人）', '2025-04-10 14:00:00', NOW(), NOW()),
(5, 1, 1, '支出', 7, 1200, '町内通信費', '2025-04-12 11:30:00', NOW(), NOW()),
(6, 1, 1, '支出', 5, 8000, '夏祭り用テント購入', '2025-04-15 16:45:00', NOW(), NOW()),
(7, 1, 1, '収入', 1, 15000, '町内会費 4月分', '2025-04-01 09:00:00', NOW(), NOW()),
(8, 1, 1, '支出', 8, 5000, 'イベント保険料支払い', '2025-04-18 13:15:00', NOW(), NOW()),
(9, 1, 1, '支出', 4, 3000, '町内イベント軽食費', '2025-04-20 18:00:00', NOW(), NOW()),
(10, 1, 1, '収入', 3, 8000, '市からの防災補助金', '2025-04-25 10:20:00', NOW(), NOW()),
(11, 1, 1, '支出', 6, 2000, '会議室使用料', '2025-04-28 14:00:00', NOW(), NOW()),
(12, 1, 1, '支出', 9, 700, '備品修理雑費', '2025-04-29 10:00:00', NOW(), NOW());

-- 5月分データ
INSERT IGNORE INTO accounting (id, district_id, recorded_by, type, account_category_id, amount, description, transaction_date, created_at, updated_at)
VALUES 
(13, 1, 1, '収入', 1, 12000, '町内会費 5月分', '2025-05-01 09:00:00', NOW(), NOW()),
(14, 1, 1, '支出', 2, 4500, '子供イベント景品購入', '2025-05-03 10:30:00', NOW(), NOW()),
(15, 1, 1, '収入', 3, 5000, '市補助金（防犯灯設置）', '2025-05-05 11:00:00', NOW(), NOW()),
(16, 1, 1, '支出', 5, 7000, '防犯灯設置費用', '2025-05-07 15:20:00', NOW(), NOW()),
(17, 1, 1, '支出', 7, 1500, '町内掲示板更新費', '2025-05-10 14:00:00', NOW(), NOW());

-- 6月分データ
INSERT IGNORE INTO accounting (id, district_id, recorded_by, type, account_category_id, amount, description, transaction_date, created_at, updated_at)
VALUES 
(18, 1, 1, '収入', 1, 13000, '町内会費 6月分', '2025-06-01 09:00:00', NOW(), NOW()),
(19, 1, 1, '支出', 4, 4000, '親睦会飲食代', '2025-06-05 12:00:00', NOW(), NOW()),
(20, 1, 1, '収入', 2, 2500, '寄付金（企業）', '2025-06-10 13:00:00', NOW(), NOW()),
(21, 1, 1, '支出', 8, 3500, '夏イベント保険料', '2025-06-12 15:00:00', NOW(), NOW());

-- 7月分データ
INSERT IGNORE INTO accounting (id, district_id, recorded_by, type, account_category_id, amount, description, transaction_date, created_at, updated_at)
VALUES 
(22, 1, 1, '収入', 1, 14000, '町内会費 7月分', '2025-07-01 09:00:00', NOW(), NOW()),
(23, 1, 1, '支出', 5, 10000, '夏祭り準備品購入', '2025-07-03 14:00:00', NOW(), NOW()),
(24, 1, 1, '収入', 3, 6000, '市補助金（防災訓練）', '2025-07-05 11:00:00', NOW(), NOW()),
(25, 1, 1, '支出', 2, 3500, '防災訓練用品購入', '2025-07-07 10:30:00', NOW(), NOW()),
(26, 1, 1, '支出', 4, 4500, '夏祭りポスター印刷代', '2025-07-10 16:00:00', NOW(), NOW()),
(27, 1, 1, '収入', 2, 3000, '寄付金（個人）', '2025-07-12 10:00:00', NOW(), NOW());

INSERT IGNORE INTO membership_fees (id, district_id, recorded_by, year, amount, created_at, updated_at)
VALUES
(1, 1, 2, 2025, 5000, now(), now());

