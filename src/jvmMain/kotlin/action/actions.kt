package action

import java.util.Properties

class AppTitle(val title:String)
class GetProperties()
class HaveProperties(val props:Properties)
class SetProperty(val key:String, val value:String)

class CalculateEvent(val action: CalculateAction)
class FileEvent(val action: FileAction)
class PaletteEvent(val action: PaletteAction)
class UIEvent(val action: UIAction, val data: Any)