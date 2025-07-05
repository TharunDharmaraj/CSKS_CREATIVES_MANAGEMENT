package com.example.csks_creatives.domain.model.utills.enums.tasks

enum class TaskStatusType(val order: Int) {
    BACKLOG(0),
    IN_PROGRESS(1),
    IN_REVIEW(2),
    IN_REVISION(3),
    PAUSED(4),
    COMPLETED(5),

    // Added for backward Compatability
    REVISION_1(100),
    REVISION_2(101),
    REVISION_3(102),
    REVISION_4(103),
    REVISION_5(104),
    REVISION_6(105),
    REVISION_7(106),
}