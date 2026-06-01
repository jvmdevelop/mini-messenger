package com.jvmdevelop.strife.reqandresp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateChatRequest {
    private String title;
    private List<Long> userIds;
    private Boolean isTetATet;
    private Long recipientId;

}
