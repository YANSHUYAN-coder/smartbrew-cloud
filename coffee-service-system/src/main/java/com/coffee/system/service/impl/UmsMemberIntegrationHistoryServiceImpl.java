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

