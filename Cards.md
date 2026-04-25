classDiagram
class Card {
<<abstract>>
+ CardType type
+ Color color
+ int value
}

    class NumberCard {
        + int number
    }

    class TakeTwoCard {
    }

    class ReverseCard {
    }

    class SkipCard {
    }

    class WildCard {
    }

    class WildTakeFourCard {
    }

    Card <|-- NumberCard
    Card <|-- TakeTwoCard
    Card <|-- ReverseCard
    Card <|-- SkipCard
    Card <|-- WildCard
    Card <|-- WildTakeFourCard