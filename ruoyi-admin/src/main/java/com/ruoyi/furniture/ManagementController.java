package com.ruoyi.furniture;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.furniture.Models.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/furniture")
public class ManagementController {
  private final CatalogService catalog;
  private final InquiryService inquiries;

  public ManagementController(CatalogService catalog, InquiryService inquiries) {
    this.catalog = catalog;
    this.inquiries = inquiries;
  }

  @GetMapping("/products")
  @PreAuthorize("@ss.hasPermi('furniture:product:list')")
  public AjaxResult products() {
    return AjaxResult.success(catalog.list(false));
  }

  @PostMapping("/products")
  @PreAuthorize("@ss.hasPermi('furniture:product:edit')")
  @Log(title = "产品新增", businessType = BusinessType.INSERT)
  public AjaxResult add(@Valid @RequestBody Product p) {
    catalog.save(p, null);
    return AjaxResult.success();
  }

  @PutMapping("/products/{id}")
  @PreAuthorize("@ss.hasPermi('furniture:product:edit')")
  @Log(title = "产品编辑", businessType = BusinessType.UPDATE)
  public AjaxResult update(@PathVariable long id, @Valid @RequestBody Product p) {
    catalog.save(p, id);
    return AjaxResult.success();
  }

  @DeleteMapping("/products/{id}")
  @PreAuthorize("@ss.hasPermi('furniture:product:edit')")
  @Log(title = "产品删除", businessType = BusinessType.DELETE)
  public AjaxResult delete(@PathVariable long id) {
    catalog.delete(id);
    return AjaxResult.success();
  }

  @GetMapping("/messages")
  @PreAuthorize("@ss.hasPermi('furniture:message:list')")
  public AjaxResult messages(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size) {
    return AjaxResult.success(inquiries.list(status, keyword, page, size));
  }

  @GetMapping("/messages/{id}")
  @PreAuthorize("@ss.hasPermi('furniture:message:list')")
  public AjaxResult message(@PathVariable long id) {
    return AjaxResult.success(inquiries.detail(id));
  }

  @PutMapping("/messages/{id}")
  @PreAuthorize("@ss.hasPermi('furniture:message:edit')")
  @Log(
      title = "留言审核跟进",
      businessType = BusinessType.UPDATE,
      isSaveRequestData = false,
      isSaveResponseData = false)
  public AjaxResult moderate(@PathVariable long id, @Valid @RequestBody Moderation m) {
    inquiries.moderate(id, m, SecurityUtils.getUsername());
    return AjaxResult.success();
  }

  @DeleteMapping("/messages/{id}")
  @PreAuthorize("@ss.hasPermi('furniture:message:remove')")
  @Log(title = "留言删除", businessType = BusinessType.DELETE, isSaveRequestData = false)
  public AjaxResult deleteMessage(@PathVariable long id) {
    inquiries.delete(id);
    return AjaxResult.success();
  }

  @GetMapping("/content")
  @PreAuthorize("@ss.hasPermi('furniture:content:list')")
  public AjaxResult content() {
    return AjaxResult.success(catalog.content());
  }

  @PutMapping("/content")
  @PreAuthorize("@ss.hasPermi('furniture:content:edit')")
  @Log(title = "官网文案", businessType = BusinessType.UPDATE)
  public AjaxResult content(@Valid @RequestBody Content c) {
    catalog.saveContent(c);
    return AjaxResult.success();
  }
}
