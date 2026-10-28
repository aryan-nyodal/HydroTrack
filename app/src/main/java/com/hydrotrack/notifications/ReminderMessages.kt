package com.hydrotrack.notifications

object ReminderMessages {

    private val reminderMessages = listOf(
        "Time to hydrate! Your body will thank you 💧",
        "Water break! Stay refreshed and focused 🌊",
        "Hey there! A glass of water goes a long way 🥤",
        "Your cells are thirsty! Give them a drink 💦",
        "Hydration check! How's your water intake today? 🫧",
        "Quick sip time! Keep that energy up ⚡",
        "Don't forget your H₂O! Your brain needs it 🧠",
        "Water o'clock! Stay on track with your goal 🎯",
        "Feeling tired? A glass of water might help! 🌟",
        "Reminder: Water is your superpower! Drink up 💪",
        "Your future self says thanks for hydrating now 🙏",
        "Splash! Time for another glass of water 🌈",
        "Keep the flow going! Log some water now 🏄",
        "Hydration station! Your body runs on water 🚂",
        "A little water now, a lot of energy later ✨"
    )

    private val streakAtRiskMessages = listOf(
        "Your streak is at risk! Don't let it slip 🔥",
        "Almost there! Keep your streak alive 💪",
        "Your streak needs you! Log some water now 🛡️",
        "Don't break the chain! Hydrate to save your streak 🔗",
        "Evening check-in: Your streak is counting on you ⏰"
    )

    private val goalCompleteMessages = listOf(
        "🎉 You did it! Daily goal crushed!",
        "🏆 Goal complete! You're a hydration champion!",
        "⭐ Amazing! You've met your water goal!",
        "🌟 Stellar work! Daily hydration goal achieved!",
        "🥳 Woohoo! You've hit your target today!"
    )

    private val milestoneMessages = mapOf(
        3 to "🔥 3-day streak! You're building a great habit!",
        7 to "🌟 One full week of hydration! Incredible!",
        14 to "💎 Two weeks strong! You're unstoppable!",
        30 to "🏅 30-day streak! That's true dedication!",
        60 to "🏆 60 days! You're a hydration legend!",
        100 to "👑 100-day streak! You've mastered hydration!"
    )

    fun getRandomMessage(): String {
        return reminderMessages.random()
    }

    fun getRandomStreakAtRiskMessage(): String {
        return streakAtRiskMessages.random()
    }

    fun getRandomGoalCompleteMessage(): String {
        return goalCompleteMessages.random()
    }

    fun getMilestoneMessage(streakCount: Int): String {
        return milestoneMessages[streakCount]
            ?: "🔥 $streakCount-day streak! Keep it going!"
    }
}
