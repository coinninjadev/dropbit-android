package com.coinninja.coinkeeper.ui.segwit

enum class UpgradeState constructor(val id:Int) {
    NotStarted(0),
    Started(1),
    StepOneCompleted(2),
    StepTwoCompleted(3),
    StepThreeCompleted(4),
    Finished(5),
    Error(6);

    companion object {
        fun from(id:Int?):UpgradeState = when(id) {
            0 -> NotStarted
            1 -> Started
            2 -> StepOneCompleted
            3 -> StepTwoCompleted
            4 -> StepThreeCompleted
            5 -> Finished
            else -> NotStarted
        }
    }
}