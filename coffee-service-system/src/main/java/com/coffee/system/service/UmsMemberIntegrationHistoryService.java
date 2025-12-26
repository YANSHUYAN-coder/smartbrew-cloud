package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.UmsMemberIntegrationHistory;
import com.coffee.system.domain.vo.IntegrationHistoryVO;

/**
 * 用户积分明细服务接口
 */
public interface UmsMemberIntegrationHistoryService extends IService<UmsMemberIntegrationHistory> {
    
    /**
     * 记录积分变动
     * @param memberId 用户ID
     * @param changeType 变动类型：1->签到获得；2->兑换优惠券扣除；3->订单完成获得；4->其他
     * @param changePoints 变动积分（正数表示增加，负数表示减少）
     * @param sourceType 来源类型
     * @param sourceId 来源ID
     * @param note 备注说明
     */
    void recordIntegrationChange(Long memberId, Integer changeType, Integer changePoints, 
                                 String sourceType, Long sourceId, String note);
    
    /**
     * 获取当前用户的积分明细列表
     * @param pageParam 分页参数
     * @return 积分明细列表
     */
    Page<IntegrationHistoryVO> getMyIntegrationHistory(PageParam pageParam);
}

