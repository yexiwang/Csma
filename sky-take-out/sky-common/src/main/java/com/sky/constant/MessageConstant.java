package com.sky.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {

    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String ACCOUNT_LOCKED = "账号被锁定";
    public static final String ALREADY_EXISTS = "已存在";
    public static final String UNKNOWN_ERROR = "未知错误";
    public static final String USER_NOT_LOGIN = "用户未登录";
    public static final String CATEGORY_BE_RELATED_BY_SETMEAL = "当前分类关联了套餐,不能删除";
    public static final String CATEGORY_BE_RELATED_BY_DISH = "当前分类关联了菜品,不能删除";
    public static final String SHOPPING_CART_IS_NULL = "购物车数据为空，不能下单";
    public static final String ADDRESS_BOOK_IS_NULL = "用户地址为空，不能下单";
    public static final String LOGIN_FAILED = "登录失败";
    public static final String UPLOAD_FAILED = "文件上传失败";
    public static final String SETMEAL_ENABLE_FAILED = "套餐内包含未启售菜品，无法启售";
    public static final String PASSWORD_EDIT_FAILED = "密码修改失败";
    public static final String DISH_ON_SALE = "起售中的菜品不能删除";
    public static final String SETMEAL_ON_SALE = "起售中的套餐不能删除";
    public static final String DISH_BE_RELATED_BY_SETMEAL = "当前菜品关联了套餐,不能删除";
    public static final String ORDER_STATUS_ERROR = "订单状态错误";
    public static final String ORDER_NOT_FOUND = "订单不存在";

    public static final String DISH_DINING_POINT_REQUIRED = "菜品所属助餐点不能为空";
    public static final String SETMEAL_DINING_POINT_REQUIRED = "套餐所属助餐点不能为空";
    public static final String DINING_POINT_NOT_FOUND = "助餐点不存在";
    public static final String DINING_POINT_RESTING = "当前所属助餐点已休息，暂不可下单";
    public static final String DISH_DINING_POINT_UNAVAILABLE = "当前菜品所属助餐点不可用";
    public static final String SETMEAL_DINING_POINT_UNAVAILABLE = "当前套餐所属助餐点不可用";
    public static final String SETMEAL_DISH_REQUIRED = "套餐菜品不能为空";
    public static final String SETMEAL_DISH_NOT_FOUND = "套餐内存在不存在的菜品，无法保存";
    public static final String SETMEAL_DISH_DINING_POINT_REQUIRED = "套餐内存在未绑定助餐点的菜品，无法保存";
    public static final String SETMEAL_DISH_DINING_POINT_MISMATCH = "套餐内存在跨助餐点菜品，无法保存";
    public static final String USER_DISH_QUERY_DINING_POINT_REQUIRED = "未指定助餐点，无法查询可售菜品";
    public static final String USER_SETMEAL_QUERY_DINING_POINT_REQUIRED = "未指定助餐点，无法查询可售套餐";
    public static final String ORDER_PAYMENT_DINING_POINT_RESTING = "当前助餐点已休息，暂不可继续支付，请联系管理员处理";
}
