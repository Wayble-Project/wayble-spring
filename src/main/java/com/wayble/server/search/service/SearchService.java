package com.wayble.server.search.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.search.exception.SearchErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

    public void makeException() {
        throw new ApplicationException(SearchErrorCase.SEARCH_EXCEPTION);
    }
}
