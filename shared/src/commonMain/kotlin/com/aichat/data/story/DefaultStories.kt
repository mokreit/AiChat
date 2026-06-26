package com.aichat.data.story

import com.aichat.data.database.entity.StoryEntity

object DefaultStories {

    val darkForest = StoryEntity(
        id = "builtin-dark-forest",
        title = "黑暗之森的封印",
        description = "远古的封印正在松动，黑暗之森中的魔物开始苏醒。三位命运交织的旅人，将在月圆之夜揭开尘封千年的秘密。",
        characterIds = "builtin-alice,builtin-shadow,builtin-luna",
        systemPrompt = "这是一个奇幻冒险故事。故事发生在一片被称为\"黑暗之森\"的古老森林中。远古时期，强大的巫女用月之力封印了森林深处的黑暗魔物，但千年的时光让封印逐渐松动。玩家将与其他角色一同探索森林，面对魔物的威胁，寻找加固封印的方法。故事风格偏向史诗奇幻，氛围神秘而紧张，但角色之间也会有温暖的互动。",
        createdAt = 1700000003000L,
        updatedAt = 1700000003000L,
    )

    val all: List<StoryEntity> = listOf(darkForest)
}
