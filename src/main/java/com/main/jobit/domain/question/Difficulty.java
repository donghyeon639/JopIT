package com.main.jobit.domain.question;

// 문제 난이도(하/중/상). DB에는 EnumType.STRING으로 이름 그대로 저장된다(순서 변경에 안전하도록).
public enum Difficulty {
    LOW, MID, HIGH
}