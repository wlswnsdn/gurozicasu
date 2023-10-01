package hello.board.dto;

import hello.board.entity.BoardEntity;
import hello.board.entity.BoardFileEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//Data Transfer Object(파라미터 따로 보내지말고 하나에 담아서 주고받자)
@Getter
@Setter
@NoArgsConstructor  //기본 생성자
@AllArgsConstructor //모든 필드를 매개변수로 하는 생성자
public class BoardDTO {
    private Long id;
    private String boardWriter;
    private String boardTitle;
    private String boardContents;
    private String boardPass;
    private int boardHits;
    private LocalDateTime boardCreatedTime;
    private LocalDateTime boardUpdatedTime;

    private MultipartFile boardFile; //save.html에서 controller로 넘어올 때 파일 담는 용도
    private String originalFileName; // 원본 파일 이름
    private String storedFileName; //서버 저장용 파일 이름  왜 구분? -> 어제 파일 "내 사진" 올림, 오늘도 "내 사진" 올림 , 두 개가 혼동될 수 있음
    private int fileAttached; // 파일 첨부 여부(첨부 1, 아님 0) (boolean으로 하면 나중에 boardEntity할 때 귀찮음)

    public BoardDTO(Long id, String boardWriter, String boardTitle, int boardHits, LocalDateTime boardCreatedTime) {
        this.id = id;
        this.boardWriter = boardWriter;
        this.boardTitle = boardTitle;
        this.boardHits = boardHits;
        this.boardCreatedTime = boardCreatedTime;
    }

    public static BoardDTO toBoardDTO(BoardEntity boardEntity){
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setBoardContents(boardEntity.getBoardContents());
        boardDTO.setBoardPass(boardEntity.getBoardPass());
        boardDTO.setBoardTitle(boardEntity.getBoardTitle());
        boardDTO.setBoardWriter(boardEntity.getBoardWriter());
        boardDTO.setId(boardEntity.getId());
        boardDTO.setBoardCreatedTime(boardEntity.getCreatedTime());
        boardDTO.setBoardUpdatedTime(boardEntity.getUpdatedTime());
        boardDTO.setBoardHits(boardEntity.getBoardHits());

        if (boardEntity.getFileAttached() == 0) {
            boardDTO.setFileAttached(boardEntity.getFileAttached()); // 0
        } else {
            boardDTO.setFileAttached(boardEntity.getFileAttached()); // 1
            // 파일 이름을 가져와야됨
            // originalFileName, storedFileName은 board_file_table(BoardFileEntity)에 있음 (여기는 BoardEntity)
            // 근데 우리는 두 테이블간의 종속관계를 만들어놨었음 ( BoardFileEntity에는 BoardEntity, BoardEntity에는 BoardFileEntityList
            // 서로 다른 테이블에 있는 값 가져올 때 -> Join
            // select * from board_table b, board_file_table bf where b.id=bf.board_id and where b.id=?
            // spring JPA에서는 어떻게 하는가 vv
            // get(0) -> 첫 번째 index에 있는 값
            boardDTO.setOriginalFileName(boardEntity.getBoardFileEntityList().get(0).getOriginalFileName());
            boardDTO.setStoredFileName(boardEntity.getBoardFileEntityList().get(0).getStoredFileName());
        }
        return boardDTO;
    }
}
