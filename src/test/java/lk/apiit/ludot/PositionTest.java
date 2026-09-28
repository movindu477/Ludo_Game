package lk.apiit.ludot;

import lk.apiit.ludot.domain.Colour;
import lk.apiit.ludot.domain.Position;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PositionTest {

    @Test
    void pathCellLabelIsJustItsNumber() {
        assertThat(Position.onPath(26).label()).isEqualTo("26");
    }

    @Test
    void homeStraightUsesTheNamingFromTheBrief() {
        assertThat(Position.homeStraight(Colour.GREEN, 2).label())
                .isEqualTo("greenhomepath2");
    }

    @Test
    void basePositionIsNeverEqualToAPathPosition() {
        // the reason Position exists instead of a plain int
        assertThat(Position.inBase(Colour.RED)).isNotEqualTo(Position.onPath(0));
    }

    @Test
    void anIndexPastTheEndOfThePathIsRejected() {
        assertThatThrownBy(() -> Position.onPath(52))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
