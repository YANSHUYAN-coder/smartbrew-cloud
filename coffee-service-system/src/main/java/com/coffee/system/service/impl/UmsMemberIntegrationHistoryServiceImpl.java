package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.context.UserContext;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.UmsMemberIntegrationHistory;
import com.coffee.system.domain.vo.IntegrationHistoryVO;
import com.coffee.system.mapper.UmsMemberIntegrationHistoryMapper;
import com.coffee.system.service.UmsMemberIntegrationHistoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户积分明细服务实现
 */
@Service
public class UmsMemberIntegrationHistoryServiceImpl 
        extends ServiceImpl<UmsMemberIntegrationHistoryMapper, UmsMemberIntegrationHistory>
        implements UmsMemberIntegrationHistoryService {
    
    @Override
    public void recordIntegrationChange(Long memberId, Integer changeType, Integer changePoints, 
                                        String sourceType, Long sourceId, String note) {
        UmsMemberIntegrationHistory history = new UmsMemberIntegrationHistory();
        history.setMemberId(memberId);
        history.setChangeType(changeType);
        history.setChangePoints(changePoints);
        history.setSourceType(sourceType);
        history.setSourceId(sourceId);
        history.setNote(note);
        this.save(history);
    }
    
    @Override
    public Page<IntegrationHistoryVO> getMyIntegrationHistory(PageParam pageParam) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        Page<UmsMemberIntegrationHistory> page = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<UmsMemberIntegrationHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UmsMemberIntegrationHistory::getMemberId, userId)
               .orderByDesc(UmsMemberIntegrationHistory::getCreateTime);
        
        Page<UmsMemberIntegrationHistory> historyPage = this.page(page, wrapper);
        
        // 转换为VO
        Page<IntegrationHistoryVO> voPage = new Page<>(historyPage.getCurrent(), historyPage.getSize(), historyPage.getTotal());
        List<IntegrationHistoryVO> voList = historyPage.getRecords().stream().map(history -> {
            IntegrationHistoryVO vo = new IntegrationHistoryVO();
            BeanUtils.copyProperties(history, vo);
            // 设置变动类型描述
            vo.setChangeTypeDesc(getChangeTypeDesc(history.getChangeType()));
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }

    /**
     * 【新增】检查该订单是否已经发放过积分
     * 用于保证 RabbitMQ 消费的幂等性
     *
     * @param orderId 订单ID
     * @return true=已发放过, false=未发放
     */
    @Override
    public boolean hasRecord(Long orderId) {
        if (orderId == null) {
            return false;
        }

        // 查询条件：订单ID匹配
        // 注意：这里假设你的实体类字段名为 orderId。
        // 如果你的实体类使用的是通用的 sourceId，请改为 .eq(UmsMemberIntegrationHistory::getSourceId, orderId)
        LambdaQueryWrapper<UmsMemberIntegrationHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UmsMemberIntegrationHistory::getSourceId, orderId);

        // 可选：如果你同一个订单有多种积分变动类型，建议加上类型限制
        // 3 代表订单完成获得的积分 (根据你Listener里的 recordIntegrationChange 调用推断)
         wrapper.eq(UmsMemberIntegrationHistory::getChangeType, 3);

        // selectCount 比 selectList 效率更高，只返回数量
        long count = this.count(wrapper);

        return count > 0;
    }

    /**
     * 获取变动类型描述
     */
    private String getChangeTypeDesc(Integer changeType) {
        if (changeType == null) {
            return "其他";
        }
        switch (changeType) {
            case 1:
                return "签到获得";
            case 2:
                return "兑换优惠券";
            case 3:
                return "订单完成";
            case 4:
                return "其他";
            default:
                return "其他";
        }
    }
}

