package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OperatorOrderOverviewVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    void paySuccess(String outTradeNo);

    PageResult pageQuery4User(int page, int pageSize, Integer status);

    /**
     * Query orders assigned to the current volunteer user.
     */
    PageResult pageQuery4Volunteer(int page, int pageSize, Integer status);

    OrderVO details(Long id);

    void userCancelById(Long id) throws Exception;

    void repetition(Long id);

    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    PageResult operatorBoard(OperatorOrderBoardQueryDTO queryDTO);

    OrderStatisticsVO statistics();

    OperatorOrderOverviewVO operatorOverview();

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception;

    void delivery(Long id);

    void complete(Long id);

    void reminder(Long id);

    void dispatch(Long id, Long volunteerId);

    void startPreparing(Long id);

    void assignVolunteer(OperatorAssignVolunteerDTO assignVolunteerDTO);

    void markMealReady(Long id);

    void confirmPickup(Long id);

    /**
     * Volunteer confirms the order has been picked up from the dining point.
     */
    void volunteerConfirmPickup(Long id);

    /**
     * Volunteer confirms the order has been delivered.
     */
    void volunteerComplete(Long id);
}
