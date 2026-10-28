package com.hydrotrack.domain.model

enum class BadgeType(
    val displayName: String,
    val description: String,
    val icon: String
) {
    FIRST_SIP("First Sip", "Log your first glass of water", "💧"),
    HYDRATION_HERO("Hydration Hero", "Earn 10,000 total points", "🏆"),
    EARLY_BIRD("Early Bird", "Log water before 8 AM", "🌅"),
    GOAL_CRUSHER("Goal Crusher", "Hit your daily goal 30 times", "💪"),
    SPARK_3("3-Day Spark", "Maintain a 3-day streak", "✨"),
    WEEK_WARRIOR("Week Warrior", "Maintain a 7-day streak", "⚔️"),
    FORTNIGHT_FLOW("Fortnight Flow", "Maintain a 14-day streak", "🌊"),
    MONTHLY_MASTER("Monthly Master", "Maintain a 30-day streak", "👑"),
    UNSTOPPABLE("Unstoppable", "Maintain a 60-day streak", "🚀"),
    CENTURY_CLUB("Century Club", "Maintain a 100-day streak", "💯"),
    WATER_FOUNTAIN("Water Fountain", "Log 1,000 ml in a single day", "⛲"),
    OCEAN_DRINKER("Ocean Drinker", "Drink 100,000 ml total", "🌊"),
    CONSISTENT("Consistent", "Log water 7 days in a row", "📅"),
    NIGHT_OWL("Night Owl", "Log water after 10 PM", "🦉")
}

data class Badge(
    val type: BadgeType,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)
