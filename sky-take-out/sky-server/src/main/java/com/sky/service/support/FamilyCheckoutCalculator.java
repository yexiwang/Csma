package com.sky.service.support;

import com.sky.entity.ShoppingCart;
import com.sky.vo.ShoppingCartSummaryVO;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

@Component
public class FamilyCheckoutCalculator {

    private static final BigDecimal DELIVERY_FEE = BigDecimal.ZERO;
    private static final BigDecimal TABLEWARE_UNIT_FEE = BigDecimal.ONE;
    private static final BigDecimal SUBSIDY_AMOUNT = BigDecimal.ZERO;

    public ShoppingCartSummaryVO calculate(Long elderId, Long diningPointId, List<ShoppingCart> shoppingCartList,
                                           Integer tablewareStatus, Integer tablewareNumber) {
        int totalCount = CollectionUtils.isEmpty(shoppingCartList)
                ? 0
                : shoppingCartList.stream()
                .map(ShoppingCart::getNumber)
                .filter(number -> number != null && number > 0)
                .mapToInt(Integer::intValue)
                .sum();

        BigDecimal dishAmount = CollectionUtils.isEmpty(shoppingCartList)
                ? BigDecimal.ZERO
                : shoppingCartList.stream()
                .map(this::calculateLineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int effectiveTablewareNumber = resolveEffectiveTablewareNumber(tablewareStatus, tablewareNumber, totalCount);
        BigDecimal tablewareFee = TABLEWARE_UNIT_FEE.multiply(BigDecimal.valueOf(effectiveTablewareNumber));
        BigDecimal payAmount = dishAmount.add(DELIVERY_FEE).add(tablewareFee).subtract(SUBSIDY_AMOUNT);

        return ShoppingCartSummaryVO.builder()
                .elderId(elderId)
                .diningPointId(diningPointId)
                .totalCount(totalCount)
                .dishAmount(dishAmount)
                .deliveryFee(DELIVERY_FEE)
                .tablewareFee(tablewareFee)
                .subsidyAmount(SUBSIDY_AMOUNT)
                .payAmount(payAmount)
                .effectiveTablewareNumber(effectiveTablewareNumber)
                .build();
    }

    public int resolveEffectiveTablewareNumber(Integer tablewareStatus, Integer tablewareNumber, int totalCount) {
        if (tablewareStatus != null && tablewareStatus == 1) {
            return Math.max(totalCount, 0);
        }
        return Math.max(tablewareNumber == null ? 0 : tablewareNumber, 0);
    }

    private BigDecimal calculateLineAmount(ShoppingCart shoppingCart) {
        if (shoppingCart == null || shoppingCart.getAmount() == null || shoppingCart.getNumber() == null) {
            return BigDecimal.ZERO;
        }
        return shoppingCart.getAmount().multiply(BigDecimal.valueOf(Math.max(shoppingCart.getNumber(), 0)));
    }
}
