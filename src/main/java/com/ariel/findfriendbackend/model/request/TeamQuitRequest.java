package com.ariel.findfriendbackend.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeamQuitRequest implements Serializable {

    /**
     * id
     */
    private Long teamId;

}
