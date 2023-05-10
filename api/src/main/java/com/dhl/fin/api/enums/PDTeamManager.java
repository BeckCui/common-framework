package com.dhl.fin.api.enums;

import com.dhl.fin.api.common.annotation.DictionaryEnum;
import lombok.Getter;

@Getter
@DictionaryEnum(code = "PDTeamManager", name = "PDTeam的manager")
public enum PDTeamManager {

    FIN("FIN", "FIN", "qisma", 1),
    OPS("OPS", "OPS", "bruceyu", 2),
    CSD("CSD", "CSD", "boblicn", 3),
    SLS("SLS", "SLS", "yzhao", 4);

    private String code;
    private String name;
    private String manager;
    private Integer order;

    PDTeamManager(String code, String name, String manager, Integer order) {
        this.code = code;
        this.name = name;
        this.manager = manager;
        this.order = order;
    }

}
