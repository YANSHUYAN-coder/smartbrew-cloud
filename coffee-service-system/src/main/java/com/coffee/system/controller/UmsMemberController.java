package com.coffee.system.controller;

import com.coffee.common.result.Result;
import com.coffee.system.domain.UmsMember;
import com.coffee.system.service.UmsMemberService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/member")
@AllArgsConstructor
public class UmsMemberController {

    private final UmsMemberService memberService;

    // 1. 获取会员列表
    @GetMapping("/list")
    public Result<List<UmsMember>> list() {
        List<UmsMember> list = memberService.list();
        return Result.success(list);
    }

    // 2. 添加会员
    @PostMapping("/add")
    public Result<String> add(@RequestBody UmsMember member) {
        boolean success = memberService.save(member);
        return success ? Result.success("添加成功") : Result.failed("添加失败");
    }

    // 3. 删除会员
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = memberService.removeById(id);
        return success ? Result.success("删除成功") : Result.failed("删除失败");
    }
}