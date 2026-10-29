package vin.pam.closetly.core.navigation

enum class AppModule(
    val title: String,
    val owner: String,
    val branch: String,
    val description: String,
) {
    Wardrobe(
        title = "Digital Wardrobe",
        owner = "Vincent",
        branch = "feature/digital-wardrobe",
        description = "Catalog and organize personal clothing.",
    ),
    Outfit(
        title = "Mix & Match Outfit",
        owner = "Janice",
        branch = "feature/mix-and-match",
        description = "Create combinations from wardrobe items.",
    ),
    Planner(
        title = "Outfit Planner",
        owner = "Vara",
        branch = "feature/outfit-planner",
        description = "Plan outfits for dates and activities.",
    ),
    Inspiration(
        title = "Style Inspiration Board",
        owner = "Sela",
        branch = "feature/inspiration-board",
        description = "Collect and organize fashion references.",
    ),
    Wishlist(
        title = "Fashion Wishlist",
        owner = "Gres",
        branch = "feature/fashion-wishlist",
        description = "Track fashion items intended for purchase.",
    ),
    WearHistory(
        title = "Wear History",
        owner = "Aurel",
        branch = "feature/wear-history",
        description = "Review a visual archive of worn outfits.",
    ),
}
