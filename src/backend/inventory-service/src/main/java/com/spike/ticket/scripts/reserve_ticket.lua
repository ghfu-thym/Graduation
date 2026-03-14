-- KEYS: Danh sách các Key kho vé (VD: {"ticket:category:1:stock", "ticket:category:2:stock"})
-- ARGV: Danh sách số lượng vé khách muốn mua tương ứng (VD: {"2", "1"})

-- ==========================================
-- PHASE 1: KIỂM TRA TẤT CẢ (Check-all)
-- Đảm bảo nguyên tắc All-or-Nothing
-- ==========================================
for i = 1, #KEYS do
    -- Lấy số lượng tồn kho hiện tại trên Redis
    local current_stock = tonumber(redis.call('GET', KEYS[i]))
    local requested_quantity = tonumber(ARGV[i])

    -- Kiểm tra 3 trường hợp:
    -- 1. Key không tồn tại (Kho chưa mở / Lỗi)
    -- 2. Số lượng hiện tại bằng 0 (Hết vé)
    -- 3. Số lượng hiện tại < Số lượng muốn mua (Không đủ vé)
    if current_stock == nil or current_stock < requested_quantity then
        return i -- return index của loại vé ko đủ số lượng
    end
end

-- ==========================================
-- PHASE 2: CHỐT TRỪ KHO (Deduct)
-- Chỉ chạy khi Phase 1 đã pass 100%
-- ==========================================
for i = 1, #KEYS do
    local requested_quantity = tonumber(ARGV[i])
    -- Trừ lùi số lượng trong Redis
    redis.call('DECRBY', KEYS[i], requested_quantity)
end

return 0 -- Thành công giữ chỗ!

return 1 -- Thành công giữ chỗ!