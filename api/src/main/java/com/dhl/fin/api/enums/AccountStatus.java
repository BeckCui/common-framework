package com.dhl.fin.api.enums;


import com.dhl.fin.api.common.annotation.DictionaryEnum;
import lombok.Getter;


/**
 * 用户在职离职状态
 */
@Getter
@DictionaryEnum(code = "accountStatus", name = "用户在职离职状态")
public enum AccountStatus {

    TRUE("true", "在职"),
    FALSE("false", "离职");

    private String code;
    private String name;

    AccountStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
