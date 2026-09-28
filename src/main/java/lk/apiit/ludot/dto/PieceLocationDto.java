package lk.apiit.ludot.dto;

/* DTO - carries one piece's location out of the domain.

   Notes 3c: "DTOs are simple objects that do not contain any business
   logic" and "may be (should be?) immutable". A Java record is exactly
   that - final fields, no setters, no behaviour.

   Handing a live Piece to the view or the gateway would let them mutate
   the running game. A snapshot cannot. */
public record PieceLocationDto(String pieceName, String location) {
}
