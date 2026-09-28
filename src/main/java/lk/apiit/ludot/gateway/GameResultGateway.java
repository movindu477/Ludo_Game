package lk.apiit.ludot.gateway;

import lk.apiit.ludot.dto.GameResultDto;

import java.util.List;

/* DATA GATEWAY - the interface.

   Notes 3d: "A simple interface with several standard methods - insert(),
   a series of find() methods". All persistence code sits behind this, so
   the rules never contain a file handle or an SQL string.

   It is an interface, not a class, so a test can substitute an in-memory
   version and the game logic cannot tell the difference. */
public interface GameResultGateway {

    void insert(GameResultDto result);

    List<GameResultDto> findAll();
}
