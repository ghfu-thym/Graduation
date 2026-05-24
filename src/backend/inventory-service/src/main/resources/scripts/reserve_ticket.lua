-- KEYS: Danh sách các Key kho vé (VD: {"ticket:category:1:stock", "ticket:category:2:stock"})
-- ARGV: Danh sách số lượng vé khách muốn mua tương ứng (VD: {"2", "1"})

for i = 1, #KEYS do

    local current_stock = tonumber(redis.call('GET', KEYS[i]))
    local requested_quantity = tonumber(ARGV[i])

    -- return -1 neu
    --  key không tồn tại
    --  số lượng hiện tại < số lượng muốn mua
    if current_stock == nil or current_stock < requested_quantity then
        return tostring(-i) -- return index của loại vé ko đủ số lượng
    end
end


for i = 1, #KEYS do
    local requested_quantity = tonumber(ARGV[i])

    redis.call('DECRBY', KEYS[i], requested_quantity)
end


return "1" -- thành công giữ chỗ!