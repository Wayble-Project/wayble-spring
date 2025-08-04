package com.wayble.server.direction.dto.toilet;

import java.util.List;

public record KricToiletRawBody(
        List<KricToiletRawItem> item
) {}
