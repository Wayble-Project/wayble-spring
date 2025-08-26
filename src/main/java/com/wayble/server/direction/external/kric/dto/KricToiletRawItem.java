package com.wayble.server.direction.external.kric.dto;

import lombok.Getter;

public record KricToiletRawItem(
        String railOprIsttCd,
        String lnCd,
        String stinCd,
        String grndDvNm,
        String stinFlor,
        String gateInotDvNm,
        String exitNo,
        String dtlLoc,
        String mlFmlDvNm,
        String toltNum,
        String diapExchNum
) {}
