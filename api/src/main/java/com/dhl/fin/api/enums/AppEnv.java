package com.dhl.fin.api.enums;


import com.dhl.fin.api.common.annotation.DictionaryEnum;
import lombok.Getter;


/**
 * 账号类型
 */
@Getter
@DictionaryEnum(code = "appEnv", name = "应用环境")
public enum AppEnv {

    PROD("PRO", "PRO"),
    UAT("UAT", "UAT"),
    DEV("DEV", "DEV");

    private String code;
    private String name;

    AppEnv(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
