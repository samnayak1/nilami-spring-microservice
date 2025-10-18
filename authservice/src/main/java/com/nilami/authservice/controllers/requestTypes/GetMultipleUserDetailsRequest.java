package com.nilami.authservice.controllers.requestTypes;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetMultipleUserDetailsRequest {
     private List<String> userIds;
}
