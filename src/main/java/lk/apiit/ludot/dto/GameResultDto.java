package lk.apiit.ludot.dto;

import java.util.List;

/* DTO - the finished game, ready to be written away by the gateway.

   Rule 11: the game continues past the winner, so the placings list holds
   all four colours in finishing order. */
public record GameResultDto(String winner, List<String> placings, int rounds) {

    public GameResultDto {
        placings = List.copyOf(placings);
    }
}
