package hello.board.repository;

import hello.board.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//<BoardEntity, pk 타입>
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    //update board_table set board_hits=board_hits+1 where id=?
    /*update BoardEntity b ->이제 BoardEntity를 b라고 부르겠다(약어)
      BoardEntity(테이블)에 있는 boardHits를 ++
      id는 변하는거니까 :id, @Param("id")랑 매칭됨
     */
    @Modifying  //delete같은 쿼리문 실행하려면 있어야됨 (쿼리는 DB에 작업 수행할때 쓰는 명령문)
    @Query(value = "update BoardEntity b set b.boardHits=b.boardHits+1 where b.id=:id")
    void updateHits(@Param("id") Long id);
}
