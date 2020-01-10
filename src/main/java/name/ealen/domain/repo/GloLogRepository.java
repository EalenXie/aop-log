package name.ealen.domain.repo;

import name.ealen.global.advice.log.LogDefine;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author EalenXie Created on 2020/1/6 13:15.
 */
public interface GloLogRepository extends JpaRepository<LogDefine,Integer> {
}
