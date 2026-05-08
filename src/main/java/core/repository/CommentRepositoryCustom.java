package core.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import core.dto.CommentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static core.entity.QComment.comment1;
@Repository
public class CommentRepositoryCustom {

    private final JPAQueryFactory query;

    @Autowired
    public CommentRepositoryCustom(JPAQueryFactory query) {
        this.query = query;
    }

    public List<CommentDto> getCommentList(String memorialCode) {
        List<CommentDto> list = query.select(Projections.bean(CommentDto.class,
                        comment1.id,
                        comment1.name,
                        comment1.comment,
                        comment1.memorialCode))
                .from(comment1)
                .where(comment1.memorialCode.eq(memorialCode))
                .orderBy(comment1.regDate.desc())
                .fetch();
        return list;
    }


    public String editComment(Long id, String comment, String memorialCode) {
        query.update(comment1)
                .set(comment1.comment, comment)
                .where(comment1.id.eq(id),
                        comment1.memorialCode.eq(memorialCode))
                .execute();

        return "00";
    }


    public String deleteComment(Long id, String memorialCode) {
        query.delete(comment1)
                .where(comment1.id.eq(id),
                        comment1.memorialCode.eq(memorialCode))
                .execute();

        return "00";
    }

}
