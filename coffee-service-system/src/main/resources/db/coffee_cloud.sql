/*
 Navicat Premium Data Transfer

 Source Server         : MySQL
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : coffee_cloud

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 23/12/2025 17:38:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_knowledge_doc
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_doc`;
CREATE TABLE `ai_knowledge_doc`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件名',
  `filepath` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件路径',
  `vectorized_status` int NULL DEFAULT 0 COMMENT '向量化状态：0->未处理；1->处理中；2->已完成；3->失败',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI知识库文档表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_knowledge_doc
-- ----------------------------

-- ----------------------------
-- Table structure for oms_cart_item
-- ----------------------------
DROP TABLE IF EXISTS `oms_cart_item`;
CREATE TABLE `oms_cart_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `product_id` bigint NULL DEFAULT NULL COMMENT '商品ID',
  `product_sku_id` bigint NULL DEFAULT NULL COMMENT 'SKU ID',
  `product_pic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品主图',
  `product_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品名称',
  `product_sub_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品副标题(SKU规格描述)',
  `product_sku_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SKU条码',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '加入时价格',
  `quantity` int NULL DEFAULT 1 COMMENT '购买数量',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_cart_item
-- ----------------------------

-- ----------------------------
-- Table structure for oms_order
-- ----------------------------
DROP TABLE IF EXISTS `oms_order`;
CREATE TABLE `oms_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `pickup_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取餐码 (例如 A101)',
  `member_id` bigint NOT NULL COMMENT '用户ID',
  `coupon_id` bigint NULL DEFAULT NULL COMMENT '优惠券ID',
  `total_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '订单总金额',
  `promotion_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '促销优化金额（促销价、满减、阶梯价）',
  `coupon_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '优惠券抵扣金额',
  `pay_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '应付金额（实际支付金额）',
  `pay_type` int NULL DEFAULT 1 COMMENT '支付方式：0->未支付；1->支付宝；2->微信',
  `status` int NULL DEFAULT 1 COMMENT '订单状态：0->待付款；1->待制作；2->制作中；3->待取餐；4->已完成；5->已取消',
  `delivery_company` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流公司(配送方式)',
  `delivery_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流单号',
  `receiver_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人电话',
  `receiver_post_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人邮编',
  `receiver_province` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '省份/直辖市',
  `receiver_city` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '城市',
  `receiver_region` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '区',
  `receiver_detail_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单备注',
  `confirm_status` int NULL DEFAULT 0 COMMENT '确认收货状态：0->未确认；1->已确认',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `delivery_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime NULL DEFAULT NULL COMMENT '确认收货时间',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_sn`(`order_sn` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_order
-- ----------------------------
INSERT INTO `oms_order` VALUES (1, '2025121917153016b5c89', NULL, 1, NULL, 20.00, 0.00, 0.00, 20.00, 1, 3, '门店自提', NULL, '张三', '13800138000', '518000', '广东省', '深圳市', '南山区', '粤海街道科技园中区科兴科学园B栋301', '少冰', 0, NULL, NULL, NULL, '2025-12-19 17:15:31', '2025-12-23 16:48:50');
INSERT INTO `oms_order` VALUES (2, '202512221352321470791', NULL, 1, NULL, 20.00, 0.00, 0.00, 20.00, 0, 5, '门店自提', NULL, '张三', '13800138000', '518000', '广东省', '深圳市', '南山区', '粤海街道科技园中区科兴科学园B栋301', '', 0, NULL, NULL, NULL, '2025-12-22 13:52:33', '2025-12-23 16:14:38');
INSERT INTO `oms_order` VALUES (3, '202512231724491f96382', '101', 1, NULL, 42.00, 0.00, 0.00, 42.00, 0, 0, '门店自提', NULL, '张三', '13800138000', '518000', '广东省', '深圳市', '南山区', '粤海街道科技园中区科兴科学园B栋301', '', 0, NULL, NULL, NULL, '2025-12-23 17:24:49', '2025-12-23 17:24:49');
INSERT INTO `oms_order` VALUES (4, '2025122317281316eb1f4', '102', 1, NULL, 25.00, 0.00, 0.00, 25.00, 1, 1, '门店自提', NULL, '张三', '13800138000', '518000', '广东省', '深圳市', '南山区', '粤海街道科技园中区科兴科学园B栋301', '', 0, NULL, NULL, NULL, '2025-12-23 17:28:14', '2025-12-23 17:28:59');

-- ----------------------------
-- Table structure for oms_order_item
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_item`;
CREATE TABLE `oms_order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `product_id` bigint NULL DEFAULT NULL COMMENT '商品ID',
  `product_pic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片',
  `product_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品名称',
  `product_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品货号',
  `product_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '销售价格',
  `product_quantity` int NULL DEFAULT NULL COMMENT '购买数量',
  `product_sku_id` bigint NULL DEFAULT NULL COMMENT '商品SKU ID',
  `product_sku_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品SKU条码',
  `product_attr` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品销售属性:[{\"key\":\"规格\",\"value\":\"大杯\"}]',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_item
-- ----------------------------
INSERT INTO `oms_order_item` VALUES (1, 1, '2025121917153016b5c89', 1, 'https://images.unsplash.com/photo-1541167760496-1628856ab772?q=80&w=200&auto=format&fit=crop', '生椰拿铁', NULL, 20.00, 1, 2, NULL, '大杯,冰,半糖', '2025-12-19 17:15:31');
INSERT INTO `oms_order_item` VALUES (2, 2, '202512221352321470791', 1, 'https://images.unsplash.com/photo-1541167760496-1628856ab772?q=80&w=200&auto=format&fit=crop', '生椰拿铁', NULL, 20.00, 1, 1, NULL, '大杯,冰,全糖', '2025-12-22 13:52:33');
INSERT INTO `oms_order_item` VALUES (3, 3, '202512231724491f96382', 1, 'https://images.unsplash.com/photo-1541167760496-1628856ab772?q=80&w=200&auto=format&fit=crop', '生椰拿铁', NULL, 20.00, 1, 1, NULL, '大杯,冰,全糖', '2025-12-23 17:24:49');
INSERT INTO `oms_order_item` VALUES (4, 3, '202512231724491f96382', 21, 'http://192.168.110.110:8081/api/app/image/proxy?url=http%3A%2F%2F192.168.248.128%3A9000%2Fcoffee-bucket%2Fproduct%2Fa147c5273d844ae0ad287d30fce5b1fb.png', '香草拿铁', NULL, 22.00, 1, 11, NULL, '中杯,热,标准', '2025-12-23 17:24:49');
INSERT INTO `oms_order_item` VALUES (5, 4, '2025122317281316eb1f4', 21, 'http://192.168.110.110:8081/api/app/image/proxy?url=http%3A%2F%2F192.168.248.128%3A9000%2Fcoffee-bucket%2Fproduct%2Fa147c5273d844ae0ad287d30fce5b1fb.png', '香草拿铁', NULL, 25.00, 1, 12, NULL, '大杯,热,标准', '2025-12-23 17:28:14');

-- ----------------------------
-- Table structure for pms_category
-- ----------------------------
DROP TABLE IF EXISTS `pms_category`;
CREATE TABLE `pms_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类图标',
  `sort` int NULL DEFAULT 0 COMMENT '排序权重, 值越大越靠前',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pms_category
-- ----------------------------
INSERT INTO `pms_category` VALUES (1, '咖啡', 'https://cdn-icons-png.flaticon.com/512/2935/2935413.png', 100, 1, '2025-12-16 15:13:35', '2025-12-16 15:13:35');
INSERT INTO `pms_category` VALUES (2, '茶饮', 'https://cdn-icons-png.flaticon.com/512/924/924514.png', 90, 1, '2025-12-16 15:13:35', '2025-12-16 15:13:35');
INSERT INTO `pms_category` VALUES (3, '甜点', 'https://cdn-icons-png.flaticon.com/512/3081/3081840.png', 80, 1, '2025-12-16 15:13:35', '2025-12-16 15:13:35');
INSERT INTO `pms_category` VALUES (4, '烘焙', 'https://cdn-icons-png.flaticon.com/512/992/992747.png', 70, 1, '2025-12-16 15:13:35', '2025-12-16 15:13:35');
INSERT INTO `pms_category` VALUES (5, '轻食', 'https://cdn-icons-png.flaticon.com/512/2515/2515127.png', 60, 1, '2025-12-16 15:13:35', '2025-12-16 15:13:35');
INSERT INTO `pms_category` VALUES (6, '周边', 'https://cdn-icons-png.flaticon.com/512/862/862856.png', 50, 1, '2025-12-16 15:13:35', '2025-12-16 15:13:35');
INSERT INTO `pms_category` VALUES (7, '人气新品', 'https://cdn-icons-png.flaticon.com/512/785/785116.png', 200, 1, '2025-12-17 10:00:47', '2025-12-17 10:00:47');

-- ----------------------------
-- Table structure for pms_product
-- ----------------------------
DROP TABLE IF EXISTS `pms_product`;
CREATE TABLE `pms_product`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `price` decimal(10, 2) NOT NULL COMMENT '价格',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类(如: 咖啡, 甜点)',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `pic_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片地址',
  `sales` int NULL DEFAULT 0 COMMENT '销量',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态: 1-上架, 0-下架',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pms_product
-- ----------------------------
INSERT INTO `pms_product` VALUES (1, '生椰拿铁', 18.00, 7, 'YYDS的生椰拿铁，清爽好喝', 'https://images.unsplash.com/photo-1541167760496-1628856ab772?q=80&w=200&auto=format&fit=crop', 2000, 1, '2025-12-05 23:23:00', '2025-12-22 13:10:56');
INSERT INTO `pms_product` VALUES (2, '美式咖啡', 12.00, 1, '提神醒脑，打工人必备', 'https://images.unsplash.com/photo-1497935586351-b67a49e012bf?q=80&w=200&auto=format&fit=crop', 0, 1, '2025-12-05 23:23:00', '2025-12-16 14:53:00');
INSERT INTO `pms_product` VALUES (3, '提拉米苏', 22.00, 1, '入口即化，甜蜜享受', 'https://images.unsplash.com/photo-1571115177098-24ec42ed204d?q=80&w=200&auto=format&fit=crop', 0, 1, '2025-12-05 23:23:00', '2025-12-16 14:53:00');
INSERT INTO `pms_product` VALUES (4, '焦糖玛奇朵', 32.00, 1, '精选深度烘焙的阿拉比卡咖啡豆，融合丝滑蒸奶与绵密奶泡，顶部淋上香甜浓郁的手工熬制焦糖酱。层次分明，入口先是焦糖的甜香，随后是咖啡的醇厚，甜蜜中带有微苦的复杂口感，适合喜欢丰富层次感的您。', 'https://images.unsplash.com/photo-1485808191679-5f86510681a2?q=80&w=600&auto=format&fit=crop', 120, 1, '2025-12-16 14:47:20', '2025-12-16 14:53:00');
INSERT INTO `pms_product` VALUES (5, '法式草莓拿破仑', 45.00, 1, '经典法式甜品代表，采用72层手工起酥工艺，酥皮金黄酥脆，层次分明。夹层填充特制香草卡士达酱与新鲜采摘的有机红颜草莓，每一口都能感受到酥脆与柔滑的完美碰撞，甜而不腻，果香四溢。', 'https://images.unsplash.com/photo-1565958011703-44f9829ba187?q=80&w=600&auto=format&fit=crop', 85, 1, '2025-12-16 14:47:20', '2025-12-16 14:53:00');
INSERT INTO `pms_product` VALUES (6, '冷萃耶加雪菲', 38.00, 1, '选用埃塞俄比亚耶加雪菲G1级咖啡豆，经过12小时低温慢速萃取。有效降低了咖啡的酸涩感，最大限度保留了豆子本身的花香与柑橘风味。口感干净清爽，带有明亮的果酸和回甘，是炎炎夏日的解暑神器。', 'https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?q=80&w=600&auto=format&fit=crop', 210, 1, '2025-12-16 14:47:20', '2025-12-16 14:53:00');
INSERT INTO `pms_product` VALUES (7, '宇治抹茶千层', 36.00, 1, '严选日本宇治丸久小山园抹茶粉，手工煎制20余层极薄的可丽饼皮。每层之间均匀涂抹特调抹茶奶油，入口即化，茶香浓郁深邃，微苦回甘，顶部撒满细腻抹茶粉，为您带来极致的日式风味体验。', 'https://images.unsplash.com/photo-1505252585461-04db1eb84625?q=80&w=600&auto=format&fit=crop', 156, 1, '2025-12-16 14:47:20', '2025-12-16 14:53:00');
INSERT INTO `pms_product` VALUES (8, '海盐芝士拿铁', 34.00, 1, '创新风味拿铁，在浓缩咖啡与鲜奶的基础上，覆盖一层厚实的喜马拉雅粉红海盐芝士奶盖。咸甜交织的奇妙口感，极大地提升了咖啡的香气，口感顺滑细腻，独特的咸味更能衬托出牛奶的香甜。', 'https://images.unsplash.com/photo-1541167760496-1628856ab772?q=80&w=600&auto=format&fit=crop', 98, 1, '2025-12-16 14:47:20', '2025-12-16 14:53:00');
INSERT INTO `pms_product` VALUES (9, '伯爵红茶司康', 18.00, 1, '传统英式下午茶必备点心，面团中揉入上等伯爵红茶碎与糖渍橙皮丁。外层烘烤至酥松，内里保持湿润松软，散发出佛手柑的独特清香。建议加热后搭配凝结奶油（Clotted Cream）或草莓果酱食用，风味更佳。', 'https://images.unsplash.com/photo-1571115177098-24ec42ed204d?q=80&w=600&auto=format&fit=crop', 340, 1, '2025-12-16 14:47:20', '2025-12-16 14:53:00');
INSERT INTO `pms_product` VALUES (10, '焦糖玛奇朵', 32.00, 1, '精选深度烘焙的阿拉比卡咖啡豆，融合丝滑蒸奶与绵密奶泡，顶部淋上香甜浓郁的手工熬制焦糖酱。层次分明，入口先是焦糖的甜香，随后是咖啡的醇厚，甜蜜中带有微苦的复杂口感。', 'https://images.unsplash.com/photo-1485808191679-5f86510681a2?q=80&w=600&auto=format&fit=crop', 120, 1, '2025-12-16 14:53:31', '2025-12-16 14:53:31');
INSERT INTO `pms_product` VALUES (11, '冷萃耶加雪菲', 38.00, 1, '选用埃塞俄比亚耶加雪菲G1级咖啡豆，经过12小时低温慢速萃取。有效降低了咖啡的酸涩感，最大限度保留了豆子本身的花香与柑橘风味。口感干净清爽，带有明亮的果酸和回甘。', 'https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?q=80&w=600&auto=format&fit=crop', 210, 1, '2025-12-16 14:53:31', '2025-12-23 15:40:28');
INSERT INTO `pms_product` VALUES (12, '海盐芝士拿铁', 34.00, 1, '创新风味拿铁，在浓缩咖啡与鲜奶的基础上，覆盖一层厚实的喜马拉雅粉红海盐芝士奶盖。咸甜交织的奇妙口感，极大地提升了咖啡的香气，口感顺滑细腻，独特的咸味更能衬托出牛奶的香甜。', 'https://images.unsplash.com/photo-1541167760496-1628856ab772?q=80&w=600&auto=format&fit=crop', 98, 1, '2025-12-16 14:53:31', '2025-12-16 14:53:31');
INSERT INTO `pms_product` VALUES (13, '满杯红柚', 28.00, 2, '严选南非红宝石西柚，果肉饱满多汁。搭配清雅茉莉绿茶底，茶香与果香完美融合。每一口都能吸到爆汁的西柚果粒，酸甜解腻，维C满满，是夏日里的一抹清凉。', 'https://images.unsplash.com/photo-1546173159-315724a31696?q=80&w=600&auto=format&fit=crop', 350, 1, '2025-12-16 14:53:31', '2025-12-16 14:53:31');
INSERT INTO `pms_product` VALUES (14, '芝士白桃乌龙', 26.00, 2, '选用高山白桃乌龙茶作为茶底，香气高扬持久。顶部覆盖3cm厚打发咸芝士奶盖，撒上少许抹茶粉点缀。建议45度角大口饮用，同时品尝到奶盖的绵密与茶汤的清冽。', 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?q=80&w=600&auto=format&fit=crop', 280, 1, '2025-12-16 14:53:31', '2025-12-16 14:53:31');
INSERT INTO `pms_product` VALUES (15, '法式草莓拿破仑', 45.00, 3, '经典法式甜品代表，采用72层手工起酥工艺，酥皮金黄酥脆，层次分明。夹层填充特制香草卡士达酱与新鲜采摘的有机红颜草莓，每一口都能感受到酥脆与柔滑的完美碰撞。', 'https://images.unsplash.com/photo-1565958011703-44f9829ba187?q=80&w=600&auto=format&fit=crop', 85, 1, '2025-12-16 14:53:31', '2025-12-16 14:53:31');
INSERT INTO `pms_product` VALUES (16, '宇治抹茶千层', 36.00, 3, '严选日本宇治丸久小山园抹茶粉，手工煎制20余层极薄的可丽饼皮。每层之间均匀涂抹特调抹茶奶油，入口即化，茶香浓郁深邃，微苦回甘，顶部撒满细腻抹茶粉。', 'https://images.unsplash.com/photo-1505252585461-04db1eb84625?q=80&w=600&auto=format&fit=crop', 156, 1, '2025-12-16 14:53:31', '2025-12-16 14:53:31');
INSERT INTO `pms_product` VALUES (17, '经典黄油可颂', 18.00, 4, '使用法国AOP产区发酵黄油，经过多次折叠擀压，形成蜂窝状的完美切面。外壳酥脆掉渣，内里柔软充满空气感，浓郁的黄油香气在唇齿间久久不散，早餐的最佳拍档。', 'https://images.unsplash.com/photo-1555507036-ab1f4038808a?q=80&w=600&auto=format&fit=crop', 420, 1, '2025-12-16 14:53:31', '2025-12-16 14:53:31');
INSERT INTO `pms_product` VALUES (18, '蓝莓乳酪贝果', 22.00, 4, '低糖低油的健康贝果，面团经过水煮工艺，口感扎实有嚼劲。内馅包裹着大颗蓝莓果肉与顺滑奶油奶酪，经过烘烤后爆浆流心，酸甜适口，越嚼越香。', 'https://images.unsplash.com/photo-1517433670267-08bbd4be890f?q=80&w=600&auto=format&fit=crop', 190, 1, '2025-12-16 14:53:31', '2025-12-16 14:53:31');
INSERT INTO `pms_product` VALUES (19, '烟熏鸡胸肉三明治', 38.00, 5, '精选全麦吐司，夹入低温慢煮的烟熏鸡胸肉，肉质鲜嫩不柴。搭配新鲜生菜、番茄片、黄瓜与特制低脂蜂蜜芥末酱。营养均衡，饱腹感强，是健身人士的首选。', 'https://images.unsplash.com/photo-1528735602780-2552fd46c7af?q=80&w=600&auto=format&fit=crop', 160, 1, '2025-12-16 14:53:31', '2025-12-16 14:53:31');
INSERT INTO `pms_product` VALUES (20, '品牌纪念马克杯', 88.00, 6, '独家定制的品牌纪念马克杯，采用优质骨瓷烧制，质地轻盈透光。极简设计风格，杯身印有经典Logo。宽口设计适合拉花，为您居家品尝咖啡增添一份仪式感。', 'https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?q=80&w=600&auto=format&fit=crop', 50, 1, '2025-12-16 14:53:31', '2025-12-16 14:53:31');
INSERT INTO `pms_product` VALUES (21, '香草拿铁', 25.00, 1, '精选阿拉比卡咖啡豆，融合香草糖浆与丝滑蒸奶，顶部覆盖绵密奶泡。口感层次丰富，香草的甜美与咖啡的醇厚完美融合，带来温暖舒适的味觉体验。', 'http://192.168.248.128:9000/coffee-bucket/product/a147c5273d844ae0ad287d30fce5b1fb.png', 0, 1, '2025-12-23 14:52:45', '2025-12-23 15:48:29');

-- ----------------------------
-- Table structure for pms_sku_stock
-- ----------------------------
DROP TABLE IF EXISTS `pms_sku_stock`;
CREATE TABLE `pms_sku_stock`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `sku_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SKU编码',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '价格',
  `stock` int NULL DEFAULT 0 COMMENT '库存',
  `low_stock` int NULL DEFAULT NULL COMMENT '预警库存',
  `spec` json NULL COMMENT '规格属性(JSON格式)',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SKU库存表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pms_sku_stock
-- ----------------------------
INSERT INTO `pms_sku_stock` VALUES (1, 1, '2024010101', 20.00, 100, 10, '[{\"key\": \"容量\", \"value\": \"大杯\"}, {\"key\": \"温度\", \"value\": \"冰\"}, {\"key\": \"糖度\", \"value\": \"全糖\"}]', '2025-12-10 15:07:14', '2025-12-10 15:07:14');
INSERT INTO `pms_sku_stock` VALUES (2, 1, '2024010102', 20.00, 100, 10, '[{\"key\": \"容量\", \"value\": \"大杯\"}, {\"key\": \"温度\", \"value\": \"冰\"}, {\"key\": \"糖度\", \"value\": \"半糖\"}]', '2025-12-10 15:07:14', '2025-12-10 15:07:14');
INSERT INTO `pms_sku_stock` VALUES (3, 1, '2024010103', 20.00, 100, 10, '[{\"key\": \"容量\", \"value\": \"大杯\"}, {\"key\": \"温度\", \"value\": \"热\"}, {\"key\": \"糖度\", \"value\": \"全糖\"}]', '2025-12-10 15:07:14', '2025-12-10 15:07:14');
INSERT INTO `pms_sku_stock` VALUES (4, 1, '2024010104', 18.00, 100, 10, '[{\"key\": \"容量\", \"value\": \"中杯\"}, {\"key\": \"温度\", \"value\": \"冰\"}, {\"key\": \"糖度\", \"value\": \"全糖\"}]', '2025-12-10 15:07:14', '2025-12-10 15:07:14');
INSERT INTO `pms_sku_stock` VALUES (5, 1, '2024010105', 18.00, 100, 10, '[{\"key\": \"容量\", \"value\": \"中杯\"}, {\"key\": \"温度\", \"value\": \"热\"}, {\"key\": \"糖度\", \"value\": \"半糖\"}]', '2025-12-10 15:07:14', '2025-12-10 15:07:14');
INSERT INTO `pms_sku_stock` VALUES (6, 2, '2024020101', 14.00, 200, 20, '[{\"key\": \"容量\", \"value\": \"大杯\"}, {\"key\": \"温度\", \"value\": \"冰\"}]', '2025-12-10 15:07:14', '2025-12-10 15:07:14');
INSERT INTO `pms_sku_stock` VALUES (7, 2, '2024020102', 14.00, 200, 20, '[{\"key\": \"容量\", \"value\": \"大杯\"}, {\"key\": \"温度\", \"value\": \"热\"}]', '2025-12-10 15:07:14', '2025-12-10 15:07:14');
INSERT INTO `pms_sku_stock` VALUES (8, 2, '2024020103', 12.00, 200, 20, '[{\"key\": \"容量\", \"value\": \"中杯\"}, {\"key\": \"温度\", \"value\": \"冰\"}]', '2025-12-10 15:07:14', '2025-12-10 15:07:14');
INSERT INTO `pms_sku_stock` VALUES (9, 2, '2024020104', 12.00, 200, 20, '[{\"key\": \"容量\", \"value\": \"中杯\"}, {\"key\": \"温度\", \"value\": \"热\"}]', '2025-12-10 15:07:14', '2025-12-10 15:07:14');
INSERT INTO `pms_sku_stock` VALUES (10, 3, '2024030101', 22.00, 50, 5, '[{\"key\": \"规格\", \"value\": \"标准份\"}]', '2025-12-10 15:07:14', '2025-12-10 15:07:14');
INSERT INTO `pms_sku_stock` VALUES (11, 21, '', 22.00, 100, NULL, '[{\"key\": \"容量\", \"value\": \"中杯\"}, {\"key\": \"温度\", \"value\": \"热\"}, {\"key\": \"糖度\", \"value\": \"标准\"}]', '2025-12-23 14:52:45', '2025-12-23 14:52:45');
INSERT INTO `pms_sku_stock` VALUES (12, 21, '', 25.00, 100, NULL, '[{\"key\": \"容量\", \"value\": \"大杯\"}, {\"key\": \"温度\", \"value\": \"热\"}, {\"key\": \"糖度\", \"value\": \"标准\"}]', '2025-12-23 14:52:45', '2025-12-23 14:52:45');
INSERT INTO `pms_sku_stock` VALUES (13, 11, '', 38.00, 100, NULL, '[{\"key\": \"容量\", \"value\": \"大杯\"}, {\"key\": \"温度\", \"value\": \"热\"}]', '2025-12-23 15:40:28', '2025-12-23 15:40:28');

-- ----------------------------
-- Table structure for ums_member
-- ----------------------------
DROP TABLE IF EXISTS `ums_member`;
CREATE TABLE `ums_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `gender` int NULL DEFAULT 0 COMMENT '性别：0->未知；1->男；2->女',
  `status` int NULL DEFAULT 1 COMMENT '用户状态：1-正常 0-禁用',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所做城市',
  `job` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职业',
  `signature` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个性签名',
  `integration` int NULL DEFAULT 0 COMMENT '积分',
  `growth` int NULL DEFAULT 0 COMMENT '成长值',
  `level_id` bigint NULL DEFAULT NULL COMMENT '会员等级ID',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '会员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ums_member
-- ----------------------------
INSERT INTO `ums_member` VALUES (1, 'test_user', '13800138000', '$2a$10$qER.iOY2WSDk/8vRUXv9BeLrKgfAxVXpezwHTdXB6NxtwZWlQmMQ2', '自信阿德勒', 'http://192.168.248.128:9000/coffee-bucket/avatar/07b8af66566f4e67836b52f9fafed6d6.jpg', 2, 1, '1999-12-22', '广东省 汕尾市 城区', NULL, '', 0, 0, 1, '2025-12-07 18:23:53', '2025-12-23 15:13:00');
INSERT INTO `ums_member` VALUES (8, 'admin', '1380013802', '$2a$10$2asM8m6GCZYchML7cKQdaurUDLEA8Ulrv5tkTiTZKsVv6MCDuaYva', 'admin', NULL, 1, 1, NULL, NULL, NULL, NULL, 0, 0, 1, '2025-12-07 18:23:53', '2025-12-13 17:04:19');
INSERT INTO `ums_member` VALUES (9, 'test_user2', '13800138001', '.ZpFBbxBmMAdLP7xCisGHi', 'test_user2', NULL, 2, 1, NULL, NULL, NULL, NULL, 0, 0, 1, '2025-12-07 18:23:53', '2025-12-13 16:54:33');

-- ----------------------------
-- Table structure for ums_member_level
-- ----------------------------
DROP TABLE IF EXISTS `ums_member_level`;
CREATE TABLE `ums_member_level`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '等级名称',
  `growth_point` int NULL DEFAULT 0 COMMENT '成长值门槛（达到此成长值可升级到此等级）',
  `discount` decimal(3, 2) NULL DEFAULT 1.00 COMMENT '折扣率（0.00-1.00，1.00表示无折扣）',
  `integration_rate` decimal(3, 2) NULL DEFAULT 1.00 COMMENT '积分倍率（积分获取倍率，1.00表示正常倍率）',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '等级图标',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '等级描述',
  `status` int NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员等级表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ums_member_level
-- ----------------------------
INSERT INTO `ums_member_level` VALUES (1, '普通会员', 0, 1.00, 1.00, NULL, '新注册用户默认等级', 1, '2025-12-13 00:00:00', '2025-12-13 17:18:16');
INSERT INTO `ums_member_level` VALUES (2, '银卡会员', 100, 0.95, 1.10, NULL, '成长值达到100可升级，享受95折优惠，积分获取1.1倍', 1, '2025-12-13 00:00:00', '2025-12-13 00:00:00');
INSERT INTO `ums_member_level` VALUES (3, '金卡会员', 500, 0.90, 1.20, NULL, '成长值达到500可升级，享受9折优惠，积分获取1.2倍', 1, '2025-12-13 00:00:00', '2025-12-13 00:00:00');
INSERT INTO `ums_member_level` VALUES (4, '钻石会员', 2000, 0.85, 1.50, NULL, '成长值达到2000可升级，享受85折优惠，积分获取1.5倍', 1, '2025-12-13 00:00:00', '2025-12-13 00:00:00');

-- ----------------------------
-- Table structure for ums_member_receive_address
-- ----------------------------
DROP TABLE IF EXISTS `ums_member_receive_address`;
CREATE TABLE `ums_member_receive_address`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人名称',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人电话',
  `default_status` int NULL DEFAULT 0 COMMENT '是否为默认地址：0->否；1->是',
  `post_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮政编码',
  `province` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '省份/直辖市',
  `city` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '城市',
  `region` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '区',
  `detail_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址(街道)',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '会员收货地址表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ums_member_receive_address
-- ----------------------------
INSERT INTO `ums_member_receive_address` VALUES (1, 1, '张三', '13800138000', 1, '518000', '广东省', '深圳市', '南山区', '粤海街道科技园中区科兴科学园B栋301', '2025-12-10 15:53:18', '2025-12-22 15:33:23');
INSERT INTO `ums_member_receive_address` VALUES (2, 1, '张三(公司)', '13800138000', 0, '518057', '广东省', '深圳市', '福田区', '福田街道平安金融中心108层', '2025-12-10 15:53:18', '2025-12-22 15:33:23');
INSERT INTO `ums_member_receive_address` VALUES (3, 8, '李四', '13900139000', 1, '100000', '北京市', '北京市', '朝阳区', '建国门外街道国贸三期55层', '2025-12-10 15:53:18', '2025-12-10 15:53:18');

-- ----------------------------
-- Table structure for ums_permission
-- ----------------------------
DROP TABLE IF EXISTS `ums_permission`;
CREATE TABLE `ums_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限描述',
  `url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限路径',
  `method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方法(GET/POST/PUT/DELETE)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ums_permission
-- ----------------------------
INSERT INTO `ums_permission` VALUES (1, 'pms:product:list', '查看商品列表', '/admin/product/list', 'GET', '2025-12-07 23:00:32', '2025-12-07 23:00:32');
INSERT INTO `ums_permission` VALUES (2, 'pms:product:add', '添加商品', '/admin/product/add', 'POST', '2025-12-07 23:00:32', '2025-12-07 23:00:32');
INSERT INTO `ums_permission` VALUES (3, 'pms:product:update', '修改商品信息', '/admin/product/update', 'POST', '2025-12-07 23:00:32', '2025-12-07 23:00:32');
INSERT INTO `ums_permission` VALUES (4, 'pms:product:delete', '删除商品', '/admin/product/delete/*', 'DELETE', '2025-12-07 23:00:32', '2025-12-07 23:00:32');
INSERT INTO `ums_permission` VALUES (5, 'pms:product:status', '修改商品上下架状态', '/admin/product/updateStatus', 'POST', '2025-12-07 23:00:32', '2025-12-07 23:00:32');
INSERT INTO `ums_permission` VALUES (6, 'oms:order:list', '查看订单列表', '/admin/order/list', 'GET', '2025-12-07 23:00:32', '2025-12-07 23:00:32');
INSERT INTO `ums_permission` VALUES (7, 'oms:order:detail', '查看订单详情', '/admin/order/detail/*', 'GET', '2025-12-07 23:00:32', '2025-12-07 23:00:32');
INSERT INTO `ums_permission` VALUES (8, 'oms:order:status', '修改订单状态(制作中/已完成)', '/admin/order/updateStatus', 'POST', '2025-12-07 23:00:32', '2025-12-07 23:00:32');
INSERT INTO `ums_permission` VALUES (9, 'oms:order:delete', '删除订单', '/admin/order/delete/*', 'DELETE', '2025-12-07 23:00:32', '2025-12-07 23:00:32');
INSERT INTO `ums_permission` VALUES (10, 'oms:order:refund', '订单退款处理', '/admin/order/refund', 'POST', '2025-12-07 23:00:32', '2025-12-07 23:00:32');
INSERT INTO `ums_permission` VALUES (11, 'ums:member:list', '查看会员列表', '/admin/member/list', 'GET', '2025-12-07 23:00:32', '2025-12-07 23:00:32');
INSERT INTO `ums_permission` VALUES (12, 'ums:member:detail', '查看会员详情', '/admin/member/detail/*', 'GET', '2025-12-07 23:00:32', '2025-12-07 23:00:32');
INSERT INTO `ums_permission` VALUES (13, 'ums:member:update', '修改会员信息', '/admin/member/update', 'POST', '2025-12-07 23:00:32', '2025-12-07 23:00:32');
INSERT INTO `ums_permission` VALUES (14, 'ums:member:delete', '删除会员', '/admin/member/delete/*', 'DELETE', '2025-12-07 23:00:32', '2025-12-07 23:00:32');
INSERT INTO `ums_permission` VALUES (15, 'sms:coupon:add', '发放优惠券', '/admin/coupon/add', 'POST', '2025-12-07 23:00:32', '2025-12-07 23:00:32');
INSERT INTO `ums_permission` VALUES (16, 'ai:doc:upload', '上传RAG知识库文档', '/admin/ai/doc/upload', 'POST', '2025-12-07 23:00:32', '2025-12-07 23:00:32');
INSERT INTO `ums_permission` VALUES (17, 'ai:doc:list', '查看知识库文档列表', '/admin/ai/doc/list', 'GET', '2025-12-07 23:00:32', '2025-12-07 23:00:32');
INSERT INTO `ums_permission` VALUES (18, 'app:home:content', 'APP-首页内容', '/app/home/content', 'GET', '2025-12-07 23:02:38', '2025-12-07 23:02:38');
INSERT INTO `ums_permission` VALUES (19, 'app:product:list', 'APP-商品列表', '/app/product/list', 'GET', '2025-12-07 23:02:38', '2025-12-07 23:02:38');
INSERT INTO `ums_permission` VALUES (20, 'app:product:detail', 'APP-商品详情', '/app/product/detail/*', 'GET', '2025-12-07 23:02:38', '2025-12-07 23:02:38');
INSERT INTO `ums_permission` VALUES (21, 'app:cart:list', 'APP-购物车列表', '/app/cart/list', 'GET', '2025-12-07 23:02:38', '2025-12-07 23:02:38');
INSERT INTO `ums_permission` VALUES (22, 'app:cart:add', 'APP-添加购物车', '/app/cart/add', 'POST', '2025-12-07 23:02:38', '2025-12-07 23:02:38');
INSERT INTO `ums_permission` VALUES (23, 'app:cart:update', 'APP-修改购物车', '/app/cart/update', 'POST', '2025-12-07 23:02:38', '2025-12-07 23:02:38');
INSERT INTO `ums_permission` VALUES (24, 'app:cart:delete', 'APP-删除购物车', '/app/cart/delete', 'POST', '2025-12-07 23:02:38', '2025-12-07 23:02:38');
INSERT INTO `ums_permission` VALUES (25, 'app:order:create', 'APP-创建订单', '/app/order/create', 'POST', '2025-12-07 23:02:38', '2025-12-07 23:02:38');
INSERT INTO `ums_permission` VALUES (26, 'app:order:list', 'APP-订单列表', '/app/order/list', 'GET', '2025-12-07 23:02:38', '2025-12-07 23:02:38');
INSERT INTO `ums_permission` VALUES (27, 'app:order:detail', 'APP-订单详情', '/app/order/detail/*', 'GET', '2025-12-07 23:02:38', '2025-12-07 23:02:38');
INSERT INTO `ums_permission` VALUES (28, 'app:member:info', 'APP-个人信息', '/app/member/info', 'GET', '2025-12-07 23:02:38', '2025-12-07 23:02:38');
INSERT INTO `ums_permission` VALUES (29, 'system:menu:list', '查看权限列表', '/admin/permission/list', 'GET', '2025-12-13 16:16:00', '2025-12-13 16:16:00');
INSERT INTO `ums_permission` VALUES (30, 'system:menu:add', '添加权限', '/admin/permission/create', 'POST', '2025-12-13 16:16:00', '2025-12-13 16:16:00');
INSERT INTO `ums_permission` VALUES (31, 'system:menu:update', '更新权限', '/admin/permission/update', 'POST', '2025-12-13 16:16:00', '2025-12-13 16:16:00');
INSERT INTO `ums_permission` VALUES (32, 'system:menu:delete', '删除权限', '/admin/permission/delete', 'DELETE', '2025-12-13 16:17:29', '2025-12-13 16:29:40');
INSERT INTO `ums_permission` VALUES (33, 'ums:member-level:list', '获取会员等级列表', '/admin/member-level/list', 'GET', '2025-12-13 17:07:29', '2025-12-13 17:07:29');
INSERT INTO `ums_permission` VALUES (34, 'ums:member-level:detail', '获取会员等级详情', '/admin/member-level/detail', 'GET', '2025-12-13 17:08:10', '2025-12-13 17:08:10');
INSERT INTO `ums_permission` VALUES (35, 'ums:member-level:add', '新增会员等级', '/admin/member-level/add', 'POST', '2025-12-13 17:08:50', '2025-12-13 17:08:50');
INSERT INTO `ums_permission` VALUES (36, 'ums:member-level:update', '更新会员等级', '/admin/member-level/update', 'POST', '2025-12-13 17:09:26', '2025-12-13 17:09:26');
INSERT INTO `ums_permission` VALUES (37, 'ums:member-level:delete', '删除会员等级', '/admin/member-level/delete', 'DELETE', '2025-12-13 17:10:05', '2025-12-13 17:10:05');
INSERT INTO `ums_permission` VALUES (38, 'ums:member-level:update', '修改会员等级状态', '/admin/member-level/updateStatus', 'POST', '2025-12-13 17:10:53', '2025-12-13 17:10:53');
INSERT INTO `ums_permission` VALUES (39, 'dashboard:view', '获取统计数据', '/admin/dashboard/statistics', 'GET', '2025-12-13 17:27:36', '2025-12-13 17:28:59');

-- ----------------------------
-- Table structure for ums_role
-- ----------------------------
DROP TABLE IF EXISTS `ums_role`;
CREATE TABLE `ums_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色描述',
  `status` int NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ums_role
-- ----------------------------
INSERT INTO `ums_role` VALUES (3, 'ROLE_ADMIN', '超级管理员：系统最高权限，拥有所有功能', 1, '2025-12-07 22:08:39', '2025-12-07 22:08:39');
INSERT INTO `ums_role` VALUES (4, 'ROLE_MANAGER', '店长：负责门店运营，拥有商品管理、订单管理及门店报表权限', 1, '2025-12-07 22:08:39', '2025-12-07 22:08:39');
INSERT INTO `ums_role` VALUES (5, 'ROLE_BARISTA', '咖啡师：负责接单、制作咖啡、核销取餐码及更新订单状态', 1, '2025-12-07 22:08:39', '2025-12-07 22:08:39');
INSERT INTO `ums_role` VALUES (6, 'ROLE_MEMBER', '普通会员：C端APP注册用户默认角色', 1, '2025-12-07 22:08:39', '2025-12-13 16:35:55');

-- ----------------------------
-- Table structure for ums_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `ums_role_permission`;
CREATE TABLE `ums_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 219 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ums_role_permission
-- ----------------------------
INSERT INTO `ums_role_permission` VALUES (32, 4, 1, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (33, 4, 2, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (34, 4, 3, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (35, 4, 4, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (36, 4, 5, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (37, 4, 6, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (38, 4, 7, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (39, 4, 8, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (40, 4, 9, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (41, 4, 10, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (42, 4, 11, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (43, 4, 12, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (44, 4, 13, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (45, 4, 14, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (46, 4, 15, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (47, 4, 16, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (48, 4, 17, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (63, 5, 1, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (64, 5, 6, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (65, 5, 7, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (66, 5, 8, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (70, 6, 18, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (71, 6, 19, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (72, 6, 20, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (73, 6, 21, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (74, 6, 22, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (75, 6, 23, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (76, 6, 24, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (77, 6, 25, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (78, 6, 26, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (79, 6, 27, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (80, 6, 28, '2025-12-07 23:10:50', '2025-12-13 15:49:44');
INSERT INTO `ums_role_permission` VALUES (180, 3, 39, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (181, 3, 38, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (182, 3, 37, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (183, 3, 36, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (184, 3, 35, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (185, 3, 34, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (186, 3, 33, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (187, 3, 32, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (188, 3, 31, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (189, 3, 30, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (190, 3, 29, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (191, 3, 23, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (192, 3, 24, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (193, 3, 25, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (194, 3, 26, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (195, 3, 27, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (196, 3, 28, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (197, 3, 18, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (198, 3, 19, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (199, 3, 20, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (200, 3, 21, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (201, 3, 22, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (202, 3, 2, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (203, 3, 3, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (204, 3, 4, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (205, 3, 5, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (206, 3, 6, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (207, 3, 7, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (208, 3, 17, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (209, 3, 16, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (210, 3, 15, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (211, 3, 14, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (212, 3, 13, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (213, 3, 12, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (214, 3, 11, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (215, 3, 10, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (216, 3, 9, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (217, 3, 8, '2025-12-13 17:28:18', '2025-12-13 17:28:18');
INSERT INTO `ums_role_permission` VALUES (218, 3, 1, '2025-12-13 17:28:18', '2025-12-13 17:28:18');

-- ----------------------------
-- Table structure for ums_user_role
-- ----------------------------
DROP TABLE IF EXISTS `ums_user_role`;
CREATE TABLE `ums_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ums_user_role
-- ----------------------------
INSERT INTO `ums_user_role` VALUES (1, 1, 6, '2025-12-07 23:13:51');
INSERT INTO `ums_user_role` VALUES (2, 8, 3, '2025-12-07 23:28:43');
INSERT INTO `ums_user_role` VALUES (3, 9, 4, '2025-12-13 16:02:09');

SET FOREIGN_KEY_CHECKS = 1;
