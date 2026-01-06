package com.coffee.ai.tools;

import com.coffee.common.context.UserContext;
import com.coffee.common.dto.ProductSearchParam;
import com.coffee.common.result.Result;
import com.coffee.common.vo.ProductDetailVO;
import com.coffee.system.domain.entity.OmsCartItem;
import com.coffee.system.domain.entity.Product;
import com.coffee.system.domain.entity.SkuStock;
import com.coffee.system.service.CartService;
import com.coffee.system.service.ProductService;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 智咖云 AI Agent 工具集
 * 使用 @Tool 注解定义 AI 可调用的能力
 */
@Component
@Slf4j
public class CoffeeAgentTools {

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ================= Scenario 1: 智能订单查询 =================

    /**
     * 查询订单状态工具
     * @Tool 注解直接定义了工具的描述，name 默认为方法名 queryLatestOrder
     */
    @Tool(description = "根据用户ID查询最近的订单状态和配送距离")
    public OrderStatusResponse queryLatestOrder(OrderQueryRequest request) {
        log.info("AI Agent 正在调用 @Tool 工具查询订单，用户ID: {}", request.userId());

        // TODO: 这里应该注入 FeignClient 调用 coffee-service-system 的接口
        // 模拟数据返回
        if ("unknown".equals(request.userId()) || "guest".equals(request.userId())) {
            return new OrderStatusResponse("未找到相关订单", "0km", 0);
        }

        // 模拟：查询到最近一笔订单是冰拿铁
        return new OrderStatusResponse(
                "您的订单[冰拿铁]正在由骑手配送中",
                "1.2公里",
                5
        );
    }

    // 入参定义
    @JsonClassDescription("订单查询请求参数")
    public record OrderQueryRequest(
            @JsonPropertyDescription("用户ID，如果上下文中没有明确ID，可以填默认值") String userId
    ) {}

    // 出参定义
    public record OrderStatusResponse(String statusDesc, String distance, int etaMinutes) {}


    // ================= Scenario 2: 语音/对话式下单 =================

