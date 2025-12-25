package com.coffee.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffee.system.domain.entity.SmsCouponHistory;
import com.coffee.system.domain.vo.CouponHistoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SmsCouponHistoryMapper extends BaseMapper<SmsCouponHistory> {
    
    /**
     * 查询用户的优惠券列表（包含优惠券详情）
     */
    List<CouponHistoryVO> selectMemberCouponsWithDetail(@Param("memberId") Long memberId, 
                                                          @Param("useStatus") Integer useStatus);
}
