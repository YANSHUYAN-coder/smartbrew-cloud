package com.coffee.ai.tools;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffee.system.domain.entity.Product;
import com.coffee.system.domain.entity.SkuStock;
import com.coffee.system.service.ProductService;
import com.coffee.system.service.SkuStockService;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static cn.hutool.core.util.IdUtil.fastSimpleUUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderTools {

    private final ProductService productService;
    private final SkuStockService skuStockService;

    public record OrderRequest(
            @ToolParam(description = "商品名称，必须与菜单中的名称精确匹配") String productName,
            @ToolParam(description = "用户选择的规格描述(如: 冰, 大杯)。") String specs,
            @ToolParam(description = "购买数量") Integer quantity
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OrderCardData(
            String cardId, // 新增：唯一卡片ID
            String type,
            Long productId,
            Long skuId,
            String productName,
            String productPic,
            String specs,
            Integer quantity,
            BigDecimal price,
            BigDecimal totalPrice,
            String msg
    ) {}

    @Tool(description = "生成下单卡片。条件：1.用户明确表达下单意图 2.商品存在 3.所有必选规格已确认。")
    public OrderCardData createOrderCard(OrderRequest request) {
        log.info("AI 触发下单工具: {}", request);

        // 1. 查找商品 (使用 like 进行一定的宽容度，但必须上架)
        List<Product> products = productService.list(new LambdaQueryWrapper<Product>()
                .like(Product::getName, request.productName())
                .eq(Product::getStatus, 1)
                .last("LIMIT 1"));

        if (CollUtil.isEmpty(products)) {
            // 【修复 1】找不到商品直接返回错误，禁止瞎编
            return new OrderCardData(null, "error", null, null, request.productName(), null, null, 0, null, null, "抱歉，未找到商品：" + request.productName());
        }

        Product product = products.get(0);
        List<SkuStock> skuList = skuStockService.listByProductId(product.getId());

        if (CollUtil.isEmpty(skuList)) {
            return new OrderCardData(null, "error", null, null, product.getName(), null, null, 0, null, null, "该商品暂无库存信息");
        }

        // 2. 匹配 SKU 规格 (严格匹配模式)
        SkuStock bestMatchSku = null;

        // 如果商品只有一个 SKU (例如固定规格)，直接使用，不需要用户选
        if (skuList.size() == 1) {
            bestMatchSku = skuList.get(0);
        } else {
            // 如果有多个规格，必须根据 specs 匹配
            if (StrUtil.isBlank(request.specs())) {
                // 依然没规格，说明 AI 判断失误，返回错误提示 AI 继续追问
                return new OrderCardData(null, "error", null, null, product.getName(), null, null, 0, null, null, "请询问用户具体的规格（如温度、大小）");
            }

            for (SkuStock sku : skuList) {
                // 将 SKU 的 specs (JSON) 和用户输入的 specs 进行比对
                // 只要用户的描述词（如 "大杯"）都出现在了 SKU 属性中，就算匹配
                if (containsAllKeywords(sku.getSpec(), request.specs())) {
                    bestMatchSku = sku;
                    break;
                }
            }
        }

        if (bestMatchSku == null) {
            // 【修复 2】规格匹配失败
            return new OrderCardData(null, "error", null, null, product.getName(), null, null, 0, null, null, "抱歉，没有找到符合“" + request.specs() + "”的规格，请重新选择");
        }

        // 3. 成功构建数据
        int qty = request.quantity() == null ? 1 : request.quantity();
        BigDecimal price = bestMatchSku.getPrice();
        BigDecimal total = price.multiply(BigDecimal.valueOf(qty));

        // 生成唯一 cardId
        String cardId = fastSimpleUUID();

        return new OrderCardData(
                cardId,
                "order_card",
                product.getId(),
                bestMatchSku.getId(),
                product.getName(),
                product.getPicUrl(),
                StrUtil.isBlank(request.specs()) ? "标准规格" : request.specs(),
                qty,
                price,
                total,
                "success"
        );
    }

    private boolean containsAllKeywords(String source, String keywords) {
        if (source == null) return false;
        if (keywords == null) return true; // 不需要关键词
        String[] keys = keywords.split("[,，\\s]+");
        for (String key : keys) {
            if (!source.contains(key)) return false;
        }
        return true;
    }
}