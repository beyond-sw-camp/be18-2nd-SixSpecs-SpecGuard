package com.beyond.specguard.crawling.repository;

import com.beyond.specguard.crawling.entity.CrawlingResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawlingRepository  extends JpaRepository<CrawlingResult, String> {


}
