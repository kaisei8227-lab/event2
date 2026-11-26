package com.example.eventreserve.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 企業名変更の入力 */
public class CompanyNameForm {

    @NotBlank(message = "企業名を入力してください")
    @Size(max = 100, message = "企業名は100文字以内にしてください")
    private String companyName;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
}
