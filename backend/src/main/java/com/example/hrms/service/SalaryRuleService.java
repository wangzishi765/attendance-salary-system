package com.example.hrms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hrms.entity.SalaryRule;
import com.example.hrms.mapper.SalaryRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SalaryRuleService {

    private final SalaryRuleMapper salaryRuleMapper;

    /** 获取当前生效的薪资规则（取第一条，不存在则返回默认值） */
    public SalaryRule current() {
        SalaryRule rule = salaryRuleMapper.selectOne(
                new LambdaQueryWrapper<SalaryRule>().orderByAsc(SalaryRule::getId).last("limit 1"));
        if (rule == null) {
            rule = new SalaryRule();
            rule.setName("默认薪资规则");
            rule.setLateDeduct(new BigDecimal("50"));
            rule.setAbsentDeduct(new BigDecimal("200"));
            rule.setLeaveDeduct(new BigDecimal("100"));
            rule.setOvertimeRate(new BigDecimal("30"));
            rule.setFullAttendanceBonus(new BigDecimal("300"));
            salaryRuleMapper.insert(rule);
        }
        return rule;
    }

    public void update(SalaryRule rule) {
        SalaryRule current = current();
        rule.setId(current.getId());
        salaryRuleMapper.updateById(rule);
    }
}
