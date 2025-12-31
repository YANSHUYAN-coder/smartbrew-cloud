package com.coffee.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ums_member_receive_address")
public class UmsMemberReceiveAddress extends BaseEntity {
    private Long memberId;
    private String name;
    private String phone;
    private Integer defaultStatus; // 1-默认 0-非默认
    private String postCode;
    private String province;
    private String city;
    private String region;
    private String detailAddress;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;
}