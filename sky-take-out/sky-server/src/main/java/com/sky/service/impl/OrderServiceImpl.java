package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OperatorAssignVolunteerDTO;
import com.sky.dto.OperatorOrderBoardQueryDTO;
import com.sky.dto.OrderReviewSubmitDTO;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.Dish;
import com.sky.entity.Elderly;
import com.sky.entity.OrderDetail;
import com.sky.entity.OrderReview;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.entity.User;
import com.sky.entity.DiningPoint;
import com.sky.entity.VolunteerStats;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.DiningPointMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.ElderlyMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.OrderReviewMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mapper.UserMapper;
import com.sky.mapper.VolunteerStatsMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.service.support.FamilyCheckoutCalculator;
import com.sky.service.support.VolunteerLevelSupport;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderReviewVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OperatorOrderOverviewVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.vo.ShoppingCartSummaryVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_OPERATOR = "OPERATOR";
    private static final String ROLE_FAMILY = "FAMILY";
    private static final String ROLE_VOLUNTEER = "VOLUNTEER";
    private static final String VIEW_PENDING_PREPARE = "PENDING_PREPARE";
    private static final String VIEW_PREPARING = "PREPARING";
    private static final String VIEW_PENDING_ASSIGNMENT = "PENDING_ASSIGNMENT";
    private static final String VIEW_MEAL_READY = "MEAL_READY";
    private static final String VIEW_DELIVERING = "DELIVERING";
    private static final String VIEW_COMPLETED = "COMPLETED";
    private static final String HANDOVER_PENDING_PREPARE = "待出餐";
    private static final String HANDOVER_PENDING_PICKUP = "待交接";
    private static final String HANDOVER_IN_DELIVERY = "已取餐";
    private static final String HANDOVER_UNASSIGNED = "未分配志愿者";

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderReviewMapper orderReviewMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private VolunteerStatsMapper volunteerStatsMapper;
    @Autowired
    private ElderlyMapper elderlyMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DiningPointMapper diningPointMapper;
    @Autowired
    private FamilyCheckoutCalculator familyCheckoutCalculator;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Value("${sky.baidu.ak}")
    private String ak;

    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        requireRole(ROLE_FAMILY);

        Long userId = BaseContext.getCurrentId();
        if (ordersSubmitDTO.getAddressBookId() == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null || !userId.equals(addressBook.getUserId())) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long elderId = ordersSubmitDTO.getElderId();
        if (elderId == null) {
            throw new OrderBusinessException("请选择服务老人");
        }
        Elderly submitElderly = elderlyMapper.getById(elderId);
        if (submitElderly == null) {
            throw new OrderBusinessException("服务老人不存在");
        }
        if (!userId.equals(submitElderly.getUserId())) {
            throw new OrderBusinessException("无权为该老人下单");
        }

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (CollectionUtils.isEmpty(shoppingCartList)) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        DiningPoint diningPoint = requireAvailableDiningPointForOrder(submitElderly);
        validateShoppingCartElderly(shoppingCartList, submitElderly.getId());
        validateShoppingCartItemsForOrder(shoppingCartList, diningPoint.getId());
        Long diningPointId = diningPoint.getId();
        ShoppingCartSummaryVO summary = familyCheckoutCalculator.calculate(
                submitElderly.getId(),
                diningPointId,
                shoppingCartList,
                ordersSubmitDTO.getTablewareStatus(),
                ordersSubmitDTO.getTablewareNumber()
        );
        validateSubmittedAmounts(ordersSubmitDTO, summary);
        String fullAddress = buildFullAddress(addressBook);
        // 社区助餐主流程当前不启用苍穹外卖遗留的地图距离校验，保留方法以备后续扩展。
        // checkOutOfRange(diningPoint.getAddress(), fullAddress);

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders, "packAmount", "tablewareNumber");
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(userId);
        orders.setElderId(submitElderly.getId());
        orders.setDiningPointId(diningPointId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPackAmount(0);
        orders.setTablewareNumber(ordersSubmitDTO.getTablewareNumber() == null ? 0 : ordersSubmitDTO.getTablewareNumber());
        orders.setExpectedTime(ordersSubmitDTO.getEstimatedDeliveryTime());
        orders.setEstimatedDeliveryTime(ordersSubmitDTO.getEstimatedDeliveryTime());
        orders.setDeliveryStatus(ordersSubmitDTO.getDeliveryStatus() == null ? 0 : ordersSubmitDTO.getDeliveryStatus());
        orders.setUserName(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(fullAddress);
        orders.setConsignee(addressBook.getConsignee());
        orders.setSubsidyAmount(summary.getSubsidyAmount());
        orders.setDeliveryFee(summary.getDeliveryFee());
        orders.setTablewareFee(summary.getTablewareFee());
        orders.setPersonalPay(summary.getPayAmount());
        orders.setAmount(summary.getPayAmount());

        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        shoppingCartMapper.deleteByUserId(userId);

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        requireRole(ROLE_FAMILY);
        Long userId = BaseContext.getCurrentId();
        String orderNumber = ordersPaymentDTO.getOrderNumber();
        Orders ordersDB = orderMapper.getByNumber(orderNumber);
        if (ordersDB == null || !userId.equals(ordersDB.getUserId())) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        assertStatus(ordersDB, Orders.PENDING_PAYMENT);
        requireAvailableDiningPointForPendingPayment(ordersDB);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        if (ordersPaymentDTO.getPayMethod() != null && !ordersPaymentDTO.getPayMethod().equals(ordersDB.getPayMethod())) {
            Orders payMethodUpdate = Orders.builder()
                    .id(ordersDB.getId())
                    .payMethod(ordersPaymentDTO.getPayMethod())
                    .build();
            orderMapper.update(payMethodUpdate);
        }

        log.info("模拟支付成功，按订单号更新订单状态: {}", orderNumber);
        paySuccess(orderNumber);

        return vo;
    }

    @Override
    public void paySuccess(String outTradeNo) {
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);
        if (ordersDB == null) {
            log.warn("忽略paySuccess，因为订单不存在, orderNumber={}", outTradeNo);
            return;
        }
        if (!Orders.PENDING_PAYMENT.equals(ordersDB.getStatus())) {
            log.warn("忽略paySuccess，因为订单不再处于待处理状态, orderNumber={}, status={}",
                    outTradeNo, ordersDB.getStatus());
            return;
        }
        if (!isDiningPointAvailableForPendingPayment(ordersDB)) {
            log.warn("忽略paySuccess，因为餐厅正在休息, orderNumber={}, orderId={}, diningPointId={}",
                    outTradeNo, ordersDB.getId(), ordersDB.getDiningPointId());
            return;
        }
        LocalDateTime checkoutTime = LocalDateTime.now();
        log.info("模拟paySuccess，更新订单状态为待配送, orderNumber={}, orderId={}", outTradeNo, ordersDB.getId());
        orderMapper.updateStatus(Orders.TO_BE_SCHEDULED, Orders.PAID, checkoutTime, outTradeNo);
        notifyOrderPaid(ordersDB.getId(), outTradeNo);
    }

    @Override
    public PageResult pageQuery4User(int pageNum, int pageSize, Integer status) {
        requireRole(ROLE_FAMILY);
        return pageQueryByScope(pageNum, pageSize, status);
    }

    @Override
    public PageResult pageQuery4Volunteer(int pageNum, int pageSize, Integer status) {
        Long volunteerId = requireVolunteerUserId();
        log.info("查询当前志愿者订单，VolunteerId={}, page={}, pageSize={}, status={}",
                volunteerId, pageNum, pageSize, status);
        return pageQueryByScope(pageNum, pageSize, status);
    }

    @Override
    public OrderVO details(Long id) {
        Orders orders = getAccessibleOrder(id);
        OrderVO orderVO = buildOrderVO(orders);
        attachReviewedFlag(orderVO);
        return orderVO;
    }

    @Override
    public void userCancelById(Long id) throws Exception {
        requireRole(ROLE_FAMILY);
        Orders ordersDB = getAccessibleOrder(id);
        assertStatusIn(ordersDB, Orders.PENDING_PAYMENT, Orders.TO_BE_SCHEDULED);

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        if (Orders.TO_BE_SCHEDULED.equals(ordersDB.getStatus())) {
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    @Transactional
    public void submitReview(OrderReviewSubmitDTO orderReviewSubmitDTO) {
        requireRole(ROLE_FAMILY);
        validateReviewSubmitDTO(orderReviewSubmitDTO);

        Orders orders = getAccessibleOrder(orderReviewSubmitDTO.getOrderId());
        assertStatus(orders, Orders.COMPLETED);

        if (orderReviewMapper.getByOrderId(orders.getId()) != null) {
            throw new OrderBusinessException("当前订单已评价");
        }

        User volunteer = requireReviewedVolunteer(orders);
        LocalDateTime now = LocalDateTime.now();
        OrderReview orderReview = OrderReview.builder()
                .orderId(orders.getId())
                .volunteerUserId(volunteer.getId())
                .familyUserId(requireCurrentUserId())
                .score(orderReviewSubmitDTO.getScore())
                .content(normalizeReviewContent(orderReviewSubmitDTO.getContent()))
                .createTime(now)
                .updateTime(now)
                .isDeleted(0)
                .build();

        try {
            orderReviewMapper.insert(orderReview);
        } catch (DuplicateKeyException duplicateKeyException) {
            throw new OrderBusinessException("当前订单已评价");
        }

        refreshVolunteerRating(volunteer.getId());
    }

    @Override
    public OrderReviewVO getReviewByOrderId(Long orderId) {
        requireRole(ROLE_FAMILY);
        Orders orders = getAccessibleOrder(orderId);
        OrderReview orderReview = orderReviewMapper.getByOrderId(orders.getId());
        if (orderReview == null) {
            return null;
        }
        return OrderReviewVO.builder()
                .orderId(orderReview.getOrderId())
                .score(orderReview.getScore())
                .content(orderReview.getContent())
                .createTime(orderReview.getCreateTime())
                .build();
    }

    @Override
    @Transactional
    public void repetition(Long id) {
        requireRole(ROLE_FAMILY);
        Orders ordersDB = getAccessibleOrder(id);

        Long userId = BaseContext.getCurrentId();
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        if (CollectionUtils.isEmpty(orderDetailList)) {
            throw new OrderBusinessException("原订单中没有可恢复的菜品");
        }

        if (ordersDB.getElderId() == null) {
            throw new OrderBusinessException("原订单缺少服务老人信息，无法恢复到购物车");
        }

        Elderly elderly = elderlyMapper.getById(ordersDB.getElderId());
        if (elderly == null) {
            throw new OrderBusinessException("服务老人不存在");
        }
        if (!userId.equals(elderly.getUserId())) {
            throw new OrderBusinessException("无权为该老人恢复购物车");
        }
        DiningPoint diningPoint = requireAvailableDiningPointForRepeat(elderly);
        List<ShoppingCart> shoppingCartList = buildRepeatShoppingCartList(orderDetailList, userId, elderly.getId(), diningPoint.getId());
        shoppingCartMapper.deleteByUserId(userId);
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        applyQueryScope(ordersPageQueryDTO);
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        return new PageResult(page.getTotal(), getOrderVOList(page.getResult()));
    }

    @Override
    public PageResult operatorBoard(OperatorOrderBoardQueryDTO queryDTO) {
        requireRole(ROLE_ADMIN, ROLE_OPERATOR);
        OrdersPageQueryDTO ordersPageQueryDTO = buildOperatorBoardQuery(queryDTO);
        applyQueryScope(ordersPageQueryDTO);
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        return new PageResult(page.getTotal(), getOrderVOList(page.getResult()));
    }

    @Override
    public OrderStatisticsVO statistics() {
        Map<String, Object> baseScope = buildScopeMap();

        Map<String, Object> toBeScheduled = new HashMap<>(baseScope);
        toBeScheduled.put("status", Orders.TO_BE_SCHEDULED);

        Map<String, Object> preparing = new HashMap<>(baseScope);
        preparing.put("status", Orders.CONFIRMED);

        Map<String, Object> mealReady = new HashMap<>(baseScope);
        mealReady.put("status", Orders.MEAL_READY);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        // Legacy fields are kept for compatibility:
        // toBeConfirmed => status 2 待调度
        // confirmed => status 3 制作中
        // deliveryInProgress => status 4 待取餐
        orderStatisticsVO.setToBeConfirmed(orderMapper.countByMap(toBeScheduled));
        orderStatisticsVO.setConfirmed(orderMapper.countByMap(preparing));
        orderStatisticsVO.setDeliveryInProgress(orderMapper.countByMap(mealReady));
        return orderStatisticsVO;
    }

    @Override
    public OperatorOrderOverviewVO operatorOverview() {
        requireRole(ROLE_ADMIN, ROLE_OPERATOR);
        OperatorOrderOverviewVO overviewVO = new OperatorOrderOverviewVO();
        overviewVO.setPendingPrepare(orderMapper.countByMap(buildOperatorCountMap(Orders.TO_BE_SCHEDULED)));
        overviewVO.setPreparing(orderMapper.countByMap(buildOperatorCountMap(Orders.CONFIRMED)));
        overviewVO.setPendingAssignment(orderMapper.countByMap(buildOperatorPendingAssignmentMap()));
        overviewVO.setMealReady(orderMapper.countByMap(buildOperatorCountMap(Orders.MEAL_READY)));
        overviewVO.setDelivering(orderMapper.countByMap(buildOperatorCountMap(Orders.DELIVERY_IN_PROGRESS)));
        overviewVO.setCompleted(orderMapper.countByMap(buildOperatorCountMap(Orders.COMPLETED)));
        return overviewVO;
    }

    @Override
    public void dispatch(Long id, Long volunteerId) {
        requireRole(ROLE_ADMIN);
        Orders ordersDB = getAccessibleOrder(id);
        assignVolunteerToOrder(ordersDB, volunteerId);
    }

    @Override
    public void startPreparing(Long id) {
        requireRole(ROLE_ADMIN, ROLE_OPERATOR);
        Orders ordersDB = getAccessibleOrder(id);
        assertStatus(ordersDB, Orders.TO_BE_SCHEDULED);
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void assignVolunteer(OperatorAssignVolunteerDTO assignVolunteerDTO) {
        requireRole(ROLE_ADMIN, ROLE_OPERATOR);
        if (assignVolunteerDTO == null || assignVolunteerDTO.getOrderId() == null) {
            throw new OrderBusinessException("订单不能为空");
        }
        Orders ordersDB = getAccessibleOrder(assignVolunteerDTO.getOrderId());
        assignVolunteerToOrder(ordersDB, assignVolunteerDTO.getVolunteerId());
    }

    private void assignVolunteerToOrder(Orders ordersDB, Long volunteerId) {
        User volunteer = getDispatchVolunteer(volunteerId);
        assertStatusIn(ordersDB, Orders.TO_BE_SCHEDULED, Orders.CONFIRMED, Orders.MEAL_READY);

        log.info("分配志愿者，orderId={}, volunteerId={}, volunteerName={}",
                ordersDB.getId(), volunteer.getId(), volunteer.getName());
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .volunteerId(volunteerId)
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void markMealReady(Long id) {
        requireRole(ROLE_ADMIN, ROLE_OPERATOR);
        Orders ordersDB = getAccessibleOrder(id);
        assertStatus(ordersDB, Orders.CONFIRMED);

        log.info("operator mark meal ready, orderId={}, operatorId={}, diningPointId={}",
                id, BaseContext.getCurrentId(), BaseContext.getCurrentDiningPointId());
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.MEAL_READY)
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void confirmPickup(Long id) {
        requireRole(ROLE_ADMIN, ROLE_OPERATOR);
        Orders ordersDB = getAccessibleOrder(id);
        ensureVolunteerAssigned(ordersDB);
        updateToDeliveryInProgress(ordersDB);
    }

    @Override
    public void volunteerConfirmPickup(Long id) {
        Long volunteerId = requireVolunteerUserId();
        log.info("volunteer confirm pickup in service, volunteerId={}, orderId={}", volunteerId, id);
        Orders ordersDB = getAccessibleVolunteerOrder(id);
        ensureVolunteerAssigned(ordersDB);
        updateToDeliveryInProgress(ordersDB);
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        dispatch(ordersConfirmDTO.getId(), ordersConfirmDTO.getVolunteerId());
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        requireRole(ROLE_ADMIN);
        Orders ordersDB = getAccessibleOrder(ordersRejectionDTO.getId());
        assertStatus(ordersDB, Orders.TO_BE_SCHEDULED);

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        requireRole(ROLE_ADMIN);
        Orders ordersDB = getAccessibleOrder(ordersCancelDTO.getId());
        if (ordersDB.getPayStatus() != null && ordersDB.getPayStatus() == Orders.PAID) {
            log.info("mock refund for admin cancel, order number: {}", ordersDB.getNumber());
        }

        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        confirmPickup(id);
    }

    @Override
    public void complete(Long id) {
        requireRole(ROLE_ADMIN, ROLE_VOLUNTEER);
        Orders ordersDB = getAccessibleOrder(id);
        updateToCompleted(ordersDB);
    }

    @Override
    public void volunteerComplete(Long id) {
        Long volunteerId = requireVolunteerUserId();
        log.info("volunteer confirm delivery in service, volunteerId={}, orderId={}", volunteerId, id);
        Orders ordersDB = getAccessibleVolunteerOrder(id);
        updateToCompleted(ordersDB);
    }

    @Override
    public void reminder(Long id) {
        requireRole(ROLE_FAMILY);
        Orders ordersDB = getAccessibleOrder(id);

        Map<String, Object> map = new HashMap<>();
        map.put("type", 2);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + ordersDB.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    private void notifyOrderPaid(Long orderId, String orderNumber) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", orderId);
        map.put("content", "订单号：" + orderNumber);
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    private OrdersPageQueryDTO buildOperatorBoardQuery(OperatorOrderBoardQueryDTO queryDTO) {
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setPage(queryDTO.getPage() > 0 ? queryDTO.getPage() : 1);
        ordersPageQueryDTO.setPageSize(queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10);
        ordersPageQueryDTO.setNumber(queryDTO.getNumber());
        ordersPageQueryDTO.setPhone(queryDTO.getPhone());
        ordersPageQueryDTO.setBeginTime(queryDTO.getBeginTime());
        ordersPageQueryDTO.setEndTime(queryDTO.getEndTime());

        String view = queryDTO.getView() == null ? VIEW_PENDING_PREPARE : queryDTO.getView().trim().toUpperCase();
        switch (view) {
            case VIEW_PENDING_PREPARE:
                ordersPageQueryDTO.setStatus(Orders.TO_BE_SCHEDULED);
                break;
            case VIEW_PREPARING:
                ordersPageQueryDTO.setStatus(Orders.CONFIRMED);
                break;
            case VIEW_PENDING_ASSIGNMENT:
                ordersPageQueryDTO.setStatusList(Arrays.asList(Orders.TO_BE_SCHEDULED, Orders.CONFIRMED, Orders.MEAL_READY));
                ordersPageQueryDTO.setUnassignedOnly(true);
                break;
            case VIEW_MEAL_READY:
                ordersPageQueryDTO.setStatus(Orders.MEAL_READY);
                break;
            case VIEW_DELIVERING:
                ordersPageQueryDTO.setStatus(Orders.DELIVERY_IN_PROGRESS);
                break;
            case VIEW_COMPLETED:
                ordersPageQueryDTO.setStatus(Orders.COMPLETED);
                break;
            default:
                throw new OrderBusinessException("无效的订单视图类型");
        }
        return ordersPageQueryDTO;
    }

    private Map<String, Object> buildOperatorCountMap(Integer status) {
        Map<String, Object> map = buildScopeMap();
        map.put("status", status);
        return map;
    }

    private Map<String, Object> buildOperatorPendingAssignmentMap() {
        Map<String, Object> map = buildScopeMap();
        map.put("statusList", Arrays.asList(Orders.TO_BE_SCHEDULED, Orders.CONFIRMED, Orders.MEAL_READY));
        map.put("unassignedOnly", true);
        return map;
    }

    private PageResult pageQueryByScope(int pageNum, int pageSize, Integer status) {
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setPage(pageNum);
        ordersPageQueryDTO.setPageSize(pageSize);
        ordersPageQueryDTO.setStatus(status);
        applyQueryScope(ordersPageQueryDTO);

        PageHelper.startPage(pageNum, pageSize);
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOList = getOrderVOList(page.getResult());
        attachReviewedFlags(orderVOList);
        return new PageResult(page.getTotal(), orderVOList);
    }

    private void applyQueryScope(OrdersPageQueryDTO ordersPageQueryDTO) {
        String role = normalizeRole(BaseContext.getCurrentRole());

        if (ROLE_OPERATOR.equals(role)) {
            ordersPageQueryDTO.setDiningPointId(requireCurrentDiningPointId());
            ordersPageQueryDTO.setUserId(null);
            ordersPageQueryDTO.setVolunteerId(null);
        } else if (ROLE_VOLUNTEER.equals(role)) {
            ordersPageQueryDTO.setVolunteerId(requireCurrentUserId());
            ordersPageQueryDTO.setUserId(null);
            ordersPageQueryDTO.setDiningPointId(null);
        } else if (ROLE_FAMILY.equals(role)) {
            ordersPageQueryDTO.setUserId(requireCurrentUserId());
            ordersPageQueryDTO.setVolunteerId(null);
            ordersPageQueryDTO.setDiningPointId(null);
        }
    }

    private Map<String, Object> buildScopeMap() {
        Map<String, Object> map = new HashMap<>();
        String role = normalizeRole(BaseContext.getCurrentRole());
        if (ROLE_OPERATOR.equals(role)) {
            map.put("diningPointId", requireCurrentDiningPointId());
        } else if (ROLE_VOLUNTEER.equals(role)) {
            map.put("volunteerId", BaseContext.getCurrentId());
        } else if (ROLE_FAMILY.equals(role)) {
            map.put("userId", BaseContext.getCurrentId());
        }
        return map;
    }

    private Orders getAccessibleOrder(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        validateDataScope(orders);
        return orders;
    }

    private User getDispatchVolunteer(Long volunteerId) {
        if (volunteerId == null) {
            throw new OrderBusinessException("请选择志愿者");
        }

        User volunteer = userMapper.getById(volunteerId);
        if (volunteer == null || !ROLE_VOLUNTEER.equalsIgnoreCase(normalizeRole(volunteer.getRole()))
                || volunteer.getStatus() == null || volunteer.getStatus() != 1) {
            throw new OrderBusinessException("志愿者不存在或不可用");
        }
        return volunteer;
    }

    private Orders getAccessibleVolunteerOrder(Long id) {
        requireVolunteerUserId();
        return getAccessibleOrder(id);
    }

    private void validateDataScope(Orders orders) {
        String role = normalizeRole(BaseContext.getCurrentRole());
        Long currentId = BaseContext.getCurrentId();
        if (ROLE_ADMIN.equals(role)) {
            return;
        }
        if (ROLE_OPERATOR.equals(role)) {
            Long currentDiningPointId = requireCurrentDiningPointId();
            if (currentDiningPointId == null || orders.getDiningPointId() == null || !currentDiningPointId.equals(orders.getDiningPointId())) {
                throw new OrderBusinessException("无权操作当前订单");
            }
            return;
        }
        if (ROLE_VOLUNTEER.equals(role)) {
            if (currentId == null || orders.getVolunteerId() == null || !currentId.equals(orders.getVolunteerId())) {
                throw new OrderBusinessException("无权操作当前订单");
            }
            return;
        }
        if (ROLE_FAMILY.equals(role) && (currentId == null || !currentId.equals(orders.getUserId()))) {
            throw new OrderBusinessException("无权操作当前订单");
        }
    }

    private void updateToDeliveryInProgress(Orders ordersDB) {
        assertStatus(ordersDB, Orders.MEAL_READY);

        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(orders);
    }

    private void ensureVolunteerAssigned(Orders ordersDB) {
        if (ordersDB.getVolunteerId() == null) {
            throw new OrderBusinessException("订单尚未分配志愿者，无法确认取餐");
        }
    }

    private void updateToCompleted(Orders ordersDB) {
        assertStatus(ordersDB, Orders.DELIVERY_IN_PROGRESS);

        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
        refreshVolunteerCompletionStats(ordersDB.getVolunteerId());
    }

    private void validateReviewSubmitDTO(OrderReviewSubmitDTO orderReviewSubmitDTO) {
        if (orderReviewSubmitDTO == null || orderReviewSubmitDTO.getOrderId() == null) {
            throw new OrderBusinessException("订单ID不能为空");
        }
        Integer score = orderReviewSubmitDTO.getScore();
        if (score == null || score < 1 || score > 5) {
            throw new OrderBusinessException("评分只允许 1~5 分");
        }
    }

    private User requireReviewedVolunteer(Orders orders) {
        if (orders.getVolunteerId() == null) {
            throw new OrderBusinessException("当前订单未分配志愿者，无法评价");
        }
        User volunteer = userMapper.getById(orders.getVolunteerId());
        if (volunteer == null || !ROLE_VOLUNTEER.equalsIgnoreCase(normalizeRole(volunteer.getRole()))) {
            throw new OrderBusinessException("当前订单关联的志愿者不存在，无法评价");
        }
        return volunteer;
    }

    private String normalizeReviewContent(String content) {
        if (!StringUtils.hasText(content)) {
            return null;
        }
        String trimmed = content.trim();
        if (trimmed.length() > 255) {
            throw new OrderBusinessException("评价内容不能超过255个字符");
        }
        return trimmed;
    }

    private void refreshVolunteerRating(Long volunteerUserId) {
        BigDecimal averageScore = orderReviewMapper.avgScoreByVolunteerUserId(volunteerUserId);
        BigDecimal normalizedRating = averageScore == null ? null : averageScore.setScale(1, RoundingMode.HALF_UP);
        LocalDateTime now = LocalDateTime.now();
        int completedOrderCount = getCompletedOrderCount(volunteerUserId);
        int calculatedLevel = VolunteerLevelSupport.calculateLevel(completedOrderCount);

        VolunteerStats volunteerStats = volunteerStatsMapper.getByUserId(volunteerUserId);
        if (volunteerStats == null) {
            volunteerStatsMapper.insert(VolunteerStats.builder()
                    .userId(volunteerUserId)
                    .totalOrders(completedOrderCount)
                    .totalHours(new BigDecimal("0.0"))
                    .rating(normalizedRating)
                    .level(calculatedLevel)
                    .createTime(now)
                    .updateTime(now)
                    .build());
            return;
        }

        volunteerStatsMapper.update(VolunteerStats.builder()
                .id(volunteerStats.getId())
                .totalOrders(completedOrderCount)
                .rating(normalizedRating)
                .level(calculatedLevel)
                .updateTime(now)
                .build());
    }

    private void refreshVolunteerCompletionStats(Long volunteerUserId) {
        if (volunteerUserId == null) {
            return;
        }

        int completedOrderCount = getCompletedOrderCount(volunteerUserId);
        int calculatedLevel = VolunteerLevelSupport.calculateLevel(completedOrderCount);
        LocalDateTime now = LocalDateTime.now();

        VolunteerStats volunteerStats = volunteerStatsMapper.getByUserId(volunteerUserId);
        if (volunteerStats == null) {
            volunteerStatsMapper.insert(VolunteerStats.builder()
                    .userId(volunteerUserId)
                    .totalOrders(completedOrderCount)
                    .totalHours(new BigDecimal("0.0"))
                    .rating(null)
                    .level(calculatedLevel)
                    .createTime(now)
                    .updateTime(now)
                    .build());
            return;
        }

        volunteerStatsMapper.update(VolunteerStats.builder()
                .id(volunteerStats.getId())
                .totalOrders(completedOrderCount)
                .level(calculatedLevel)
                .updateTime(now)
                .build());
    }

    private int getCompletedOrderCount(Long volunteerUserId) {
        if (volunteerUserId == null) {
            return 0;
        }
        Integer completedOrders = orderMapper.countByMap(buildCompletedOrderCountMap(volunteerUserId));
        return completedOrders == null ? 0 : completedOrders;
    }

    private Map<String, Object> buildCompletedOrderCountMap(Long volunteerUserId) {
        Map<String, Object> map = new HashMap<>();
        map.put("volunteerId", volunteerUserId);
        map.put("status", Orders.COMPLETED);
        return map;
    }

    private void assertStatus(Orders orders, Integer expectedStatus) {
        if (!expectedStatus.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
    }

    private void assertStatusIn(Orders orders, Integer... statuses) {
        for (Integer status : statuses) {
            if (status.equals(orders.getStatus())) {
                return;
            }
        }
        throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
    }

    private void requireRole(String... allowedRoles) {
        String currentRole = normalizeRole(BaseContext.getCurrentRole());
        for (String allowedRole : allowedRoles) {
            if (allowedRole.equals(currentRole)) {
                return;
            }
        }
        throw new OrderBusinessException("当前角色无权执行该操作");
    }

    private Long requireVolunteerUserId() {
        requireRole(ROLE_VOLUNTEER);
        return requireCurrentUserId();
    }

    private Long requireCurrentDiningPointId() {
        Long diningPointId = BaseContext.getCurrentDiningPointId();
        if (diningPointId == null) {
            throw new OrderBusinessException("当前账号未绑定助餐点");
        }
        return diningPointId;
    }

    private Long requireCurrentUserId() {
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new OrderBusinessException(MessageConstant.USER_NOT_LOGIN);
        }
        return currentId;
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toUpperCase();
    }

    private List<OrderVO> getOrderVOList(List<Orders> ordersList) {
        List<OrderVO> orderVOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(ordersList)) {
            return orderVOList;
        }
        for (Orders orders : ordersList) {
            orderVOList.add(buildOrderVO(orders));
        }
        return orderVOList;
    }

    private OrderVO buildOrderVO(Orders orders) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        orderVO.setOrderDishes(getOrderDishesStr(orderDetailList));

        if (orders.getVolunteerId() != null) {
            User volunteer = userMapper.getById(orders.getVolunteerId());
            orderVO.setVolunteerName(volunteer == null ? null : volunteer.getName());
            orderVO.setVolunteerPhone(volunteer == null ? null : volunteer.getPhone());
            orderVO.setVolunteerStatus(volunteer == null ? null : volunteer.getStatus());
        }
        if (orders.getDiningPointId() != null) {
            DiningPoint diningPoint = diningPointMapper.getById(orders.getDiningPointId());
            orderVO.setDiningPointName(diningPoint == null ? null : diningPoint.getName());
        }
        if (orders.getElderId() != null) {
            Elderly elderly = elderlyMapper.getById(orders.getElderId());
            orderVO.setElderName(elderly == null ? null : elderly.getName());
            orderVO.setElderPhone(elderly == null ? null : elderly.getPhone());
            orderVO.setElderAddress(elderly == null ? null : elderly.getAddress());
            orderVO.setElderSpecialNeeds(elderly == null ? null : elderly.getSpecialNeeds());
        }
        orderVO.setHandoverStatus(buildHandoverStatus(orders));
        return orderVO;
    }

    private void attachReviewedFlags(List<OrderVO> orderVOList) {
        if (!ROLE_FAMILY.equals(normalizeRole(BaseContext.getCurrentRole())) || CollectionUtils.isEmpty(orderVOList)) {
            return;
        }

        List<Long> completedOrderIds = orderVOList.stream()
                .filter(orderVO -> orderVO.getId() != null && Orders.COMPLETED.equals(orderVO.getStatus()))
                .map(OrderVO::getId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(completedOrderIds)) {
            return;
        }

        List<OrderReview> orderReviewList = orderReviewMapper.listByOrderIds(completedOrderIds);
        if (CollectionUtils.isEmpty(orderReviewList)) {
            return;
        }

        Map<Long, Boolean> reviewedMap = orderReviewList.stream()
                .filter(orderReview -> orderReview.getOrderId() != null)
                .collect(Collectors.toMap(OrderReview::getOrderId, orderReview -> Boolean.TRUE, (left, right) -> left));

        for (OrderVO orderVO : orderVOList) {
            if (orderVO.getId() != null) {
                orderVO.setReviewed(Boolean.TRUE.equals(reviewedMap.get(orderVO.getId())));
            }
        }
    }

    private void attachReviewedFlag(OrderVO orderVO) {
        if (orderVO == null || !ROLE_FAMILY.equals(normalizeRole(BaseContext.getCurrentRole()))
                || orderVO.getId() == null || !Orders.COMPLETED.equals(orderVO.getStatus())) {
            return;
        }
        orderVO.setReviewed(orderReviewMapper.getByOrderId(orderVO.getId()) != null);
    }

    private String buildHandoverStatus(Orders orders) {
        if (orders.getVolunteerId() == null) {
            return HANDOVER_UNASSIGNED;
        }
        if (orders.getStatus() == null || orders.getStatus() < Orders.MEAL_READY) {
            return HANDOVER_PENDING_PREPARE;
        }
        if (Orders.MEAL_READY.equals(orders.getStatus())) {
            return HANDOVER_PENDING_PICKUP;
        }
        return HANDOVER_IN_DELIVERY;
    }

    private String getOrderDishesStr(List<OrderDetail> orderDetailList) {
        return orderDetailList.stream()
                .map(detail -> detail.getName() + "*" + detail.getNumber() + ";")
                .collect(Collectors.joining(""));
    }

    private String buildFullAddress(AddressBook addressBook) {
        StringBuilder builder = new StringBuilder();
        if (addressBook.getProvinceName() != null) {
            builder.append(addressBook.getProvinceName());
        }
        if (addressBook.getCityName() != null) {
            builder.append(addressBook.getCityName());
        }
        if (addressBook.getDistrictName() != null) {
            builder.append(addressBook.getDistrictName());
        }
        if (addressBook.getDetail() != null) {
            builder.append(addressBook.getDetail());
        }
        return builder.toString();
    }

    private BigDecimal calculateGoodsAmount(List<ShoppingCart> shoppingCartList) {
        return shoppingCartList.stream()
                .map(cart -> cart.getAmount().multiply(BigDecimal.valueOf(cart.getNumber())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateSubmittedAmounts(OrdersSubmitDTO ordersSubmitDTO, ShoppingCartSummaryVO summary) {
        if (!moneyEquals(ordersSubmitDTO.getDishAmount(), summary.getDishAmount())
                || !moneyEquals(ordersSubmitDTO.getDeliveryFee(), summary.getDeliveryFee())
                || !moneyEquals(ordersSubmitDTO.getTablewareFee(), summary.getTablewareFee())
                || !moneyEquals(ordersSubmitDTO.getSubsidyAmount(), summary.getSubsidyAmount())
                || !moneyEquals(ordersSubmitDTO.getPayAmount(), summary.getPayAmount())) {
            throw new OrderBusinessException("订单金额已变化，请刷新后重试");
        }
    }

    private boolean moneyEquals(BigDecimal left, BigDecimal right) {
        if (left == null || right == null) {
            return false;
        }
        return normalizeMoney(left).compareTo(normalizeMoney(right)) == 0;
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
    }

    private DiningPoint requireElderlyDiningPoint(Elderly elderly) {
        if (elderly.getDiningPointId() == null) {
            throw new OrderBusinessException("当前老人未绑定助餐点，无法下单");
        }

        DiningPoint diningPoint = diningPointMapper.getById(elderly.getDiningPointId());
        if (diningPoint == null) {
            throw new OrderBusinessException("当前老人绑定的助餐点不存在，无法下单");
        }
        if (diningPoint.getAddress() == null || diningPoint.getAddress().trim().isEmpty()) {
            throw new OrderBusinessException("当前老人绑定助餐点缺少地址，无法下单");
        }
        return diningPoint;
    }

    private void validateShoppingCartDishesForElderlyDiningPoint(List<ShoppingCart> shoppingCartList, Long expectedDiningPointId) {
        for (ShoppingCart cart : shoppingCartList) {
            if (cart.getSetmealId() != null) {
                throw new OrderBusinessException("当前版本暂不支持套餐下单");
            }
            if (cart.getDishId() == null) {
                throw new OrderBusinessException("购物车菜品数据异常，无法提交订单");
            }

            Dish dish = dishMapper.getById(cart.getDishId());
            if (dish == null) {
                throw new OrderBusinessException("购物车中存在已删除菜品，无法提交订单");
            }
            if (!StatusConstant.ENABLE.equals(dish.getStatus())) {
                throw new OrderBusinessException("购物车中存在已停售菜品，无法提交订单");
            }
            if (dish.getDiningPointId() == null) {
                throw new OrderBusinessException("所选菜品未绑定助餐点，无法提交订单");
            }
            if (!expectedDiningPointId.equals(dish.getDiningPointId())) {
                throw new OrderBusinessException("所选菜品不属于当前老人对应助餐点");
            }
        }
    }

    private void validateShoppingCartElderly(List<ShoppingCart> shoppingCartList, Long expectedElderId) {
        for (ShoppingCart cart : shoppingCartList) {
            if (cart.getElderId() == null) {
                throw new OrderBusinessException("购物车存在历史数据，请先清空购物车后重新为当前老人选餐");
            }
            if (!expectedElderId.equals(cart.getElderId())) {
                throw new OrderBusinessException("当前购物车已绑定其他老人，请清空购物车后重新下单");
            }
        }
    }

    /**
     * 历史订单“再来一单”仍需要逐项校验菜品有效性和助餐点一致性，
     * 这是旧数据兼容保护，不再承担订单归属助餐点的推导职责。
     */
    private List<ShoppingCart> buildRepeatShoppingCartList(List<OrderDetail> orderDetailList, Long userId, Long elderId) {
        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        Long currentDiningPointId = null;

        for (OrderDetail detail : orderDetailList) {
            if (detail.getSetmealId() != null) {
                throw new OrderBusinessException("当前版本暂不支持套餐再来一单");
            }
            if (detail.getDishId() == null) {
                throw new OrderBusinessException("订单菜品数据异常，无法再来一单");
            }
            if (detail.getNumber() == null || detail.getNumber() <= 0) {
                throw new OrderBusinessException("订单菜品数量异常，无法再来一单");
            }

            Dish dish = dishMapper.getById(detail.getDishId());
            if (dish == null) {
                throw new OrderBusinessException("部分菜品已删除，无法再来一单");
            }
            if (!StatusConstant.ENABLE.equals(dish.getStatus())) {
                throw new OrderBusinessException("部分菜品已下架，无法再来一单");
            }
            if (dish.getDiningPointId() == null) {
                throw new OrderBusinessException("部分菜品未绑定助餐点，无法再来一单");
            }

            DiningPoint diningPoint = diningPointMapper.getById(dish.getDiningPointId());
            if (diningPoint == null) {
                throw new OrderBusinessException("部分菜品所属助餐点不存在，无法再来一单");
            }
            if (currentDiningPointId == null) {
                currentDiningPointId = dish.getDiningPointId();
            } else if (!currentDiningPointId.equals(dish.getDiningPointId())) {
                throw new OrderBusinessException("当前订单菜品所属助餐点已变化，无法再来一单");
            }

            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);
            shoppingCart.setElderId(elderId);
            shoppingCart.setDishId(dish.getId());
            shoppingCart.setDishFlavor(detail.getDishFlavor());
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
            shoppingCart.setNumber(detail.getNumber());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartList.add(shoppingCart);
        }

        if (CollectionUtils.isEmpty(shoppingCartList)) {
            throw new OrderBusinessException("原订单中没有可恢复的菜品");
        }

        return shoppingCartList;
    }

    private DiningPoint requireAvailableDiningPointForOrder(Elderly elderly) {
        if (elderly.getDiningPointId() == null) {
            throw new OrderBusinessException("当前老人未绑定助餐点，无法下单");
        }

        DiningPoint diningPoint = diningPointMapper.getById(elderly.getDiningPointId());
        if (diningPoint == null) {
            throw new OrderBusinessException("当前老人绑定的助餐点不存在，无法下单");
        }
        if (!StatusConstant.ENABLE.equals(diningPoint.getStatus())) {
            throw new OrderBusinessException(MessageConstant.DINING_POINT_RESTING);
        }
        if (diningPoint.getAddress() == null || diningPoint.getAddress().trim().isEmpty()) {
            throw new OrderBusinessException("当前老人绑定助餐点缺少地址，无法下单");
        }
        return diningPoint;
    }

    private DiningPoint requireAvailableDiningPointForRepeat(Elderly elderly) {
        if (elderly.getDiningPointId() == null) {
            throw new OrderBusinessException("当前老人未绑定助餐点，无法再来一单");
        }

        DiningPoint diningPoint = diningPointMapper.getById(elderly.getDiningPointId());
        if (diningPoint == null) {
            throw new OrderBusinessException("当前老人绑定的助餐点不存在，无法再来一单");
        }
        if (!StatusConstant.ENABLE.equals(diningPoint.getStatus())) {
            throw new OrderBusinessException(MessageConstant.DINING_POINT_RESTING);
        }
        return diningPoint;
    }

    private void requireAvailableDiningPointForPendingPayment(Orders orders) {
        if (!isDiningPointAvailableForPendingPayment(orders)) {
            throw new OrderBusinessException(MessageConstant.ORDER_PAYMENT_DINING_POINT_RESTING);
        }
    }

    private boolean isDiningPointAvailableForPendingPayment(Orders orders) {
        DiningPoint diningPoint = resolveDiningPointForExistingOrder(orders);
        return diningPoint != null && StatusConstant.ENABLE.equals(diningPoint.getStatus());
    }

    private DiningPoint resolveDiningPointForExistingOrder(Orders orders) {
        if (orders == null) {
            return null;
        }
        if (orders.getDiningPointId() != null) {
            return diningPointMapper.getById(orders.getDiningPointId());
        }
        if (orders.getElderId() == null) {
            return null;
        }
        Elderly elderly = elderlyMapper.getById(orders.getElderId());
        if (elderly == null || elderly.getDiningPointId() == null) {
            return null;
        }
        return diningPointMapper.getById(elderly.getDiningPointId());
    }

    private void validateShoppingCartItemsForOrder(List<ShoppingCart> shoppingCartList, Long expectedDiningPointId) {
        for (ShoppingCart cart : shoppingCartList) {
            if (cart.getSetmealId() != null) {
                throw new OrderBusinessException("当前版本暂不支持套餐下单");
            }
            if (cart.getDishId() == null) {
                throw new OrderBusinessException("购物车菜品数据异常，无法提交订单");
            }

            Dish dish = dishMapper.getById(cart.getDishId());
            if (dish == null) {
                throw new OrderBusinessException("购物车中存在已删除菜品，无法提交订单");
            }
            if (!StatusConstant.ENABLE.equals(dish.getStatus())) {
                throw new OrderBusinessException("购物车中存在已停售菜品，无法提交订单");
            }
            if (dish.getDiningPointId() == null) {
                throw new OrderBusinessException("所选菜品未绑定助餐点，无法提交订单");
            }

            DiningPoint diningPoint = diningPointMapper.getById(dish.getDiningPointId());
            if (diningPoint == null || !StatusConstant.ENABLE.equals(diningPoint.getStatus())) {
                throw new OrderBusinessException(MessageConstant.DISH_DINING_POINT_UNAVAILABLE);
            }
            if (!expectedDiningPointId.equals(dish.getDiningPointId())) {
                throw new OrderBusinessException("所选菜品不属于当前老人对应助餐点");
            }
        }
    }

    private List<ShoppingCart> buildRepeatShoppingCartList(List<OrderDetail> orderDetailList, Long userId, Long elderId, Long expectedDiningPointId) {
        List<ShoppingCart> shoppingCartList = new ArrayList<>();

        for (OrderDetail detail : orderDetailList) {
            if (detail.getSetmealId() != null) {
                throw new OrderBusinessException("当前版本暂不支持套餐再来一单");
            }
            if (detail.getDishId() == null) {
                throw new OrderBusinessException("订单菜品数据异常，无法再来一单");
            }
            if (detail.getNumber() == null || detail.getNumber() <= 0) {
                throw new OrderBusinessException("订单菜品数量异常，无法再来一单");
            }

            Dish dish = dishMapper.getById(detail.getDishId());
            if (dish == null) {
                throw new OrderBusinessException("部分菜品已删除，无法再来一单");
            }
            if (!StatusConstant.ENABLE.equals(dish.getStatus())) {
                throw new OrderBusinessException("部分菜品已下架，无法再来一单");
            }
            if (dish.getDiningPointId() == null) {
                throw new OrderBusinessException("部分菜品未绑定助餐点，无法再来一单");
            }

            DiningPoint diningPoint = diningPointMapper.getById(dish.getDiningPointId());
            if (diningPoint == null || !StatusConstant.ENABLE.equals(diningPoint.getStatus())) {
                throw new OrderBusinessException(MessageConstant.DISH_DINING_POINT_UNAVAILABLE);
            }
            if (!expectedDiningPointId.equals(dish.getDiningPointId())) {
                throw new OrderBusinessException("当前订单菜品已不属于老人当前所属助餐点，无法再来一单");
            }

            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);
            shoppingCart.setElderId(elderId);
            shoppingCart.setDishId(dish.getId());
            shoppingCart.setDishFlavor(detail.getDishFlavor());
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
            shoppingCart.setNumber(detail.getNumber());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartList.add(shoppingCart);
        }

        if (CollectionUtils.isEmpty(shoppingCartList)) {
            throw new OrderBusinessException("原订单中没有可恢复的菜品");
        }

        return shoppingCartList;
    }

    private void checkOutOfRange(String diningPointAddress, String address) {
        Map<String, String> map = new HashMap<>();
        map.put("address", diningPointAddress);
        map.put("output", "json");
        map.put("ak", ak);

        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);
        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("店铺地址解析失败");
        }

        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        String shopLngLat = lat + "," + lng;

        map.put("address", address);
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("收货地址解析失败");
        }

        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        String userLngLat = lat + "," + lng;

        map.put("origin", shopLngLat);
        map.put("destination", userLngLat);
        map.put("steps_info", "0");

        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);
        jsonObject = JSON.parseObject(json);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("配送路线规划失败");
        }

        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if (distance > 5000) {
            log.info("delivery distance: {}m", distance);
            throw new OrderBusinessException("超出配送范围");
        }
        log.info("delivery distance: {}m", distance);
    }

}
