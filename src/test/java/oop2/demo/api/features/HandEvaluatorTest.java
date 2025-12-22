package oop2.demo.api.features;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/*
 * Kiểm tra các trường hợp cho từng loại tay bài.
 */
public class HandEvaluatorTest {

    @Test
    //Lá cao nhất
    public void highCard() {
        HandEvaluator evaluator;
        int value1, value2, value3;

        //Hand 1 có lá cao nhất là Ace
        evaluator = new HandEvaluator(new Hand("Ab Qc Tn 8r 5r 4c 2n"));
        Assertions.assertEquals(HandValueType.HIGH_CARD, evaluator.getType());
        value1 = evaluator.getValue();

        //Hand 2 có lá cao nhất là Ace (khác suit)
        evaluator = new HandEvaluator(new Hand("Ab Qr Tr 8c 5b 4n 2r"));
        Assertions.assertEquals(HandValueType.HIGH_CARD, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertEquals(value1, value2);

        //Hand 3 có lá cao nhất là Ace (5 lá đầu giống)
        evaluator = new HandEvaluator(new Hand("Ab Qc Tn 8r 5r 4c 3n"));
        Assertions.assertEquals(HandValueType.HIGH_CARD, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertEquals(value1, value2);

        //Hand 4 có lá cao nhất là King
        evaluator = new HandEvaluator(new Hand("Kb Qc Tn 8r 5r 4c 2n"));
        Assertions.assertEquals(HandValueType.HIGH_CARD, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 5 có lá cao nhất là King (lá thứ 5 nhỏ hơn)
        evaluator = new HandEvaluator(new Hand("Kb Qc Tn 8r 4r 3c 2n"));
        Assertions.assertEquals(HandValueType.HIGH_CARD, evaluator.getType());
        value3 = evaluator.getValue();
        Assertions.assertTrue(value2 > value3);
    }

    @Test
    //1 đôi
    public void onePair() {
        HandEvaluator evaluator;
        int value1, value2;

        //Hand 1 có đôi Queen
        evaluator = new HandEvaluator(new Hand("Qb Qc 9n 7n 5r 3b 2c"));
        Assertions.assertEquals(HandValueType.ONE_PAIR, evaluator.getType());
        value1 = evaluator.getValue();

        //Hand 2 có đôi Jack
        evaluator = new HandEvaluator(new Hand("Jb Jc 9n 7n 5r 3b 2c"));
        Assertions.assertEquals(HandValueType.ONE_PAIR, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 3 có đôi Queen và kicker nhỏ hơn
        evaluator = new HandEvaluator(new Hand("Qb Qc 8n 7n 5r 3b 2c"));
        Assertions.assertEquals(HandValueType.ONE_PAIR, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 4 có đôi Queen và kicker thứ 3 nhỏ hơn
        evaluator = new HandEvaluator(new Hand("Qb Qc 9n 7n 4r 3b 2c"));
        Assertions.assertEquals(HandValueType.ONE_PAIR, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 5 có đôi Queen và kicker bằng
        evaluator = new HandEvaluator(new Hand("Qb Qc 9n 7n 5r 2r"));
        Assertions.assertEquals(HandValueType.ONE_PAIR, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertEquals(value1, value2);
    }

    @Test
    //2 đôi
    public void twoPairs() {
        HandEvaluator evaluator;
        int value1, value2;

        //Hand 1 có hai đôi 5 và 2
        evaluator = new HandEvaluator(new Hand("Kb Qc Tn 5r 5n 2c 2n"));
        Assertions.assertEquals(HandValueType.TWO_PAIRS, evaluator.getType());
        value1 = evaluator.getValue();

        //Hand 2 có hai đôi 4 và 2
        evaluator = new HandEvaluator(new Hand("Kb Qc Tc 4r 4n 2c 2n"));
        Assertions.assertEquals(HandValueType.TWO_PAIRS, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 3 có hai đôi 4 và 3
        evaluator = new HandEvaluator(new Hand("Kb Qc Tn 4r 4n 3c 3n"));
        Assertions.assertEquals(HandValueType.TWO_PAIRS, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 4 có hai đôi 5 và 2 (kicker lớn hơn)
        evaluator = new HandEvaluator(new Hand("Ab Qc Tn 5r 5n 2c 2n"));
        Assertions.assertEquals(HandValueType.TWO_PAIRS, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 < value2);

        //Hand 5 có hai đôi 5 và 2 (kicker bằng)
        evaluator = new HandEvaluator(new Hand("Kb Jc Tn 5r 5n 2c 2n"));
        Assertions.assertEquals(HandValueType.TWO_PAIRS, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertEquals(value1, value2);
    }

    @Test
    //Bộ ba
    public void threeOfAKind() {
        HandEvaluator evaluator;
        int value1, value2;

        //Hand 1 có bộ ba Queen
        evaluator = new HandEvaluator(new Hand("Ac Qb Qc Qn Tc 8b 6n"));
        Assertions.assertEquals(HandValueType.THREE_OF_A_KIND, evaluator.getType());
        value1 = evaluator.getValue();

        //Hand 2 có bộ ba Jack
        evaluator = new HandEvaluator(new Hand("Ac Jb Jc Jn Tc 8b 6n"));
        Assertions.assertEquals(HandValueType.THREE_OF_A_KIND, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 3 có bộ ba Queen và kicker nhỏ hơn
        evaluator = new HandEvaluator(new Hand("Kb Qb Qc Qn Tc 8b 6n"));
        Assertions.assertEquals(HandValueType.THREE_OF_A_KIND, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 4 có bộ ba Queen và kicker thứ 2 nhỏ hơn
        evaluator = new HandEvaluator(new Hand("Ab Qb Qc Qn 9c 8b 6n"));
        Assertions.assertEquals(HandValueType.THREE_OF_A_KIND, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 5 có bộ ba Queen và kicker bằng
        evaluator = new HandEvaluator(new Hand("Ab Qb Qc Qn Tc 7b 6n"));
        Assertions.assertEquals(HandValueType.THREE_OF_A_KIND, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertEquals(value1, value2);
    }

    @Test
    //Sảnh
    public void straight() {
        HandEvaluator evaluator;
        int value1, value2, value3;
        
        //Hand 1 có 6 7 8 9 10
        evaluator = new HandEvaluator(new Hand("Kb Tc 9b 8r 7n 6c 4n"));
        Assertions.assertEquals(HandValueType.STRAIGHT, evaluator.getType());
        value1 = evaluator.getValue();

        //Hand 2 có 10 J Q K A
        evaluator = new HandEvaluator(new Hand("Ab Kb Qb Jb Tc 4r 2n"));
        Assertions.assertEquals(HandValueType.STRAIGHT, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value2 > value1);

        //Hand 3 có 6 7 8 9 10 giống hand 1 nhưng khác suit
        evaluator = new HandEvaluator(new Hand("Kb Tn 9r 8c 7r 6b 4n"));
        Assertions.assertEquals(HandValueType.STRAIGHT, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 == value2);

        //Hand 4 có 5 6 7 8 9
        evaluator = new HandEvaluator(new Hand("Kb 9r 8c 7r 6b 5n 2r"));
        Assertions.assertEquals(HandValueType.STRAIGHT, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 5 có 6 7 8 9 10 (các lá còn lại không ảnh hưởng gì đến giá trị tay bài nên = hand 1)
        evaluator = new HandEvaluator(new Hand("Ab Tc 9b 8r 7n 6c 4n"));
        Assertions.assertEquals(HandValueType.STRAIGHT, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 == value2);

        //Hand 6 có A 2 3 4 5 (Wheeling Ace)
        evaluator = new HandEvaluator(new Hand("Ar Qn Tc 5b 4r 3c 2n"));
        Assertions.assertEquals(HandValueType.STRAIGHT, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 7 có 2 3 4 5 6 > hand 6
        evaluator = new HandEvaluator(new Hand("Ar Qn 6c 5b 4r 3c 2n"));
        Assertions.assertEquals(HandValueType.STRAIGHT, evaluator.getType());
        value3 = evaluator.getValue();
        Assertions.assertTrue(value3 > value2);
    }

    @Test
    //Thùng
    public void flush() {
        HandEvaluator evaluator;
        int value1, value2;
        
        //Hand 1 có thùng Bích
        evaluator = new HandEvaluator(new Hand("Ab Qb Tb 8b 6b 4r 2n"));
        Assertions.assertEquals(HandValueType.FLUSH, evaluator.getType());
        value1 = evaluator.getValue();
        
        //Hand 2 có thùng Rô (lá bằng hand 1)
        evaluator = new HandEvaluator(new Hand("Ar Qr Tr 8r 6r 4n 2c"));
        Assertions.assertEquals(HandValueType.FLUSH, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 == value2);

        //Hand 3 có 4 lá cùng chất (không phải thùng)
        evaluator = new HandEvaluator(new Hand("Kc Jc Jr 8c 6r 5b 3c"));
        Assertions.assertFalse(evaluator.getType() == HandValueType.FLUSH);
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);
        
        //Hand 4 có thùng Bích (lá cao nhất nhỏ hơn)
        evaluator = new HandEvaluator(new Hand("Kb Qb Tb 8b 6b 4r 2n"));
        Assertions.assertEquals(HandValueType.FLUSH, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 5 có thùng Bích (lá thứ 5 nhỏ hơn)
        evaluator = new HandEvaluator(new Hand("Ab Qb Tb 8b 5b 4r 2n"));
        Assertions.assertEquals(HandValueType.FLUSH, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 6 có thùng Bích (lá bằng hand 1)
        evaluator = new HandEvaluator(new Hand("Ab Qb Tb 8b 6b 5b 2b"));
        Assertions.assertEquals(HandValueType.FLUSH, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 == value2);
    }

    @Test
    //Cù lũ
    public void fullHouse() {
        HandEvaluator evaluator;
        int value1, value2;
        
        //Hand 1 có cù lũ Queen trên 10
        evaluator = new HandEvaluator(new Hand("Ab Qb Qc Qn Tn Tr 4n"));
        Assertions.assertEquals(HandValueType.FULL_HOUSE, evaluator.getType());
        value1 = evaluator.getValue();

        //Hand 2 có cù lũ Jack trên 10
        evaluator = new HandEvaluator(new Hand("Ab Jb Jc Jn Tn Tr 4n"));
        Assertions.assertEquals(HandValueType.FULL_HOUSE, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 3 có cù lũ Queen trên 9
        evaluator = new HandEvaluator(new Hand("Ab Qb Qc Qn 9n 9r 4n"));
        Assertions.assertEquals(HandValueType.FULL_HOUSE, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 4 có cù lũ Jack trên King
        evaluator = new HandEvaluator(new Hand("Ab Jb Jc Jn Kn Kr 4n"));
        Assertions.assertEquals(HandValueType.FULL_HOUSE, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 5 có cù lũ Queen trên 10 (các lá thừa không ảnh hưởng)
        evaluator = new HandEvaluator(new Hand("Kb Qb Qc Qn Tn Tr 4n"));
        Assertions.assertEquals(HandValueType.FULL_HOUSE, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 == value2);
    }

    @Test
    //Tứ quý
    public void fourOfAKind() {
        HandEvaluator evaluator;
        int value1, value2;
        
        //Hand 1 có tứ quý Ace
        evaluator = new HandEvaluator(new Hand("Ab Ac An Ar Qb Tc 8n"));
        Assertions.assertEquals(HandValueType.FOUR_OF_A_KIND, evaluator.getType());
        value1 = evaluator.getValue();

        //Hand 2 có tứ quý King
        evaluator = new HandEvaluator(new Hand("Kb Kc Kn Kr Qb Tc 8n"));
        Assertions.assertEquals(HandValueType.FOUR_OF_A_KIND, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 3 có tứ quý Ace và lá kicker nhỏ hơn
        evaluator = new HandEvaluator(new Hand("Ab Ac An Ar Jb Tc 8n"));
        Assertions.assertEquals(HandValueType.FOUR_OF_A_KIND, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 4 có tứ quý Ace (các lá thừa không ảnh hưởng)
        evaluator = new HandEvaluator(new Hand("Ab Ac An Ar Qb 3r 2n"));
        Assertions.assertEquals(HandValueType.FOUR_OF_A_KIND, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 == value2);
    }

    @Test
    //Sảnh thùng
    public void straightFlush() {
        HandEvaluator evaluator;
        int value1, value2;
        
        //Hand 1 có sảnh thùng K Q J 10 9
        evaluator = new HandEvaluator(new Hand("Kb Qb Jb Tb 9b 4r 2n"));
        Assertions.assertEquals(HandValueType.STRAIGHT_FLUSH, evaluator.getType());
        value1 = evaluator.getValue();
        
        //Hand 2 có sảnh thùng Q J 10 9 8
        evaluator = new HandEvaluator(new Hand("Qc Jc Tc 9c 8c 4r 2n"));
        Assertions.assertEquals(HandValueType.STRAIGHT_FLUSH, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 > value2);

        //Hand 3 có sảnh thùng K Q J 10 9 (các lá thừa không ảnh hưởng)
        evaluator = new HandEvaluator(new Hand("Kb Qb Jb Tb 9b 3r 2n"));
        Assertions.assertEquals(HandValueType.STRAIGHT_FLUSH, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 == value2);

        //Hand 4 có sảnh thùng A Q 10 5 4 3 2 (Steel Wheel)
        evaluator = new HandEvaluator(new Hand("Ab Qn Tr 5b 4b 3b 2b"));
        Assertions.assertEquals(HandValueType.STRAIGHT_FLUSH, evaluator.getType());
        
        //Hand 5 có sảnh A Q 10 5 4 3 2 nhưng không cùng chất (không phải sảnh thùng)
        evaluator = new HandEvaluator(new Hand("Ac Qn Tr 5b 4b 3b 2b"));
        Assertions.assertEquals(HandValueType.STRAIGHT, evaluator.getType());

        //Hand 6 có thùng nhưng không sảnh (không phải sảnh thùng)
        evaluator = new HandEvaluator(new Hand("Kc Qb Jc Tc 9c 4c 2n"));
        Assertions.assertEquals(HandValueType.FLUSH, evaluator.getType());
    }

    @Test
    //Thùng phá sảnh
    public void royalFlush() {
        HandEvaluator evaluator;
        int value1, value2;
        
        //Hand 1 có thùng phá sảnh
        evaluator = new HandEvaluator(new Hand("Ab Kb Qb Jb Tb 4r 2n"));
        Assertions.assertEquals(HandValueType.ROYAL_FLUSH, evaluator.getType());
        value1 = evaluator.getValue();

        //Hand 2 có thùng phá sảnh (các lá thừa không ảnh hưởng)
        evaluator = new HandEvaluator(new Hand("Ab Kb Qb Jb Tb 3r 2n"));
        Assertions.assertEquals(HandValueType.ROYAL_FLUSH, evaluator.getType());
        value2 = evaluator.getValue();
        Assertions.assertTrue(value1 == value2);

        //Hand 3 có thùng nhưng không sảnh (không phải thùng phá sảnh)
        evaluator = new HandEvaluator(new Hand("Ab Kc Qb Jb Tb 4b 2n"));
        Assertions.assertEquals(HandValueType.FLUSH, evaluator.getType());
    }

}
