package action

enum class CalculateAction {
    CALCULATE_BASE, RECALCULATE, REFINE, REFRESH, SHOW_HISTOGRAM
}
enum class FileAction {
    OPEN_FRACTAL, OPEN_JSON, OPEN_PALETTE, SAVE_FRACTAL, SAVE_JSON, SAVE_IMAGE, SAVE_PALETTE, WRITE_IMAGE
}

enum class PaletteAction {
    ANIMATE, CHANGED, DEFAULT, RANDOM, SMOOTH, INTERPOLATE, MARKERS_CHANGED
}

enum class PaletteType {
    GRAY_SCALE, RANDOM, SMOOTH, CUSTOM
}

enum class PaletteSliderType{
    SIZE, COLOR_RANGE
}

enum class UIAction {
    INIT, CHANGE, SETTINGS
}
