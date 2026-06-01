package com.jvmdevelop.strife.reqandresp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@lombok.NoArgsConstructor
public class GetChatMessagesRequest {
    private Long chatId;
    private Integer offset = 0;

}
