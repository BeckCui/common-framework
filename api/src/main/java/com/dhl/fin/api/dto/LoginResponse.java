package com.dhl.fin.api.dto;


import lombok.Data;

import java.util.Map;

/**
 * @author becui
 */
@Data
public class LoginResponse {
    private String token;
    private String uuid;
    private String status;
    private String routeUrl;
    private String userName;
    private String userId;
    private Map urlParams;
}
