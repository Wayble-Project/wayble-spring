package com.wayble.server.direction.external.kric.dto;

import java.util.List;

public record KricToiletRawResponse(
        List<KricToiletRawItem> body
) {}