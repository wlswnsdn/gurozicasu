package hello.board.service;

import hello.board.dto.BoardDTO;
import hello.board.entity.BoardEntity;
import hello.board.entity.BoardFileEntity;
import hello.board.repository.BoardFileRepository;
import hello.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//DTO로 넘겨줌 -> entity로 바꿔서 받음   (Entity 클래스에서)
//entity로 넘겨줌 -> DTO로 바꿔서 받음   (DTO 클래스에서)
@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardFileRepository boardFileRepository;

    public void save(BoardDTO boardDTO) throws IOException {
        // 파일 첨부 여부에 따라 로직 분리
        if (boardDTO.getBoardFile().isEmpty()) {
            // 첨부 파일 없음.
            BoardEntity boardEntity = BoardEntity.toSaveEntity(boardDTO);
            boardRepository.save(boardEntity);
        } else {
            // 첨부 파일 있음.
            /*
                1. DTO에 담긴 파일을 꺼냄
                2. 파일의 이름 가져옴
                3. 서버 저장용 이름을 만듦
                // 내사진.jpg => 839798375892_내사진.jpg
                4. 저장 경로 설정
                5. 해당 경로에 파일 저장
                6. board_table에 해당 데이터 save 처리
                7. board_file_table에 해당 데이터 save 처리
             */
            MultipartFile boardFile = boardDTO.getBoardFile(); // 1.
            String originalFilename = boardFile.getOriginalFilename(); // 2.
            String storedFileName = System.currentTimeMillis() + "_" + originalFilename; // 3.
            String savePath = "C:/Users/wlswn/spring study/board_image/" + storedFileName;   //4.
//            String savePath = "/Users/사용자이름/springboot_img/" + storedFileName; // C:/springboot_img/9802398403948_내사진.jpg
            boardFile.transferTo(new File(savePath)); // 5.

            /* 일단 저장 -> id 가져옴 (DB에 저장해야 id가 생김)
               왜? 자식 테이블(board_file_table)에서 board_id가 필요하니까 (부모 게시글에대한 PK값이 필요함)
               + 우린 자식 테이블에서 BoardEntity 타입으로 선언했음
               -> 자식 테이블에서 DB작업 수행할 때도 PK값이 아닌 BoardEntity 값을 전달 해야됨
            */
            BoardEntity boardEntity = BoardEntity.toSaveFileEntity(boardDTO);
            Long savedId = boardRepository.save(boardEntity).getId();
            BoardEntity board = boardRepository.findById(savedId).get();

            BoardFileEntity boardFileEntity = BoardFileEntity.toBoardFileEntity(board, originalFilename, storedFileName);
            boardFileRepository.save(boardFileEntity);
        }


    }
    @Transactional
    //repository에서 가져올 땐 항상 BoardEntity로 넘겨줌. 이걸 보여주려면 다시 BoardDTO형식으로 바꿔야됨( toBoardDTO )
    public List<BoardDTO> findAll() {
        List<BoardEntity> boardEntityList = boardRepository.findAll();
        List<BoardDTO> boardDTOList = new ArrayList<>();
        for (BoardEntity boardEntity : boardEntityList) {
            boardDTOList.add(BoardDTO.toBoardDTO(boardEntity));
        }
        return boardDTOList;
    }

    @Transactional  //jpa에서 제공하는 method가 아닌 별도의 method 쓸 때 붙여야됨
    public void updateHits(Long id) {
        boardRepository.updateHits(id);
    }

    @Transactional // 부모 entity가 에서 자식 entity를 호출할 때는 붙여줘야됨
    public BoardDTO findById(Long id) {
        Optional<BoardEntity> boardEntity = boardRepository.findById(id);
        if (boardEntity.isPresent()) {                                  //optional 객체가 값 포함-> true
            BoardDTO boardDTO = BoardDTO.toBoardDTO(boardEntity.get()); //optional인거는 가져오려면 .get() 해야됨
            return boardDTO;
        } else return null;
    }

    public BoardDTO update(BoardDTO boardDTO) {
        BoardEntity boardEntity = BoardEntity.toUpdateEntity(boardDTO);
        boardRepository.save(boardEntity);
        return findById(boardDTO.getId());
    }

    public void delete(Long id) {
        boardRepository.deleteById(id);
    }

    public Page<BoardDTO> paging(Pageable pageable) {
        //PageRequest.of(page)는 0부터 시작함 -> 우리가 생각하는 페이지는 1부터 시작( 1차이가 있음 ) 그걸 보정
        // ex. 사용자는 7인데(우린 1부터 세니까), DB에서는 6을 가져와야됨(0부터 세니까) 그래서 넘겨줄 때 1빼고 넘겨줘야됨
        int page = pageable.getPageNumber() - 1;
        int pageLimit = 3;    //한 페이지에 보여줄 글 갯수
        //한 페이지당 글 3개씩 보여주고 순서는 id 기준 내림차순으로 정렬하겠다
        // ^^기준으로 페이징 처리된 데이터를 가져옴
        Page<BoardEntity> boardEntities =
                boardRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));

        System.out.println("boardEntities.getContent() = " + boardEntities.getContent());   // 요청 페이지에 해당하는 글
        System.out.println("boardEntities.getTotalElements() = " + boardEntities.getTotalElements());   // 전체 글갯수
        System.out.println("boardEntities.getNumber() = " + boardEntities.getNumber());     // DB로 요청한 페이지 번호
        System.out.println("boardEntities.getTotalPages() = " + boardEntities.getTotalPages()); //  전체 페이지 갯수
        System.out.println("boardEntities.getSize() = " + boardEntities.getSize());     //한 페이지에 보여지는 글 갯수
        System.out.println("boardEntities.hasPrevious() = " + boardEntities.hasPrevious()); // 이전 페이지 존재 여부
        System.out.println("boardEntities.isFirst() = " + boardEntities.isFirst());     // 첫 페이지 여부
        System.out.println("boardEntities.isLast() = " + boardEntities.isLast());       // 마지막 페이지 여부

        //목록 : id, writer, title, hit, createdTime
        //entity 객체를 DTO 객체로 옮기는 과정
        // board == boardEntity
        Page<BoardDTO> boardDTOS = boardEntities.map(
                board -> new BoardDTO(board.getId(), board.getBoardWriter(),
                        board.getBoardTitle(), board.getBoardHits(), board.getCreatedTime()));
        return boardDTOS;
    }

}
