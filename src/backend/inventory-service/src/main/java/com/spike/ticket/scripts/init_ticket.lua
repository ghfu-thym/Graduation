-- KEYS: Danh sách các Key kho vé (VD: {"ticket:category:1:stock", "ticket:category:2:stock"})
-- ARGV: Danh sách số lượng vé ban đầu tương ứng (VD: {"500", "1000"})

local initialized_count = 0

for i = 1, #KEYS do
    local quantity = tonumber(ARGV[i])

    -- SETNX trả về 1 nếu Key chưa tồn tại và được set thành công.
    -- Trả về 0 nếu Key ĐÃ TỒN TẠI (Lúc này Redis sẽ bỏ qua, không ghi đè giá trị cũ)
    local result = redis.call('SETNX', KEYS[i], quantity)

    if result == 1 then
        initialized_count = initialized_count + 1
    end
end

-- Trả về số lượng các loại vé đã được khởi tạo thành công mới
return initialized_count