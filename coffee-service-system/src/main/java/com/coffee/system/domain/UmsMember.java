package com.coffee.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("ums_member")
public class UmsMember extends BaseEntity {
    private String username;
    private String phone;
    private String password;
    private String nickname;
    private String avatar;
    private Integer gender; // 0->未知；1->男；2->女
    private LocalDate birthday;
    private String city;
    private String job;
    private String signature;
    private Integer integration; // 积分
    private Integer growth; // 成长值
    private Long levelId; // 会员等级ID
    private Integer status; // 用户状态：1-正常 0-禁用
}