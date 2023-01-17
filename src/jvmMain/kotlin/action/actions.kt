package action

import Complex
import Palette
import java.util.Properties

class AppTitle(val title: String)
class GetProperties
class GetProperty(val key:String)
class HaveProperties(val props: Properties)
class HaveProperty(val key:String, val value: String?)
class SetProperty(val key: String, val value: String)
