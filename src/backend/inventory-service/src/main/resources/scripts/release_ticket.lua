-- KEYS: Danh sách các Key kho vé (VD: {"ticket:category:1:stock", "ticket:category:2:stock"})
-- ARGV: Danh sách số lượng vé khách bị hủy tương ứng (VD: {"2", "1"})

for i = 1, #KEYS do
    local quantity_to_return = tonumber(ARGV[i])
    -- Cộng trả lại số lượng vào Redis
    redis.call('INCRBY', KEYS[i], quantity_to_return)
end

return 1 -- Hoàn thành nhả vé