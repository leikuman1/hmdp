-- 1. 参数
local voucherId = ARGV[1]
local userId = ARGV[2]

-- 2. key
local stockKey = 'seckill:stock:' .. voucherId
local orderKey = 'seckill:order:' .. voucherId

-- 3. 只有预占成功过才回滚，避免重复回滚导致库存加多
if(redis.call('sismember', orderKey, userId) == 0) then
    return 0
end

redis.call('incrby', stockKey, 1)
redis.call('srem', orderKey, userId)
return 1
