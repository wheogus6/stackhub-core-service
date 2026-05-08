package core.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import core.dto.CodeCreateInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static core.entity.QCodeCreateInfo.codeCreateInfo;

@Repository
public class CodeCreateInfoRepositoryCustom {

    private final JPAQueryFactory query;

    @Autowired
    public CodeCreateInfoRepositoryCustom(JPAQueryFactory query) {
        this.query = query;
    }

    public CodeCreateInfoDto getCodeInfo(String id) {
        CodeCreateInfoDto dto = query.select(Projections.bean(CodeCreateInfoDto.class,
                        codeCreateInfo.code,
                        codeCreateInfo.category,
                        codeCreateInfo.number))
                .from(codeCreateInfo)
                .where(codeCreateInfo.category.eq(id))
                .fetchOne();
        return dto;
    }

    public void updateNumber(String id, String num) {
        query.update(codeCreateInfo)
                .set(codeCreateInfo.number, num)
                .where(codeCreateInfo.category.eq(id))
                .execute();
    }

}
