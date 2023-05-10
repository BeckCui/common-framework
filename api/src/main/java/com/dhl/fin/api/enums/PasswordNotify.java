package com.dhl.fin.api.enums;


import com.dhl.fin.api.common.annotation.DictionaryEnum;
import lombok.Getter;

@Getter
@DictionaryEnum(code = "passwordNotify", name = "账号密码到期提醒")
public enum PasswordNotify {

    TRUE("true", "是"),
    FALSE("false", "否");

    private String code;
    private String name;


    PasswordNotify(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
