package com.nadiannis.common.dto;

import com.nadiannis.common.dto.order.OrderResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {

    private String status;

    private OrderResDto payload;

}
