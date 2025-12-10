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

 Date: 10/12/2025 17:39:20
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI知识库文档表' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

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
  `member_id` bigint NOT NULL COMMENT '用户ID',
  `coupon_id` bigint NULL DEFAULT NULL COMMENT '优惠券ID',
  `total_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '订单总金额',
  `promotion_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '促销优化金额（促销价、满减、阶梯价）',
  `coupon_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '优惠券抵扣金额',
  `pay_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '应付金额（实际支付金额）',
  `pay_type` int NULL DEFAULT 1 COMMENT '支付方式：0->未支付；1->支付宝；2->微信',
  `status` int NULL DEFAULT 1 COMMENT '订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单',
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_order
-- ----------------------------

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_item
-- ----------------------------

-- ----------------------------
-- Table structure for pms_product
-- ----------------------------
DROP TABLE IF EXISTS `pms_product`;
CREATE TABLE `pms_product`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `price` decimal(10, 2) NOT NULL COMMENT '价格',
  `category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类(如: 咖啡, 甜点)',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `pic_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片地址',
  `sales` int NULL DEFAULT 0 COMMENT '销量',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态: 1-上架, 0-下架',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pms_product
-- ----------------------------
INSERT INTO `pms_product` VALUES (1, '生椰拿铁', 18.00, '咖啡', 'YYDS的生椰拿铁，清爽好喝', NULL, 0, 1, '2025-12-05 23:23:00', '2025-12-05 23:23:00');
INSERT INTO `pms_product` VALUES (2, '美式咖啡', 12.00, '咖啡', '提神醒脑，打工人必备', NULL, 0, 1, '2025-12-05 23:23:00', '2025-12-05 23:23:00');
INSERT INTO `pms_product` VALUES (3, '提拉米苏', 22.00, '甜点', '入口即化，甜蜜享受', NULL, 0, 1, '2025-12-05 23:23:00', '2025-12-05 23:23:00');

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
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SKU库存表' ROW_FORMAT = DYNAMIC;

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
INSERT INTO `ums_member` VALUES (1, 'test_user', '13800138000', '$2a$10$67icrSr09JZPjVMT5BuCKOav6TnA1TU.ZpFBbxBmMAdLP7xCisGHi', NULL, NULL, 0, 1, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, NULL);
INSERT INTO `ums_member` VALUES (8, 'test_user1', '1380013802', '$2a$10$2asM8m6GCZYchML7cKQdaurUDLEA8Ulrv5tkTiTZKsVv6MCDuaYva', NULL, NULL, 0, 1, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, NULL);
INSERT INTO `ums_member` VALUES (9, 'test_user2', '13800138001', '.ZpFBbxBmMAdLP7xCisGHi', NULL, NULL, 0, 1, NULL, NULL, NULL, NULL, 0, 0, NULL, '2025-12-07 18:23:53', '2025-12-07 18:23:53');

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
INSERT INTO `ums_member_receive_address` VALUES (1, 1, '张三', '13800138000', 1, '518000', '广东省', '深圳市', '南山区', '粤海街道科技园中区科兴科学园B栋301', '2025-12-10 15:53:18', '2025-12-10 15:53:18');
INSERT INTO `ums_member_receive_address` VALUES (2, 1, '张三(公司)', '13800138000', 0, '518057', '广东省', '深圳市', '福田区', '福田街道平安金融中心108层', '2025-12-10 15:53:18', '2025-12-10 15:53:18');
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
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限表' ROW_FORMAT = DYNAMIC;

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
INSERT INTO `ums_role` VALUES (6, 'ROLE_MEMBER', '普通会员：C端APP注册用户默认角色', 1, '2025-12-07 22:08:39', '2025-12-07 22:08:39');

-- ----------------------------
-- Table structure for ums_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `ums_role_permission`;
CREATE TABLE `ums_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 81 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ums_role_permission
-- ----------------------------
INSERT INTO `ums_role_permission` VALUES (1, 3, 1, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (2, 3, 2, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (3, 3, 3, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (4, 3, 4, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (5, 3, 5, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (6, 3, 6, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (7, 3, 7, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (8, 3, 8, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (9, 3, 9, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (10, 3, 10, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (11, 3, 11, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (12, 3, 12, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (13, 3, 13, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (14, 3, 14, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (15, 3, 15, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (16, 3, 16, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (17, 3, 17, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (18, 3, 18, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (19, 3, 19, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (20, 3, 20, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (21, 3, 21, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (22, 3, 22, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (23, 3, 23, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (24, 3, 24, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (25, 3, 25, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (26, 3, 26, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (27, 3, 27, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (28, 3, 28, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (32, 4, 1, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (33, 4, 2, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (34, 4, 3, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (35, 4, 4, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (36, 4, 5, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (37, 4, 6, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (38, 4, 7, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (39, 4, 8, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (40, 4, 9, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (41, 4, 10, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (42, 4, 11, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (43, 4, 12, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (44, 4, 13, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (45, 4, 14, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (46, 4, 15, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (47, 4, 16, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (48, 4, 17, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (63, 5, 1, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (64, 5, 6, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (65, 5, 7, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (66, 5, 8, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (70, 6, 18, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (71, 6, 19, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (72, 6, 20, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (73, 6, 21, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (74, 6, 22, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (75, 6, 23, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (76, 6, 24, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (77, 6, 25, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (78, 6, 26, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (79, 6, 27, '2025-12-07 23:10:50');
INSERT INTO `ums_role_permission` VALUES (80, 6, 28, '2025-12-07 23:10:50');

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
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ums_user_role
-- ----------------------------
INSERT INTO `ums_user_role` VALUES (1, 1, 6, '2025-12-07 23:13:51');
INSERT INTO `ums_user_role` VALUES (2, 2, 3, '2025-12-07 23:28:43');

SET FOREIGN_KEY_CHECKS = 1;
