package com.umc.product.support.isolation;

import java.util.List;

interface TableNameExtractor {

    /**
     * DB 내에 존재하는 모든 테이블을 가져옵니다.
     */
    List<String> getNames();
}
