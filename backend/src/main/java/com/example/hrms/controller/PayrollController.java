package com.example.hrms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.common.Result;
import com.example.hrms.entity.Payroll;
import com.example.hrms.security.SecurityUtil;
import com.example.hrms.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    /** 生成/重算某月工资单 */
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Map<String, Object>> generate(@RequestParam String month) {
        int count = payrollService.generateMonth(month);
        Map<String, Object> map = new HashMap<>();
        map.put("month", month);
        map.put("count", count);
        return Result.success("已生成 " + count + " 条工资单", map);
    }

    @GetMapping
    public Result<Page<Payroll>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String month) {
        if (!SecurityUtil.isAdminOrHr()) {
            employeeId = SecurityUtil.getCurrentUser().getEmployeeId();
        }
        return Result.success(payrollService.page(current, size, employeeId, month));
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<?> markPaid(@PathVariable Long id) {
        payrollService.markPaid(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<?> delete(@PathVariable Long id) {
        payrollService.delete(id);
        return Result.success();
    }

    /** 导出工资单为 Excel（管理员/人事） */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public void export(@RequestParam(required = false) Long employeeId,
                       @RequestParam(required = false) String month,
                       HttpServletResponse response) throws Exception {
        List<Payroll> list = payrollService.listForExport(employeeId, month);
        String fileName = "工资单" + (month != null && !month.isEmpty() ? "_" + month : "") + ".xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("工资单");
            String[] headers = {"月份", "员工", "部门", "基本工资", "全勤奖", "加班费",
                    "迟到扣款", "缺勤扣款", "事假扣款", "应发工资", "个税", "实发工资", "状态", "说明"};

            // 表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 12 * 256);
            }

            int rowIdx = 1;
            for (Payroll p : list) {
                Row row = sheet.createRow(rowIdx++);
                int c = 0;
                row.createCell(c++).setCellValue(safe(p.getSalaryMonth()));
                row.createCell(c++).setCellValue(safe(p.getEmployeeName()));
                row.createCell(c++).setCellValue(safe(p.getDepartmentName()));
                row.createCell(c++).setCellValue(num(p.getBaseSalary()));
                row.createCell(c++).setCellValue(num(p.getAttendanceBonus()));
                row.createCell(c++).setCellValue(num(p.getOvertimePay()));
                row.createCell(c++).setCellValue(num(p.getLateDeduct()));
                row.createCell(c++).setCellValue(num(p.getAbsentDeduct()));
                row.createCell(c++).setCellValue(num(p.getLeaveDeduct()));
                row.createCell(c++).setCellValue(num(p.getGrossSalary()));
                row.createCell(c++).setCellValue(num(p.getTax()));
                row.createCell(c++).setCellValue(num(p.getNetSalary()));
                row.createCell(c++).setCellValue("PAID".equals(p.getStatus()) ? "已发放" : "已生成");
                row.createCell(c).setCellValue(safe(p.getRemark()));
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");
            try (OutputStream os = response.getOutputStream()) {
                workbook.write(os);
                os.flush();
            }
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private double num(BigDecimal b) {
        return b == null ? 0d : b.doubleValue();
    }
}
