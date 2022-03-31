package com.cos.iter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class OnPremSaveService {
    @Value("${onpremise.store-path}")
    private String storePath;
}
