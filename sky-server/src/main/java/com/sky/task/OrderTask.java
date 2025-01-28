package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 自定义定时任务，实现订单状态定时处理
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;


    /**
     * 处理支付超时订单
     */
    @Scheduled(cron = "0 * * * * ?")  // 定时任务注解，每分钟执行一次
    public void processTimeoutOrder(){
        // 打印日志，表示开始处理支付超时订单
        log.info("处理支付超时订单：{}", new Date());

        // 获取当前时间，并计算出当前时间往前推15分钟的时间点
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        // 查询状态为“待支付”且支付时间早于当前时间15分钟前的订单
        // 使用了自定义的查询方法 getByStatusAndOrdertimeLT，传入参数：状态和时间
        List<Orders> ordersList = orderMapper.getByStatusAndOrdertimeLT(Orders.PENDING_PAYMENT, time);

        // 判断查询到的订单列表是否为空且包含数据
        if (ordersList != null && ordersList.size() > 0) {
            // 如果有超时订单，则遍历处理每一条
            ordersList.forEach(order -> {
                // 设置订单状态为“已取消”
                order.setStatus(Orders.CANCELLED);
                // 设置取消原因
                order.setCancelReason("支付超时，自动取消");
                // 设置取消时间为当前时间
                order.setCancelTime(LocalDateTime.now());
                // 更新订单状态到数据库
                orderMapper.update(order);
            });
        }
    }

    /**
     * 处理“派送中”状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")  // 定时任务注解，每天的 01:00 执行一次
    public void processDeliveryOrder(){
        // 打印日志，表示开始处理“派送中”订单
        log.info("处理派送中订单：{}", new Date());

        // 获取当前时间，并计算出当前时间往前推1小时的时间点
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        // 查询状态为“派送中”且派送时间早于当前时间1小时的订单
        List<Orders> ordersList = orderMapper.getByStatusAndOrdertimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        // 判断查询到的订单列表是否为空且包含数据
        if (ordersList != null && ordersList.size() > 0) {
            // 如果有超时派送订单，则遍历处理每一条
            ordersList.forEach(order -> {
                // 设置订单状态为“已完成”
                order.setStatus(Orders.COMPLETED);
                // 更新订单状态到数据库
                orderMapper.update(order);
            });
        }
    }


}