    /**
     * 加购商品工具
     */
    @Tool(description = "加购/下单工具。当用户表达购买意图（如“我要”、“来一杯”、“需要”、“想喝”）时，即使未提供完整规格，也必须调用此工具。")
    public AddToCartResponse addToCart(AddToCartRequest request) {
        // 【企业级】输入校验：商品名、数量
        String rawKeyword = request.productName() == null ? "" : request.productName().trim();
        if (rawKeyword.isEmpty() || rawKeyword.length() > 50) {
            log.warn("商品名无效: 空或过长(>50): {}", rawKeyword);
            return new AddToCartResponse(false, "商品名称不能为空，且长度不能超过50个字符");
        }

        int quantity = request.quantity();
        if (quantity <= 0) {
            quantity = 1; // 默认1
        } else if (quantity > 99) {
            log.warn("数量超限: {}", quantity);
            return new AddToCartResponse(false, "单次加购数量不能超过99件，请分批添加");
        }

        // 【企业级安全】用户ID：不信任 tool 入参，优先从 UserContext 获取（已在 CoffeeAiService 中设置）
        Long actualUserId = UserContext.getUserId();
        if (actualUserId == null) {
            // 兜底：如果 UserContext 没有，尝试从 request 解析（但记录警告）
            try {
                if (request.userId() != null && !request.userId().isBlank()) {
                    actualUserId = Long.parseLong(request.userId());
                    log.warn("从 tool 入参获取 userId，建议从 UserContext 获取: {}", actualUserId);
                    UserContext.setUserId(actualUserId);
                } else {
                    log.error("用户ID缺失: UserContext 和 request 均为空");
                    return new AddToCartResponse(false, "用户身份异常，请先登录");
                }
            } catch (NumberFormatException e) {
                log.error("用户ID格式错误: {}", request.userId());
                return new AddToCartResponse(false, "用户身份异常，请先登录");
            }
        }

        log.info("AI Agent 加购工具调用: 商品={}, 规格={}, 数量={}, 用户ID={}",
                rawKeyword, request.specs(), quantity, actualUserId);

        try {
            // 1. 调用 ProductService 搜索商品
            String normalizedKeyword = normalizeProductKeyword(rawKeyword);
            String usedKeyword = rawKeyword;

            ProductSearchParam searchParam = new ProductSearchParam();
            searchParam.setKeyword(usedKeyword);
            searchParam.setStatus(1);
            searchParam.setPage(1);
            searchParam.setPageSize(10); // 查多一点，以便进行匹配

            var productPage = productService.search(searchParam);
            List<Product> products = productPage.getRecords();

            if (products.isEmpty()) {
                // 回退：如果用户把“冰/热/大杯/价格/加入购物车”等拼进了商品名，尝试归一化后再搜一次
                if (!normalizedKeyword.isBlank() && !normalizedKeyword.equals(rawKeyword)) {
                    usedKeyword = normalizedKeyword;
                    searchParam.setKeyword(usedKeyword);
                    productPage = productService.search(searchParam);
                    products = productPage.getRecords();
                }

                if (products.isEmpty()) {
                    String tried = rawKeyword;
                    if (!normalizedKeyword.isBlank() && !normalizedKeyword.equals(rawKeyword)) {
                        tried = rawKeyword + " / " + normalizedKeyword;
                    }
                    return new AddToCartResponse(false,
                            "抱歉，没找到相关商品。已尝试关键词: " + tried + "。请确认商品名称或先同步菜单到知识库/数据库。");
                }
            }

            // 【企业级】智能匹配逻辑：完全匹配 > 销量优先 > 多结果反问
            Product selectedProduct = null;

            // A. 优先完全匹配名称 (例如用户说 "生椰拿铁"，数据库正好有 "生椰拿铁")
            Optional<Product> exactMatch = products.stream()
                    .filter(p -> p.getName().equals(rawKeyword) || (!normalizedKeyword.isBlank() && p.getName().equals(normalizedKeyword)))
                    .findFirst();

            if (exactMatch.isPresent()) {
                selectedProduct = exactMatch.get();
                log.debug("完全匹配商品: {}", selectedProduct.getName());
            } else if (products.size() > 1) {
                // B. 多结果时：尝试自动择优（按销量降序，选第一个）
                // 如果销量相同或都为0，则按创建时间倒序（新品优先）
                Product bestMatch = products.stream()
                        .sorted((p1, p2) -> {
                            int salesCompare = Integer.compare(
                                    p2.getSales() != null ? p2.getSales() : 0,
                                    p1.getSales() != null ? p1.getSales() : 0
                            );
                            if (salesCompare != 0) return salesCompare;
                            // 销量相同，按推荐状态（推荐优先）
                            int recommendCompare = Integer.compare(
                                    p2.getRecommendStatus() != null ? p2.getRecommendStatus() : 0,
                                    p1.getRecommendStatus() != null ? p1.getRecommendStatus() : 0
                            );
                            return recommendCompare;
                        })
                        .findFirst()
                        .orElse(null);

                if (bestMatch != null) {
                    // 如果最佳匹配的商品名包含原始关键词（或归一化关键词），自动选中
                    String bestName = bestMatch.getName();
                    if (bestName.contains(rawKeyword) || (!normalizedKeyword.isBlank() && bestName.contains(normalizedKeyword))) {
                        selectedProduct = bestMatch;
                        log.info("多商品命中，自动择优: {} (销量={})", bestName, bestMatch.getSales());
                    } else {
                        // 置信度不够，反问用户
                        String candidateNames = products.stream()
                                .map(Product::getName)
                                .limit(3)
                                .collect(Collectors.joining("、"));
                        return new AddToCartResponse(false,
                                "找到多款相关商品：" + candidateNames + " 等。请问您具体想要哪一款？");
                    }
                } else {
                    // 兜底：选第一个
                    selectedProduct = products.get(0);
                }
            } else {
                // C. 只有一个结果，直接选中
                selectedProduct = products.get(0);
                log.debug("单结果匹配: {}", selectedProduct.getName());
            }

            ProductDetailVO detail = productService.getMenuDetail(selectedProduct.getId());

            // 【核心优化】：检测是否需要选择规格
            // 如果用户没传规格 (specs为空)，但商品实际上有多个 SKU (skus.size > 1)，则打回请求，强制 AI 去问用户
            List<?> rawSkuList = detail.getSkuList();
            List<SkuStock> skus = null;
            if (rawSkuList != null && !rawSkuList.isEmpty()) {
                // ProductDetailVO.skuList 是 List<?>，这里做一次安全过滤转换，避免未经检查的强转告警
                skus = rawSkuList.stream()
                        .filter(SkuStock.class::isInstance)
                        .map(SkuStock.class::cast)
                        .collect(Collectors.toList());
            }
            boolean isSpecsEmpty = request.specs() == null || request.specs().trim().isEmpty();
            boolean hasMultipleSpecs = skus != null && skus.size() > 1;

            if (isSpecsEmpty && hasMultipleSpecs) {
                // 动态提取该商品的所有可选规格
                String detailedOptions = buildDetailedSpecOptions(skus);
                return new AddToCartResponse(false,
                        "抱歉，该商品（" + selectedProduct.getName() + "）有多种规格可选，请明确您的需求。可选规格：" + detailedOptions);
            }


            // 2. 【企业级】尝试匹配最佳 SKU（严格匹配 + 库存检查）
            SkuStock selectedSku = null;
            if (skus != null && !skus.isEmpty()) {
                selectedSku = findBestSku(skus, request.specs());
                
                // 【企业级】库存检查：如果选中 SKU 库存不足，返回明确错误
                if (selectedSku != null) {
                    int availableStock = selectedSku.getStock() != null ? selectedSku.getStock() : 0;
                    if (availableStock < quantity) {
                        String specDesc = buildCartItemSubTitle(selectedSku, request.specs());
                        log.warn("SKU库存不足: 商品={}, 规格={}, 需要={}, 可用={}",
                                selectedProduct.getName(), specDesc, quantity, availableStock);
                        return new AddToCartResponse(false,
                                String.format("抱歉，%s (%s) 当前库存不足，仅剩 %d 件。", 
                                        selectedProduct.getName(), specDesc, availableStock));
                    }
                }
            }

            // 3. 【企业级】构建购物车项（补齐所有字段）
            OmsCartItem cartItem = new OmsCartItem();
            cartItem.setProductId(selectedProduct.getId());
            cartItem.setProductName(selectedProduct.getName());
            cartItem.setProductPic(selectedProduct.getPicUrl());
            cartItem.setQuantity(quantity);

            if (selectedSku != null) {
                cartItem.setProductSkuId(selectedSku.getId());
                cartItem.setPrice(selectedSku.getPrice());
                // 【企业级】写入 SKU 编码（用于订单追溯）
                if (selectedSku.getSkuCode() != null && !selectedSku.getSkuCode().isBlank()) {
                    cartItem.setProductSkuCode(selectedSku.getSkuCode());
                }
                String subTitle = buildCartItemSubTitle(selectedSku, request.specs());
                cartItem.setProductSubTitle(subTitle != null ? subTitle : selectedProduct.getName());
                log.debug("选中SKU: id={}, code={}, price={}, stock={}",
                        selectedSku.getId(), selectedSku.getSkuCode(), selectedSku.getPrice(), selectedSku.getStock());
            } else {
                cartItem.setPrice(selectedProduct.getPrice());
                String subTitle = buildCartItemSubTitle(null, request.specs());
                cartItem.setProductSubTitle(subTitle);
            }

            // 4. 【企业级】调用 CartService 加入购物车（带详细日志）
            log.info("准备加入购物车: 商品ID={}, SKU ID={}, 数量={}, 价格={}, 用户ID={}",
                    cartItem.getProductId(), cartItem.getProductSkuId(), cartItem.getQuantity(),
                    cartItem.getPrice(), actualUserId);

            Result<String> result = cartService.addToCart(cartItem);

            boolean success = result.getCode() == 200;
            String msg;
            if (success) {
                msg = String.format("已为您将 %d 件 %s (%s) 加入购物车",
                        cartItem.getQuantity(), selectedProduct.getName(), cartItem.getProductSubTitle());
                log.info("加购成功: 商品={}, 数量={}, 用户ID={}", selectedProduct.getName(), cartItem.getQuantity(), actualUserId);
            } else {
                msg = "加入购物车失败：" + (result.getMessage() != null ? result.getMessage() : "未知错误");
                log.warn("加购失败: 商品={}, 原因={}, 用户ID={}", selectedProduct.getName(), msg, actualUserId);
            }

            return new AddToCartResponse(success, msg);

        } catch (IllegalArgumentException e) {
            // 业务校验异常：返回友好提示
            log.warn("加购参数校验失败: {}", e.getMessage());
            return new AddToCartResponse(false, "参数错误：" + e.getMessage());
        } catch (Exception e) {
            // 【企业级】异常处理：记录完整堆栈，返回通用错误（不暴露内部细节）
            log.error("AI 加购工具执行异常: 商品={}, 用户ID={}, 异常类型={}",
                    rawKeyword, actualUserId, e.getClass().getSimpleName(), e);
            return new AddToCartResponse(false, "系统繁忙，请稍后重试。如问题持续，请联系客服。");
        } finally {
            UserContext.remove();
        }
    }

