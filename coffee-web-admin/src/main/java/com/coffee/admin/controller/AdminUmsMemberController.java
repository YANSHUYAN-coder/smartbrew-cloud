package com.coffee.admin.controller;

import com.coffee.common.dto.MemberStatusDTO;
import com.coffee.common.result.Result;
import com.coffee.system.domain.UmsMember;
import com.coffee.system.service.UmsMemberService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/member")
@AllArgsConstructor
public class AdminUmsMemberController {

    private final UmsMemberService memberService;

    // 1. 获取会员列表
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ums:member:list')")
    public Result<List<UmsMember>> list() {
        List<UmsMember> list = memberService.list();
        return Result.success(list);
    }

    // 2. 查看会员详情 (例如查看积分历史，这里暂时返回基本信息)
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAuthority('ums:member:detail')")
    public Result<UmsMember> detail(@PathVariable("id") Long id) {
        UmsMember member = memberService.getById(id);
        return Result.success(member);
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
}