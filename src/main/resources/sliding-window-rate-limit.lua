local key = KEYS[1]
local now = tonumber(ARGV[1])
local windowMillis = tonumber(ARGV[2])
local maxRequests = tonumber(ARGV[3])
local requestId = ARGV[4]

local windowStart = now - windowMillis

redis.call('ZREMRANGEBYSCORE', key, 0, windowStart)

local current = redis.call('ZCARD', key)
if current >= maxRequests then
    return 0
end

redis.call('ZADD', key, now, requestId)
redis.call('PEXPIRE', key, windowMillis)
return 1
