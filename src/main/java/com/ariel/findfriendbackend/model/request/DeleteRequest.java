package com.ariel.findfriendbackend.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Ariel
 */
@Data
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = -5860707094194210842L;

    private long id;
}
