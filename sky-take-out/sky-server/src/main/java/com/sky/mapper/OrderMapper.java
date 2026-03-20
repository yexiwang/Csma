package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param orders 订单
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber 订单号
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders 订单
     */
    void update(Orders orders);

    /**
     * 支付成功后更新订单状态
     */
    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} " +
            "where number = #{orderNumber}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, String orderNumber);

    /**
     * 分页查询订单
     * @param ordersPageQueryDTO 查询参数
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据ID查询订单
     * @param id 订单ID
     */
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    /**
     * 根据状态统计订单数量
     * @param status 订单状态
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);

    /**
     * 统计老人关联订单数量
     * @param elderId 老人ID
     */
    Integer countByElderId(Long elderId);

    /**
     * 根据订单状态和下单时间查询订单
     */
    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getByStatusAndTimeLT(Integer status, LocalDateTime time);

    /**
     * 根据动态条件统计金额
     */
    Double sumByMap(Map map);

    /**
     * 根据动态条件统计订单数量
     */
    Integer countByMap(Map map);

    /**
     * 统计指定时间区间内销量 top10
     */
    Integer countOverdueByStatus(Integer status, LocalDateTime cutoffTime);

    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}
