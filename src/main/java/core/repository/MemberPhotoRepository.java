package core.repository;

import core.entity.MemberPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberPhotoRepository extends JpaRepository<MemberPhoto, Long>{


    Optional<MemberPhoto> findByFileIdAndTypeAndMemorialCode(String fileId, String pType, String memorialCode);

    List<MemberPhoto> findByMemorialCode(String memorialCode);
}
