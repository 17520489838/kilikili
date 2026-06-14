package com.kilikili.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlayHistoryQuery extends BaseParma {
    private String userId;
    private String videoId;
    private Date lastPlayTimeStart;
    private Date lastPlayTimeEnd;
}