    /**
     * 归一化用户输入的商品关键词，避免把“冰/热/大杯/价格/加入购物车”等混入商品名导致检索不到。
     * 例：冰生椰拿铁大杯 -> 生椰拿铁
     */
    private String normalizeProductKeyword(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.isEmpty()) return "";

        // 去掉空格，降低分词差异
        s = s.replaceAll("\\s+", "");

        // 去掉常见动作/问价词
        String[] actionTokens = {
                "加入购物车", "加购", "下单", "购买", "买", "要", "我要", "我想要", "我需要", "需要",
                "来一杯", "来一杯", "来个", "帮我", "请帮我",
                "价格", "多少钱"
        };
        for (String t : actionTokens) {
            s = s.replace(t, "");
        }

        // 去掉杯型/容量
        String[] sizeTokens = {"大杯", "中杯", "小杯", "杯"};
        for (String t : sizeTokens) {
            s = s.replace(t, "");
        }

        // 去掉“冰的/热的”等描述
        s = s.replace("冰的", "").replace("热的", "");

        // 去掉开头单字温度（例如“冰生椰拿铁” -> “生椰拿铁”）
        if (s.startsWith("冰") || s.startsWith("热")) {
            s = s.substring(1);
        }

        return s.trim();
    }

    /**
     * 聚合所有SKU的规格选项，生成易读的提示字符串
     * 例如：温度: [冰/热]; 糖度: [标准糖/少糖/无糖]
     */
    private String buildDetailedSpecOptions(List<SkuStock> skus) {
        // 使用 LinkedHashMap 保持插入顺序（如温度在前，糖度在后）
        Map<String, Set<String>> optionsMap = new LinkedHashMap<>();

        for (SkuStock sku : skus) {
            String json = sku.getSpec();
            if (json == null || json.isEmpty()) continue;
            try {
                // spec json 示例: [{"key":"温度","value":"冰"},{"key":"糖度","value":"半糖"}]
                List<Map<String, String>> list = objectMapper.readValue(json, new TypeReference<>() {});
                for (Map<String, String> item : list) {
                    String key = item.get("key");
                    String value = item.get("value");
                    if (key != null && value != null) {
                        optionsMap.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(value);
                    }
                }
            } catch (Exception e) {
                // 忽略解析错误的 SKU
                log.warn("SKU规格解析失败: {}", sku.getId());
            }
        }

        if (optionsMap.isEmpty()) return "常规规格";

        // 格式化输出
        StringBuilder sb = new StringBuilder();
        optionsMap.forEach((key, values) -> {
            sb.append(key).append(": [").append(String.join("/", values)).append("]; ");
        });
        return sb.toString().trim();
    }

    /**
     * 【企业级】SKU 匹配逻辑：严格匹配规格值
     * 策略：优先完全匹配 > 部分匹配 > 默认第一个
     */
    private SkuStock findBestSku(List<SkuStock> skuList, String specs) {
        if (skuList == null || skuList.isEmpty()) {
            return null;
        }

        // 如果用户没传规格，返回第一个有库存的 SKU（优先）或第一个 SKU
        if (specs == null || specs.trim().isEmpty()) {
            Optional<SkuStock> inStockSku = skuList.stream()
                    .filter(sku -> sku.getStock() != null && sku.getStock() > 0)
                    .findFirst();
            return inStockSku.orElse(skuList.get(0));
        }

        String normalizedSpecs = specs.trim().replace("，", ",").replace("、", ",");

        // 解析用户输入的规格值（支持逗号分隔）
        List<String> userSpecValues = new ArrayList<>();
        for (String part : normalizedSpecs.split(",")) {
            String cleaned = part.trim();
            if (!cleaned.isEmpty()) {
                // 去掉 key:value 格式中的 key 部分
                int colonIdx = cleaned.indexOf(':');
                if (colonIdx >= 0 && colonIdx < cleaned.length() - 1) {
                    cleaned = cleaned.substring(colonIdx + 1).trim();
                }
                if (!cleaned.isEmpty()) {
                    userSpecValues.add(cleaned);
                }
            }
        }

        if (userSpecValues.isEmpty()) {
            // 用户规格解析失败，返回第一个有库存的
            Optional<SkuStock> inStockSku = skuList.stream()
                    .filter(sku -> sku.getStock() != null && sku.getStock() > 0)
                    .findFirst();
            return inStockSku.orElse(skuList.get(0));
        }

        // 尝试匹配：完全匹配 > 部分匹配
        SkuStock exactMatch = null;
        SkuStock partialMatch = null;
        int maxMatchCount = 0;

        for (SkuStock sku : skuList) {
            String skuSpecJson = sku.getSpec();
            if (skuSpecJson == null || skuSpecJson.isEmpty()) continue;

            try {
                List<Map<String, String>> specItems = objectMapper.readValue(skuSpecJson, new TypeReference<>() {});
                if (specItems == null || specItems.isEmpty()) continue;

                // 提取 SKU 的所有规格值
                List<String> skuSpecValues = specItems.stream()
                        .map(item -> item.get("value"))
                        .filter(v -> v != null && !v.isBlank())
                        .collect(Collectors.toList());

                if (skuSpecValues.isEmpty()) continue;

                // 计算匹配度：用户输入的每个值是否都在 SKU 规格中存在
                int matchCount = 0;
                boolean allUserSpecsMatched = true;
                for (String userValue : userSpecValues) {
                    boolean found = skuSpecValues.stream()
                            .anyMatch(skuValue -> skuValue.contains(userValue) || userValue.contains(skuValue));
                    if (found) {
                        matchCount++;
                    } else {
                        allUserSpecsMatched = false;
                    }
                }

                // 完全匹配：用户所有规格值都在 SKU 中，且 SKU 所有值都在用户输入中
                if (allUserSpecsMatched && userSpecValues.size() == skuSpecValues.size()) {
                    exactMatch = sku;
                    log.debug("SKU完全匹配: id={}, specs={}", sku.getId(), skuSpecValues);
                    break; // 找到完全匹配，直接返回
                }

                // 部分匹配：记录匹配度最高的
                if (matchCount > maxMatchCount) {
                    maxMatchCount = matchCount;
                    partialMatch = sku;
                }
            } catch (Exception e) {
                log.warn("解析 SKU 规格异常: skuId={}, error={}", sku.getId(), e.getMessage());
            }
        }

        // 返回：完全匹配 > 部分匹配 > 第一个有库存的 > 第一个
        if (exactMatch != null) {
            return exactMatch;
        }
        if (partialMatch != null) {
            log.debug("SKU部分匹配: id={}, matchCount={}", partialMatch.getId(), maxMatchCount);
            return partialMatch;
        }

        // 兜底：返回第一个有库存的 SKU
        Optional<SkuStock> inStockSku = skuList.stream()
                .filter(sku -> sku.getStock() != null && sku.getStock() > 0)
                .findFirst();
        SkuStock fallback = inStockSku.orElse(skuList.get(0));
        log.debug("SKU匹配失败，使用默认: id={}", fallback.getId());
        return fallback;
    }

    /**
     * 生成入库用的标准规格串：大杯,热,半糖（与点单页保持一致）
     * - 优先使用已匹配到的 SKU 的 spec(JSON) 生成，保证顺序与值准确
     * - 若拿不到 SKU，则对传入的 specs 文本做清洗/拆分，尽量补齐逗号分隔
     */
    private String buildCartItemSubTitle(SkuStock selectedSku, String specsText) {
        String fromSku = buildSubTitleFromSkuSpecJson(selectedSku);
        if (fromSku != null && !fromSku.isBlank()) return fromSku;
        return normalizeSpecsToCommaSeparated(specsText);
    }

    private String buildSubTitleFromSkuSpecJson(SkuStock sku) {
        if (sku == null) return null;
        String json = sku.getSpec();
        if (json == null || json.isBlank()) return null;
        try {
            // spec json 示例: [{"key":"温度","value":"热"},{"key":"容量","value":"大杯"},{"key":"糖度","value":"半糖"}]
            List<Map<String, String>> list = objectMapper.readValue(json, new TypeReference<>() {});
            if (list == null || list.isEmpty()) return null;
            // 只取 value，并用逗号拼接
            List<String> values = list.stream()
                    .map(m -> m.get("value"))
                    .filter(v -> v != null && !v.isBlank())
                    .collect(Collectors.toList());
            if (values.isEmpty()) return null;
            return String.join(",", values);
        } catch (Exception e) {
            log.warn("SKU规格JSON解析失败: {}", sku.getId());
            return null;
        }
    }

    private String normalizeSpecsToCommaSeparated(String specsText) {
        if (specsText == null) return null;
        String s = specsText.trim();
        if (s.isEmpty()) return null;

        // 统一分隔符/去空白
        s = s.replace("，", ",")
                .replace("、", ",")
                .replace("；", ",")
                .replace(";", ",")
                .replace("|", ",")
                .replaceAll("\\s+", "");

        // 形如：容量:大杯,温度:热,糖度:半糖 -> 大杯,热,半糖
        if (s.contains(":") || s.contains("：")) {
            String[] parts = s.split(",");
            List<String> values = new ArrayList<>();
            for (String p : parts) {
                if (p == null || p.isBlank()) continue;
                String seg = p;
                int idx = seg.indexOf(':');
                if (idx < 0) idx = seg.indexOf('：');
                if (idx >= 0 && idx < seg.length() - 1) {
                    seg = seg.substring(idx + 1);
                }
                if (!seg.isBlank()) values.add(seg);
            }
            if (!values.isEmpty()) {
                return String.join(",", values);
            }
        }

        // 已经是逗号分隔则直接返回（同时去掉重复逗号）
        if (s.contains(",")) {
            String[] parts = s.split(",");
            List<String> values = new ArrayList<>();
            for (String p : parts) {
                if (p != null && !p.isBlank()) values.add(p);
            }
            return values.isEmpty() ? null : String.join(",", values);
        }

        // 尝试从无分隔的短串中提取常见规格（大杯/中杯/小杯 + 冰/热 + 糖度 + 去冰/少冰）
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        String[] sizeTokens = {"大杯", "中杯", "小杯"};
        String[] tempTokens = {"热", "冰", "常温"};
        String[] sugarTokens = {"全糖", "标准糖", "半糖", "少糖", "无糖", "不加糖"};
        String[] iceTokens = {"去冰", "少冰", "多冰", "正常冰"};

        for (String t : sizeTokens) if (s.contains(t)) tokens.add(t);
        // 温度优先：避免把“冰的”这类残留带进来
        for (String t : tempTokens) if (s.contains(t)) tokens.add(t);
        for (String t : sugarTokens) if (s.contains(t)) tokens.add(t);
        for (String t : iceTokens) if (s.contains(t)) tokens.add(t);

        if (!tokens.isEmpty()) {
            return String.join(",", tokens);
        }

        // 兜底：返回原始清洗串
        return s;
    }

    @JsonClassDescription("加购请求参数")
    public record AddToCartRequest(
            @JsonPropertyDescription("商品模糊名称，如'热美式','冰拿铁'") String productName,
            @JsonPropertyDescription("商品规格描述，如'少糖','大杯','去冰'") String specs,
            @JsonPropertyDescription("购买数量，默认为1") int quantity,
            @JsonPropertyDescription("用户ID，必须从上下文获取并传递") String userId
    ) {}

    public record AddToCartResponse(boolean success, String message) {}
}