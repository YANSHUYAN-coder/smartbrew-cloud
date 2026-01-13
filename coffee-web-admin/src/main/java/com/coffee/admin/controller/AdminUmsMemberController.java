package com.coffee.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.common.dto.MemberStatusDTO;
import com.coffee.common.dto.PageParam;
import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.UmsMember;
import com.coffee.system.domain.entity.UmsMemberLevel;
import com.coffee.system.domain.entity.UmsRole;
import com.coffee.system.domain.vo.MemberVO;
import com.coffee.system.mapper.UmsMemberLevelMapper;
import com.coffee.system.mapper.UmsUserRoleMapper;
import com.coffee.system.service.UmsMemberService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/member")
@AllArgsConstructor
public class AdminUmsMemberController {

    private final UmsMemberService memberService;
    private final UmsUserRoleMapper userRoleMapper;
    private final UmsMemberLevelMapper memberLevelMapper;

    // 1. 获取会员列表 - 分页查询（包含角色信息）
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ums:member:list')")
    public Result<Page<MemberVO>> list(@ModelAttribute PageParam pageParam,
                                        @RequestParam(name = "phone",required = false) String phone) {
        Page<MemberVO> page = memberService.getListWithRoles(pageParam, phone);
        return Result.success(page);
    }

    // 2. 查看会员详情 (例如查看积分历史，这里暂时返回基本信息)
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAuthority('ums:member:detail')")
    public Result<MemberVO> detail(@PathVariable("id") Long id) {
        UmsMember member = memberService.getById(id);
        if (member == null) {
            return Result.failed("会员不存在");
        }
        
        // 转换为 MemberVO 并填充角色和等级信息
        MemberVO vo = new MemberVO();
        BeanUtil.copyProperties(member, vo);
        // 查询用户角色
        List<UmsRole> roles = userRoleMapper.selectRolesByUserId(id);
        vo.setRoles(roles);
        // 查询会员等级
        if (member.getLevelId() != null) {
            UmsMemberLevel level = memberLevelMapper.selectById(member.getLevelId());
            vo.setLevel(level);
        }
        return Result.success(vo);
    }

    // 3. 修改会员信息 (例如封号/解封 status=0/1)
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('ums:member:update')")
    public Result<String> update(@RequestBody MemberStatusDTO dto) {
        boolean success = memberService.updateStatus(dto);
        return success ? Result.success("修改成功") : Result.failed("修改失败");
    }

    // 4. 删除会员 (通常很少物理删除，这里为了演示完整性保留)
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ums:member:delete')")
    public Result<String> delete(@PathVariable("id") Long id) {
        boolean success = memberService.removeById(id);
        return success ? Result.success("删除成功") : Result.failed("删除失败");
    }

    // 5. 分配角色给用户
    @PostMapping("/allocRoles")
    @PreAuthorize("hasAuthority('ums:member:alloc')")
    public Result<String> allocRoles(@RequestBody Map<String, Object> params) {
        // 处理 userId：前端传递的可能是 Integer 类型，需要转换为 Long
        Object userIdObj = params.get("userId");
        Long userId;
        if (userIdObj instanceof Long) {
            userId = (Long) userIdObj;
        } else if (userIdObj instanceof Integer) {
            userId = ((Integer) userIdObj).longValue();
        } else {
            userId = Long.valueOf(userIdObj.toString());
        }

        // 处理 roleIds：前端传递的可能是 Integer 类型，需要转换为 Long
        @SuppressWarnings("unchecked")
        List<Object> roleIdsObj = (List<Object>) params.get("roleIds");
        List<Long> roleIds = roleIdsObj.stream()
                .map(id -> {
                    if (id instanceof Long) {
                        return (Long) id;
                    } else if (id instanceof Integer) {
                        return ((Integer) id).longValue();
                    } else {
                        return Long.valueOf(id.toString());
                    }
                })
                .collect(java.util.stream.Collectors.toList());

        boolean success = memberService.allocRoles(userId, roleIds);
        return success ? Result.success("分配成功") : Result.failed("分配失败");
    }
}