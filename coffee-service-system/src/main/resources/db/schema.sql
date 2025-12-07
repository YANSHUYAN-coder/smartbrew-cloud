-- 用户表
CREATE TABLE IF NOT EXISTS ums_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) UNIQUE NOT NULL COMMENT '用户名',
    phone VARCHAR(32) UNIQUE NOT NULL COMMENT '手机号',
    password VARCHAR(128) NOT NULL COMMENT '密码',
    nickname VARCHAR(64) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像',
    gender INT DEFAULT 0 COMMENT '性别：0->未知；1->男；2->女',
    birthday DATE COMMENT '生日',
    city VARCHAR(64) COMMENT '所做城市',
    job VARCHAR(64) COMMENT '职业',
    signature VARCHAR(255) COMMENT '个性签名',
    integration INT DEFAULT 0 COMMENT '积分',
    growth INT DEFAULT 0 COMMENT '成长值',
    level_id BIGINT COMMENT '会员等级ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

-- 商品表
CREATE TABLE IF NOT EXISTS pms_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL COMMENT '商品名称',
    pic VARCHAR(255) COMMENT '图片',
    album_pics VARCHAR(1000) COMMENT '画册图片，连产品图片限制为5张，以逗号分割',
    description TEXT COMMENT '描述',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    original_price DECIMAL(10,2) COMMENT '市场价',
    sales INT DEFAULT 0 COMMENT '销量',
    publish_status INT DEFAULT 1 COMMENT '上架状态：0->下架；1->上架',
    new_status INT DEFAULT 0 COMMENT '新品状态:0->不是新品；1->新品',
    recommand_status INT DEFAULT 0 COMMENT '推荐状态；0->不推荐；1->推荐',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品信息';

-- SKU库存表
CREATE TABLE IF NOT EXISTS pms_sku_stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL COMMENT '商品ID',
    sku_code VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    price DECIMAL(10,2) COMMENT '价格',
    stock INT DEFAULT 0 COMMENT '库存',
    low_stock INT COMMENT '预警库存',
    spec JSON COMMENT '规格属性(JSON格式)',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU库存表';

-- 订单表
CREATE TABLE IF NOT EXISTS oms_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_sn VARCHAR(64) UNIQUE NOT NULL COMMENT '订单编号',
    member_id BIGINT NOT NULL COMMENT '用户ID',
    coupon_id BIGINT COMMENT '优惠券ID',
    total_amount DECIMAL(10,2) COMMENT '订单总金额',
    promotion_amount DECIMAL(10,2) COMMENT '促销优化金额（促销价、满减、阶梯价）',
    coupon_amount DECIMAL(10,2) COMMENT '优惠券抵扣金额',
    pay_amount DECIMAL(10,2) COMMENT '应付金额（实际支付金额）',
    pay_type INT DEFAULT 1 COMMENT '支付方式：0->未支付；1->支付宝；2->微信',
    status INT DEFAULT 1 COMMENT '订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单',
    delivery_company VARCHAR(64) COMMENT '物流公司(配送方式)',
    delivery_sn VARCHAR(64) COMMENT '物流单号',
    receiver_name VARCHAR(100) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(32) NOT NULL COMMENT '收货人电话',
    receiver_post_code VARCHAR(32) COMMENT '收货人邮编',
    receiver_province VARCHAR(32) COMMENT '省份/直辖市',
    receiver_city VARCHAR(32) COMMENT '城市',
    receiver_region VARCHAR(32) COMMENT '区',
    receiver_detail_address VARCHAR(200) COMMENT '详细地址',
    note VARCHAR(500) COMMENT '订单备注',
    confirm_status INT DEFAULT 0 COMMENT '确认收货状态：0->未确认；1->已确认',
    payment_time DATETIME COMMENT '支付时间',
    delivery_time DATETIME COMMENT '发货时间',
    receive_time DATETIME COMMENT '确认收货时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- AI知识库文档表
CREATE TABLE IF NOT EXISTS ai_knowledge_doc (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    filename VARCHAR(255) NOT NULL COMMENT '文件名',
    filepath VARCHAR(500) NOT NULL COMMENT '文件路径',
    vectorized_status INT DEFAULT 0 COMMENT '向量化状态：0->未处理；1->处理中；2->已完成；3->失败',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI知识库文档表';