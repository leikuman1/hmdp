package com.hmdp.hmdp_server.mq;

import com.hmdp.hmdp_common.config.RabbitMQConfig;
import com.hmdp.hmdp_pojo.entity.VoucherOrder;
import com.hmdp.hmdp_server.service.IVoucherOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
@Slf4j
public class SeckillOrderConsumer {

    @Resource
    private IVoucherOrderService voucherOrderService;

    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE)
    public void onMessage(VoucherOrder voucherOrder, Channel channel, Message message) throws IOException {
        // deliveryTag 是 RabbitMQ 在当前 Channel 内部唯一的消息标识，ACK/NACK 都依赖它。
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        // redelivered=true 表示这条消息至少被投递过一次（曾经消费失败并重回队列）。
        boolean redelivered = message.getMessageProperties().getRedelivered();

        try {
            // 1) 正常业务处理：创建订单（service 上有事务注解）
            voucherOrderService.createVoucherOrder(voucherOrder);
            // 2) 处理成功后手动 ACK，告知 Broker 可以删除该消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 失败重试策略（避免无限重试）：
            // - 首次失败(redelivered=false)：NACK 并 requeue=true，允许再尝试一次
            // - 再次失败(redelivered=true)：REJECT 且 requeue=false，停止重试
            //   如果后续给队列配置了死信交换机(DLX)，这里会自动进入死信队列，便于排查与补偿。
            if (!redelivered) {
                log.error("消费秒杀订单失败，准备重试一次, deliveryTag={}, order={}", deliveryTag, voucherOrder, e);
                channel.basicNack(deliveryTag, false, true);
                return;
            }

            log.error("消费秒杀订单二次失败，停止重试, deliveryTag={}, order={}", deliveryTag, voucherOrder, e);
            channel.basicReject(deliveryTag, false);
        }
    }
}
