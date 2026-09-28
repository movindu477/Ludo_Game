package lk.apiit.ludot;

import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.Colour;
import lk.apiit.ludot.domain.Direction;
import lk.apiit.ludot.domain.GameRules;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.domain.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void startingSquaresMatchTheBrief() {
        assertThat(board.startIndexOf(Colour.YELLOW)).isEqualTo(0);
        assertThat(board.startIndexOf(Colour.BLUE)).isEqualTo(13);
        assertThat(board.startIndexOf(Colour.RED)).isEqualTo(26);
        assertThat(board.startIndexOf(Colour.GREEN)).isEqualTo(39);
    }

    @Test
    void approachCellSitsJustBehindTheStartingSquare() {
        // yellow wraps back to 51, the case most likely to break
        assertThat(board.approachIndexOf(Colour.YELLOW)).isEqualTo(51);
        assertThat(board.approachIndexOf(Colour.BLUE)).isEqualTo(12);
        assertThat(board.approachIndexOf(Colour.RED)).isEqualTo(25);
        assertThat(board.approachIndexOf(Colour.GREEN)).isEqualTo(38);
    }

    @Test
    void specialCellsResolveFromTheYellowApproach() {
        // Rule T-11 - proves Alpha 8, Beta 26, Gamma 45
        int yellowApproach = board.approachIndexOf(Colour.YELLOW);

        assertThat(board.step(yellowApproach, Direction.CLOCKWISE,
                GameRules.ALPHA_OFFSET)).isEqualTo(8);
        assertThat(board.step(yellowApproach, Direction.CLOCKWISE,
                GameRules.BETA_OFFSET)).isEqualTo(26);
        assertThat(board.step(yellowApproach, Direction.CLOCKWISE,
                GameRules.GAMMA_OFFSET)).isEqualTo(45);
    }

    @Test
    void movementWrapsAtCellFiftyOneInBothDirections() {
        assertThat(board.step(50, Direction.CLOCKWISE, 4)).isEqualTo(2);
        assertThat(board.step(2, Direction.COUNTER_CLOCKWISE, 4)).isEqualTo(50);
    }

    @Test
    void twoSameColouredPiecesOnACellFormABlock() {
        // Rule T-3
        Piece g1 = new Piece(Colour.GREEN, 1);
        Piece g2 = new Piece(Colour.GREEN, 2);
        board.placeOnPath(g1, 10);
        board.placeOnPath(g2, 10);

        assertThat(board.hasBlockAt(10)).isTrue();
        assertThat(board.isBlockedFor(10, Colour.RED)).isTrue();
        assertThat(board.isBlockedFor(10, Colour.GREEN)).isFalse();
    }

    @Test
    void aSinglePieceCannotBeCapturedIfItIsPartOfABlock() {
        // Rule T-8 - a lone piece cannot take a block
        Piece g1 = new Piece(Colour.GREEN, 1);
        Piece g2 = new Piece(Colour.GREEN, 2);
        board.placeOnPath(g1, 10);
        board.placeOnPath(g2, 10);

        assertThat(board.opponentsAt(10, Colour.RED)).isEmpty();
    }

    @Test
    void aCounterClockwisePieceCannotTurnInOnItsFirstApproachPass() {
        // Rule T-1 - without this it would reach home one step after leaving base
        Piece piece = new Piece(Colour.RED, 1);
        piece.enterBoard(board.startIndexOf(Colour.RED), Direction.COUNTER_CLOCKWISE);
        piece.recordCapture();                  // Rule T-7 already satisfied

        assertThat(piece.mayEnterHomeStraight()).isFalse();

        piece.notePassedApproach();
        assertThat(piece.mayEnterHomeStraight()).isTrue();
    }

    @Test
    void onTheFirstPassACounterClockwisePieceCarriesOnRoundThePath() {
        // Rule T-1 - red starts on 26, its approach cell 25 is one step behind
        Piece piece = new Piece(Colour.RED, 1);
        piece.enterBoard(board.startIndexOf(Colour.RED), Direction.COUNTER_CLOCKWISE);
        piece.recordCapture();

        assertThat(board.targetOf(piece, 3)).isEqualTo(Position.onPath(23));

        piece.notePassedApproach();
        assertThat(board.targetOf(piece, 3)).isEqualTo(Position.homeStraight(Colour.RED, 1));
    }

    @Test
    void twoPiecesOfDifferentColoursSharingACellAreNotABlock() {
        // Rule T-3 - a block needs one colour; entering the board can
        // leave a piece sharing its start square with an opponent
        Piece green = new Piece(Colour.GREEN, 1);
        Piece red = new Piece(Colour.RED, 1);
        board.placeOnPath(green, 26);
        board.placeOnPath(red, 26);

        assertThat(board.hasBlockAt(26)).isFalse();
        assertThat(board.isBlockedFor(26, Colour.BLUE)).isFalse();
        assertThat(board.opponentsAt(26, Colour.RED)).containsExactly(green);
    }
}
