package net.risesoft.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * @author qinman
 */
@Data
public class TradeInfoResult extends Result {

    @JsonProperty("trade_list")
    private List<TradeInfo> list;
}
