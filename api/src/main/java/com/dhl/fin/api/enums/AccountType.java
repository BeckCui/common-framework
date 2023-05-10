package com.dhl.fin.api.enums;


import com.dhl.fin.api.common.annotation.DictionaryEnum;
import lombok.Getter;


/**
 * 账号类型
 */
@Getter
@DictionaryEnum(code = "accountType", name = "账号类型")
public enum AccountType {

    AD("AD", "AD账号"),
    NORMAL("NORMAL", "普通账号");

    private String code;
    private String name;

    AccountType(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
