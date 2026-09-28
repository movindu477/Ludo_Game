package lk.apiit.ludot.dto;

import java.util.List;

/* DTO - one player's whole status in a single object.

   Notes 3c: the DTO exists so several separate reads become one. The
   end-of-round report needs four counts and four locations per player;
   this carries all of it in one trip instead of nine getter calls. */
public record PlayerStatusDto(String colour,
                              int piecesOnBoard,
                              int piecesInBase,
                              int piecesHome,
                              List<PieceLocationDto> pieces) {

    public PlayerStatusDto {
        pieces = List.copyOf(pieces);   // defensive copy keeps it immutable
    }
}
